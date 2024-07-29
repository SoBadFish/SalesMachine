package org.sobadfish.sales.items.custom;

import cn.lanink.customitemapi.item.ItemCustom;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import org.sobadfish.sales.items.ISaleItem;
import org.sobadfish.sales.items.ItemAction;

/**
 * @author Sobadfish
 * @date 2024/5/10
 */
public class BaseSaleItem extends ItemCustom implements ISaleItem {



    //2001 ~ v6
    public BaseSaleItem(int id,String textureName) {
        super(id,0,1, "售货机", textureName);
    }



    @Override
    public boolean canBeActivated() {
        return true;
    }


    @Override
    public boolean onActivate(Level level, Player player, Block block, Block target, BlockFace face, double fx, double fy, double fz) {
        return ItemAction.onSalePlace(this,block, player,getSaleMeta());
    }

    @Override
    public int getSaleMeta() {
        return 0;
    }
}
