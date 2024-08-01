package org.sobadfish.sales.items.custom;

import cn.lanink.customitemapi.item.ItemCustom;

/**
 * @author Sobadfish
 * @date 2024/7/31
 */
public class CtKeyItem extends ItemCustom {

    public CtKeyItem() {
        super(2017,0,1, "售货机锁", "ct_key");
    }


    @Override
    public boolean canBeActivated() {
        return true;
    }


    @Override
    public int getMaxStackSize() {
        return 1;
    }






}
