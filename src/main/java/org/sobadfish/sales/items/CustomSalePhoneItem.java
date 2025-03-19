package org.sobadfish.sales.items;

import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;

/**
 * @author Sobadfish
 * @date 2025/3/19
 */
public class CustomSalePhoneItem extends ItemCustom {

    public CustomSalePhoneItem() {
        super("minecraft:sale_phone", "手机", "sale_phone");
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
