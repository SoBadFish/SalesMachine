package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.Utils;
import org.sobadfish.sales.form.AdminForm;
import org.sobadfish.sales.items.SaleItem;
import org.sobadfish.sales.panel.lib.ChestPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sobadfish
 * @date 2023/11/22
 */
public class PanelSettingItem extends BasePlayPanelItemInstance{
    public int click = 0;

    public SaleItem showItem;



    public PanelSettingItem(SaleItem item){
        this.showItem = item;
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
    public void onClick(ChestPanel inventory, Player player) {
        if(click == 0){
            click++;
            Server.getInstance().getScheduler().scheduleDelayedTask(() -> click = 0,40);
        }else {
            inventory.close(player);
            Server.getInstance().getScheduler().scheduleDelayedTask(() -> {
                AdminForm adminForm = new AdminForm(showItem);
                adminForm.display(player);
            },20);

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
        lore.add(format(Utils.getCentontString("&r&e▶&7 库存: &a"+(showItem.stack),length)));
        lore.add(format(Utils.getCentontString("&r&e▶&7 限购数量: &a"+limit,length)));
        lore.add(format(Utils.getCentontString("&r&e▶&7 刷新时长(h): &a"+time,length)));

        lore.add("  ");
        i.setLore(lore.toArray(new String[0]));

        i.setNamedTag(i.getNamedTag().putInt("index",index));
        return i;
    }

    private String format(String format){
        return TextFormat.colorize('&',format);
    }
}
