package org.sobadfish.sales.items.sales;

import org.sobadfish.sales.items.BaseSaleItem;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomV5SaleItem extends BaseSaleItem {

    public CustomV5SaleItem() {
        super("minecraft:sale_v5", "sale_item_v5");
    }


    @Override
    public int getSaleMeta() {
        return 4;
    }
}
