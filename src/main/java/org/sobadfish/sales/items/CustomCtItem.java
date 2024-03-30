package org.sobadfish.sales.items;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.item.customitem.data.ItemCreativeCategory;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import org.sobadfish.sales.SalesListener;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.entity.SalesEntity;

/**
 * @author Sobadfish
 * @date 2024/3/30
 */
public class CustomCtItem extends ItemCustom {

    public CustomCtItem() {
        super("minecraft:ct_iron", "售货机搬运器", "ct_iron");
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
        //编写拿取的逻辑
        if(player.isSneaking()){
            return false;
        }
        SalesEntity salesEntity = SalesListener.getEntityByPos(target);
        if(salesEntity != null){
            if(player.isOp() || salesEntity.master.equalsIgnoreCase(player.getName())){
                Item sitem = SalesMainClass.CUSTOM_ITEMS.get("ct_sale");
                sitem.setCount(1);
                sitem.addEnchantment(Enchantment.getEnchantment(0));
                CompoundTag compoundTag = sitem.getNamedTag();
                compoundTag.putCompound(salesEntity.toPackage());
                sitem.setCompoundTag(compoundTag);
                player.getInventory().setItemInHand(sitem);
            }
        }



        return false;
    }
}
