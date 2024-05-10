package org.sobadfish.sales.items.sales;

import org.sobadfish.sales.items.BaseSaleItem;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomV1SaleItem extends BaseSaleItem {

    public CustomV1SaleItem() {
        super("minecraft:sale_v1", "sale_item_v1");
    }


    @Override
    public int getSaleMeta() {
        return 0;
    }
}
