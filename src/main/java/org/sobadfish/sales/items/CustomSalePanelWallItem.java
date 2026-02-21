package org.sobadfish.sales.items;

import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomSalePanelWallItem extends ItemCustom {

    public CustomSalePanelWallItem() {
        super("minecraft:sale_panel_wall", "边框", "sale_panel_wall");
    }


    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition.customBuilder(this, CreativeItemCategory.ITEMS).build();
    }

}
