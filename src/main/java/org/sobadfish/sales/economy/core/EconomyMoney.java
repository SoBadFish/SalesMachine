package org.sobadfish.sales.economy.core;

import me.onebone.economyapi.EconomyAPI;
import org.sobadfish.sales.economy.IMoney;
import org.sobadfish.sales.entity.SalesEntity;

/**
 * 经济核心实现接口
 * @author Sobadfish
 * @date 2024/4/8
 */
public class EconomyMoney implements IMoney {


    @Override
    public String displayName() {
        return "金币";
    }

    @Override
    public boolean reduceMoney(String player, double money, SalesEntity sales) {
        return EconomyAPI.getInstance().reduceMoney(player, money, true) == 1;
    }

    @Override
    public boolean addMoney(String player, double money, SalesEntity sales) {
        return EconomyAPI.getInstance().addMoney(player, money, true) == 1;
    }

    @Override
    public double myMoney(String player, SalesEntity sales) {
        return EconomyAPI.getInstance().myMoney(player) ;
    }
}
