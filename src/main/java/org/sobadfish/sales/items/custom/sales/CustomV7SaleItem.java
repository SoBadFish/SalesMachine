package org.sobadfish.sales.items.custom.sales;

import org.sobadfish.sales.items.custom.BaseSaleItem;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomV7SaleItem extends BaseSaleItem {

    public CustomV7SaleItem() {
        super(2022, "sale_item_v7");
    }

    @Override
    public int getSaleMeta() {
        return 6;
    }


}
