package org.sobadfish.sales.economy.core;

import money.Money;
import org.sobadfish.sales.economy.IMoney;
import org.sobadfish.sales.entity.SalesEntity;

/**
 * @author Sobadfish
 * @date 2024/4/8
 */
public class MoneyCoreMoney implements IMoney {
    @Override
    public String displayName() {
        return "金钱";
    }

    @Override
    public boolean reduceMoney(String player, double money, SalesEntity sales) {
        if(Money.getInstance().getPlayers().contains(player)){
            return Money.getInstance().reduceMoney(player, (float) money);
        }
        return false;
    }

    @Override
    public boolean addMoney(String player, double money, SalesEntity sales) {
        if(Money.getInstance().getPlayers().contains(player)){
            return Money.getInstance().addMoney(player, (float) money);

        }
        return false;
    }

    @Override
    public double myMoney(String player) {
        if(Money.getInstance().getPlayers().contains(player)){
            return Money.getInstance().getMoney(player);
        }
        return 0;
    }
}
