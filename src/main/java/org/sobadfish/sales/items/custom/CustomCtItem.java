package org.sobadfish.sales.items.custom;

import cn.lanink.customitemapi.item.ItemCustom;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import org.sobadfish.sales.SalesListener;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.items.ItemAction;

/**
 * @author Sobadfish
 * @date 2024/3/30
 */
public class CustomCtItem extends ItemCustom {

    public CustomCtItem() {
        super(1996,0,1, "售货机搬运器", "ct_iron");
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
        return ItemAction.onCtActivate(level,player,block,target,face,fx,fy,fz);
    }
}
