package org.sobadfish.sales.entity;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityArmorStand;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;

/**
 * @author Sobadfish
 * @date 2024/4/7
 */
public class DisplayItemStandEntity extends EntityArmorStand {


    private DisplayItemStandEntity(Item item, FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        namedTag.putBoolean("Marker",true);
        namedTag.putBoolean("Invisible",true);
        namedTag.putBoolean("NoBasePlate",true);
        this.getEquipmentInventory().setItemInHand(item,true);

        this.dataProperties.putInt(79, 9);
        //隐身
        this.setDataFlag(0,5,true);

//        Effect ef = Effect.getEffect(Effect.INVISIBILITY);
//        addEffect(new Effect(Effect.INVISIBILITY,));

    }

    @Override
    public float getWidth() {
        return 0f;
    }

    @Override
    public float getHeight() {
        return 0f;
    }

    public static AddEntityPacket getStandPacket(Item item, Position position, BlockFace face,long eid) {

        AddEntityPacket entityPacket = new AddEntityPacket();
        entityPacket.type = 61;
        entityPacket.entityUniqueId = eid;
        entityPacket.entityRuntimeId = eid;
        entityPacket.bodyYaw = face.getHorizontalAngle();
        entityPacket.metadata.putInt(Entity.DATA_ARMOR_STAND_POSE_INDEX, 9);
        //隐身
        entityPacket.metadata.putBoolean(5,true);
        entityPacket.x = (float)position.x;
        entityPacket.y = (float)position.y;
        entityPacket.z = (float)position.z;
        entityPacket.metadata.putFloat(54, 0);
        entityPacket.metadata.putFloat(53,0);
        entityPacket.metadata.putNBT(Entity.DATA_TYPE_NBT,new CompoundTag().putCompound("Mainhand",
                NBTIO.putItemHelper(item)));

        return entityPacket;

    }

    public static DisplayItemStandEntity getItemEntity(Item item, Position position, BlockFace face){

        switch (face){
            case NORTH: {
                position.add(2.5,-3.5,0);
                break;
            }
            case WEST: {
                position.add(1.5,-3.5,0.5);
                break;
            }
            case SOUTH: {
                position.add(2.5,-3.5,0);
                break;
            }
            case EAST: {
                position.add(1.5,-3.5,0);
                break;
            }
            default: {
                break;
            }
        }

        return new DisplayItemStandEntity(item,
                position.getChunk(), Entity.getDefaultNBT(position));
    }

    @Override
    protected float getGravity() {
        return 0f;
    }

    @Override
    public void saveNBT() {

    }
}
