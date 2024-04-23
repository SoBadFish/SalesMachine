package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.SalesListener;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.Utils;
import org.sobadfish.sales.form.AdminForm;
import org.sobadfish.sales.items.SaleItem;
import org.sobadfish.sales.panel.DisplaySaleInventoryPanel;
import org.sobadfish.sales.panel.lib.ChestPanel;
import org.sobadfish.sales.panel.lib.ISalePanel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sobadfish
 * @date 2023/11/22
 */
public class PanelSettingItem extends BasePlayPanelItemInstance{
    public int click = 0;

    public SaleItem showItem;

    public boolean isInv;

    public PanelSettingItem(SaleItem item){
        this.showItem = item;
        this.isInv = false;
    }


    public PanelSettingItem(SaleItem item,boolean isInv){
        this.showItem = item;
        this.isInv = isInv;
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
        }else {
            ((ChestPanel)inventory).close(player);
            if(!isInv){
                Server.getInstance().getScheduler().scheduleDelayedTask(SalesMainClass.INSTANCE,() -> {
                    AdminForm adminForm = new AdminForm(showItem);
                    adminForm.display(player);
                },10);
            }else{
                //开启库存页面
                //关闭其他玩家的
                inventory.getSales().closePanel();

                Server.getInstance().getScheduler().scheduleDelayedTask(SalesMainClass.INSTANCE,() -> {
                    DisplaySaleInventoryPanel displayPlayerPanel = new DisplaySaleInventoryPanel(inventory.getSales(), showItem.saleItem);
                    displayPlayerPanel.open(player);
                    SalesListener.chestPanelLinkedHashMap.put(player.getName(), displayPlayerPanel);
                },20);
            }


        }
    }

    @Override
    public Item getPanelItem(Player info, int index) {
        Item i =  showItem.saleItem.clone();
        List<String> lore = new ArrayList<String>();
        int length = 25;
        lore.add(" ");
        int limit = -1;
        int time = -1;

        if(showItem.tag.contains("limitCount") ){
            limit = showItem.tag.getInt("limitCount");
        }
        if(showItem.tag.contains("limitTime") ){
            time = (int) (showItem.tag.getLong("limitTime") / (60 * 60 * 1000));
        }
        lore.add(format(Utils.getCentontString("&r&e▶&7库存: &a"+(showItem.stack),length)));
        if(!isInv){
            lore.add(format(Utils.getCentontString("&r&e▶&7限购数量: &a"+limit,length)));
            lore.add(format(Utils.getCentontString("&r&e▶&7刷新时长(h): &a"+time,length)));

            lore.add("  ");
            lore.add(format(Utils.getCentontString("&r&e▶&7双击编辑&e◀",length)));
        }else{
            lore.add(format(Utils.getCentontString("&r&e▶&7双击查看&e◀",length)));
        }


        i.setLore(lore.toArray(new String[0]));

        i.setNamedTag(i.getNamedTag().putInt("index",index));
        return i;
    }

    private String format(String format){
        return TextFormat.colorize('&',format);
    }
}
