package org.sobadfish.sales.items.custom.sales;

import org.sobadfish.sales.items.custom.BaseSaleItem;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomV6SaleItem extends BaseSaleItem {

    public CustomV6SaleItem() {
        super(2008, "sale_item_v6");
    }

    @Override
    public int getSaleMeta() {
        return 5;
    }


}
