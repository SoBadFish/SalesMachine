package org.sobadfish.sales.items.custom;

import cn.lanink.customitemapi.item.ItemCustom;

/**
 * @author Sobadfish
 * @date 2024/7/31
 */
public class CoinItem extends ItemCustom {

    public CoinItem() {
        super(2016,0,1, "硬币", "sale_coin");
    }


    @Override
    public boolean canBeActivated() {
        return true;
    }


    @Override
    public int getMaxStackSize() {
        return 64;
    }






}
