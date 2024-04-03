package org.sobadfish.sales.items.custom;

import cn.lanink.customitemapi.item.ItemCustom;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import org.sobadfish.sales.items.ItemAction;

/**
 * @author Sobadfish
 * @date 2024/4/3
 */
public class CustomWrench extends ItemCustom {

    public CustomWrench() {
        super(1998,0,1, "模型修改器", "pipe_wrench");
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
        return ItemAction.onSaleModelChange(this,level,player,block,target,face,fx,fy,fz);
    }
}
