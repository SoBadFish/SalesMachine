package org.sobadfish.sales.items;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustomTool;
import cn.nukkit.item.customitem.data.ItemCreativeCategory;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;

/**
 * @author Sobadfish
 * @date 2024/5/10
 */
public abstract class BaseCtSaleItem extends ItemCustomTool {

    public BaseCtSaleItem(String id, String textureName) {
        super(id, "搬运器", textureName);
    }


    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition.toolBuilder(this, ItemCreativeCategory.ITEMS).build();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxDurability() {
        return 20;
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }


    @Override
    public boolean onActivate(Level level, Player player, Block block, Block target, BlockFace face, double fx, double fy, double fz) {
        return ItemAction.onSaleActivate(this, level, player, block);
    }
}