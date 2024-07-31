package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.Utils;
import org.sobadfish.sales.economy.IMoney;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.form.BuyItemForm;
import org.sobadfish.sales.items.SaleItem;
import org.sobadfish.sales.panel.lib.ChestPanel;
import org.sobadfish.sales.panel.lib.ISalePanel;

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
    public void onClick(ISalePanel inventory, Player player) {
        if(click == 0){
            click++;
            Server.getInstance().getScheduler().scheduleDelayedTask(SalesMainClass.INSTANCE,() -> click = 0,40);
        }else{
            SalesEntity sales = inventory.getSales();
            if(showItem.stack <= showItem.saleItem.getCount() &&
                    !showItem.isNoReduce()
            ) {
                showItem.toBuyItem(sales, player, true, 1);
            }else{
                ((ChestPanel)inventory).close(player);
                Server.getInstance().getScheduler().scheduleDelayedTask(SalesMainClass.INSTANCE,() -> {

                    if(sales != null && !sales.finalClose && !sales.closed){
                        BuyItemForm buyItemForm = new BuyItemForm(sales,showItem);
                        buyItemForm.display(player);

                    }

                },10);
            }



        }
    }





    @Override
    public Item getPanelItem(Player info, int index) {

        Item i =  showItem.saleItem.clone();
        IMoney im = SalesMainClass.getMoneyCoreByName(showItem.loadMoney);

        List<String> lore = new ArrayList<>(Arrays.asList(showItem.saleItem.getLore()));
        int length = 0;
        boolean v = false;
        List<String> vl = new ArrayList<>();
        //TODO 价格
        SaleItem.ZkMoney zkMoney = showItem.getMoneyStr(im);

        if(showItem.isAcquisition()){
            vl.add(format("&r&7库存: &a"+(getStockStr())));
            vl.add(format("&r&7"+im.displayName()+" &7* &e"+(showItem.money != 0?showItem.money:"免费")));
            v = true;

        }else{
            vl.add(format("&r&7库存: &a"+(getStockStr())));
            vl.add(format("&r&7价格: "+zkMoney.msg ));
        }
//
        if(showItem.tag.contains("limitCount") ){
            int limit = showItem.tag.getInt("limitCount");
            if(limit > 0){
                int upsLimit = showItem.getUserLimitCount(info.getName());
                vl.add(format("&r&7限购: &e"+upsLimit+" &7/&7 "+limit));
                if(!showItem.tag.contains("limit")){
                    CompoundTag limitList = showItem.tag.getCompound("limit");

                    if(limitList.contains(info.getName())) {
                        CompoundTag user = limitList.getCompound(info.getName());
                        if(user.contains("buyTime")){
                            long lastByTime = user.getLong("buyTime");
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
                            String date = format.format(lastByTime);//注意这里返回的是string类型
                            vl.add(format("&r&7首次购买: &e"+date));
                        }
                    }
                }
            }
        }
        //找出最长的
        for (String mvl : vl){
            int l = mvl.length();
            if(l > length){
                length = l;
            }
        }
        for(String mvl : vl){
            lore.add(Utils.getCentontString(mvl,length));
        }

        if(v){
            lore.add(format(Utils.getCentontString("&r&e▶&7 双击出售 &e◀",length)));
        }else{
            lore.add(format(Utils.getCentontString("&r&e▶&7 双击购买 &e◀",length)));
        }
        i.setLore(lore.toArray(new String[0]));
        i.setNamedTag(i.getNamedTag().putInt("index",index));
        return i;
    }



    public String getStockStr(){

        if(showItem.isNoReduce()){
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
