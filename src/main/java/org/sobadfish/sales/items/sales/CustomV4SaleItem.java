package org.sobadfish.sales.items.sales;

import org.sobadfish.sales.items.BaseSaleItem;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomV4SaleItem extends BaseSaleItem {

    public CustomV4SaleItem() {
        super("minecraft:sale_v4", "sale_item_v4");
    }


    @Override
    public int getSaleMeta() {
        return 3;
    }
}
