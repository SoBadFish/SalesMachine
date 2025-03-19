package org.sobadfish.sales.items.custom;

import cn.lanink.customitemapi.item.ItemCustom;

/**
 * @author Sobadfish
 * @date 2025/3/19
 */
public class CustomSaleNetItem extends ItemCustom {

    public CustomSaleNetItem() {
        super(2019, 0, 1, "网店许可", "sale_netxk");
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