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
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.particle.DestroyBlockParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.AddItemEntityPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.RemoveEntityPacket;
import org.sobadfish.sales.SalesListener;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.items.SaleItem;
import org.sobadfish.sales.panel.DisplayPlayerPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Sobadfish
 * @date 2023/11/16
 */
public class SalesEntity extends EntityHuman {

    public static final String ENTITY_TYPE = "SalesEntity";

    public static final String SALE_MASTER_TAG = "SalesMaster";

    public List<AddItemEntityPacket> ipacket = new ArrayList<>();

    public List<SaleItem> items = new ArrayList<>();
    
    public List<String> clickPlayers = new ArrayList<>();

    public BlockFace blockFace;

    public String master;


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

    public SalesEntity(FullChunk chunk, CompoundTag nbt, BlockFace face,String master) {
        super(chunk, nbt);
        this.blockFace = face;
        setScale(0.9f);
        this.master = master;
        //解包物品
        setImmobile();
        if(nbt.contains("sale_items")){
            ListTag<CompoundTag> cl =  nbt.getList("sale_items",CompoundTag.class);
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
        ListTag<CompoundTag> cl = namedTag.getList("sale_items",CompoundTag.class);
        int index = 0;
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

                    return 0;
                }else{

                    if(tg.getInt("stack") >= count){
                        tg.putInt("stack",tg.getInt("stack") - count);
                        saleItem.stack -= count;
                        return count;
                    }else{
                        int count2 = tg.getInt("stack");
                        saleItem.stack = 0;
                        cl.remove(tg);
                        items.remove(saleItem);
                        return count2;
                    }
                }

            }
            index++;
        }
        return 0;
    }


    public boolean addItem(SaleItem item){

        ListTag<CompoundTag> cl = namedTag.getList("sale_items",CompoundTag.class);
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
        namedTag.putList(cl);
        removePackets();
        return true;
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
        if(who != null){
            if(!clickPlayers.contains(who.getName())){
                clickPlayers.add(who.getName());
            }

        }
    }

    public void closeIt(Player who){
        animLoad = 2;
        if(who != null){
            clickPlayers.remove(who.getName());
        }
    }


    public float right = 42f;

    public float yawSpeed = 3f;

    @Override
    public boolean onUpdate(int currentTick) {
        boolean b = super.onUpdate(currentTick);
        // 将角度限制在 -180 到 180 之间
        if(animLoad == 1){

            if(yaw > right){
                yaw -= yawSpeed;
            }else if(yaw < right){
                yaw += yawSpeed;
            }else{
                animLoad = 0;
                isOpen = true;
            }
        }

        if(animLoad == 2){
            if(yaw > 0){
                yaw -= yawSpeed;
            }else if(yaw < 0){
                yaw += yawSpeed;
            }else{
                animLoad = 0;
                isOpen = false;
            }
        }
        updateMovement();

        boolean s = false;
        for(Player player: getLevel().getPlayers().values()){
            if(player.distance(this) <= 10){
                showItems();
                s = true;
                break;
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
            for(int i = 0; i< Math.min(items.size(),6);i++){
                long eid = (long) ((int) this.x + new Random().nextDouble() + (int) this.z + new Random().nextDouble()) + new Random().nextLong();
                ipacket.add(i,getEntityTag(asPosition(i),items.get(i).saleItem,eid));
            }
            for(DataPacket dataPacket: ipacket){
                Server.broadcastPacket(Server.getInstance().getOnlinePlayers().values(),dataPacket);
            }
        }
    }

    public Position asPosition(int index){
        Position pos;
        float yy = 1.23f;
        if(index >= 2 && index < 4){
            yy-= 0.8f;
        } else if(index >= 4){
            yy -= 1.22f;
        }
        BlockFace right = blockFace.rotateY();
        if(right.getXOffset() > 0){
           if(index % 2 == 0){
               pos = new Position(this.x + 0.15,this.y + yy,this.z + 0.15);
           }else{
               pos = new Position(this.x - 0.34,this.y + yy,this.z + 0.15);
           }
        }else if(right.getXOffset() < 0){
            if(index % 2 == 0){
                pos = new Position(this.x + 0.15,this.y + yy,this.z + 0.15);
            }else{
                pos = new Position(this.x - 0.34,this.y + yy,this.z + 0.15);
            }
        }else{
            if(right.getZOffset() > 0){
                if(index % 2 == 0){
                    pos = new Position(this.x + 0.15,this.y + yy,this.z - 0.22);
                }else{
                    pos = new Position(this.x + 0.15,this.y + yy,this.z + 0.15);
                }
            }else {
                if(index % 2 == 0){
                    pos = new Position(this.x + 0.15,this.y + yy,this.z +0.22);
                }else{
                    pos = new Position(this.x +0.15,this.y + yy,this.z - 0.1 );
                }
            }
        }
        return pos;

    }



    private AddItemEntityPacket getEntityTag(Position position, Item item,long eid){
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
                .putBoolean(Entity.DATA_FLAG_GRAVITY,false);

        return pk;
    }

    public boolean finalClose;

    @Override
    public void close() {
        Position[] p3 = new Position[]{this,this.add(0,1)};
        for(Position position: p3){
            level.setBlock(position,new BlockAir(),true);
            level.addParticle(new DestroyBlockParticle(position,new BlockStone()));
            BlockEntity be = position.level.getBlockEntity(position);
            if(be != null){
                level.removeBlockEntity(be);
            }

        }
        removePackets();
        super.close();


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
    }

    public static boolean spawnToAll(Position position,BlockFace bf,String master){


        Position pos = new Position(
                position.getFloorX() + 0.5,
                position.getFloorY(),
                position.getFloorZ() + 0.5,
                position.level);
        if(position.level.getBlock(position).getId() == 0 && position.level.getBlock(position.add(0,1)).getId() == 0){

            Skin skin = SalesMainClass.ENTITY_SKIN.get(bf);

            Position p2 = pos.add(0,1);
            CompoundTag tg = BlockEntity.getDefaultCompound(pos, SalesBlockEntity.ENTITY_TYPE);
            CompoundTag tag = EntityHuman.getDefaultNBT(pos);
            tag.putString(SALE_MASTER_TAG,master);
            tag.putCompound("Skin",new CompoundTag()
                    .putByteArray("Data", skin.getSkinData().data)
                    .putString("ModelId",skin.getSkinId())
            );
            SalesEntity sales = new SalesEntity(position.getChunk(),tag,bf,master);

            sales.setSkin(skin);
            sales.spawnToAll();


            tg.putCompound(SalesEntity.ENTITY_TYPE,sales.namedTag);
            tg.putByte("face",bf.getIndex());
            BlockEntity.createBlockEntity(SalesBlockEntity.ENTITY_TYPE,pos.getChunk(),tg,sales);
            BlockEntity.createBlockEntity(SalesBlockEntity.ENTITY_TYPE,p2.getChunk(),BlockEntity.getDefaultCompound(p2, SalesBlockEntity.ENTITY_TYPE),sales);
            position.getLevel().setBlock(position, (Block) SalesMainClass.INSTANCE.iBarrier,false,false);
            position.getLevel().setBlock(position.add(0,1), (Block) SalesMainClass.INSTANCE.iBarrier,false,false);

            return true;
        }
        return false;
    }

    public static class SalesBlockEntity extends BlockEntity{

        public static final String ENTITY_TYPE = "SalesBlock";

        public SalesEntity sales;

        public SalesBlockEntity(FullChunk chunk, CompoundTag nbt) {
            super(chunk, nbt);

            if(this.level.getBlock(this).getId() != SalesMainClass.INSTANCE.iBarrier.getBid()
                    && this.level.getBlock(this.add(0,1)).getId() != SalesMainClass.INSTANCE.iBarrier.getBid()
                    && this.level.getBlock(this.add(0,-1)).getId() != SalesMainClass.INSTANCE.iBarrier.getBid()){
                this.level.removeBlockEntity(this);
                //移除相关实体
                for(Entity entity: level.getEntities()){
                    if(entity instanceof SalesEntity){
                        if(level.getBlockEntity(entity) == null){
                            ((SalesEntity) entity).toClose();
                        }
                    }
                }
                return;
            }
            if(sales == null){
                if(nbt.contains(SalesEntity.ENTITY_TYPE)){
                    //重新生成方块
                    level.setBlock(this,new BlockAir(),false,false);
                    level.setBlock(this.add(0,1),new BlockAir(),false,false);
                    CompoundTag ct = nbt.getCompound(SalesEntity.ENTITY_TYPE);
                    BlockFace bf = BlockFace.fromIndex(nbt.getByte("face"));
                    Skin skin = SalesMainClass.ENTITY_SKIN.get(bf);
                    ct.putCompound("Skin",new CompoundTag()
                            .putByteArray("Data", skin.getSkinData().data)
                            .putString("ModelId",skin.getSkinId())
                    );
                    String master = null;
                    if(ct.contains(SALE_MASTER_TAG)){
                        master = ct.getString(SALE_MASTER_TAG);
                    }
                    SalesEntity sales = new SalesEntity(chunk,ct,bf,master);
                    sales.setSkin(skin);
                    sales.spawnToAll();
//                System.out.println("生成位置: "+sales.getPosition());
                    level.setBlock(this, (Block) SalesMainClass.INSTANCE.iBarrier,false,false);
                    level.setBlock(this.add(0,1), (Block) SalesMainClass.INSTANCE.iBarrier,false,false);
                    this.sales = sales;
                    BlockEntity be = level.getBlockEntity(this.add(0,1));

                    if(be instanceof SalesBlockEntity){
                        ((SalesBlockEntity) be).sales = this.sales;
                    }
                }else{
                    BlockEntity be = level.getBlockEntity(this.add(0,-1));
                    if(be instanceof SalesBlockEntity){
                        this.sales = ((SalesBlockEntity) be).sales;
                    }
                }
            }


        }


        public SalesBlockEntity(FullChunk chunk, CompoundTag nbt,SalesEntity sales) {
            super(chunk, nbt);
            this.sales = sales;


        }



        @Override
        public boolean isBlockEntityValid() {
            return true;
        }
    }



}
