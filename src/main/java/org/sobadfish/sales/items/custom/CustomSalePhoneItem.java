package org.sobadfish.sales.items.custom;

import cn.lanink.customitemapi.item.ItemCustom;

/**
 * @author Sobadfish
 * @date 2025/3/19
 */
public class CustomSalePhoneItem extends ItemCustom {

    public CustomSalePhoneItem() {
        super(2018,0,1, "手机", "sale_phone");
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
