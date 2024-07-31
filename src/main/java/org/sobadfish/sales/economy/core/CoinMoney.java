package org.sobadfish.sales.economy.core;

import org.sobadfish.sales.economy.IMoney;

/**
 * @author Sobadfish
 * @date 2024/7/31
 */
public class CoinMoney implements IMoney {

    @Override
    public String displayName() {
        return "硬币";
    }

    @Override
    public boolean reduceMoney(String player, double money) {
        return false;
    }

    @Override
    public boolean addMoney(String player, double money) {
        return false;
    }

    @Override
    public double myMoney(String player) {
        return 0;
    }
}
