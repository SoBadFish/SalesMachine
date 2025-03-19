package org.sobadfish.sales.items;

import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;

/**
 * @author Sobadfish
 * @date 2025/3/19
 */
public class CustomSaleDefSettingItem extends ItemCustom {
    public CustomSaleDefSettingItem() {
        super("minecraft:sale_def_setting", "设置", "sale_def_setting");
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
