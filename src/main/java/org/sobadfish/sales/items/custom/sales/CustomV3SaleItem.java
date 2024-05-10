package org.sobadfish.sales.items.custom.sales;

import org.sobadfish.sales.items.custom.BaseSaleItem;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomV3SaleItem extends BaseSaleItem {

    public CustomV3SaleItem() {
        super(2005, "sale_item_v3");
    }

    @Override
    public int getSaleMeta() {
        return 2;
    }


}
