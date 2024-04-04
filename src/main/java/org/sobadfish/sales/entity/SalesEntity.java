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
import org.sobadfish.sales.SalesListener;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.config.SaleSettingConfig;
import org.sobadfish.sales.config.SaleSkinConfig;
import org.sobadfish.sales.config.SalesData;
import org.sobadfish.sales.db.SqlData;
import org.sobadfish.sales.items.SaleItem;
import org.sobadfish.sales.panel.DisplayPlayerPanel;
import org.sobadfish.sales.panel.lib.ChestPanel;

import java.util.*;

/**
 * @author Sobadfish
 * @date 2023/11/16
 */
public class SalesEntity extends EntityHuman{

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

    public SalesEntity(FullChunk chunk, CompoundTag nbt, BlockFace face,String master,SaleSettingConfig config,ListTag<CompoundTag> load) {
        super(chunk, nbt);
        this.blockFace = face;
        this.loadItems = load;
        this.saleSettingConfig = config;
        setScale((float) saleSettingConfig.entitySize);
        this.master = master;
        //解包物品
        setImmobile();
        if(loadItems != null){
            ListTag<CompoundTag> cl = loadItems;
            for(CompoundTag compoundTag: cl.getAll()){
                Item item = NBTIO.getItemHelper(compoundTag.getCompound("item"));
                int stack = compoundTag.getInt("stack");
                double money = compoundTag.getDouble("money");
                items.add(new SaleItem(compoundTag,item,stack,money));

            }
        }

    }

    /**
     * 减少物品
     *
     * @param item 需要移除的物品
     * @return 实际移除的数量
     * */
    public int removeItem(String playerName,SaleItem item,int count){
//        ListTag<CompoundTag> cl = namedTag.getList("sale_items",CompoundTag.class);
        ListTag<CompoundTag> cl = loadItems;
        int index = 0;
        int cc = 0;
        for(SaleItem saleItem: new ArrayList<>(items)) {
            if (saleItem.saleItem.equals(item.saleItem, true, true)) {
                //相同物品
                if(cl.size() <= index){
                    //理论不会出现这个问题... 以防万一
                    return 0;
                }
                CompoundTag tg = cl.get(index);
                //如果传入的是 0 直接移除就行
                if(count == 0 && master.equalsIgnoreCase(playerName)){
                    cl.remove(tg);
                    items.remove(saleItem);
                    break;
                }else{

                    if(tg.getInt("stack") >= count){
                        tg.putInt("stack",tg.getInt("stack") - count);
                        saleItem.stack -= count;
                        cc = count;
                        break;
                    }else{
                        int count2 = tg.getInt("stack");
                        saleItem.stack = 0;
                        cl.remove(tg);
                        items.remove(saleItem);
                        cc = count2;
                        break;
                    }
                }

            }
            index++;
        }
        //重新写入
//        salesData.saveItemSlots(cl);
//
//        saveData();




        return cc;
    }

    public static String asLocation(Position position){
        return position.getFloorX()+":"+position.getFloorY()+":"+position.getFloorZ()+":"+position.level.getFolderName();
    }

    public void saveData(){
        String location = asLocation(this);
        if(SalesMainClass.INSTANCE.sqliteHelper.hasData(SalesMainClass.DB_TABLE,"location",location)){
            SqlData data = new SqlData();
            data.put("location",location);
            SalesMainClass.INSTANCE.sqliteHelper.set(SalesMainClass.DB_TABLE,data,salesData);
        }
    }

    public boolean hasItem(Item item){
        for(SaleItem saleItem: items) {
            if (saleItem.saleItem.equals(item, true, true)) {
                return true;
            }
        }
        return false;
    }

    public boolean addItem(Item item){
        SaleItem saleItem = new SaleItem(item,item.count,0);
        return addItem(saleItem);
    }

    public boolean addItem(SaleItem item){

        ListTag<CompoundTag> cl = loadItems;
//        ListTag<CompoundTag> cl = namedTag.getList("sale_items",CompoundTag.class);
        int index = 0;
        for(SaleItem saleItem: items){
            if(saleItem.saleItem.equals(item.saleItem,true,true)){
                //相同物品
                if(cl.size() <= index){
                    //理论不会出现这个问题... 以防万一
                    return false;
                }
                CompoundTag tg = cl.get(index);
                tg.putInt("stack",tg.getInt("stack") + item.stack);
                if(item.money > 0){
                    tg.putDouble("money",item.money);
                    saleItem.money = item.money;
                }
                saleItem.stack += item.stack;
//                salesData.saveItemSlots(cl);
//                saveData();
                updateInventory();


                return true;
            }
            index++;
        }


        if(items.size() >= InventoryType.CHEST.getDefaultSize() - 2){
            return false;
        }
        if(item.saleItem.getId() == 0){
            return false;
        }

        CompoundTag ct = item.tag;
        ct.putCompound("item",NBTIO.putItemHelper(item.saleItem));
        ct.putInt("stack",item.stack);
        ct.putDouble("money",item.money);

        cl.add(ct);
        item.tag = ct;
        items.add(item);

        removePackets();
        updateInventory();
        //重新写入
//        salesData.saveItemSlots(cl);
//
//        saveData();
        return true;
    }

    public void updateInventory(){
        for (ChestPanel chestPanel: clickPlayers.values()){
            chestPanel.update();
        }
    }


    public boolean isOpen;

    /**
     * 2: 正在关闭
     * 1: 正在打开
     * 0： 无
     * */
    public int animLoad = 0;
    
    public void openIt(Player who){
        animLoad = 1;

    }

    public void closeIt(Player who){
        animLoad = 2;

    }


    public float right = 42f;

    public float yawSpeed = 3f;

    @Override
    public boolean onUpdate(int currentTick) {
        boolean b = super.onUpdate(currentTick);
        // 将角度限制在 -180 到 180 之间
        if(saleSettingConfig.enableAnim) {
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
        if(saleSettingConfig.enableItem){
            for(Player player: getLevel().getPlayers().values()){
                if(player.distance(this) <= 10){
                    showItems();
                    s = true;
                    break;
                }
            }
        }


        if(!s){
            removePackets();
        }


        return b;
    }


    private void removePackets(){
        for(AddItemEntityPacket dataPacket: new ArrayList<>(ipacket)){
            RemoveEntityPacket pk1 = new RemoveEntityPacket();
            pk1.eid = dataPacket.entityUniqueId;
            Server.broadcastPacket(Server.getInstance().getOnlinePlayers().values(), pk1);
        }
        ipacket.clear();
    }

    private void showItems(){
        if(ipacket.size() == 0){
            int size = 6;
            if(saleSettingConfig.floatItemPos.containsKey(blockFace)) {
                size = saleSettingConfig.floatItemPos.get(blockFace).size();
            }

            for(int i = 0; i< Math.min(items.size(),size);i++){
                long eid = (long) ((int) this.x + new Random().nextDouble() + (int) this.z + new Random().nextDouble()) + new Random().nextLong();
                Position ps = asPosition(i);
                if(ps == null){
                    continue;
                }
                ipacket.add(i,getEntityTag(ps,items.get(i).saleItem,eid));
            }
            for(AddItemEntityPacket dataPacket: ipacket){
                Server.broadcastPacket(Server.getInstance().getOnlinePlayers().values(),dataPacket);
            }
        }
    }

    public Position asPosition(int index){
        if(saleSettingConfig.floatItemPos.containsKey(blockFace)){
            List<Vector3> vector3s = saleSettingConfig.floatItemPos.get(blockFace);

            if(vector3s.size() > index){
                Vector3 v3 = vector3s.get(index);
                return new Position(this.x + v3.x,this.y + v3.y,this.z + v3.z);
            }else{
                return null;
            }

        }else{
            return null;
        }


    }



    private AddItemEntityPacket getEntityTag(Position position, Item item, long eid){

        Item ic = item.clone();
        ic.setCount(1);
        AddItemEntityPacket pk = new AddItemEntityPacket();
        pk.item = ic;
        pk.entityRuntimeId = eid;
        pk.entityUniqueId = eid;
        pk.x = (float)(position.x);
        pk.y = (float)(position.y);
        pk.z = (float)(position.z);
        pk.speedX = 0.0F;
        pk.speedY = 0.0F;
        pk.speedZ = 0.0F;
        pk.metadata = new EntityMetadata()
                .putBoolean(80,true)

                .putBoolean(Entity.DATA_FLAG_IMMOBILE,true)
                .putLong(Entity.DATA_LEAD_HOLDER_EID, -1)
                .putFloat(Entity.DATA_SCALE, 0.5f)
                .putBoolean(Entity.DATA_FLAG_GRAVITY,false)
                .putInt(79,0);

        return pk;

    }

    public boolean finalClose;

    public boolean isPackage;

    @Override
    public void close() {
        //移除
        List<Position> p3 = positionListByConfig(this,blockFace,salesData.width,salesData.height);
        for(Position position: p3){
            String lo = asLocation(position);
            if(SalesListener.cacheEntitys.containsKey(lo)){
                SalesEntity salesEntity = SalesListener.cacheEntitys.get(lo);
                if(salesEntity.equals(this)){
                    SalesListener.cacheEntitys.remove(lo);
                }else{
                    continue;
                }
            }


            level.setBlock(position,new BlockAir(),true,true);
            if(!isPackage){
                level.addParticle(new DestroyBlockParticle(position,new BlockStone()));
            }
            BlockEntity be = level.getBlockEntity(position);
            if(be instanceof SalesBlockEntity){
                level.removeBlockEntity(be);
            }
        }

        if(!finalClose){
            salesData.saveItemSlots(loadItems);
            saveData();
        }

        removePackets();
        super.close();


    }

    //打包带走
    public CompoundTag toPackage(){
        //直接打包成tag 然后移除
        salesData.saveItemSlots(loadItems);
        CompoundTag tag = salesData.toPackage();
        finalClose = true;
        isPackage = true;
        for(Map.Entry<String,DisplayPlayerPanel> dis: SalesListener.chestPanelLinkedHashMap.entrySet()){
            if(dis.getValue().sales.equals(this)){
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
                SalesMainClass.INSTANCE.sqliteHelper.remove(SalesMainClass.DB_TABLE,"location",as);
            }
        });

//        SalesListener.cacheEntitys.remove(as);
        return tag;

    }

    public void setCustomName(String name){
        salesData.customname = name;
        saveData();

    }


    public void toClose(){
        finalClose = true;
        //生成一地掉落物
        for(SaleItem saleItem: items){
            Item cl = saleItem.saleItem.clone();
            cl.setCount(saleItem.stack);
            level.dropItem(this,cl);
        }
        for(Map.Entry<String,DisplayPlayerPanel> dis: SalesListener.chestPanelLinkedHashMap.entrySet()){
            if(dis.getValue().sales.equals(this)){
                dis.getValue().close();
                SalesListener.chestPanelLinkedHashMap.remove(dis.getKey());
            }

        }
        close();
        String as = asLocation(this);
        SalesMainClass.INSTANCE.sqliteHelper.remove(SalesMainClass.DB_TABLE,"location",as);
//        SalesListener.cacheEntitys.remove(as);

//        level.save(true);
    }

    public static SalesEntity spawnToAll(Position position,BlockFace bf,String master,SalesData data){
        return spawnToAll(position,bf,master,data,false);
    }

    public static SalesEntity spawnToAll(Position position,BlockFace bf,String master,SalesData data, boolean ignoreBlocks){
        if(bf == null){
            bf = BlockFace.EAST;
        }
        //检测地图
        Player player = Server.getInstance().getPlayer(master);
        if(player == null){
            return null;
        }
        if(SalesMainClass.banWorlds.contains(position.level.getFolderName()) && !player.isOp()){
            return null;
        }
        if(SalesMainClass.ENTITY_SKIN.size() == 0){
            return null;
        }
        String modelName = new ArrayList<>(SalesMainClass.ENTITY_SKIN.keySet()).get(0);
        if(data != null && data.skinmodel != null){
            String smd = data.skinmodel;
            if(SalesMainClass.ENTITY_SKIN.containsKey(smd)){
                modelName = smd;
            }
        }
        SaleSkinConfig saleSkinConfig = SalesMainClass.ENTITY_SKIN.get(modelName);



        Position pos = new Position(
                position.getFloorX() + 0.5,
                position.getFloorY(),
                position.getFloorZ() + 0.5,
                position.level);
        SaleSettingConfig.SaleWeight weight = saleSkinConfig.config.weight;
        int width = weight.width;
        int height = weight.height;

        List<Position> psconfig = positionListByConfig(position,bf,width,height);

        boolean hasBlock = false;
        if(!ignoreBlocks){
            for(Position ps : psconfig){
                Block block =  position.level.getBlock(ps);
                if(block.getId() != 0){
                    hasBlock = true;
                    break;
                }

            }
        }

        if(!hasBlock){
            Skin skin = saleSkinConfig.skinLinkedHashMap.get(bf);

            CompoundTag tag = EntityHuman.getDefaultNBT(pos);
            tag.putString(SALE_MASTER_TAG,master);
            tag.putCompound("Skin",new CompoundTag()
                    .putByteArray("Data", skin.getSkinData().data)
                    .putString("ModelId",skin.getSkinId())
            );
            ListTag<CompoundTag> tagListTag = new ListTag<>();
            if(data != null){
                tagListTag = data.asItemSlots();

            }
            SalesEntity sales = new SalesEntity(position.getChunk(),tag,bf,master,saleSkinConfig.config,tagListTag);

            sales.setSkin(skin);
            sales.spawnToAll();


            for (Position sp : psconfig){
                String pps = asLocation(sp);
                position.getLevel().setBlock(sp, (Block) SalesMainClass.INSTANCE.iBarrier,false,false);
                BlockEntity.createBlockEntity(SalesBlockEntity.ENTITY_TYPE,pos.getChunk(),
                        BlockEntity.getDefaultCompound(sp, SalesBlockEntity.ENTITY_TYPE),sales);
                if(!SalesListener.cacheEntitys.containsKey(pps)){
                    SalesListener.cacheEntitys.put(pps,sales);
                }

            }

            String ps = asLocation(position);
            if(data == null){
                data = new SalesData();
                data.saveItemSlots(tagListTag);
                data.chunkx = pos.getChunkX();
                data.chunkz = pos.getChunkZ();
                data.width = width;
                data.height = height;
                data.location = ps;
                data.master = master;
                data.bf = bf.getName();
                data.skinmodel = modelName;
                data.customname = master+" 的售货机";
                if(SalesMainClass.INSTANCE.sqliteHelper.hasData(SalesMainClass.DB_TABLE,"location",ps)){
                    SalesMainClass.INSTANCE.sqliteHelper.set(SalesMainClass.DB_TABLE,"location",ps,data);
                }else{
                    SalesMainClass.INSTANCE.sqliteHelper.add(SalesMainClass.DB_TABLE,data);
                }


            }
            sales.salesData = data;



            return sales;
        }
        return null;
    }

    public boolean setModel(String model){
        //重设
        if(SalesMainClass.ENTITY_SKIN.containsKey(model)){
            close();
            SalesData oldSd = salesData;
            oldSd.skinmodel = model;
            SaleSkinConfig saleSkinConfig = SalesMainClass.ENTITY_SKIN.get(model);
            oldSd.width = saleSkinConfig.config.weight.width;
            oldSd.height = saleSkinConfig.config.weight.height;
            saveData();

            SalesEntity.spawnToAll(oldSd.asPosition(),
                    BlockFace.valueOf(oldSd.bf.toUpperCase()), oldSd.master, oldSd,
                    true);



        }

        return false;
    }

    public static List<Position> positionListByConfig(Position position,BlockFace blockFace,int width,int height){
        List<Position> positions = new ArrayList<>();
        BlockFace rf = blockFace.rotateY();
        for(int i = 0; i < width;i++){
            Position ry = position.getSide(rf,i);
            for(int y = 0; y < height; y++){
                positions.add(ry.add(0,y));
            }

        }
        return positions;
    }

    public static class SalesBlockEntity extends BlockEntity implements InventoryHolder{

        public SaleBlockEntityInventory entityInventory;

        public static final String ENTITY_TYPE = "Sales";

        public SalesEntity salesEntity;

        public SalesBlockEntity(FullChunk chunk, CompoundTag nbt,SalesEntity entity) {
            super(chunk, nbt);
            entityInventory = new SaleBlockEntityInventory(entity,this);
            this.salesEntity = entity;
        }

        @Override
        public boolean isBlockEntityValid() {
            return true;
        }

        @Override
        public void saveNBT() {}

        @Override
        public Inventory getInventory() {
            return entityInventory;
        }

        public static class SaleBlockEntityInventory extends BaseInventory{

            public SalesEntity salesEntity;

            public SaleBlockEntityInventory(SalesEntity salesEntity,InventoryHolder holder) {
                super(holder, InventoryType.CHEST);
                this.salesEntity = salesEntity;
            }

            public SalesEntity getSalesEntity() {
                return salesEntity;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SalesEntity){
            return asLocation(this).equalsIgnoreCase(
                    asLocation((SalesEntity) obj)
            ) && blockFace == ((SalesEntity) obj).blockFace;
        }
        return false;
    }
}
