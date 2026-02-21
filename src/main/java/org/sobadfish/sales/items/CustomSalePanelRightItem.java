package org.sobadfish.sales.items;

import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomSalePanelRightItem extends ItemCustom {

    public CustomSalePanelRightItem() {
        super("minecraft:sale_panel_right", "下一页", "sale_panel_right");
    }


    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition.customBuilder(this, CreativeItemCategory.ITEMS).build();
    }

}
