package org.sobadfish.sales.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementSlider;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.economy.IMoney;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.items.SaleItem;

/**
 * @author Sobadfish
 * @date 2024/4/27
 */
public class BuyItemForm extends AbstractSaleForm{


    public SaleItem salesItem;

    public SalesEntity salesEntity;




    public BuyItemForm(SalesEntity entity,SaleItem salesItem) {
        super();
        this.salesEntity = entity;
        this.salesItem = salesItem;
    }

    @Override
    public FormWindow getForm(Player player){
        IMoney iMoney = SalesMainClass.getMoneyCoreByName(salesItem.loadMoney);

        int limitCount = getLimitCount(player.getName());
        int maxCount = limitCount;
        int stock = (int)Math.floor(salesItem.stack / (float)salesItem.saleItem.getCount());
        if(maxCount == -1){
            //最大选择为 99
            maxCount = 99;
        }
        if(!salesItem.isNoReduce()){
            maxCount = Math.min(stock,99);
        }
        boolean isSell = false;
        String title = "购买";
        if(salesItem.isAcquisition()){
            //收购
            title = "出售";
            isSell = true;
        }

        int disCount = 0;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("&e").append(iMoney.displayName()).append(" &r: ").append(String.format("%.2f",iMoney.myMoney(player.getName()))).append("\n\n");
        stringBuilder.append("&7售货机名称: &r: ").append(salesEntity.salesData.customname).append("\n\n");
        stringBuilder.append("&7售货机坐标: &r: ").append(salesEntity.salesData.location).append("\n");
        stringBuilder.append("&7售货机主人: &r: ").append(salesEntity.master).append("\n\n");
        if(salesItem.isNoReduce()){
            stringBuilder.append("&7当前库存: &l&e无限").append("\n\n");
        }else{
            stringBuilder.append("&7当前库存: &l&a").append(stock > 0 ? stock : "&c缺货").append("\n\n");
        }

        if(!isSell){
            Item dItem = salesItem.getDiscountItem(player,salesEntity,salesItem.saleItem);
            if(dItem != null){
                disCount = dItem.count;
            }

            stringBuilder.append("&7可用优惠券: &l&a").append(disCount).append("\n\n");
            SaleItem.ZkMoney zkMoney = salesItem.getMoneyStr();
            stringBuilder.append("&7价格: &l&a").append(zkMoney.msg).append("\n");

        }else{
            stringBuilder.append("&7价格: &l&a" + "&r&7").append(iMoney.displayName()).append(" &7* &e").append(salesItem.money != 0 ? salesItem.money : "免费").append("\n\n");
            int pStock = salesItem.getInventoryItemCount(player.getInventory(),salesItem.saleItem);
            int canCell = (int) Math.floor(pStock / (float) salesItem.saleItem.getCount());
            maxCount = Math.min(canCell,99);
            stringBuilder.append("&7我的库存: &l&a").append(pStock).append(" &7(可出售: &e").append(canCell).append("&7)").append("\n\n");
        }
        if(limitCount != -1){
            stringBuilder.append("&c剩余购买次数: &l&a").append(limitCount);
        }



        FormWindowCustom custom = new FormWindowCustom("售货机 ————— "+title);
        custom.addElement(new ElementLabel(TextFormat.colorize('&',stringBuilder.toString())));
        custom.addElement(new ElementSlider("请选择"+title+"数量： ",0,maxCount,1,0));
        if(disCount > 0){
            custom.addElement(new ElementToggle("是否使用优惠券",true));
        }

        return custom;
    }


    @Override
    public void onListener(Player player, FormResponse response){
        FormResponseCustom responseCustom = (FormResponseCustom) response;
        if(salesEntity != null && !salesEntity.finalClose && !salesEntity.closed){
            boolean dis = false;
            if(responseCustom.getResponses().size() > 2){
                dis = responseCustom.getToggleResponse(2);
            }
            int buyCount;
            buyCount = (int) responseCustom.getSliderResponse(1);

            if(buyCount > 0){
                if(salesItem.toBuyItem(salesEntity,player,dis,buyCount)){
                    SalesMainClass.sendMessageToObject("&a交易完成！", player);
                }
            }
        }

    }


    public int getLimitCount(String player){
        if(salesItem.tag.contains("limitCount") ) {
            int limit = salesItem.tag.getInt("limitCount");
            int upsLimit = salesItem.getUserLimitCount(player);
            return Math.max(limit - upsLimit,-1);
        }
        return -1;
    }
}
