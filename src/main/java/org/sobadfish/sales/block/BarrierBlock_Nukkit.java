package org.sobadfish.sales.block;

import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockSolidMeta;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.DyeColor;

/**
 * @author Sobadfish
 * @date 2023/11/21
 */
public class BarrierBlock_Nukkit extends BlockSolidMeta implements IBarrier {
    public BarrierBlock_Nukkit() {
        super(0);
    }

    @Override
    public int getId() {
        return -161;
    }

    @Override
    public String getName() {
        return "Barrier";
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public double getResistance() {
        return 2.5D;
    }

    @Override
    public double getHardness() {
        return 0.5D;
    }

    @Override
    public int getToolType() {
        return 2;
    }

    @Override
    public boolean onActivate(Item item) {
        return true;
    }

    @Override
    public boolean isBreakable(Item item) {
        return true;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.TRANSPARENT_BLOCK_COLOR;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public Item toItem() {

        return new ItemBlock(new BlockAir());
    }





    public DyeColor getDyeColor() {
        return DyeColor.getByWoolData(this.getDamage());
    }

    @Override
    public int getBid() {
        return -161;
    }
}
