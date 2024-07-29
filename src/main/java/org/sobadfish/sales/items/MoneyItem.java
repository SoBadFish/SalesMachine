package org.sobadfish.sales.items;

import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.RegisterItemServices;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.economy.IMoney;

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


    public Item getItem(String loadMoney){
        Item item = RegisterItemServices.CUSTOM_ITEMS.get("money");
        IMoney iMoney = SalesMainClass.getMoneyCoreByName(loadMoney);
        if(iMoney == null){
            iMoney = SalesMainClass.getMoneyCoreByName(SalesMainClass.getFirstMoney());
            if(iMoney == null){
                return item;
            }
        }
        item.addEnchantment(Enchantment.getEnchantment(0));
        item.setCustomName(TextFormat.colorize('&',"&r&l&e"+iMoney.displayName()+" &7x &a"+money));
        CompoundTag ct = item.getNamedTag();
        ct.putString("loadMoney",loadMoney);
        ct.putDouble(TAG,money);
        item.setNamedTag(ct);
        return item;

    }

}
