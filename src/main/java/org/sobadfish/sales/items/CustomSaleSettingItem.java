package org.sobadfish.sales.items;

import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.item.customitem.data.ItemCreativeCategory;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomSaleSettingItem extends ItemCustom {
    public CustomSaleSettingItem() {
        super("minecraft:sale_setting", "设置", "sale_setting");
    }


    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition.customBuilder(this, ItemCreativeCategory.ITEMS).build();
    }

}
