package org.sobadfish.sales.items.custom.sales;

import org.sobadfish.sales.items.custom.BaseSaleItem;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomV2SaleItem extends BaseSaleItem {

    public CustomV2SaleItem() {
        super(2004, "sale_item_v2");
    }


    @Override
    public int getSaleMeta() {
        return 1;
    }
}
