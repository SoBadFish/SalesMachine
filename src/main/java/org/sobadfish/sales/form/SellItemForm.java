package org.sobadfish.sales.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.*;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.Utils;
import org.sobadfish.sales.economy.IMoney;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.items.SaleItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public static LinkedHashMap<String, SellItemForm> DISPLAY_FROM = new LinkedHashMap<>();

    public Item item;

    public SellItemForm(SalesEntity sales, Item item) {
        this.item = item;
        this.sales = sales;
        this.id = getRid();
    }

    public List<String> sv;

    public void display(Player player){

        FormWindowCustom custom = new FormWindowCustom("售货机 ————— 上架");
        custom.addElement(new ElementLabel(TextFormat.colorize('&',
                "&l物品: &r&a"+getItemName()+
                        "&r\n&l数量: &r&a"+item.getCount()+"\n")));
        custom.addElement(new ElementSlider("请选择商品的数量",0,item.getCount(),1,0));
        custom.addElement(new ElementInput("请输入价格 若不填则默认为 0 ","商品的价格"));
        sv = new ArrayList<>();
        for(Map.Entry<String,IMoney> entry: SalesMainClass.getLoadMoney().entrySet()){
            if(SalesMainClass.OnlyUserAdminCore.contains(entry.getKey()) && !player.isOp()){
                continue;
            }
            sv.add(entry.getValue().displayName());
        }
        custom.addElement(new ElementDropdown("请选择货币类型 ",sv,0));
        custom.addElement(new ElementToggle("是否为收购"));


//        custom.addElement(new ElementToggle("是否收购"));
        player.showFormWindow(custom,getId());
        DISPLAY_FROM.put(player.getName(),this);

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
        int index = sv.indexOf(responseCustom.getDropdownResponse(3).getElementContent());
        if(index == -1){
            index = 0;
        }

        SaleItem saleItem = new SaleItem(cl,stack,new ArrayList<>(SalesMainClass.getLoadMoney().keySet()).
                get(index),money);
        if(responseCustom.getResponses().size() > 4){
            saleItem.tag.putBoolean("sales_exchange",responseCustom.getToggleResponse(4));
        }


        if(sales.addItem(saleItem,true)){
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
