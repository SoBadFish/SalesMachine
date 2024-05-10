package org.sobadfish.sales.items.sales;

import org.sobadfish.sales.items.BaseSaleItem;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomV3SaleItem extends BaseSaleItem {

    public CustomV3SaleItem() {
        super("minecraft:sale_v3", "sale_item_v3");
    }


    @Override
    public int getSaleMeta() {
        return 2;
    }
}
