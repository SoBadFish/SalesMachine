package org.sobadfish.sales.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.Utils;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.items.CustomSaleDiscountItem;
import org.sobadfish.sales.items.SaleItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Sobadfish
 * @date 2024/4/24
 */
public class DiscountForm {

    private final int id;

    private static int getRid(){
        return Utils.rand(323301,423300);
    }

    public Item item;

    public SalesEntity salesEntity;

    public int getId() {
        return id;
    }

    public static LinkedHashMap<String, DiscountForm> DISPLAY_FROM = new LinkedHashMap<>();

    public List<String> dItem;

    public DiscountForm(SalesEntity entity, Item item) {
        this.salesEntity = entity;
        this.item = item;
        this.id = getRid();
    }

    public void display(Player player){
        FormWindowCustom custom = new FormWindowCustom("售货机 ————— 优惠券");
        custom.addElement(new ElementInput("优惠折扣","请输入0 ~ 10的折扣值"));
        custom.addElement(new ElementToggle("是否仅对此售货机打折",false));
        dItem = new ArrayList<>();
        dItem.add("全部");
        for(SaleItem saleItem: salesEntity.items){
            dItem.add(TextFormat.colorize('&',saleItem.getItemName()));
        }

        custom.addElement(new ElementDropdown("选择打折的物品",dItem,0));
        custom.addElement(new ElementInput("使用期限","-1则不限制时间 单位为 天 从创建时开始计算","-1"));
        player.showFormWindow(custom,getId());
        DISPLAY_FROM.put(player.getName(),this);
    }

    public void onListener(Player player, FormResponseCustom responseCustom){
        String zks = responseCustom.getInputResponse(0);
        float zk = 10;
        boolean only = false;
        try{
            zk = Float.parseFloat(zks);
        }catch (Exception ignore){}
        if(zk > 10 || zk < 0){
            zk = 10;
        }
        if(responseCustom.getToggleResponse(1)){
            only = true;
        }
        SaleItem sl = null;
        int sIndex = dItem.indexOf(responseCustom.getDropdownResponse(2).getElementContent());
        sIndex -= 1;
        if(sIndex >= 0 && sIndex < salesEntity.items.size()){
            sl = salesEntity.items.get(sIndex);
        }
        int day = -1;
        try{
            day = Integer.parseInt(responseCustom.getInputResponse(3));
        }catch (Exception ignore){}
        if(day > 365){
            day = -1;
        }
        item.setCustomName(TextFormat.colorize('&',"&r&7"+salesEntity.salesData.customname+"&r&l&a 优惠券 &7(&a"+String.format("%.1f",zk)+"折&7)"));
        CompoundTag tag = item.getNamedTag();
        tag.putFloat(CustomSaleDiscountItem.USE_ZK_TAG,zk);
        List<String> lore = new ArrayList<>();
        lore.add("");
        if(only){
            lore.add(format("&7仅对 &r"+salesEntity.salesData.customname+" &7有效"));
            tag.putString(CustomSaleDiscountItem.USE_TAG,salesEntity.salesData.uuid);
        }else{
            lore.add(format("&7仅&e "+salesEntity.master+" &7的售货机有效"));
            tag.putString(CustomSaleDiscountItem.USE_TAG,salesEntity.master);
        }
        if(sl != null){
            lore.add(format("&7仅对&r "+sl.getItemName()+" &7有效"));
            tag.putCompound(CustomSaleDiscountItem.USE_ONLY_ITEM_TAG, NBTIO.putItemHelper(sl.saleItem));
        }
        if(day > 0){
            Long t = Utils.getFutureTime(System.currentTimeMillis(),day);
            Date date = new Date(t);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            lore.add("\n"+format("&c有效期至 "+dateFormat.format(date)));
            tag.putInt(CustomSaleDiscountItem.USE_TIME_TAG, day);
            tag.putLong(CustomSaleDiscountItem.CRETE_TIME_TAG, System.currentTimeMillis());
        }
        lore.add("\n"+format("&a "+salesEntity.master+" &b发行"));
        item.setNamedTag(tag);

        item.addEnchantment(Enchantment.getEnchantment(0));
        item.setLore(lore.toArray(new String[0]));

        player.getInventory().setItemInHand(item);


    }

    private String format(String s){
        return TextFormat.colorize('&',"&r"+s);
    }
}
