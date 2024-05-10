package org.sobadfish.sales.items.custom.sales;

import org.sobadfish.sales.items.custom.BaseSaleItem;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomV4SaleItem extends BaseSaleItem {

    public CustomV4SaleItem() {
        super(2006, "sale_item_v4");
    }


    @Override
    public int getSaleMeta() {
        return 3;
    }

}
