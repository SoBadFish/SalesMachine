package org.sobadfish.sales;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import cn.nukkit.event.inventory.InventoryMoveItemEvent;
import cn.nukkit.event.inventory.InventoryTransactionEvent;
import cn.nukkit.event.level.ChunkLoadEvent;
import cn.nukkit.event.level.ChunkUnloadEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.transaction.InventoryTransaction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemHopper;
import cn.nukkit.item.ItemMinecartHopper;
import cn.nukkit.item.ItemNameTag;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.config.SalesData;
import org.sobadfish.sales.economy.IMoney;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.form.AbstractSaleForm;
import org.sobadfish.sales.form.DiscountForm;
import org.sobadfish.sales.form.PhoneForm;
import org.sobadfish.sales.form.SellItemForm;
import org.sobadfish.sales.items.ISaleItem;
import org.sobadfish.sales.items.MoneyItem;
import org.sobadfish.sales.panel.DisplayPlayerPanel;
import org.sobadfish.sales.panel.button.BasePlayPanelItemInstance;
import org.sobadfish.sales.panel.lib.ChestPanel;
import org.sobadfish.sales.panel.lib.DoubleChestPanel;
import org.sobadfish.sales.panel.lib.IDisplayPanel;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Sobadfish
 * @date 2023/11/16
 */
public class SalesListener implements Listener {

    public SalesMainClass main;

    public static LinkedHashMap<String, IDisplayPanel> chestPanelLinkedHashMap = new LinkedHashMap<>();


    public SalesListener(SalesMainClass salesMainClass){
        this.main = salesMainClass;
    }

    public static LinkedHashMap<String,SalesEntity> cacheEntitys = new LinkedHashMap<>();



    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event){
        Block block = event.getBlock();
        SalesEntity entity1 = getEntityByPos(block);
        if(event.getPlayer().isBreakingBlock()){
            return;
        }
        Player player = event.getPlayer();

        if(entity1 != null){
            if(entity1.finalClose){
                return;
            }
            Item ei = event.getItem();
            if(ei.equals(RegisterItemServices.CUSTOM_ITEMS.get("ct_key")) && !player.isSneaking()){
                //使用钥匙锁
                if(player.getName().equalsIgnoreCase(entity1.master) || player.isOp()){
                    if(entity1.salesData.lock == 0){
                        entity1.salesData.lock = 1;
                        entity1.saveData();

                    }else{
                        entity1.salesData.lock = 0;
                        entity1.saveData();
                    }
                    player.level.addSound(entity1,Sound.OPEN_WOODEN_DOOR);
                    player.sendTip(TextFormat.colorize('&',"&a已"+(entity1.salesData.lock==1?"锁定":"解锁")));

                }else{
                    player.sendTip(TextFormat.colorize('&',"&c这不是你的售货机!"));
                }

                return;
            }

            if(ei.equals(RegisterItemServices.CUSTOM_ITEMS.get("discount")) && !player.isSneaking()){
                //使用空白优惠券
                event.setCancelled();
                if(!ei.hasCompoundTag()){
                    //制作空白优惠券 UI
                    if(player.isOp() || entity1.master.equalsIgnoreCase(player.getName())){
                        DiscountForm discountForm = new DiscountForm(entity1,ei);
                        discountForm.display(player);
                    }else{
                        SalesMainClass.sendMessageToObject("&c这不是你的售货机!",player);
                    }
                }
                return;
            }
            if(ei.equals(RegisterItemServices.CUSTOM_ITEMS.get("netxk")) && !player.isSneaking()){
                //使用网店许可证
                event.setCancelled();
                if(player.isOp() || entity1.master.equalsIgnoreCase(player.getName())){
                    Item r1 = ei.clone();
                    r1.setCount(1);
                    player.getInventory().removeItem(r1);
                    entity1.salesData.netuse = 1;
                    entity1.saveData();
                    player.level.addSound(entity1,Sound.RANDOM_ORB);
                    SalesMainClass.sendMessageToObject("&a恭喜 您的售货机已获得线上商店许可证!",player);
                }else{
                    SalesMainClass.sendMessageToObject("&c这不是你的售货机!",player);
                }


                return;
            }
            if(!ei.equals(RegisterItemServices.CUSTOM_ITEMS.get("ct"),false) &&
                    !(ei instanceof ISaleItem)&&
                    !ei.equals(RegisterItemServices.CUSTOM_ITEMS.get("pipe_wrench"),false)
            && !ei.equals(new ItemHopper()) && !ei.equals(new ItemMinecartHopper())){
                event.setCancelled();
            }else{
                if(!player.isSneaking()){
                    return;
                }
            }
            if(ei instanceof ItemNameTag){
                event.setCancelled();
                if(ei.hasCustomName()){
                    String name = ei.getCustomName();
                    entity1.setCustomName(TextFormat.colorize('&',name));
                    SalesMainClass.sendMessageToObject("&a使用成功 售货机已命名为 :&r"+name,player);
                    Item cll = ei.clone();
                    cll.setCount(1);
                    player.getInventory().removeItem(cll);
                }
                return;
            }
            Server.getInstance().getScheduler().scheduleDelayedTask(SalesMainClass.INSTANCE, new Runnable() {
                @Override
                public void run() {
                    if(entity1.finalClose){
                        return;
                    }
                    if(!player.isOp() && !player.getName().equalsIgnoreCase(entity1.master) && entity1.salesData.lock == 1){
                        player.sendTip(TextFormat.colorize('&',"&c这个售货机被锁上了"));
                        return;
                    }

                    if(player.isSneaking()){
                        //看看点击的朝向是不是实体朝向
                        if(ei instanceof ISaleItem){
                            SalesMainClass.sendMessageToObject("&c禁止套娃！",player);
                            return;
                        }

                        if(event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK){
                            if(player.getHorizontalFacing() != entity1.blockFace){
                                return;
                            }
                            if(player.isOp() || (entity1.master != null && entity1.master.equalsIgnoreCase(player.getName()))){
                                if(player.getInventory().getItemInHand().getId() == 0){
                                    return;
                                }

                                SellItemForm sellItemForm = new SellItemForm(entity1,player.getInventory().getItemInHand());
                                sellItemForm.display(player);
                            }
                        }
                    }else{
                        if(event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK){
                            if(entity1.clickInvPlayers.size() > 0){
                                SalesMainClass.sendMessageToObject("&c售货机正在被编辑",player);
                                return;
                            }

                            DisplayPlayerPanel displayPlayerPanel = new DisplayPlayerPanel(entity1);
                            displayPlayerPanel.open(player);
                            chestPanelLinkedHashMap.put(player.getName(),displayPlayerPanel);

                        }

                    }

                }

            },5);
            return;
        }
        if(!event.isCancelled()){
            //使用金币
            Item item = event.getItem();
            if(item == null){
                return;
            }
            if(item.hasCompoundTag() && item.getNamedTag().contains(MoneyItem.TAG)){
                if(player.isSneaking()){
                    return;
                }
                double money = item.getNamedTag().getDouble(MoneyItem.TAG) * item.getCount();
                CompoundTag tag = item.getNamedTag();
                String mn = SalesMainClass.getFirstMoney();
                if(tag.contains("loadMoney")){
                    mn = tag.getString("loadMoney");
                }
                IMoney iMoney = SalesMainClass.getMoneyCoreByName(mn);
                if(iMoney == null){
                    SalesMainClass.sendMessageToObject("&c经济核心 "+mn+" 未装载!",player);
                    return;
                }
                if(!iMoney.addMoney(player.getName(),money,null)){
                    SalesMainClass.sendMessageToObject("&c交易失败!",player);
                    return;
                }
                item.setCount(item.getCount() - item.getCount());
                player.getInventory().setItemInHand(item);

                player.level.addSound(player, Sound.ARMOR_EQUIP_IRON);
                SalesMainClass.sendMessageToObject("&r获得"+iMoney.displayName()+" x &e"+money,player);

            }
            //TODO 自定义物品的放置
            //TODO 放置物品
//            if(SalesMainClass.LOAD_CUSTOM){
            if(item.hasCompoundTag() && item.getNamedTag().contains("saleskey")
                    && item.getNamedTag().contains("salesmeta")
            ){
                event.setCancelled();
                String skinModel = item.getNamedTag().getString("salesmeta");

                if(SalesEntity.spawnToAll(block.getSide(event.getFace()),player.getDirection(),player.getName(),null,skinModel,item) != null){
                    if (player.isSurvival() || player.isAdventure()) {
                        Item item2 = player.getInventory().getItemInHand();
                        item2.setCount(item2.getCount() - 1);
                        player.getInventory().setItemInHand(item2);

                    }
                }else{
                    SalesMainClass.sendMessageToObject("&c生成失败！ 请保证周围没有其他方块",player);
                }

            }
            //使用手机
            if(item.equals(RegisterItemServices.CUSTOM_ITEMS.get("phone"),false,false)){
                if(event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                    if(item.getDamage() >= item.getMaxDurability()){
                        player.level.addParticle(new ItemBreakParticle(player.add(0, player.getEyeY()),item));
                        player.level.addSound(player,Sound.RANDOM_BREAK);
                        player.getInventory().removeItem(item);
                        return;
                    }
                    item.setDamage(item.getDamage() + 1);
                    player.getInventory().setItemInHand(item);
                    if (!player.isSneaking()) {
                        PhoneForm phoneForm = new PhoneForm();
                        phoneForm.display(player);
                    }

                }
            }

        }

    }

    @EventHandler
    public void onLevelChange(EntityLevelChangeEvent event){
        //TODO 解决切换世界 出现的残留
        Entity player = event.getEntity();
        if(player instanceof Player){
            //上个世界
            Level level = event.getOrigin();
            for(Entity entity: level.getEntities()){
                if(entity instanceof SalesEntity){
                    ((SalesEntity) entity).removePackets((Player) player);
                }
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event){
        //加载区间下的所有 实体
//        SalesMainClass.sendMessageToConsole("区块加载: ("+event.getChunk().getX()+":"+ (event.getChunk().getZ())+")");
        loadEntityByChunk(event.getLevel(),event.getChunk());

    }


    public void loadEntityByChunk(Level level,FullChunk chunk){
        if( SalesMainClass.INSTANCE.sqliteHelper != null){
            List<SalesData> salesData = SalesMainClass.INSTANCE.sqliteHelper.getDataByString(SalesMainClass.DB_TABLE,
                    "chunkx = ? and chunkz = ?",new String[]{
                            (chunk.getX())+"",
                            (chunk.getZ())+""
                    }, SalesData.class);
            if(!salesData.isEmpty()){
                // SalesMainClass.sendMessageToConsole("加载 区块: ("+event.getChunk().getX()+":"+ (event.getChunk().getZ())+") "+salesData.size()+" 个售货机");
                Server.getInstance().getScheduler().scheduleDelayedTask(SalesMainClass.INSTANCE, () -> {
                    for(SalesData data : salesData){
                        if("null".equalsIgnoreCase(data.customname) || "".equalsIgnoreCase(data.customname)){
                            data.customname = null;
                        }

                        Position position = data.asPosition();
                        if(position.getLevel().getFolderName().equalsIgnoreCase(level.getFolderName())){
                            if("null".equalsIgnoreCase(data.world) || "".equalsIgnoreCase(data.world) || data.world == null){
                                data.world = level.getFolderName();
                                SalesMainClass.INSTANCE.sqliteHelper.set(SalesMainClass.DB_TABLE,"location",data.location,data);
                            }
                            //还出现过地图没更新的问题
                            if(data.world != null && !data.world.equalsIgnoreCase(level.getFolderName())){
                                data.world = level.getFolderName();
                                SalesMainClass.INSTANCE.sqliteHelper.set(SalesMainClass.DB_TABLE,"location",data.location,data);
                            }
                            if(!cacheEntitys.containsKey(data.location)){
                                if(SalesEntity.spawnToAll(data.asPosition(), BlockFace.valueOf(data.bf.toUpperCase()),data.master,data, true,false,null,null) == null){
                                    SalesMainClass.sendMessageToConsole("&c加载 位置: ("+data.location+") "+"区块: "+data.chunkx+","+data.chunkz+" 售货机失败!");
                                }
//                                else{
//                                    SalesMainClass.sendMessageToConsole("&a成功加载 &7位置: ("+data.location+") "+"区块: "+data.chunkx+","+data.chunkz+" &a的售货机!");
//                                }

                            }else{
                                //重新生成一下，防止不显示
                                SalesEntity et = cacheEntitys.get(data.location);
                                et.close();
                                if(SalesEntity.spawnToAll(data.asPosition(), BlockFace.valueOf(data.bf.toUpperCase()),data.master,data, true,false,null,null) == null){
                                    SalesMainClass.sendMessageToConsole("&c重新加载 位置: ("+data.location+") "+"区块: "+data.chunkx+","+data.chunkz+" 售货机失败!");
                                }
//                                else {
//                                    SalesMainClass.sendMessageToConsole("&a已重新加载 &7位置: ("+data.location+") "+"区块: "+data.chunkx+","+data.chunkz+" &a的售货机!");
//                                }
                            }
                        }

                    }
                }, 10);
            }


        }
    }

    //黑科技 接收漏斗物品
    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event){
        Inventory inventoryHolder = event.getTargetInventory();
        if(inventoryHolder instanceof SalesEntity.SalesBlockEntity.SaleBlockEntityInventory){
            SalesEntity salesEntity = ((SalesEntity.SalesBlockEntity.SaleBlockEntityInventory) inventoryHolder).getSalesEntity();
            if(salesEntity != null){
                Item item = event.getItem();
                if(salesEntity.hasItem(item)){
                    if(!salesEntity.addItem(item)){
                        event.setCancelled();
                    }
                }else{
                    event.setCancelled();
                }
                inventoryHolder.clearAll();
            }

        }

    }

    @EventHandler
    public void onChunkUnLoad(ChunkUnloadEvent event){
        //卸载区间下的所有 实体
//        SalesMainClass.sendMessageToConsole("&c区块卸载: 清理位置 &7("+event.getChunk().getX()+":"+ (event.getChunk().getZ())+") "+"&c 的售货机!");
        for(Entity e: event.getChunk().getEntities().values()){
            if(e instanceof SalesEntity salesEntity){
//                event.setCancelled();
//                break;
//                SalesMainClass.sendMessageToConsole("&c区块卸载: 移除位置&7 ("+((SalesEntity) e).salesData.location+") "+"区块: "+ ((SalesEntity) e).salesData.chunkx+","+ ((SalesEntity) e).salesData.chunkz+"&c 的售货机!");
                //顺便移除缓存
                salesEntity.closeAsChunk();
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        SalesEntity entity1 = getEntityByPos(event.getBlockAgainst());
        if(entity1 != null){
            if(player.getHorizontalFacing() == entity1.blockFace){
                event.setCancelled();
            }

            //更新区块
            event.getBlock().level.scheduleUpdate(event.getBlock(),10);
        }

    }

    @EventHandler
    public void onFormListener(PlayerFormRespondedEvent event) {
        if (event.wasClosed()) {
            return;
        }
        if(AbstractSaleForm.DISPLAY_FROM.containsKey(event.getPlayer().getName())){
            AbstractSaleForm form = AbstractSaleForm.DISPLAY_FROM.get(event.getPlayer().getName());
            if(form.getId() == event.getFormID()) {
                form.onListener(event.getPlayer(), event.getResponse());
                AbstractSaleForm.DISPLAY_FROM.remove(event.getPlayer().getName());

            }
        }
    }

    public static SalesEntity getEntityByPos(Position position){
        String v = SalesEntity.asLocation(position);
            if(cacheEntitys.containsKey(v)){
                SalesEntity salesEntity = cacheEntitys.get(v);
                if(salesEntity.finalClose){
                    cacheEntitys.remove(v);
                    return null;
                }
                return cacheEntitys.get(v);
            }
//        }
        return null;

    }


    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        if(event.isCancelled()){
            return;
        }

        if(block.getId() == main.services.iBarrier.getBid()){
            SalesEntity entity1 = getEntityByPos(event.getBlock());
            if(entity1 != null){
                if(event.getPlayer().isOp()){
                    entity1.toClose();
                }
                event.setCancelled();
            }
        }

    }

    @EventHandler
    public void onItemChange(InventoryTransactionEvent event) {
        InventoryTransaction transaction = event.getTransaction();
        for (InventoryAction action : transaction.getActions()) {
            for (Inventory inventory : transaction.getInventories()) {
                if (inventory instanceof ChestPanel) {
                    Player player = ((ChestPanel) inventory).getPlayer();
                    event.setCancelled();
                    Item i = action.getSourceItem();

                    if (i.hasCompoundTag() && i.getNamedTag().contains("index")) {
                        int index = i.getNamedTag().getInt("index");
                        BasePlayPanelItemInstance item = ((ChestPanel) inventory).getPanel().getOrDefault(index, null);

                        if (item != null) {
                            ((ChestPanel) inventory).clickSolt = index;
                            item.onClick((ChestPanel) inventory, player);
                            ((ChestPanel) inventory).update();
                        }
                    }

                }
                if(inventory instanceof DoubleChestPanel){
                    //拿取库存物品的判断
                    Player player = ((DoubleChestPanel) inventory).getPlayer();
                    Item i = action.getSourceItem();
                    Item i2 = action.getTargetItem();
                    if (i.hasCompoundTag() && i.getNamedTag().contains("index")) {
                        event.setCancelled();
                        int index = i.getNamedTag().getInt("index");
                        BasePlayPanelItemInstance item = ((DoubleChestPanel) inventory).getPanel().getOrDefault(index, null);
                        if (item != null) {
                            item.onClick((DoubleChestPanel) inventory, player);
                        }
                    }
                    boolean condition1 = i2.equals(((DoubleChestPanel) inventory).choseItem, true, true) && (
                            i.getId() == 0 || i.equals(((DoubleChestPanel) inventory).choseItem, true, true));

                    boolean condition2 = i.equals(((DoubleChestPanel) inventory).choseItem, true, true) &&
                            (i2.getId() == 0 || i2.equals(((DoubleChestPanel) inventory).choseItem, true, true));

                    if (!(condition1 || condition2)) {
                        event.setCancelled();
                    }
                }
                //如果是库存
            }
        }
    }


    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if(event.getEntity() instanceof SalesEntity){
            event.setCancelled();

        }
    }


}
