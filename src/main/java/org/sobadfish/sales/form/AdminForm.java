package org.sobadfish.sales.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.items.SaleItem;

/**
 * @author Sobadfish
 * @date 2023/11/22
 */
public class AdminForm extends AbstractSaleForm {


    public SaleItem sales;

    public SalesEntity salesEntity;



    public AdminForm(SalesEntity entity,SaleItem sales) {
        super();
        this.salesEntity = entity;
        this.sales = sales;

    }

    @Override
    public FormWindow getForm(Player player){

        FormWindowCustom custom = new FormWindowCustom("售货机 ————— 管理");

        boolean b = sales.tag.contains("noreduce") && sales.tag.getBoolean("noreduce");

        String dbuy = sales.tag.contains("limitCount")?sales.tag.getInt("limitCount")+"":"-1";
        String dre = sales.tag.contains("limitTime")?sales.tag.getInt("limitTime")+"":"-1";
        String dm = sales.tag.contains("money")?sales.tag.getDouble("money")+"":"0";
        String zk = sales.tag.contains("zk")?sales.tag.getDouble("zk")+"":"-1";

        custom.addElement(new ElementInput("玩家限购次数","若设置-1则不限制玩家购买次数",dbuy));
        custom.addElement(new ElementInput("刷新时间(h)","若设置-1则不刷新",dre));
        custom.addElement(new ElementToggle("是否为收购", sales.tag.contains("sales_exchange") && sales.tag.getBoolean("sales_exchange")));
        custom.addElement(new ElementInput("商品价格","商品的价格 出售/回收",dm));
        custom.addElement(new ElementInput("商品折扣","设置商品的折扣 -1则不打折",zk));
        custom.addElement(new ElementToggle("移除"));

        if(player.isOp()){
            custom.addElement(new ElementToggle("是否不消耗库存",b));

            custom.addElement(new ElementInput("更改数量",sales.stack+""));
        }



        return custom;

    }
    @Override
    public void onListener(Player player, FormResponse response){
        FormResponseCustom responseCustom = (FormResponseCustom) response;
        boolean b = false;
        boolean remove = false;
        int limit = -1;
        int hour = -1;
        try {
            limit = Integer.parseInt(responseCustom.getInputResponse(0));
        }catch (Exception ignore){}
        try {
            hour = Integer.parseInt(responseCustom.getInputResponse(1));

        }catch (Exception ignore){}
        if(hour < -1){
            hour = -1;
        }
        int count = sales.stack;


        remove = responseCustom.getToggleResponse(5);
        if(player.isOp()){
            b = responseCustom.getToggleResponse(6);

            String tx =  responseCustom.getInputResponse(7);
            int reset = count;
            try {
                reset = Integer.parseInt(tx);
            }catch (Exception ignore){
            }
            if(reset > 0){
                count = reset;
            }



        }
        if(remove){
            if(salesEntity.hasItem(sales.saleItem) && sales.stack > 0 &&!player.isOp()){
                SalesMainClass.sendMessageToObject("&c当前库存下存在物品 请清空库存后移除",player);
                return;
            }
            sales.isRemove = true;
            salesEntity.removeItem(player.getName(),sales,0,true);
            salesEntity.salesData.saveItemSlots(salesEntity.loadItems);
            salesEntity.saveData();


        }else{
            sales.tag.putBoolean("sales_exchange",responseCustom.getToggleResponse(2));
            sales.tag.putBoolean("noreduce",b);
            sales.tag.putInt("limitCount",limit);
            sales.tag.putInt("limitTime",hour);

            float money = 0;
            try{
                money = Float.parseFloat(responseCustom.getInputResponse(3));
                if(money < 0){
                    money = 0;
                }
            }catch (Exception ignore){}
            float zk = -1;
            try{
                zk = Float.parseFloat(responseCustom.getInputResponse(4));
                if(zk >= 10){
                    zk = -1;
                }
            }catch (Exception ignore){}

            sales.tag.putFloat("zk",zk);
            sales.stack = count;
            sales.tag.putInt("stack",count);

            sales.money = money;
            sales.tag.putDouble("money",money);

            salesEntity.salesData.saveItemSlots(salesEntity.loadItems);
            salesEntity.saveData();

        }

        SalesMainClass.sendMessageToObject("&a设置成功",player);



    }

}
