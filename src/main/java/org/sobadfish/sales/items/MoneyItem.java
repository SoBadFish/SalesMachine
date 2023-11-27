package org.sobadfish.sales.items;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemIngotGold;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class MoneyItem {

    public static final String TAG = "SalesMoneyItem";

    public double money;

    public MoneyItem(double money){
        this.money = money;
    }


    public Item getItem(){
        Item item = new ItemIngotGold();
        item.addEnchantment(Enchantment.getEnchantment(0));
        item.setCustomName(TextFormat.colorize('&',"&r&l&e金币 &7x &a"+money));
        CompoundTag ct = item.getNamedTag();
        ct.putDouble(TAG,money);
        item.setNamedTag(ct);
        return item;

    }

}
