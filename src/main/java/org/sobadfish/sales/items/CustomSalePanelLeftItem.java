package org.sobadfish.sales.items;

import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.item.customitem.data.ItemCreativeCategory;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomSalePanelLeftItem extends ItemCustom {

    public CustomSalePanelLeftItem() {
        super("minecraft:sale_panel_left", "上一页", "sale_panel_left");
    }


    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition.customBuilder(this, ItemCreativeCategory.ITEMS).build();
    }

}
