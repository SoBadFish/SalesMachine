package org.sobadfish.sales.items.custom;

import cn.lanink.customitemapi.item.ItemCustom;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import org.sobadfish.sales.items.ItemAction;

/**
 * 彩蛋功能 搬箱器
 * @author Sobadfish
 * @date 2024/7/29
 */
public class CtChestItem extends ItemCustom {

    public CtChestItem() {
        super(2015, 0,1,"搬运器", "ct_iron_chest");
    }




    @Override
    public boolean canBeActivated() {
        return true;
    }


    @Override
    public boolean onActivate(Level level, Player player, Block block, Block target, BlockFace face, double fx, double fy, double fz) {
        return ItemAction.onChestPlace(this,level,player,block,target,face,fx,fy,fz);
    }



}