package org.sobadfish.sales.items;

import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;

/**
 * @author Sobadfish
 * @date 2024/7/31
 */
public class CtKeyItem extends ItemCustom {

    public CtKeyItem() {
        super("minecraft:ct_key", "售货机锁", "ct_key");
    }


    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition.customBuilder(this, CreativeItemCategory.ITEMS).build();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }



    @Override
    public boolean canBeActivated() {
        return true;
    }



}
