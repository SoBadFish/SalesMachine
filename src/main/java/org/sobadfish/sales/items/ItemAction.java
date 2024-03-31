package org.sobadfish.sales.items;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.SalesListener;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.config.SalesData;
import org.sobadfish.sales.entity.SalesEntity;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Sobadfish
 * 23:01
 */
public class ItemAction {



    public static boolean onCtActivate(Level level, Player player, Block block, Block target, BlockFace face, double fx, double fy, double fz) {
        //编写拿取的逻辑
        if(player.isSneaking()){
            return false;
        }
        SalesEntity salesEntity = SalesListener.getEntityByPos(target);
        if(salesEntity != null){
            if(player.isOp() || salesEntity.master.equalsIgnoreCase(player.getName())){
                Item sitem = SalesMainClass.CUSTOM_ITEMS.get("ct_sale");
                sitem.setCount(1);
                String nm = "&r&e"+salesEntity.master+" 的售货机";
                if(salesEntity.salesData.customname != null){
                    nm = salesEntity.salesData.customname;
                }

                sitem.setCustomName(TextFormat.colorize('&',nm));
                sitem.addEnchantment(Enchantment.getEnchantment(0));
                CompoundTag compoundTag = sitem.getNamedTag();
                compoundTag.putCompound(salesEntity.toPackage());
                //防止过快使用锁
                compoundTag.putLong("lock",System.currentTimeMillis());
                sitem.setCompoundTag(compoundTag);
                player.getInventory().setItemInHand(sitem);
            }


        }

        return false;
    }


    public static boolean onSaleActivate(Item item,Level level, Player player, Block block, Block target, BlockFace face, double fx, double fy, double fz) {
        //编写放置的逻辑
        if(player.isSneaking()){
            return false;
        }
        if (item.hasCompoundTag()){
            CompoundTag tag = item.getNamedTag();
            if(tag.contains("lock")){
                long lockTime = tag.getLong("lock");
                if(System.currentTimeMillis() - lockTime < 500){
                    return false;
                }
            }
            if(tag.contains("sale_data")){
                level.addSound(block, Sound.MOB_ZOMBIE_WOODBREAK);
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
