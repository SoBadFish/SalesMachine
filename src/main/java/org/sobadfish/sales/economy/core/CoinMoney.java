package org.sobadfish.sales.economy.core;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import org.sobadfish.sales.RegisterItemServices;
import org.sobadfish.sales.economy.IItemMoney;
import org.sobadfish.sales.economy.IMoney;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.items.SaleItem;

/**
 * @author Sobadfish
 * @date 2024/7/31
 */
public class CoinMoney implements IMoney, IItemMoney {

    @Override
    public String displayName() {
        return "硬币";
    }

    @Override
    public boolean reduceMoney(String player, double money, SalesEntity sales) {
        int count = (int) Math.ceil(money);
        Item coin = getMoneyItem();
        if(count <= 0){
            return false;
        }
        if(player.equalsIgnoreCase(sales.master)){
            //售货机主人的
            SaleItem item = sales.getCoinSaleItem(coin);
            if(item.stack >= count){
                sales.removeItem(player,item,count,true);
                return true;
            }else{
                return false;
            }
        }
        int myCoin = (int) myMoney(player,sales);

        Player pl = Server.getInstance().getPlayer(player);
        if(pl == null){
            return false;
        }

        if(myCoin >= count){

            coin.setCount(count);
            pl.getInventory().removeItem(coin);
            return true;
        }
        return false;
    }

    @Override
    public boolean addMoney(String player, double money, SalesEntity sales) {
        int count = (int) Math.ceil(money);
        Player pl = Server.getInstance().getPlayer(player);
        Item coin = getMoneyItem();

        if(count <= 0){
            return false;
        }
        if(sales != null){
            //判断是否为店主 店主就扔到售货机..
            if(sales.master.equalsIgnoreCase(player)){
                //添加硬币到私有库存
                sales.addItem(new SaleItem(coin,count,0,false),true);
                return true;
            }
        }


        if(pl== null){
           return false;
        }
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
    public double myMoney(String player, SalesEntity sales) {
        return getCoinItemCountByPlayer(player,sales);
    }




    public int getCoinItemCountByPlayer(String player, SalesEntity sales) {
        Item coin = getMoneyItem();
        if(sales.master.equalsIgnoreCase(player)){
            //如果是店主 就用售货机的
            SaleItem item = sales.getCoinSaleItem(coin);
            if(item == null){
                return 0;
            }else{
                return item.stack;
            }
        }
        Player pl = Server.getInstance().getPlayer(player);
        if(pl == null){
            return 0;
        }
        int size = 0;

        for(Item item: pl.getInventory().getContents().values()){
            if(item.equals(coin)){
                size+= item.getCount();
            }
        }
        return size;
    }


    @Override
    public Item getMoneyItem() {
        return RegisterItemServices.CUSTOM_ITEMS.get("sale_coin").clone();
    }
}
