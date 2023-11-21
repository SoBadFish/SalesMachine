package org.sobadfish.sales.items;

import cn.nukkit.item.Item;

/**
 * @author Sobadfish
 * @date 2023/11/21
 */
public class SaleItem {

    public Item saleItem;

    public int stack;

    public double money;

    public SaleItem(Item saleItem,int stack,double money){
        this.saleItem = saleItem;
        this.stack = stack;
        this.money = money;
    }
}
