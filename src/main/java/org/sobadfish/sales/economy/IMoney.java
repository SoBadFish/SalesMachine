package org.sobadfish.sales.economy;

import org.sobadfish.sales.entity.SalesEntity;

/**
 * 经济核心的接口 如果想实现自定义其他的经济核心
 * 需要实现这个接口
 * @author Sobadfish
 * @date 2024/4/8
 */
public interface IMoney {


    /**
     * 展示的名称
     * @return 用作展示的名称
     * */
    String displayName();

    /**
     *
     * 移除玩家金钱
     *
     * @param player 玩家
     * @param money 金钱数量
     * @param sales 交易的售货机
     * @return 是否移除成功
     * */
    boolean reduceMoney(String player, double money, SalesEntity sales);

    /**
     * 增加玩家金钱
     *
     * @param player 玩家
     * @param money 金钱数量
     * @param sales 交易的售货机
     * @return 是否增加成功
     *
     * */
    boolean addMoney(String player, double money, SalesEntity sales);

    /**
     * 获取玩家金钱
     *
     * @param player 玩家
     * @return 玩家的金钱数量
     * */
    double myMoney(String player, SalesEntity sales);

}
