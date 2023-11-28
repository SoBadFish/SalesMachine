package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;
import me.onebone.economyapi.EconomyAPI;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.Utils;
import org.sobadfish.sales.items.MoneyItem;
import org.sobadfish.sales.items.SaleItem;
import org.sobadfish.sales.panel.lib.ChestPanel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
            if(showItem.tag.contains("noreduce") && showItem.tag.getBoolean("noreduce")){
                size = 1;
            }
            if(size > 0){
                if(showItem.tag.contains("sales_exchange") && showItem.tag.getBoolean("sales_exchange",false)){
                    int count = getInventoryItemCount(player.getInventory(),showItem.saleItem);
                    if(count >= showItem.saleItem.getCount()){
                        if(chunkLimit(player)){
                            player.getInventory().removeItem(showItem.saleItem);
                            player.getInventory().addItem(new MoneyItem(showItem.money).getItem());
                        }

                    }else{
                        SalesMainClass.sendMessageToObject("&c购买失败! 物品不足!",player);
                        return;
                    }
                }else{
                    if(inventory.sales.master.equalsIgnoreCase(player.getName())){
                        //店主不花钱
                        player.getInventory().addItem(showItem.saleItem);

                    }else{

                        if(EconomyAPI.getInstance().myMoney(player) >= showItem.money){

                            if(chunkLimit(player)){
                                EconomyAPI.getInstance().reduceMoney(player,showItem.money);
                                EconomyAPI.getInstance().addMoney(inventory.sales.master,showItem.money);
                                player.getInventory().addItem(showItem.saleItem);
                            }

                        }else{
                            SalesMainClass.sendMessageToObject("&c金钱不足!",player);
                            return;
                        }
                    }
                    if(!showItem.tag.contains("noreduce") || !showItem.tag.getBoolean("noreduce")){
                        inventory.sales.removeItem(player.getName(),showItem,showItem.saleItem.getCount());
                    }
                }
                player.getLevel().addSound(player.getPosition(),Sound.RANDOM_ORB);




            }else{
                if(inventory.sales.master.equalsIgnoreCase(player.getName())){
                    int cc = showItem.stack;
                    Item cl = showItem.saleItem.clone();
                    cl.setCount(cc);
                    player.getInventory().addItem(cl);
                    showItem.stack = 0;
                    inventory.sales.removeItem(player.getName(),showItem,showItem.saleItem.getCount());
                }else{
                    SalesMainClass.sendMessageToObject("&c库存不足!",player);
                }

            }


        }


    }

    public boolean chunkLimit(Player player){
        //限购
        if(showItem.tag.contains("limitCount") ){
            int limit = showItem.tag.getInt("limitCount");
            int upsLimit = 0;
            if(!showItem.tag.contains("limit")){
                showItem.tag.putCompound("limit",new CompoundTag());
            }
            CompoundTag limitList = showItem.tag.getCompound("limit");

            if(!limitList.contains(player.getName())) {
                limitList.putCompound(player.getName(),new CompoundTag());
            }
            CompoundTag user = limitList.getCompound(player.getName());
            if (user.contains("buy")) {
                upsLimit = user.getInt("buy");
            }
            if(upsLimit == limit){
                SalesMainClass.sendMessageToObject("&c购买失败! 已到达最大购买次数!",player);
                return false;
            }
            user.putInt("buy",++upsLimit);
            if(!user.contains("buyTime")){
                user.putLong("buyTime",System.currentTimeMillis());
            }
        }
        return true;
    }

    public int getInventoryItemCount(Inventory inventory,Item item){
        int c = 0;
        for(Item item1: inventory.getContents().values()){
            if(item1.equals(item,true,true)){
                c += item1.count;
            }
        }
        return c;
    }

    @Override
    public Item getPanelItem(Player info, int index) {

        Item i =  showItem.saleItem.clone();
        List<String> lore = new ArrayList<>(Arrays.asList(i.getLore()));
        int length = 25;
        lore.add(" ");

        if(showItem.tag.contains("sales_exchange") && showItem.tag.getBoolean("sales_exchange",false)){
            lore.add(format(Utils.getCentontString("&r&e▶&7 回收价: &r金币 &7* &e"+(showItem.money != 0?showItem.money:"免费"),length)));
//            i = new MoneyItem(showItem.money).getItem();
//            lore.add(format(Utils.getCentontString("&r&e▶&7 回收价: &e"+(showItem.getItemName()+" &r*&a "+showItem.saleItem.getCount()),length)));
        }else{
            lore.add(format(Utils.getCentontString("&r&e▶&7 库存: &a"+(getStockStr()),length)));
            lore.add(format(Utils.getCentontString("&r&e▶&7 价格: &e"+(showItem.money != 0?showItem.money:"免费"),length)));
        }
//
        if(showItem.tag.contains("limitCount") ){
            int limit = showItem.tag.getInt("limitCount");
            if(limit > 0){
                int upsLimit = getUserLimitCount(info);
                lore.add(format(Utils.getCentontString("&r&e▶&7 限购: &e"+upsLimit+" &7/&7 "+limit,length)));
                if(!showItem.tag.contains("limit")){
                    CompoundTag limitList = showItem.tag.getCompound("limit");

                    if(limitList.contains(info.getName())) {
                        CompoundTag user = limitList.getCompound(info.getName());
                        if(user.contains("buyTime")){
                            long lastByTime = user.getLong("buyTime");
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
                            String date = format.format(lastByTime);//注意这里返回的是string类型
                            lore.add(format(Utils.getCentontString("&r&e▶&7 首次购买: &e"+date,length)));
                        }
                    }
                }
            }
        }
        lore.add("  ");
        lore.add(format(Utils.getCentontString("&r&e▶&7 双击购买",length)));
        i.setLore(lore.toArray(new String[0]));
        i.setNamedTag(i.getNamedTag().putInt("index",index));
        return i;
    }

    public int getUserLimitCount(Player info){
        int upsLimit = 0;
        if(showItem.tag.contains("limit")){
            CompoundTag limitList = showItem.tag.getCompound("limit");
            if(limitList.contains(info.getName())){
                CompoundTag user = limitList.getCompound(info.getName());
                if(user.contains("buy")){
                    upsLimit = user.getInt("buy");
                }
                if(user.contains("buyTime")){
                    if(showItem.tag.contains("limitTime")){
                        long iniTime = showItem.tag.getLong("limitTime");
                        if(iniTime > 0){
                            if(System.currentTimeMillis() >= user.getLong("buyTime") + iniTime){
                                upsLimit = 0;
                                user.putInt("buy",0);
                                user.remove("buyTime");
                            }
                        }
                    }
                }
            }
        }
        return upsLimit;
    }

    public String getStockStr(){

        if(showItem.tag.contains("noreduce") && showItem.tag.getBoolean("noreduce")){
            return "&e无限";
        }

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
