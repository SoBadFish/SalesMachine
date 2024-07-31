package org.sobadfish.sales.items;

import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.item.customitem.data.ItemCreativeCategory;

/**
 * @author Sobadfish
 * @date 2024/7/31
 */
public class CoinItem extends ItemCustom {

    public CoinItem() {
        super("minecraft:sale_coin", "硬币", "sale_coin");
    }


    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition.customBuilder(this, ItemCreativeCategory.ITEMS).build();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }



    @Override
    public boolean canBeActivated() {
        return true;
    }



}
