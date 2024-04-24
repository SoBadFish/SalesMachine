package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.Utils;
import org.sobadfish.sales.economy.IMoney;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.items.CustomSaleDiscountItem;
import org.sobadfish.sales.items.MoneyItem;
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
           toBuyItem(inventory,player);

        }


    }

    public void toBuyItem(ISalePanel inventory, Player player){
        IMoney iMoney = SalesMainClass.getMoneyCoreByName(showItem.loadMoney);
        if(iMoney == null){
            SalesMainClass.sendMessageToObject("&c购买失败!经济核心 "+showItem.loadMoney+" 未装载",player);
            return;
        }
        //触发购买...
        int size = (int) Math.floor(showItem.stack / (float)showItem.saleItem.getCount());
        if(showItem.tag.contains("noreduce") && showItem.tag.getBoolean("noreduce")){
            size = 1;
        }
        if(size > 0){
            if(showItem.tag.contains("sales_exchange") && showItem.tag.getBoolean("sales_exchange",false)){

                if(((ChestPanel)inventory).sales.master.equalsIgnoreCase(player.getName())){
                    //店主不花钱
                    player.getInventory().addItem(showItem.saleItem);
                    if(!showItem.tag.contains("noreduce") || !showItem.tag.getBoolean("noreduce")){
                        ((ChestPanel)inventory).sales.removeItem(player.getName(),showItem,showItem.saleItem.getCount(),true);
                    }

                }else{
                    if(!showItem.tag.contains("noreduce") || !showItem.tag.getBoolean("noreduce")){

                        if(iMoney.myMoney(((ChestPanel)inventory).sales.master) < showItem.money){
                            SalesMainClass.sendMessageToObject("&c店主没有足够的!"+iMoney.displayName(),player);
                            return;
                        }else{
                            if(iMoney.reduceMoney(((ChestPanel)inventory).sales.master,showItem.money)){
                                SalesMainClass.sendMessageToObject("&a交易成功",player);
                            }else{
                                SalesMainClass.sendMessageToObject("&c交易失败!",player);
                                return;
                            }
                        }
                    }

                    int count = getInventoryItemCount(player.getInventory(),showItem.saleItem);
                    if(count >= showItem.saleItem.getCount()){
                        if(chunkLimit(player)){

                            if(SalesMainClass.canGiveMoneyItem){
                                player.getInventory().addItem(new MoneyItem(showItem.money).getItem(showItem.loadMoney));
                            }else{
                                if(iMoney.addMoney(player.getName(),showItem.money)){
                                    SalesMainClass.sendMessageToObject("&a出售成功! 获得 &r"+iMoney.displayName() +"* "+
                                            String.format("%.2f",showItem.money)+"!",player);
                                }else{
                                    SalesMainClass.sendMessageToObject("&c交易失败!",player);
                                    return;
                                }


                            }
                            showItem.stack += showItem.saleItem.getCount();
                            player.getInventory().removeItem(showItem.saleItem);
//
                        }

                    }else{
                        SalesMainClass.sendMessageToObject("&c购买失败! 物品不足!",player);
                        return;
                    }

                }

            }else{
                if(((ChestPanel)inventory).sales.master.equalsIgnoreCase(player.getName())){
                    //店主不花钱
                    player.getInventory().addItem(showItem.saleItem);

                }else{
                    if(chunkLimit(player)){
                        //判断是否存在优惠券
                        double rmoney = showItem.money;
                        //先计算优惠.
                        if(rmoney > 0 && showItem.tag.contains("zk")){
                            float zk = showItem.tag.getFloat("zk");
                            if(zk > 0){
//                                    float discountRate = zk / 10.0f;
//                                    rmoney = (float) rmoney * (1 - discountRate);
                                rmoney = Utils.mathDiscount(zk,rmoney);
                            }

                        }

                        Item discount = getDiscountItem(player,((ChestPanel)inventory).sales,showItem.saleItem);
                        Item cl = null;
                        if(discount != null && rmoney > 0){
                            //优惠券
                            cl = discount.clone();
                            float zk = discount.getNamedTag().getFloat(CustomSaleDiscountItem.USE_ZK_TAG);
//                                float discountRate = zk / 10.0f;
//                                rmoney = (float) rmoney * (1 - discountRate);
//                                String db2 = String.format("%.2f",rmoney);
//                                rmoney = Float.parseFloat(db2);
                            rmoney = Utils.mathDiscount(zk,rmoney);
                            String db2 = String.format("%.2f",rmoney);
                            rmoney = Float.parseFloat(db2);
                        }

                        if(iMoney.myMoney(player.getName()) >= rmoney){
                            if(!iMoney.reduceMoney(player.getName(),rmoney)){
                                SalesMainClass.sendMessageToObject("&c交易失败!",player);
                                return;
                            }else{
                                if(cl != null){
                                    cl.setCount(1);
                                    player.getInventory().removeItem(cl);
                                    SalesMainClass.sendMessageToObject("&b消耗 &r"+discount.getCustomName()+" &7 * &a1",player);
                                }
                                SalesMainClass.sendMessageToObject("&a交易成功! 扣除 &7*&e "+String.format("%.2f",rmoney)+iMoney.displayName(),player);


                            }
                            if(!iMoney.addMoney(((ChestPanel)inventory).sales.master,showItem.money)){
                                SalesMainClass.sendMessageToObject("&c交易失败!",player);
                                return;
                            }
                            player.getInventory().addItem(showItem.saleItem);
                        }else{
                            SalesMainClass.sendMessageToObject(iMoney.displayName()+"&c不足!",player);
                            return;
                        }

                    }else{
                        return;
                    }
                }
                if(!showItem.tag.contains("noreduce") || !showItem.tag.getBoolean("noreduce")){
                    ((ChestPanel)inventory).sales.removeItem(player.getName(),showItem,showItem.saleItem.getCount(),true);
                }
            }
            player.getLevel().addSound(player.getPosition(),Sound.RANDOM_ORB);


        }else{
            if(((ChestPanel)inventory).sales.master.equalsIgnoreCase(player.getName())){
                int cc = showItem.stack;
                Item cl = showItem.saleItem.clone();
                cl.setCount(cc);
                player.getInventory().addItem(cl);
                showItem.stack = 0;
                ((ChestPanel)inventory).sales.removeItem(player.getName(),showItem,showItem.saleItem.getCount(),true);
            }else{
                SalesMainClass.sendMessageToObject("&c库存不足!",player);
            }

        }

    }

    /**
     * 获取优惠物品
     * */
    public Item getDiscountItem(Player player, SalesEntity sales,Item saleItem){
        for (Item item: player.getInventory().getContents().values()){
            Class<?> iv = SalesMainClass.CUSTOM_ITEMS.get("discount").getClass();
            if(item.getClass() == iv){

                if(item.hasCompoundTag()){
                    CompoundTag tag = item.getNamedTag();

                    if(tag.contains(CustomSaleDiscountItem.USE_TAG)){
//                        return item;
                        String useBy = tag.getString(CustomSaleDiscountItem.USE_TAG);
                        if(!tag.contains(CustomSaleDiscountItem.USE_NONE_TAG)){
                            //不是通用优惠券
                            if(!useBy.equalsIgnoreCase(sales.salesData.uuid) && !useBy.equalsIgnoreCase(sales.master)){
                                continue;
                            }
                        }
                        if(tag.contains(CustomSaleDiscountItem.USE_ONLY_ITEM_TAG)){
                            Item only = NBTIO.getItemHelper(tag.getCompound(CustomSaleDiscountItem.USE_ONLY_ITEM_TAG));
                            if(!only.equals(saleItem,true,true)){
                                continue;
                            }
                        }
                        if(tag.contains(CustomSaleDiscountItem.USE_TIME_TAG)){
                            int ussDay = tag.getInt(CustomSaleDiscountItem.USE_TIME_TAG);
                            if(ussDay > 0){
                                long createTime = tag.getLong(CustomSaleDiscountItem.CRETE_TIME_TAG);
                                if(System.currentTimeMillis() > Utils.getFutureTime(createTime,ussDay)){
                                    //无效
                                    continue;
                                }
                            }

                        }
                        //有效
                        return item;
                    }
                }
                break;
            }
        }
        return null;
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
        IMoney im = SalesMainClass.getMoneyCoreByName(showItem.loadMoney);

        List<String> lore = new ArrayList<>(Arrays.asList(showItem.saleItem.getLore()));
        int length = 0;
        boolean v = false;
        List<String> vl = new ArrayList<>();
        double mm = showItem.money;
        String db2 = String.format("%.2f",mm);
        if(mm > 0 && showItem.tag.contains("zk")){
            float zk = showItem.tag.getFloat("zk");
            if(zk > 0){
//                float discountRate = zk / 10.0f;
//                mm = (float) mm * (1 - discountRate);
                mm = Utils.mathDiscount(zk,mm);
                db2 = "&d"+String.format("%.2f",mm);
            }
//
        }
        String moneyStr = "&e"+(showItem.money != 0?db2:"免费");
        if(showItem.tag.contains("sales_exchange") && showItem.tag.getBoolean("sales_exchange",false)){
            vl.add(format("&r&7库存: &a"+(getStockStr())));
            vl.add(format("&r&7"+im.displayName()+" &7* &e"+(showItem.money != 0?showItem.money:"免费")));
            v = true;
//            i = new MoneyItem(showItem.money).getItem();
//            lore.add(format(Utils.getCentontString("&r&e▶&7 回收价: &e"+(showItem.getItemName()+" &r*&a "+showItem.saleItem.getCount()),length)));
        }else{
            vl.add(format("&r&7库存: &a"+(getStockStr())));
            vl.add(format("&r&7价格: "+moneyStr));
        }
//
        if(showItem.tag.contains("limitCount") ){
            int limit = showItem.tag.getInt("limitCount");
            if(limit > 0){
                int upsLimit = getUserLimitCount(info);
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
        //lore.add(format(Utils.getCentontString("&r&e▶&7 双击购买 &e◀",length)));
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
                        long iniTime = showItem.tag.getLong("limitTime") * 1000 * 60 * 60;
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
