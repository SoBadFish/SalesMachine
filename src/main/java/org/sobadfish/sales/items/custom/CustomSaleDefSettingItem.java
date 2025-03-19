package org.sobadfish.sales.items.custom;

import cn.lanink.customitemapi.item.ItemCustom;


/**
 * @author Sobadfish
 * @date 2025/3/19
 */
public class CustomSaleDefSettingItem extends ItemCustom {

    public CustomSaleDefSettingItem() {
        super(2020, 0, 1, "设置", "sale_def_setting");
    }


    @Override
    public boolean canBeActivated() {
        return true;
    }


    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
