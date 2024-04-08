package org.sobadfish.sales.items;

import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import org.sobadfish.sales.SalesMainClass;

/**
 * @author Sobadfish
 * @date 2023/11/21
 */
public class SaleItem {

    public Item saleItem;

    public String loadMoney;

    public int stack;

    public boolean isRemove;


    public CompoundTag tag = new CompoundTag();

    public double money;

    public SaleItem(CompoundTag tag,Item saleItem, int stack, double money){
        this.tag = tag;
        this.saleItem = saleItem;
        this.stack = stack;
        this.money = money;
        if(tag.contains("loadMoney")){
            this.loadMoney = tag.getString("loadMoney");
        }else{
            String firstName = SalesMainClass.getFirstMoney();
            tag.putString("loadMoney",firstName);
            this.loadMoney = firstName;
        }

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
        this.loadMoney = SalesMainClass.getFirstMoney();
    }

    public SaleItem(Item saleItem, int stack,String loadMoney, double money){
        this.saleItem = saleItem;
        this.stack = stack;
        this.money = money;
        this.loadMoney = loadMoney;
    }
}
