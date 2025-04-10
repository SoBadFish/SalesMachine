package org.sobadfish.sales.entity;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockStone;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.inventory.BaseInventory;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.DestroyBlockParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.AddItemEntityPacket;
import cn.nukkit.network.protocol.RemoveEntityPacket;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.RegisterItemServices;
import org.sobadfish.sales.SalesListener;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.config.ItemData;
import org.sobadfish.sales.config.SaleSettingConfig;
import org.sobadfish.sales.config.SaleSkinConfig;
import org.sobadfish.sales.config.SalesData;
import org.sobadfish.sales.db.SqlData;
import org.sobadfish.sales.economy.IItemMoney;
import org.sobadfish.sales.economy.IMoney;
import org.sobadfish.sales.items.SaleItem;
import org.sobadfish.sales.panel.lib.ChestPanel;
import org.sobadfish.sales.panel.lib.DoubleChestPanel;
import org.sobadfish.sales.panel.lib.IDisplayPanel;

import java.util.*;

/**
 * @author Sobadfish
 * @date 2023/11/16
 */
public class SalesEntity extends EntityHuman {

    public static final String ENTITY_TYPE = "SalesEntity";

    public static final String SALE_MASTER_TAG = "SalesMaster";

    public List<AddItemEntityPacket> ipacket = new ArrayList<>();

    public List<SaleItem> items = new ArrayList<>();

    public ListTag<CompoundTag> loadItems;



    public Map<String, ChestPanel> clickPlayers = new LinkedHashMap<>();

    public SalesData salesData;

    public BlockFace blockFace;

    public String master;

    public SaleSettingConfig saleSettingConfig;

    public Map<String, DoubleChestPanel> clickInvPlayers = new LinkedHashMap<>();


    @Override
    public float getWidth() {
        return 0.0f;
    }

    @Override
    public float getHeight() {
        return 0.0f;
    }


    @Override
    public void saveNBT() {
    }

    public SalesEntity(FullChunk chunk, CompoundTag nbt, BlockFace face, String master, SaleSettingConfig config, ListTag<CompoundTag> load) {
        super(chunk, nbt);
        this.blockFace = face;
        this.loadItems = load;
        this.saleSettingConfig = config;
        setScale((float) saleSettingConfig.entitySize);
        this.master = master;
        //解包物品
        setImmobile();
        if (loadItems != null) {
            ListTag<CompoundTag> cl = loadItems;
            for (CompoundTag compoundTag : cl.getAll()) {
                Item item = NBTIO.getItemHelper(compoundTag.getCompound("item"));
                int stack = compoundTag.getInt("stack");
                double money = compoundTag.getDouble("money");
                boolean visable = compoundTag.getBoolean("visable",true);
                items.add(new SaleItem(compoundTag, item, stack, money,visable));

            }
        }

    }



    /**
     * 减少物品
     *
     * @param item 需要移除的物品
     */
    public void removeItem(String playerName, SaleItem item, int count, boolean updateInv) {
        ListTag<CompoundTag> cl = loadItems;
        int index = 0;
        for (SaleItem saleItem : new ArrayList<>(items)) {
            if (saleItem.saleItem.equals(item.saleItem, true, true)) {
                //相同物品
                if (cl.size() <= index) {
                    //理论不会出现这个问题... 以防万一
                    return;
                }
                CompoundTag tg = cl.get(index);
                //如果传入的是 0 直接移除就行
                if (count == 0 && master.equalsIgnoreCase(playerName)) {
                    cl.remove(tg);
                    items.remove(saleItem);


                    break;
                } else {

                    if (tg.getInt("stack") >= count) {
                        tg.putInt("stack", tg.getInt("stack") - count);
                        saleItem.stack -= count;

                        break;
                    } else {
//                        int count2 = tg.getInt("stack");
                        saleItem.stack = 0;
                        cl.remove(tg);
                        items.remove(saleItem);


                        break;
                    }
                }

            }
            index++;
        }
        updateInventory(updateInv);
//        //重新写入
        salesData.saveItemSlots(loadItems);
        saveData();
    }


    /**
     * 获取对应的货币库存
     * @param item 自定义货币
     * */
    public SaleItem getCoinSaleItem(Item item){
        for (SaleItem saleItem: items){
            if(!saleItem.visable){
                if(saleItem.saleItem.equals(item,true,true)){
                    return saleItem;
                }
            }
        }
        return null;
    }

    /**
     * 向售货机钱罐添加物品
     * @param moneyItem 自定义货币
     * @param count 数量
     * */
    public boolean addCoinMoney(Item moneyItem,int count){
        //添加硬币到私有库存
        Item clone = moneyItem.clone();
        clone.setCount(1);
        return addItem(new SaleItem(clone,count,0,false),true);
    }

    /**
     * 向售货机钱罐移除物品
     * @param moneyItem 自定义货币
     * @param count 数量
     * */
    public boolean reduceCoinMoney(Item moneyItem,int count){
        //添加硬币到私有库存
        Item clone = moneyItem.clone();
        clone.setCount(1);
        //售货机主人的
        SaleItem item = getCoinSaleItem(clone);
        if(item.stack >= count){
            removeItem(null,item,count,true);
            return true;
        }else{
            return false;
        }

    }

    /**
     * 计算库存数量
     * */
    public ItemStack getItemInventoryByItem(Item item) {
//        List<Item> itemMap = new ArrayList<>();
        ItemStack stack = null;
        for (SaleItem saleItem : items) {
            if (saleItem.saleItem.equals(item, true, true)) {
                Item cl = saleItem.saleItem.clone();

                if(saleItem.stack > cl.getMaxStackSize()){
                    cl.setCount(cl.getMaxStackSize());
                }

                stack = new ItemStack();
                stack.item = cl;
                stack.stack = saleItem.stack;
                stack.pageSize = (int) Math.ceil(saleItem.stack / (float) item.getMaxStackSize());
                int cc =  ((stack.pageSize - 1) * item.getMaxStackSize());
                if(cc < 0){
                    cc = 0;
                }
                int mm = saleItem.stack - cc;
                if(mm > cl.getMaxStackSize()){
                    stack.endCount = mm - cl.getMaxStackSize();
                }else{
                    stack.endCount = mm;
                }
                break;

            }
        }
        return stack;

    }

    public static String asLocation(Position position) {
        return position.getFloorX() + ":" + position.getFloorY() + ":" + position.getFloorZ() + ":" + position.level.getFolderName();
    }

    public void saveData() {
        String location = asLocation(this);
        if (SalesMainClass.INSTANCE.sqliteHelper.hasData(SalesMainClass.DB_TABLE, "location", location)) {
            SqlData data = new SqlData();
            data.put("location", location);
            SalesMainClass.INSTANCE.sqliteHelper.set(SalesMainClass.DB_TABLE, data, salesData);
        }
    }

    public boolean hasItem(Item item) {
        for (SaleItem saleItem : items) {
            if (saleItem.saleItem.equals(item, true, true)) {
                return true;
            }
        }
        return false;
    }

    public boolean addItem(Item item) {
        SaleItem saleItem = new SaleItem(item, item.count, 0);
        return addItem(saleItem, true);
    }


    public boolean addItem(SaleItem item, boolean updateInv) {

        ListTag<CompoundTag> cl = loadItems;
        int index = 0;
        for (SaleItem saleItem : items) {
            if (saleItem.saleItem.equals(item.saleItem, true, true)) {
                //相同物品
                if (cl.size() <= index) {
                    //理论不会出现这个问题... 以防万一
                    return false;
                }
                CompoundTag tg = cl.get(index);
                if(!tg.contains("loadMoney")){
                    tg.putString("loadMoney",saleItem.loadMoney);
                }
                tg.putInt("stack", tg.getInt("stack") + item.stack);
                if (item.money > 0) {
                    tg.putDouble("money", item.money);
                    saleItem.money = item.money;
                }
                saleItem.stack += item.stack;
                updateInventory(updateInv);
                salesData.saveItemSlots(loadItems);
                saveData();
                return true;
            }
            index++;
        }


        if (items.size() >= InventoryType.DOUBLE_CHEST.getDefaultSize() - 8) {
            return false;
        }
        if (item.saleItem.getId() == 0) {
            return false;
        }
        //TODO 如果经济是 自定义货币 且 本身不是货币
        if(item.visable){
            IMoney iMoney = SalesMainClass.getLoadMoney().get(item.loadMoney);
            if(iMoney instanceof IItemMoney){
                boolean exists = false;
                for (SaleItem saleItem : items) {
                    if (saleItem.saleItem.equals(((IItemMoney) iMoney).getMoneyItem(), true, true)) {
                        exists = true;
                        break;
                    }
                }
                if(!exists){
                    //递归添加经济物品
                    addItem(new SaleItem(((IItemMoney) iMoney).getMoneyItem(),0,item.loadMoney,0,false),true);
                }
            }
        }

        CompoundTag ct = item.tag;
        ct.putCompound("item", NBTIO.putItemHelper(item.saleItem));

        ct.putBoolean("visable",item.visable);
        ct.putString("loadMoney", item.loadMoney);
        ct.putInt("stack", item.stack);
        ct.putDouble("money", item.money);

        cl.add(ct);
        item.tag = ct;
        items.add(item);

        removePacketsAll();
        updateInventory(updateInv);
        salesData.saveItemSlots(loadItems);
        saveData();

        return true;
    }

    public SaleItem getSaleItemByItem(Item item) {
        for (SaleItem saleItem : items) {
            if (saleItem.saleItem.equals(item, true, true)) {
                return saleItem;
            }
        }
        return null;
    }

    public void updateInventory(boolean updateInv) {
        for (ChestPanel chestPanel : clickPlayers.values()) {
            chestPanel.update();
        }
        if (updateInv) {
            for (DoubleChestPanel chestPanel : clickInvPlayers.values()) {
                chestPanel.update(true);
            }
        }
    }


    public boolean isOpen;

    /**
     * 2: 正在关闭
     * 1: 正在打开
     * 0： 无
     */
    public int animLoad = 0;

    public void openIt(Player who) {
        animLoad = 1;

    }

    public void closeIt(Player who) {
        animLoad = 2;

    }


    public float right = 42f;

    public float yawSpeed = 3f;

    @Override
    public boolean onUpdate(int currentTick) {
        boolean b = super.onUpdate(currentTick);
        // 将角度限制在 -180 到 180 之间
        if (saleSettingConfig.enableAnim) {
            if (animLoad == 1) {

                if (yaw > right) {
                    yaw -= yawSpeed;
                } else if (yaw < right) {
                    yaw += yawSpeed;
                } else {
                    animLoad = 0;
                    isOpen = true;
                }
            }

            if (animLoad == 2) {
                if (yaw > 0) {
                    yaw -= yawSpeed;
                } else if (yaw < 0) {
                    yaw += yawSpeed;
                } else {
                    animLoad = 0;
                    isOpen = false;
                }
            }
        }
        updateMovement();

        boolean s = false;
        //检查online数组

        if (saleSettingConfig.enableItem) {
            for(Player player: new ArrayList<>(onlinePlayers)){
                if(!player.isOnline()){
                    onlinePlayers.remove(player);
                    continue;
                }
                if(!player.level.getFolderName().equalsIgnoreCase(this.level.getFolderName())){
                    //onlinePlayers.remove(player);
                    removePackets(player);
                }
            }
            for (Player player : getLevel().getPlayers().values()) {
                if (player.distance(this) <= 10) {
                    showItems(player);
                    s = true;
//                    break;
                }else{
                    removePackets(player);
                }
            }
        }


        if (!s) {
            removePacketsAll();
        }


        return b;
    }

    public List<Player> onlinePlayers = new ArrayList<>();

    public boolean equalsItemStr(String str) {
        if (str == null || str.isEmpty()) { // 过滤无效查询
            return false;
        }

        return items.stream().anyMatch(item -> {
            Item saleItem = item.saleItem;

            // 提取可能多次访问的字段，减少重复调用
            String namespaceId = saleItem.getNamespaceId();
            String itemName = item.getItemName();
            String itemDefName = saleItem.getName();
            String itemDisplayName = saleItem.getDisplayName();
            String customName = saleItem.getCustomName();

            // 合并数据源查询
            ItemData data = SalesMainClass.INSTANCE.getItemDataByItem(saleItem);
            String chineseName = (data != null) ? data.nameChinese : null;

            // 带空指针检查的字符串匹配
            return containsSafe(namespaceId, str)
                    || containsSafe(itemName, str)
                    || containsSafe(customName, str)
                    || containsSafe(chineseName, str)
                    || containsSafe(itemDefName, str)
                    || containsSafe(itemDisplayName, str);
        });
    }

    // 辅助方法避免重复的 null 检查逻辑
    private boolean containsSafe(String source, String target) {
        return source != null && source.contains(target);
    }


    public void removePackets(Player player) {
        for (AddItemEntityPacket dataPacket : new ArrayList<>(ipacket)) {
            RemoveEntityPacket pk1 = new RemoveEntityPacket();
            pk1.eid = dataPacket.entityUniqueId;
            if(player.isOnline()) {
                player.dataPacket(pk1);
            }
        }
        onlinePlayers.remove(player);
    }
    public void removePacketsAll() {
        for (AddItemEntityPacket dataPacket : new ArrayList<>(ipacket)) {
            RemoveEntityPacket pk1 = new RemoveEntityPacket();
            pk1.eid = dataPacket.entityUniqueId;
            for(Player player: onlinePlayers){
                if(player.isOnline()){
                    player.dataPacket(pk1);
                }
//                Server.broadcastPacket(Server.getInstance().getOnlinePlayers().values(), pk1);
            }
            onlinePlayers.clear();

        }
        ipacket.clear();
    }

    /**
     * 重新将包发给玩家..
     * */
    private void showItems(Player player) {
        if(ipacket.size() == 0){
            showItems();
            return;
        }

        if(!onlinePlayers.contains(player)){
            onlinePlayers.add(player);
            for (AddItemEntityPacket dataPacket : ipacket) {
                player.dataPacket(dataPacket);
            }
        }

    }

    private void showItems() {
        if (ipacket.size() == 0) {
            int size = 6;
            if (saleSettingConfig.floatItemPos.containsKey(blockFace)) {
                size = saleSettingConfig.floatItemPos.get(blockFace).size();
            }

            List<SaleItem> nItems = new ArrayList<>();
            for(SaleItem item : items){
                if(!item.visable){
                    continue;
                }
                nItems.add(item);
            }

            for (int i = 0; i < Math.min(nItems.size(), size); i++) {
                long eid = (long) ((int) this.x + new Random().nextDouble() + (int) this.z + new Random().nextDouble()) + new Random().nextLong();
                Position ps = asPosition(i);
                if (ps == null) {
                    continue;
                }
                ipacket.add(i, getEntityTag(ps, nItems.get(i).saleItem, eid));
            }
            onlinePlayers.addAll(level.getPlayers().values());
            for (AddItemEntityPacket dataPacket : ipacket) {
                Server.broadcastPacket(onlinePlayers, dataPacket);
            }
        }
    }

    public Position asPosition(int index) {
        if (saleSettingConfig.floatItemPos.containsKey(blockFace)) {
            List<Vector3> vector3s = saleSettingConfig.floatItemPos.get(blockFace);

            if (vector3s.size() > index) {
                Vector3 v3 = vector3s.get(index);
                return new Position(this.x + v3.x, this.y + v3.y, this.z + v3.z);
            } else {
                return null;
            }

        } else {
            return null;
        }


    }


    private AddItemEntityPacket getEntityTag(Position position, Item item, long eid) {

        Item ic = item.clone();
        ic.setCount(1);
        AddItemEntityPacket pk = new AddItemEntityPacket();
        pk.item = ic;
        pk.entityRuntimeId = eid;
        pk.entityUniqueId = eid;
        pk.x = (float) (position.x);
        pk.y = (float) (position.y);
        pk.z = (float) (position.z);
        pk.speedX = 0.0F;
        pk.speedY = 0.0F;
        pk.speedZ = 0.0F;
        pk.metadata = new EntityMetadata()
                .putBoolean(80, true)

                .putBoolean(Entity.DATA_FLAG_IMMOBILE, true)
                .putLong(Entity.DATA_LEAD_HOLDER_EID, -1)
                .putFloat(Entity.DATA_SCALE, 0.5f)
                .putBoolean(Entity.DATA_FLAG_GRAVITY, false)
                .putInt(79, 0);

        return pk;

    }

    public boolean finalClose;

    public boolean isPackage;

    @Override
    public void close() {
        //移除
        try {
            //不要忘记把窗口移除..
            for(Map.Entry<String, ChestPanel> chestPanelEntry:clickPlayers.entrySet()){
                //关闭..
                Player player = Server.getInstance().getPlayer(chestPanelEntry.getKey());
                if(player != null){
                    SalesMainClass.sendMessageToObject("&c售货机被移除或已移动!",player);
                    chestPanelEntry.getValue().close(player);
                }

            }
            for(Map.Entry<String, DoubleChestPanel> chestPanelEntry:clickInvPlayers.entrySet()){
                //关闭..
                Player player = Server.getInstance().getPlayer(chestPanelEntry.getKey());
                if(player != null){
                    SalesMainClass.sendMessageToObject("&c售货机被移除或已移动!",player);
                    chestPanelEntry.getValue().close(player);
                }

            }


            List<Position> p3 = positionListByConfig(this, blockFace, salesData.width, salesData.height);
            for (Position position : p3) {
                String lo = asLocation(position);
                if (SalesListener.cacheEntitys.containsKey(lo)) {
                    SalesEntity salesEntity = SalesListener.cacheEntitys.get(lo);
                    if (salesEntity.equals(this)) {
                        SalesListener.cacheEntitys.remove(lo);
                    } else {
                        continue;
                    }
                }


                level.setBlock(position, new BlockAir(), true, true);
                if (!isPackage) {
                    level.addParticle(new DestroyBlockParticle(position, new BlockStone()));
                }
                BlockEntity be = level.getBlockEntity(position);
                if (be instanceof SalesBlockEntity) {
                    level.removeBlockEntity(be);
                }
            }

            //TODO 检查缓存实体是否有残留
            for(SalesEntity salesEntity : SalesListener.cacheEntitys.values()){
                if(salesEntity.equals(this)){
                    SalesListener.cacheEntitys.remove(asLocation(salesEntity));
                }
            }

            if (!finalClose) {
                salesData.saveItemSlots(loadItems);
                saveData();
            }

        }catch (Exception ignore){
        }finally {
            //不管怎么样 保证移除
            removePacketsAll();
            super.close();
        }


    }

    public void setItem(Item item, int count, int pageSize) {
        SaleItem saleItem = getSaleItemByItem(item);

        if (saleItem != null) {
            boolean add = false;
            int cz = Math.abs(count - pageSize);
            if (count > pageSize) {
                add = true;
            } else if (pageSize == count) {
                return;
            }


            SaleItem s1 = new SaleItem(item, cz, 0);
            if (add) {
                addItem(s1, false);
            } else {
                removeItem(master, s1, cz, false);
            }
        }
    }

    //打包带走
    public CompoundTag toPackage() {
        //直接打包成tag 然后移除
        salesData.saveItemSlots(loadItems);
        CompoundTag tag = salesData.toPackage();
        finalClose = true;
        isPackage = true;
        for (Map.Entry<String, IDisplayPanel> dis : SalesListener.chestPanelLinkedHashMap.entrySet()) {
            if (dis.getValue().getSales().equals(this)) {
                dis.getValue().close();
                SalesListener.chestPanelLinkedHashMap.remove(dis.getKey());
            }

        }
        close();
        String as = asLocation(this);
        //后台写入
        Server.getInstance().getScheduler().scheduleTask(SalesMainClass.INSTANCE, new Runnable() {
            @Override
            public void run() {
                SalesMainClass.INSTANCE.sqliteHelper.remove(SalesMainClass.DB_TABLE, "location", as);
            }
        });

//        SalesListener.cacheEntitys.remove(as);
        return tag;

    }

    public void setCustomName(String name) {
        salesData.customname = name;
        saveData();

    }

    public void closePanel() {
        for (Map.Entry<String, IDisplayPanel> dis : SalesListener.chestPanelLinkedHashMap.entrySet()) {
            if (dis.getValue().getSales().equals(this)) {
                dis.getValue().close();
                SalesListener.chestPanelLinkedHashMap.remove(dis.getKey());
            }

        }
    }


    public void toClose() {
        finalClose = true;
        //生成一地掉落物

        closePanel();

        for (SaleItem saleItem : items) {
            Item cl = saleItem.saleItem.clone();
            cl.setCount(saleItem.stack);
            level.dropItem(this, cl);
        }
        //网店许可证也能掉落
        if(salesData.netuse == 1){
            level.dropItem(this, RegisterItemServices.CUSTOM_ITEMS.get("netxk"));
        }

        close();
        String as = asLocation(this);
        SalesMainClass.INSTANCE.sqliteHelper.remove(SalesMainClass.DB_TABLE, "location", as);
//        SalesListener.cacheEntitys.remove(as);

//        level.save(true);
    }

    public static SalesEntity spawnToAll(Position position, BlockFace bf, String master, SalesData data,Item handItem) {
        return spawnToAll(position, bf, master, data, false, true,null,handItem);
    }

    public static SalesEntity spawnToAll(Position position, BlockFace bf, String master, SalesData data,String skin,Item handItem) {
        return spawnToAll(position, bf, master, data, false, true,skin,handItem);
    }

    public static SalesEntity spawnToAll(Position position, BlockFace bf, String master, SalesData data, boolean ignoreBlocks, boolean init,String skin,Item handItem) {
        if (bf == null) {
            bf = BlockFace.EAST;
        }


        if (SalesMainClass.banWorlds.contains(position.level.getFolderName())) {
            if (init) {
                Player player = Server.getInstance().getPlayer(master);
                //检测地图
                if (player == null || !player.isOp()) {
                    return null;
                }

            }

        }
        if (SalesMainClass.ENTITY_SKIN.size() == 0) {
            return null;
        }
        String modelName = new ArrayList<>(SalesMainClass.ENTITY_SKIN.keySet()).get(0);
        SaleSkinConfig saleSkinConfig = null;
        if(skin == null){
            if (data != null && data.skinmodel != null) {
                String smd = data.skinmodel;
                if (SalesMainClass.ENTITY_SKIN.containsKey(smd)) {
                    modelName = smd;
                }
            }
            saleSkinConfig = SalesMainClass.ENTITY_SKIN.get(modelName);
        }else{
            if ( SalesMainClass.ENTITY_SKIN.containsKey(skin)){
                saleSkinConfig =  SalesMainClass.ENTITY_SKIN.get(skin);
                modelName = skin;
            }

        }
        if(saleSkinConfig == null){
            saleSkinConfig = SalesMainClass.ENTITY_SKIN.get(modelName);
        }

        Position pos = new Position(
                position.getFloorX() + 0.5,
                position.getFloorY(),
                position.getFloorZ() + 0.5,
                position.level);
        SaleSettingConfig.SaleWeight weight = saleSkinConfig.config.weight;
        int width = weight.width;
        int height = weight.height;

        List<Position> psconfig = positionListByConfig(position, bf, width, height);

        boolean hasBlock = false;
        if (!ignoreBlocks) {
            hasBlock = hasBlockByPositionList(psconfig);
        }

        if (!hasBlock) {
            Skin skinV2 = saleSkinConfig.skinLinkedHashMap.get(bf);

            CompoundTag tag = EntityHuman.getDefaultNBT(pos);
            tag.putString(SALE_MASTER_TAG, master);
            tag.putCompound("Skin", new CompoundTag()
                    .putByteArray("Data", skinV2.getSkinData().data)
                    .putString("ModelId", skinV2.getSkinId())
            );
            ListTag<CompoundTag> tagListTag = new ListTag<>();
            if (data != null) {
                tagListTag = data.asItemSlots();

            }
//            Item placeItem =
            SalesEntity sales = new SalesEntity(position.getChunk(), tag, bf, master, saleSkinConfig.config, tagListTag);
            sales.setSkin(skinV2);
            sales.spawnToAll();


            for (Position sp : psconfig) {
                String pps = asLocation(sp);
                position.getLevel().setBlock(sp, (Block) SalesMainClass.INSTANCE.services.iBarrier, false, false);
                BlockEntity.createBlockEntity(SalesBlockEntity.ENTITY_TYPE, pos.getChunk(),
                        BlockEntity.getDefaultCompound(sp, SalesBlockEntity.ENTITY_TYPE), sales);
//                if (!SalesListener.cacheEntitys.containsKey(pps)) {
                SalesListener.cacheEntitys.put(pps, sales);
//                }
            }

            String ps = asLocation(position);
            if (data == null) {
                data = new SalesData();
            }
            data.saveItemSlots(tagListTag);
            data.chunkx = pos.getChunkX();
            data.chunkz = pos.getChunkZ();
            data.width = width;
            data.height = height;
            data.world = position.level.getFolderName();
            data.location = ps;
            data.master = master;
            if(data.uuid == null || "".equalsIgnoreCase(data.uuid)){
                data.uuid = UUID.randomUUID().toString();
            }
            data.bf = bf.getName();
            data.skinmodel = modelName;
            if(handItem != null){
                data.setPlaceItem(handItem);
            }
            if(data.customname == null || "".equalsIgnoreCase(data.customname)){
                data.customname = master + " 的售货机";
            }

            if(handItem != null){
                data.setPlaceItem(handItem);
            }
            if (SalesMainClass.INSTANCE.sqliteHelper.hasData(SalesMainClass.DB_TABLE, "location", ps)) {
                SalesMainClass.INSTANCE.sqliteHelper.set(SalesMainClass.DB_TABLE, "location", ps, data);
            } else {
                SalesMainClass.INSTANCE.sqliteHelper.add(SalesMainClass.DB_TABLE, data);
            }
            sales.salesData = data;
            return sales;
        }
        return null;
    }

    public static boolean hasBlockByPositionList(List<Position> positions){
        for (Position ps : positions) {
            Block block = ps.level.getBlock(ps);
            if (block.getId() != 0 && block.getId() != SalesMainClass.INSTANCE.services.iBarrier.getBid()) {
                return true;
            }

        }
        return false;
    }

    public boolean setModel(String model) {
        //重设
        if (SalesMainClass.ENTITY_SKIN.containsKey(model)) {
            //先计算一下新的模块是否会覆盖方块...
            SaleSkinConfig saleSkinConfig = SalesMainClass.ENTITY_SKIN.get(model);
            SaleSettingConfig saleSettingConfig = saleSkinConfig.config;
            boolean ig = hasBlockByPositionList(positionListByConfig(this,blockFace
                    ,saleSettingConfig.weight.width
                    ,saleSettingConfig.weight.height));
            if(!ig){
                close();
                SalesData oldSd = salesData;
                oldSd.skinmodel = model;

                oldSd.width = saleSkinConfig.config.weight.width;
                oldSd.height = saleSkinConfig.config.weight.height;
                saveData();

                SalesEntity.spawnToAll(oldSd.asPosition(),
                        BlockFace.valueOf(oldSd.bf.toUpperCase()), oldSd.master, oldSd,
                        true, true,null,null);
                return true;
            }
        }

        return false;
    }

    public static List<Position> positionListByConfig(Position position, BlockFace blockFace, int width, int height) {
        List<Position> positions = new ArrayList<>();
        BlockFace rf = blockFace.rotateY();
        for (int i = 0; i < width; i++) {
            Position ry = position.getSide(rf, i);
            for (int y = 0; y < height; y++) {
                positions.add(ry.add(0, y));
            }

        }
        return positions;
    }

    public static class SalesBlockEntity extends BlockEntity implements InventoryHolder {

        public SaleBlockEntityInventory entityInventory;

        public static final String ENTITY_TYPE = "Sales";

        public SalesEntity salesEntity;

        public SalesBlockEntity(FullChunk chunk, CompoundTag nbt, SalesEntity entity) {
            super(chunk, nbt);
            entityInventory = new SaleBlockEntityInventory(entity, this);
            this.salesEntity = entity;
        }

        @Override
        public boolean isBlockEntityValid() {
            return true;
        }

        @Override
        public void saveNBT() {
        }

        @Override
        public Inventory getInventory() {
            return entityInventory;
        }

        public static class SaleBlockEntityInventory extends BaseInventory {

            public SalesEntity salesEntity;

            public SaleBlockEntityInventory(SalesEntity salesEntity, InventoryHolder holder) {
                super(holder, InventoryType.CHEST);
                this.salesEntity = salesEntity;
            }

            public SalesEntity getSalesEntity() {
                return salesEntity;
            }
        }
    }


    public Item getShaleItem(){
        Item drop = salesData.asPlaceItem();
        if(drop.getId() == 0) {
            String name = "sale_v" + (saleSettingConfig.meta + 1);
            if (!RegisterItemServices.CUSTOM_ITEMS.containsKey(name)) {
                name = "sale_v1";
            }
            Item item = RegisterItemServices.CUSTOM_ITEMS.get(name);
            item.setCustomName(TextFormat.colorize('&', "&r&l&e售货机"));

            item.setLore(TextFormat.colorize('&', "&r&7\n放置即可生成"));
            CompoundTag compoundTag = item.getNamedTag();
            compoundTag.putBoolean("saleskey", true);
            item.addEnchantment(Enchantment.getEnchantment(0).setLevel(1));
            return item;
        }else{
            return drop;
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SalesEntity) {
            return asLocation(this).equalsIgnoreCase(
                    asLocation((SalesEntity) obj)
            ) && blockFace == ((SalesEntity) obj).blockFace;
        }
        return false;
    }

    public static class ItemStack {

        public Item item;


        public int stack;

        public int pageSize;

        public int endCount;

    }
}
