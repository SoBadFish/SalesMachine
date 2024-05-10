package org.sobadfish.sales.items.sales;

import org.sobadfish.sales.items.BaseSaleItem;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomV6SaleItem extends BaseSaleItem {

    public CustomV6SaleItem() {
        super("minecraft:sale_v6", "sale_item_v6");
    }


    @Override
    public int getSaleMeta() {
        return 5;
    }
}
