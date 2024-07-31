package org.sobadfish.sales.economy.core;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import org.sobadfish.sales.RegisterItemServices;
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
        int count = (int) Math.ceil(money);
        Player pl = Server.getInstance().getPlayer(player);
        if(pl == null || count <= 0){
            return false;
        }

        if(myMoney(player) >= count){
            Item coin = RegisterItemServices.CUSTOM_ITEMS.get("sale_coin").clone();
            coin.setCount(count);
            pl.getInventory().removeItem(coin);
            return true;
        }
        return false;
    }

    @Override
    public boolean addMoney(String player, double money) {
        int count = (int) Math.ceil(money);
        Player pl = Server.getInstance().getPlayer(player);
        if(pl == null || count <= 0){
            return false;
        }
        Item coin = RegisterItemServices.CUSTOM_ITEMS.get("sale_coin").clone();
        coin.setCount(count);
        Item[] items = pl.getInventory().addItem(coin);
        if(items.length > 0){
            for (Item item: items) {
                pl.level.dropItem(pl, item);
            }
        }
        return true;
    }

    @Override
    public double myMoney(String player) {
        return getCoinItemCountByPlayer(player);
    }

    public int getCoinItemCountByPlayer(String player) {
        Player pl = Server.getInstance().getPlayer(player);
        if(pl == null){
            return 0;
        }
        int size = 0;
        Item coin = RegisterItemServices.CUSTOM_ITEMS.get("sale_coin");
        for(Item item: pl.getInventory().getContents().values()){
            if(item.equals(coin)){
                size+= item.getCount();
            }
        }
        return size;
    }


}
