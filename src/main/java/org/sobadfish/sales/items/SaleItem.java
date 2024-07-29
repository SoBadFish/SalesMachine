package org.sobadfish.sales.items;

import cn.nukkit.Player;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import org.sobadfish.sales.RegisterItemServices;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.Utils;
import org.sobadfish.sales.config.ItemData;
import org.sobadfish.sales.economy.IMoney;
import org.sobadfish.sales.entity.SalesEntity;

/**
 * @author Sobadfish
 * @date 2023/11/21
 */
public class SaleItem {

    public Item saleItem;

    public String loadMoney;

    public int stack;

    public boolean isRemove;


    public CompoundTag tag = new CompoundTag();

    public double money;

    public SaleItem(CompoundTag tag,Item saleItem, int stack, double money){
        this.tag = tag;
        this.saleItem = saleItem;
        this.stack = stack;
        this.money = money;
        if(tag.contains("loadMoney")){
            this.loadMoney = tag.getString("loadMoney");
        }else{
            String firstName = SalesMainClass.getFirstMoney();
            tag.putString("loadMoney",firstName);
            this.loadMoney = firstName;
        }

    }

    public String getItemName(){
        ItemData itemData = SalesMainClass.INSTANCE.getItemDataByItem(saleItem);
        String name = saleItem.getName();
        if(itemData != null){
            name = itemData.nameChinese;
        }
        if(saleItem.hasCustomName()){
            String id = saleItem.getId()+":"+saleItem.getDamage();
            return saleItem.getCustomName()+"&7 (&r"+id+"&7)";
        }
        return name+"&7 (&r"+saleItem.getId()+":"+saleItem.getDamage()+"&7)";
    }

    public SaleItem(Item saleItem, int stack, double money){
        this.saleItem = saleItem;
        this.stack = stack;
        this.money = money;
        this.loadMoney = SalesMainClass.getFirstMoney();
    }

    public SaleItem(Item saleItem, int stack,String loadMoney, double money){
        this.saleItem = saleItem;
        this.stack = stack;
        this.money = money;
        this.loadMoney = loadMoney;
    }

    //购买消耗此物品
    public boolean toBuyItem(SalesEntity sales, Player player, boolean useDiscount, int buyCount){
        IMoney iMoney = SalesMainClass.getMoneyCoreByName(loadMoney);
        if(iMoney == null){
            SalesMainClass.sendMessageToObject("&c购买失败!经济核心 "+loadMoney+" 未装载",player);
            return false;
        }
        //触发购买...
        int size = (int) Math.floor(stack / (float)saleItem.getCount() * buyCount);
        if(isNoReduce()){
            size = 1;
        }
        if(size > 0){
            if(isAcquisition()){

                if(sales.master.equalsIgnoreCase(player.getName())){
                    //店主不花钱
                    Item cln = saleItem.clone();
                    cln.setCount(cln.getCount() * buyCount);
                    player.getInventory().addItem(cln);
                    if(!isNoReduce()){
                        sales.removeItem(player.getName(),this,saleItem.getCount() * buyCount,true);
                    }

                }else{
                    boolean passMoney = isNoReduce();
                    if(!passMoney){
                        if(iMoney.myMoney(sales.master) < money * buyCount){
                            SalesMainClass.sendMessageToObject("&c店主没有足够的!"+iMoney.displayName(),player);
                            return false;
                        }
                    }

                    int count = getInventoryItemCount(player.getInventory(),saleItem);
                    if(count >= saleItem.getCount() * buyCount){
                        if(chunkLimit(player,buyCount)){
                            if(!passMoney){
                                if(!iMoney.reduceMoney(sales.master,money * buyCount)){
                                    SalesMainClass.sendMessageToObject("&c交易失败！ 无法扣除用户: "+sales.master+" 的 "+iMoney.displayName(),player);
                                    return false;
                                }
                            }
                            if (SalesMainClass.canGiveMoneyItem) {
                                player.getInventory().addItem(new MoneyItem(money * buyCount).getItem(loadMoney));
                            } else {
                                if (iMoney.addMoney(player.getName(), money * buyCount)) {
                                    SalesMainClass.sendMessageToObject("&a出售成功! 获得 &r" + iMoney.displayName() + "* " +
                                            String.format("%.2f", money * buyCount) + "!", player);
                                } else {
                                    SalesMainClass.sendMessageToObject("&c交易失败! 原因: 经济核心异常", player);
                                    //还钱..
                                    iMoney.addMoney(sales.master,money * buyCount);

                                    return false;
                                }
                            }
                            stack += saleItem.getCount() * buyCount;
                            Item sclon = saleItem.clone();
                            sclon.setCount(sclon.getCount() * buyCount);
                            int count2 = getInventoryItemCount(player.getInventory(),saleItem);
                            if(count2 < sclon.getCount()){
                                //特殊情况交易失败..
                                SalesMainClass.sendMessageToObject("&c交易失败! 原因: 背包物品不足", player);
                                //还钱..
                                iMoney.addMoney(sales.master,money * buyCount);
                                return false;
                            }
                            player.getInventory().removeItem(sclon);
//                                sales.addItem(this,true);
//                                SalesMainClass.sendMessageToObject("&a交易成功", player);
                            return true;

                        }

                    }else{
                        SalesMainClass.sendMessageToObject("&c购买失败! 物品不足!",player);
                        return false;
                    }

                }

            }else{
                if(sales.master.equalsIgnoreCase(player.getName())){
                    //店主不花钱
                    Item clnn = saleItem.clone();
                    clnn.setCount(clnn.getCount() * buyCount);
                    player.getInventory().addItem(clnn);

                }else{
                    if(chunkLimit(player,buyCount)){
                        //判断是否存在优惠券

                        double one =  money;
                        double rmoney = money * buyCount;
                        //先计算优惠.
                        if(rmoney > 0 && tag.contains("zk")){
                            float zk = tag.getFloat("zk");
                            if(zk > 0){
//                                    float discountRate = zk / 10.0f;
//                                    rmoney = (float) rmoney * (1 - discountRate);
                                rmoney = Utils.mathDiscount(zk,rmoney);
                                one =   Utils.mathDiscount(zk,one);
                            }

                        }

                        double del = one;


                        Item discount = null;
                        if(useDiscount){
                            discount = getDiscountItem(player,sales,saleItem);
                        }
                        Item cl = null;
                        int use = 0;

                        if(discount != null && rmoney > 0){
                            use = Math.min(buyCount,discount.count);
                            //优惠券
                            cl = discount.clone();
                            float zk = discount.getNamedTag().getFloat(CustomSaleDiscountItem.USE_ZK_TAG);
//                                float discountRate = zk / 10.0f;
//                                rmoney = (float) rmoney * (1 - discountRate);
//                                String db2 = String.format("%.2f",rmoney);
//                                rmoney = Float.parseFloat(db2);
                            del = Utils.mathDiscount(zk,del);
                            String db2 = String.format("%.2f",del);
                            del = Float.parseFloat(db2);
                            del = (one - del) * use;
                            rmoney -= del;
                        }

                        if(iMoney.myMoney(player.getName()) >= rmoney){
                            if(!iMoney.reduceMoney(player.getName(),rmoney)){
                                SalesMainClass.sendMessageToObject("&c交易失败! ",player);
                                return false;
                            }else{
                                if(cl != null && use > 0){
                                    cl.setCount(use);
                                    player.getInventory().removeItem(cl);
                                    SalesMainClass.sendMessageToObject("&b消耗 &r"+discount.getCustomName()+" &7 * &a"+use,player);
                                }
                                SalesMainClass.sendMessageToObject("&a交易成功! 扣除 &7*&e "+String.format("%.2f",rmoney)+iMoney.displayName(),player);


                            }
                            if(!iMoney.addMoney(sales.master,money * buyCount)){
                                SalesMainClass.sendMessageToObject("&c交易失败!",player);
                                return false;
                            }
                            Item cln = saleItem.clone();
                            cln.setCount(cln.getCount() * buyCount);
                            player.getInventory().addItem(cln);
                        }else{
                            SalesMainClass.sendMessageToObject(iMoney.displayName()+"&c不足!",player);
                            return false;
                        }

                    }else{
                        return false;
                    }
                }
                if(!isNoReduce()){
                    sales.removeItem(player.getName(),this,saleItem.getCount() * buyCount,true);
                }
            }
            player.getLevel().addSound(player.getPosition(), Sound.RANDOM_ORB);
            return true;


        }else{
            if(stack < saleItem.getCount()){
                if(sales.master.equalsIgnoreCase(player.getName())){
                    int cc = stack;
                    Item cl = saleItem.clone();
                    cl.setCount(cc);
                    player.getInventory().addItem(cl);
                    stack = 0;
                    sales.removeItem(player.getName(),this,saleItem.getCount(),true);
                    return true;
                }else{
                    SalesMainClass.sendMessageToObject("&c库存不足!",player);
                    return false;
                }
            }else{
                SalesMainClass.sendMessageToObject("&c库存不足!",player);
                return false;
            }


        }

    }

    /**
     * 无限库存
     * @return true 是
     * */
    public boolean isNoReduce(){
        return tag.contains("noreduce") && tag.getBoolean("noreduce");
    }

    /**
     * 是否为收购模式
     * */
    public boolean isAcquisition(){
        return tag.contains("sales_exchange") && tag.getBoolean("sales_exchange",false);
    }

    /**
     * 获取优惠物品
     * */
    public Item getDiscountItem(Player player, SalesEntity sales,Item saleItem){
        for (Item item: player.getInventory().getContents().values()){
            Class<?> iv = RegisterItemServices.CUSTOM_ITEMS.get("discount").getClass();
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

    private boolean chunkLimit(Player player,int buyCount){
        //限购
        if(tag.contains("limitCount") ){
            int limit = tag.getInt("limitCount");
            int upsLimit = 0;
            if(!tag.contains("limit")){
                tag.putCompound("limit",new CompoundTag());
            }
            CompoundTag limitList = tag.getCompound("limit");

            if(!limitList.contains(player.getName())) {
                limitList.putCompound(player.getName(),new CompoundTag());
            }
            CompoundTag user = limitList.getCompound(player.getName());
            if (user.contains("buy")) {
                upsLimit = getUserLimitCount(player.getName());

            }
            if(limit > 0 && upsLimit + buyCount > limit){
                SalesMainClass.sendMessageToObject("&c超出购买限制!",player);
                return false;
            }
            if(limit != -1 && upsLimit == limit){
                SalesMainClass.sendMessageToObject("&c购买失败! 已到达最大购买次数!",player);
                return false;
            }
            user.putInt("buy",upsLimit + buyCount);
            if(!user.contains("buyTime")){
                user.putLong("buyTime",System.currentTimeMillis());
            }
        }
        return true;
    }

    public int getInventoryItemCount(Inventory inventory, Item item){
        int c = 0;
        for(Item item1: inventory.getContents().values()){
            if(item1.equals(item,true,true)){
                c += item1.count;
            }
        }
        return c;
    }

    public int getUserLimitCount(String player){
        int upsLimit = 0;
        if(tag.contains("limit")){
            CompoundTag limitList = tag.getCompound("limit");
            if(limitList.contains(player)){
                CompoundTag user = limitList.getCompound(player);
                if(user.contains("buy")){
                    upsLimit = user.getInt("buy");
                }
                if(user.contains("buyTime")){
                    if(tag.contains("limitTime")){
                        long iniTime = tag.getLong("limitTime") * 1000 * 60 * 60;
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

    public ZkMoney getMoneyStr(){
        double mm = money;
        String db2 = String.format("%.2f",mm);
        if(mm > 0 && tag.contains("zk")){
            float zk = tag.getFloat("zk");
            if(zk > 0){
//                float discountRate = zk / 10.0f;
//                mm = (float) mm * (1 - discountRate);
                mm = Utils.mathDiscount(zk,mm);
                db2 = "&d"+String.format("%.2f",mm);
            }
//
        }
        String moneyStr = "&e"+(mm > 0?db2:"免费");

        ZkMoney zkMoney = new ZkMoney();
        zkMoney.money = mm;
        zkMoney.msg = moneyStr;
        return zkMoney;

    }

    public static class ZkMoney{
        public double money;

        public String msg;
    }
}
