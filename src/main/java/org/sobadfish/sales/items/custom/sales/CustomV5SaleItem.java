package org.sobadfish.sales.items.custom.sales;

import org.sobadfish.sales.items.custom.BaseSaleItem;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomV5SaleItem extends BaseSaleItem {

    public CustomV5SaleItem() {
        super(2007, "sale_item_v5");
    }


    @Override
    public int getSaleMeta() {
        return 4;
    }

}
