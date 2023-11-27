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

    public boolean isRemove;


    public CompoundTag tag = new CompoundTag();

    public double money;

    public SaleItem(CompoundTag tag,Item saleItem, int stack, double money){
        this.tag = tag;
        this.saleItem = saleItem;
        this.stack = stack;
        this.money = money;
    }

    public String getItemName(){
        if(saleItem.hasCustomName()){
            String id = saleItem.getId()+":"+saleItem.getDamage();
            return saleItem.getCustomName()+"&7 (&r"+id+"&7)";
        }
        return saleItem.getName()+"&7 (&r"+saleItem.getId()+":"+saleItem.getDamage()+"&7)";
    }

    public SaleItem(Item saleItem, int stack, double money){
        this.saleItem = saleItem;
        this.stack = stack;
        this.money = money;
    }
}
