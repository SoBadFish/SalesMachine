package org.sobadfish.sales.items;

import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.item.customitem.data.ItemCreativeCategory;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomSalePanelResetItem extends ItemCustom {

    public CustomSalePanelResetItem() {
        super("minecraft:sale_panel_reset", "刷新", "sale_panel_reset");
    }


    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition.customBuilder(this, ItemCreativeCategory.ITEMS).build();
    }

}
