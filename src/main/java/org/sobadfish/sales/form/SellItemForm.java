package org.sobadfish.sales.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementSlider;
import cn.nukkit.form.element.ElementStepSlider;
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
 * @date 2023/11/21
 */
public class SellItemForm {

    private final int id;

    private static int getRid(){
        return Utils.rand(11900,21000);
    }

    public SalesEntity sales;

    public int getId() {
        return id;
    }

    public static LinkedHashMap<Player, SellItemForm> DISPLAY_FROM = new LinkedHashMap<>();

    public Item item;

    public SellItemForm(SalesEntity sales,Item item) {
        this.item = item;
        this.sales = sales;
        this.id = getRid();
    }


    public void display(Player player){

        FormWindowCustom custom = new FormWindowCustom("售货机 ————— 上架");
        custom.addElement(new ElementLabel(TextFormat.colorize('&',
                "&l物品: &r&a"+getItemName()+
                        "&r\n&l数量: &r&a"+item.getCount()+"\n")));
        custom.addElement(new ElementSlider("请选择出售数量",0,item.getCount(),1,0));
        custom.addElement(new ElementInput("请输入价格 若不填则默认为 0 ","商品的价格"));
        player.showFormWindow(custom,getId());
        DISPLAY_FROM.put(player,this);

    }

    public void onListener(Player player, FormResponseCustom responseCustom){
        int stack = (int) responseCustom.getSliderResponse(1);
        if(stack == 0){
            return;
        }
        float money;
        String m = responseCustom.getInputResponse(2);
        if(m == null){
            m = "0";
        }
        try{
            money = Float.parseFloat(m);
        }catch (Exception e){
            money = 0.0f;
        }
        if(money < 0){
            SalesMainClass.sendMessageToObject("&c价格必须大于0!",player);
            return;
        }
        Item cl = item.clone();
        cl.setCount(stack);
        if(sales.addItem(new SaleItem(cl,stack,money))){
            SalesMainClass.sendMessageToObject("&a添加成功!",player);
            player.getInventory().removeItem(cl);
        }else{
            SalesMainClass.sendMessageToObject("&a添加失败!",player);
        }

    }

    private String getItemName(){
        if(item.hasCustomName()){
            return item.getCustomName()+"&7 (&r"+item.getId()+":"+item.getDamage()+"&7)";
        }
        return item.getName()+"&7 (&r"+item.getId()+":"+item.getDamage()+"&7)";
    }

}
