package org.sobadfish.sales.items;

import cn.nukkit.item.ItemDurable;
import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustomTool;
import cn.nukkit.item.customitem.data.ItemCreativeCategory;

/**
 * @author Sobadfish
 * @date 2025/3/19
 */
public class CustomSalePhoneItem extends ItemCustomTool implements ItemDurable {

    public CustomSalePhoneItem() {
        super("minecraft:sale_phone", "手机", "sale_phone");
    }

    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition.toolBuilder(this, ItemCreativeCategory.ITEMS).allowOffHand(true).build();
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public int getMaxDurability() {
        return 5;
    }


    @Override
    public boolean isTool() {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }




}
