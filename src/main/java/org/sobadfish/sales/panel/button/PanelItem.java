package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import me.onebone.economyapi.EconomyAPI;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.Utils;
import org.sobadfish.sales.items.SaleItem;
import org.sobadfish.sales.panel.lib.ChestPanel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Sobadfish
 * @date 2023/11/20
 */
public class PanelItem extends BasePlayPanelItemInstance{

    public int click = 0;

    public SaleItem showItem;




    public PanelItem(SaleItem item){
        this.showItem = item;
    }

    @Override
    public int getCount() {
        return showItem.saleItem.getCount();
    }

    @Override
    public Item getItem() {
        return showItem.saleItem;
    }

    @Override
    public void onClick(ChestPanel inventory, Player player) {
        if(click == 0){
            click++;
            Server.getInstance().getScheduler().scheduleDelayedTask(() -> click = 0,40);
        }else{
            //触发购买...
            int size = (int) Math.floor(showItem.stack / (float)showItem.saleItem.getCount());

            if(size > 0){

                if(inventory.sales.master.equalsIgnoreCase(player.getName())){
                    //店主不花钱
                    player.getInventory().addItem(showItem.saleItem);

                }else{
                    if(EconomyAPI.getInstance().myMoney(player) >= showItem.money){
                        EconomyAPI.getInstance().reduceMoney(player,showItem.money);
                        EconomyAPI.getInstance().addMoney(inventory.sales.master,showItem.money);
                        player.getInventory().addItem(showItem.saleItem);
                    }else{
                        SalesMainClass.sendMessageToObject("&c金钱不足!",player);
                    }
                }
                inventory.sales.removeItem(showItem,showItem.saleItem.getCount());
            }else{
                if(inventory.sales.master.equalsIgnoreCase(player.getName())){
                    int cc = showItem.stack;
                    Item cl = showItem.saleItem.clone();
                    cl.setCount(cc);
                    player.getInventory().addItem(cl);
                    showItem.stack = 0;
                    inventory.sales.removeItem(showItem,showItem.saleItem.getCount());
                }else{
                    SalesMainClass.sendMessageToObject("&c库存不足!",player);
                }

            }


        }


    }

    @Override
    public Item getPanelItem(Player info, int index) {
        Item i =  showItem.saleItem.clone();

        List<String> lore = new ArrayList<String>();
        int length = 25;
        lore.add(" ");
        lore.add(format(Utils.getCentontString("&r&e▶&7 库存: &a"+(getStockStr()),length)));
        lore.add(format(Utils.getCentontString("&r&e▶&7 价格: &e"+(showItem.money != 0?showItem.money:"免费"),length)));
        lore.add("  ");
        i.setLore(lore.toArray(new String[0]));
        i.setNamedTag(i.getNamedTag().putInt("index",index));
        return i;
    }

    public String getStockStr(){
        int size = (int) Math.floor(showItem.stack / (float)showItem.saleItem.getCount());
        if(size > 0){
            return size+"";
        }else{
            return "&c库存不足 &7(&a"+showItem.stack+"&7)&r";
        }
    }

    private String format(String format){
        return TextFormat.colorize('&',format);
    }
}
