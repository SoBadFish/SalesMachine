package org.sobadfish.sales.items.custom;

import cn.lanink.customitemapi.item.ItemCustom;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import org.sobadfish.sales.items.ItemAction;

/**
 * @author Sobadfish
 * @date 2024/3/30
 */
public class CustomCtSaleItem extends ItemCustom {


    public CustomCtSaleItem() {
        super(1997,0,1, "售货机搬运器", "ct_iron_sale");
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
        return ItemAction.onSaleActivate(this,level,player,block);
    }


}
