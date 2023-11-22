package org.sobadfish.sales.items;

import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author Sobadfish
 * @date 2023/11/21
 */
public class SaleItem {

    public Item saleItem;

    public int stack;


    public CompoundTag tag = new CompoundTag();

    public double money;

    public SaleItem(CompoundTag tag,Item saleItem, int stack, double money){
        this.tag = tag;
        this.saleItem = saleItem;
        this.stack = stack;
        this.money = money;
    }

    public SaleItem(Item saleItem, int stack, double money){
        this.saleItem = saleItem;
        this.stack = stack;
        this.money = money;
    }
}
