package org.sobadfish.sales.form;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.SalesListener;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.panel.DisplayPlayerPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sobadfish
 * @date 2025/3/19
 */
public class PhoneForm extends AbstractSaleForm{

    public List<SalesEntity> displays = new ArrayList<>();

    public String search = null;

    public PhoneForm() {
        super();
    }

    @Override
    public FormWindow getForm(Player player) {
        //搜索网店的商品列表
        displays.clear();
        String str;
        List<SalesEntity> entities = new ArrayList<>(SalesListener.cacheEntitys.values());
        if(search == null){
            for(SalesEntity entity : entities){
                if(entity.salesData.net == 1){
                    if(!displays.contains(entity)) {
                        displays.add(entity);
                    }
                }
            }
        }else{
            for(SalesEntity entity : entities){
                if(entity.salesData.net == 1){
                    if(entity.master.contains(search) || entity.salesData.netinfo.contains(search)
                    || entity.salesData.customname.contains(search)
                    || entity.equalsItemStr(search)
                    ){
                        if(!displays.contains(entity)){
                            displays.add(entity);
                        }

                    }
                }
            }
        }


        str = "当前检索到"+displays.size()+"个网店";
        if(search != null){
            str = "当前检索到符合: “"+search+"” "+displays.size()+"个网店 单页最多显示10个网店";
        }
        FormWindowSimple simple = new FormWindowSimple("手机", str);
        simple.addButton(new ElementButton("搜索",new ElementButtonImageData("path","textures/ui/magnifyingGlass")));
        for(SalesEntity entity : displays){
            String netInfo = entity.salesData.netinfo;
            if(netInfo == null || "".equals(netInfo)){
                netInfo = "暂无介绍";
            }
            simple.addButton(new ElementButton(entity.salesData.customname.substring(0,Math.min(entity.salesData.customname.length(),10))+"... "
                    +TextFormat.colorize('&',"&r"+entity.salesData.master)
                    +"\n"+netInfo,new ElementButtonImageData("path","textures/ui/Friend2")));
        }
        return simple;


    }

    @Override
    public void onListener(Player player, FormResponse response) {
        if(response instanceof FormResponseSimple) {
            if (((FormResponseSimple) response).getClickedButtonId() == 0) {
                //唤醒
                Server.getInstance().getScheduler().scheduleDelayedTask(SalesMainClass.INSTANCE, new Runnable() {
                    @Override
                    public void run() {
                        SearchForm discountForm = new SearchForm();
                        discountForm.display(player);
                    }
                }, 5);
            } else {
                int clickIndex = ((FormResponseSimple) response).getClickedButtonId() - 1;
                if (displays.size() > clickIndex) {
                    SalesEntity entity1 = displays.get(clickIndex);
                    Server.getInstance().getScheduler().scheduleDelayedTask(SalesMainClass.INSTANCE, new Runnable() {
                        @Override
                        public void run() {
                            if (entity1.finalClose) {
                                return;
                            }
                            if (!player.isOp() && !player.getName().equalsIgnoreCase(entity1.master) && entity1.salesData.lock == 1) {
                                player.sendTip(TextFormat.colorize('&', "&c这个售货机被锁上了"));
                                return;
                            }
                            if (entity1.clickInvPlayers.size() > 0) {
                                SalesMainClass.sendMessageToObject("&c售货机正在被编辑", player);
                                return;
                            }

                            DisplayPlayerPanel displayPlayerPanel = new DisplayPlayerPanel(entity1);
                            displayPlayerPanel.open(player);
                            SalesListener.chestPanelLinkedHashMap.put(player.getName(), displayPlayerPanel);
                        }

                    }, 5);
                }
            }
        }
    }



}
