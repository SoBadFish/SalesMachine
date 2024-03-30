package org.sobadfish.sales.items;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.item.customitem.data.ItemCreativeCategory;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import org.sobadfish.sales.SalesListener;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.config.SalesData;
import org.sobadfish.sales.entity.SalesEntity;

/**
 * @author Sobadfish
 * @date 2024/3/30
 */
public class CustomCtSaleItem extends ItemCustom {

    public CustomCtSaleItem() {
        super("minecraft:ct_iron_sale", "售货机搬运器", "ct_iron_sale");
    }


    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition.customBuilder(this, ItemCreativeCategory.ITEMS).build();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }



    @Override
    public boolean onActivate(Level level, Player player, Block block, Block target, BlockFace face, double fx, double fy, double fz) {
        //编写放置的逻辑
        if(player.isSneaking()){
            return false;
        }
        if (hasCompoundTag()){
            CompoundTag tag = getNamedTag();
            if(tag.contains("sale_data")){
                Server.getInstance().getScheduler().scheduleDelayedTask(SalesMainClass.INSTANCE, () -> {
                    CompoundTag data = tag.getCompound("sale_data");
                    SalesData salesData = SalesData.getSaleDataByCompoundTag(data);
                    salesData.bf = player.getDirection().getName();
                    salesData.location = SalesEntity.asLocation(block);
                    SalesEntity entity = SalesEntity.spawnToAll(salesData.asPosition(),
                            BlockFace.valueOf(salesData.bf.toUpperCase()), salesData.master, salesData,
                            true);
                    SalesListener.cacheEntitys.put(salesData.location, entity);
                    SalesMainClass.INSTANCE.sqliteHelper.add(SalesMainClass.DB_TABLE, salesData);
                },1);
                player.getInventory().setItemInHand(SalesMainClass.CUSTOM_ITEMS.get("ct"));
            }
        }


        return false;
    }
}
