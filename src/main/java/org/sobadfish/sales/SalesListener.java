package org.sobadfish.sales;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.block.BlockUpdateEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.InventoryTransactionEvent;
import cn.nukkit.event.level.ChunkLoadEvent;
import cn.nukkit.event.level.ChunkUnloadEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.transaction.InventoryTransaction;
import cn.nukkit.inventory.transaction.action.InventoryAction;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.math.BlockFace;
import me.onebone.economyapi.EconomyAPI;
import org.sobadfish.sales.config.SalesData;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.form.AdminForm;
import org.sobadfish.sales.form.SellItemForm;
import org.sobadfish.sales.items.MoneyItem;
import org.sobadfish.sales.panel.DisplayPlayerPanel;
import org.sobadfish.sales.panel.button.BasePlayPanelItemInstance;
import org.sobadfish.sales.panel.lib.ChestPanel;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Sobadfish
 * @date 2023/11/16
 */
public class SalesListener implements Listener {

    public SalesMainClass main;

    public static LinkedHashMap<String,DisplayPlayerPanel> chestPanelLinkedHashMap = new LinkedHashMap<>();

//    public List<Player> breakLock = new ArrayList<>();

    public SalesListener(SalesMainClass salesMainClass){
        this.main = salesMainClass;
    }

    public static LinkedHashMap<String,SalesEntity> cacheEntitys = new LinkedHashMap<>();



    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event){
        Block block = event.getBlock();
        SalesEntity entity1 = getEntityByPos(block);
        if(entity1 != null){
            Player player = event.getPlayer();
            if(player.isSneaking()){
                if(event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK){
                    if(player.isOp() || (entity1.master != null && entity1.master.equalsIgnoreCase(player.getName()))){
                        if(player.getInventory().getItemInHand().getId() == 0){
                            return;
                        }
                        event.setCancelled();
                        SellItemForm sellItemForm = new SellItemForm(entity1,player.getInventory().getItemInHand());
                        sellItemForm.display(player);
                    }
                }
            }else{
                if(event.getAction() == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK || event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK){
                    event.setCancelled();
                    DisplayPlayerPanel displayPlayerPanel = new DisplayPlayerPanel(entity1);
                    displayPlayerPanel.open(player);
                    chestPanelLinkedHashMap.put(player.getName(),displayPlayerPanel);

                }

            }

        }

//        }
        //使用金币
        Item item = event.getItem();
        Player player = event.getPlayer();
        if(item.hasCompoundTag() && item.getNamedTag().contains(MoneyItem.TAG)){
            double money = item.getNamedTag().getDouble(MoneyItem.TAG) * item.getCount();
            item.setCount(item.getCount() - item.getCount());
            player.getInventory().setItemInHand(item);
            try {
                Class.forName("me.onebone.economyapi.EconomyAPI");
                EconomyAPI.getInstance().addMoney(player,money);
                player.level.addSound(player, Sound.ARMOR_EQUIP_IRON);
                SalesMainClass.sendMessageToObject("&r获得金币 x &e"+money,player);
            } catch (ClassNotFoundException e) {
                SalesMainClass.sendMessageToObject("&c无经济核心!",player);

            }

        }
        //TODO 自定义物品的放置
        //TODO 放置物品
        if(SalesMainClass.LOAD_CUSTOM){
            if(item.hasCompoundTag() && item.getNamedTag().contains("saleskey")){
                if(SalesEntity.spawnToAll(block.getSide(event.getFace()),player.getDirection(),player.getName(),null) != null){
                    if (player.isSurvival() || player.isAdventure()) {
                        Item item2 = player.getInventory().getItemInHand();
                        item2.setCount(item2.getCount() - 1);
                        player.getInventory().setItemInHand(item2);

                    }
                }else{
                    SalesMainClass.sendMessageToObject("&c生成失败！ 请保证周围没有其他方块",player);
                }

            }
        }


//        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event){
        //加载区间下的所有 实体

        if( SalesMainClass.INSTANCE.sqliteHelper != null){
            SalesMainClass.INSTANCE.getLogger().info("chunk: "+event.getChunk().getX()+" -- "+event.getChunk().getZ());
            List<SalesData> salesData = SalesMainClass.INSTANCE.sqliteHelper.getDataByString(SalesMainClass.DB_TABLE,
                    "chunkx = ? and chunkz = ?",new String[]{
                            (event.getChunk().getX())+"",

                            (event.getChunk().getZ())+""
                    }, SalesData.class);
            if(salesData.size() > 0){
                SalesMainClass.INSTANCE.getLogger().info(salesData.toString());
                for(SalesData data : salesData){

                    if(!cacheEntitys.containsKey(data.location)){

                        SalesEntity entity = SalesEntity.spawnToAll(data.asPosition(), BlockFace.valueOf(data.bf.toUpperCase()),data.master,data);
                        cacheEntitys.put(data.location,entity);
                    }

                }
            }

        }


    }

    @EventHandler
    public void onChunkUnLoad(ChunkUnloadEvent event){
        //卸载区间下的所有 实体
        for(Entity e: event.getChunk().getEntities().values()){
            if(e instanceof SalesEntity){
                e.close();
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){

        if(event.getBlockAgainst().getId() == main.iBarrier.getBid()) {
            SalesEntity entity1 = getEntityByPos(event.getBlock());
            if(entity1 != null){
                event.setCancelled();
            }
//            BlockEntity entity = event.getBlockAgainst().level.getBlockEntity(event.getBlockAgainst());
//            if (entity instanceof SalesEntity.SalesBlockEntity) {
//
//            }
        }
    }

    @EventHandler
    public void onFormListener(PlayerFormRespondedEvent event) {
        if (event.wasClosed()) {
            return;
        }
        if(SellItemForm.DISPLAY_FROM.containsKey(event.getPlayer())){
            SellItemForm form = SellItemForm.DISPLAY_FROM.get(event.getPlayer());
            if(form.getId() == event.getFormID()){
                if(event.getResponse() instanceof FormResponseCustom){
                    form.onListener(event.getPlayer(), (FormResponseCustom) event.getResponse());

                }
                SellItemForm.DISPLAY_FROM.remove(event.getPlayer());
            }else{
                SellItemForm.DISPLAY_FROM.remove(event.getPlayer());
            }
        }
        if(AdminForm.DISPLAY_FROM.containsKey(event.getPlayer())){
            AdminForm form = AdminForm.DISPLAY_FROM.get(event.getPlayer());
            if(form.getId() == event.getFormID()){
                if(event.getResponse() instanceof FormResponseCustom){
                    form.onListener(event.getPlayer(), (FormResponseCustom) event.getResponse());

                }
                AdminForm.DISPLAY_FROM.remove(event.getPlayer());
            }else{
                AdminForm.DISPLAY_FROM.remove(event.getPlayer());
            }
        }
    }

    public SalesEntity getEntityByPos(Position position){
        String[] vl = new String[]{
                SalesEntity.asLocation(position),
                SalesEntity.asLocation(position.add(0,-1))
        };
        for(String v : vl){
            if(cacheEntitys.containsKey(v)){
                return cacheEntitys.get(v);
            }
        }
        return null;

    }

    @EventHandler
    public void onBlockUpdate(BlockUpdateEvent event){
        Block upblock = event.getBlock();
        Block old = upblock.level.getBlock(upblock);
        if(old.getId() != main.iBarrier.getBid() && upblock.getId() == main.iBarrier.getBid()){
            SalesEntity entity1 = getEntityByPos(event.getBlock());
            if(entity1 != null){
                if(entity1.finalClose){
                    return;
                }
                entity1.toClose();

            }
//            BlockEntity entity = old.level.getBlockEntity(old);
//            if(entity instanceof SalesEntity.SalesBlockEntity){
//                SalesEntity entity1 = ((SalesEntity.SalesBlockEntity) entity).sales;

//            }
        }

    }

    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        if(event.isCancelled()){
            return;
        }
        if(event.getPlayer().isOp()){
            if(block.getId() == main.iBarrier.getBid()){
                SalesEntity entity1 = getEntityByPos(event.getBlock());
                if(entity1 != null){
                    event.setCancelled();
                    entity1.toClose();

                }
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
