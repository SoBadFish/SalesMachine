package org.sobadfish.sales.economy;

import cn.nukkit.item.Item;

/**
 * 这个接口是自定义物品
 * @author Sobadfish
 * @date 2024/8/1
 */
public interface IItemMoney {

    /**
     * 这个物品用于交易
     * @return 返回物品
     * */
    Item getMoneyItem();
}
