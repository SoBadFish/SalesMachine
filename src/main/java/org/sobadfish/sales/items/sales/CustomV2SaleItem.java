package org.sobadfish.sales.items.sales;

import org.sobadfish.sales.items.BaseSaleItem;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomV2SaleItem extends BaseSaleItem {

    public CustomV2SaleItem() {
        super("minecraft:sale_v2", "sale_item_v2");
    }


    @Override
    public int getSaleMeta() {
        return 1;
    }
}
