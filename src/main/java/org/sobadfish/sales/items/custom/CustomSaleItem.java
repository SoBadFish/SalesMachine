package org.sobadfish.sales.items.custom;

import cn.lanink.customitemapi.item.ItemCustom;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomSaleItem extends ItemCustom {

    public CustomSaleItem() {
        super(1992,0,1, "售货机", "sale_item");
    }



    @Override
    public boolean canBeActivated() {
        return true;
    }


}
