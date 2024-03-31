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
        return ItemAction.onSaleActivate(this,level,player,block,target,face,fx,fy,fz);
    }
}
