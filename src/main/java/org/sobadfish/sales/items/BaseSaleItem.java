package org.sobadfish.sales.items;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.item.customitem.data.ItemCreativeCategory;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;

/**
 * @author Sobadfish
 * @date 2024/5/9
 */
public abstract class BaseSaleItem extends ItemCustom implements ISaleItem {

    public BaseSaleItem(String id,String textureName) {
        super(id, "售货机", textureName);

    }


    public BaseSaleItem() {
        super("minecraft:sale", "售货机", "sale_item");
    }


    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition.customBuilder(this, ItemCreativeCategory.ITEMS).build();
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean onActivate(Level level, Player player, Block block, Block target, BlockFace face, double fx, double fy, double fz) {
       return ItemAction.onSalePlace(this,block,player,getSaleMeta());
    }
}
