package org.sobadfish.sales.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementSlider;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.Utils;
import org.sobadfish.sales.entity.SalesEntity;
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
        sales.tag.putBoolean("noreduce",b);
        sales.tag.putInt("limitCount",limit);
        sales.tag.putInt("limitTime",hour * 60 * 60 * 1000);

        SalesMainClass.sendMessageToObject("&a设置成功",player);



    }

}
