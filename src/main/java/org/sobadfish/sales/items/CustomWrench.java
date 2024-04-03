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
 * @date 2024/4/3
 */
public class CustomWrench  extends ItemCustom {
    public CustomWrench() {
        super("minecraft:pipe_wrench", "模型修改器", "pipe_wrench");
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
//        return ItemAction.onSaleActivate(this,level,player,block,target,face,fx,fy,fz);
        return ItemAction.onSaleModelChange(this,level,player,block,target,face,fx,fy,fz);
    }
}
