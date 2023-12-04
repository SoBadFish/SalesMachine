package org.sobadfish.sales.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindowCustom;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.Utils;
import org.sobadfish.sales.items.SaleItem;

import java.util.LinkedHashMap;

/**
 * @author Sobadfish
 * @date 2023/11/22
 */
public class AdminForm {

    private final int id;

    private static int getRid(){
        return Utils.rand(223300,323300);
    }

    public SaleItem sales;

    public int getId() {
        return id;
    }

    public static LinkedHashMap<Player, AdminForm> DISPLAY_FROM = new LinkedHashMap<>();


    public AdminForm(SaleItem sales) {
        this.sales = sales;
        this.id = getRid();
    }

    public void display(Player player){

        FormWindowCustom custom = new FormWindowCustom("售货机 ————— 管理");

        boolean b = sales.tag.contains("noreduce") && sales.tag.getBoolean("noreduce");
        custom.addElement(new ElementToggle("是否不消耗库存",b));
        custom.addElement(new ElementInput("玩家限购次数","若设置-1则不限制玩家购买次数","-1"));
        custom.addElement(new ElementInput("刷新时间(h)","若设置-1则不刷新","-1"));
        custom.addElement(new ElementToggle("是否为收购", sales.tag.contains("sales_exchange") && sales.tag.getBoolean("sales_exchange")));
        custom.addElement(new ElementInput("商品价格","商品的价格 出售/回收","0"));
        custom.addElement(new ElementToggle("移除"));


        player.showFormWindow(custom,getId());
        DISPLAY_FROM.put(player,this);

    }
    public void onListener(Player player, FormResponseCustom responseCustom){
        boolean b = responseCustom.getToggleResponse(0);
        int limit = -1;
        int hour = -1;
        try {
            limit = Integer.parseInt(responseCustom.getInputResponse(1));
        }catch (Exception ignore){}
        try {
            hour = Integer.parseInt(responseCustom.getInputResponse(2));

        }catch (Exception ignore){}
        if(hour < -1){
            hour = -1;
        }
        if(responseCustom.getToggleResponse(5)){
            sales.isRemove = true;
        }else{
            sales.tag.putBoolean("sales_exchange",responseCustom.getToggleResponse(3));
            sales.tag.putBoolean("noreduce",b);
            sales.tag.putInt("limitCount",limit);
            sales.tag.putInt("limitTime",hour * 60 * 60 * 1000);
            float money = 0;
            try{
                money = Float.parseFloat(responseCustom.getInputResponse(4));
            }catch (Exception ignore){}
            sales.money = money;
            sales.tag.putDouble("money",money);
        }

        SalesMainClass.sendMessageToObject("&a设置成功",player);



    }

}
