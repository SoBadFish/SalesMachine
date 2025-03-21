package org.sobadfish.sales.items.sales;

import org.sobadfish.sales.items.BaseSaleItem;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomV7SaleItem extends BaseSaleItem {

    public CustomV7SaleItem() {
        super("minecraft:sale_v7", "sale_item_v7");
    }


    @Override
    public int getSaleMeta() {
        return 6;
    }
}
