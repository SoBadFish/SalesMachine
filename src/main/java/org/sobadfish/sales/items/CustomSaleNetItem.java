package org.sobadfish.sales.items;

import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;

/**
 * @author Sobadfish
 * @date 2025/3/19
 */
public class CustomSaleNetItem extends ItemCustom {

    public CustomSaleNetItem() {
        super("minecraft:sale_net", "网店许可证", "sale_netxk");
    }

    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition.customBuilder(this, CreativeItemCategory.ITEMS).build();
    }





    @Override
    public boolean canBeActivated() {
        return true;
    }
}
