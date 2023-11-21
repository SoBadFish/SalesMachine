package org.sobadfish.sales.block;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSolid;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.TextFormat;

/**
 * @author Sobadfish
 * @date 2023/11/21
 */
public class BarrierBlock_Nukkit extends BlockSolid implements IBarrier {
    public BarrierBlock_Nukkit() {
    }

    @Override
    public int getId() {
        return 95;
    }

    @Override
    public String getName() {
        return "Invisible Bedrock";
    }

    @Override
    public double getHardness() {
        return -1.0D;
    }

    @Override
    public double getResistance() {
        return 1.8E7D;
    }

    @Override
    public boolean isBreakable(Item item) {
        return false;
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
        return new ItemBlock(Block.get(0));
    }

    @Override
    public Item getShaleItem(){
        Item item = Item.get(54);
        item.setCustomName(TextFormat.colorize('&',"&r&l&e售卖机"));

        item.setLore(TextFormat.colorize('&',"&r&7\n放置即可生成"));
        CompoundTag compoundTag = item.getNamedTag();
        compoundTag.putBoolean("saleskey",true);
        item.addEnchantment(Enchantment.getEnchantment(0).setLevel(1));
        return item;
    }

    @Override
    public int getBid() {
        return 95;
    }
}
