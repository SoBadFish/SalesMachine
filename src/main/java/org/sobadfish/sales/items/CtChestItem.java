package org.sobadfish.sales.items;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustomTool;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;

/**
 * 彩蛋功能 搬箱器
 * @author Sobadfish
 * @date 2024/7/29
 */
public class CtChestItem extends ItemCustomTool {

    public CtChestItem() {
        super("minecraft:ct_iron_chest", "搬运器", "ct_iron_chest");
    }


    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition.toolBuilder(this, CreativeItemCategory.ITEMS).build();
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
//        return ItemAction.onCtActivate(this,player,target);
        return ItemAction.onChestPlace(this,level,player,block,target,face,fx,fy,fz);


    }




}