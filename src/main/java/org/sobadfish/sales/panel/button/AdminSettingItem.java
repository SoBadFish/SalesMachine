package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.items.CustomSaleSettingItem;
import org.sobadfish.sales.items.SaleItem;
import org.sobadfish.sales.panel.lib.ChestPanel;

import java.util.LinkedHashMap;

/**
 * @author Sobadfish
 * @date 2023/11/22
 */
public class AdminSettingItem extends BasePlayPanelItemInstance{

    public int click = 0;

    public SalesEntity sales;

    public AdminSettingItem(SalesEntity sales){
        this.sales = sales;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Item getItem() {
        return new CustomSaleSettingItem();
    }

    @Override
    public void onClick(ChestPanel inventory, Player player) {
        if(click == 0){
            click++;
            Server.getInstance().getScheduler().scheduleDelayedTask(() -> click = 0,40);
        }else {
            LinkedHashMap<Integer,BasePlayPanelItemInstance> items = new LinkedHashMap<>();
            int i = 0;
            for(SaleItem item: sales.items){
                items.put(i++, new PanelSettingItem(item));

            }
            inventory.setPanel(items);

        }
    }

    @Override
    public Item getPanelItem(Player info, int index) {
        Item item = getItem();
        item.setCustomName(TextFormat.colorize('&',"&r&l&c管理"));
        item.setLore(TextFormat.colorize('&',"&r&7&l\n管理此售卖机"));
        item.setNamedTag(item.getNamedTag().putInt("index",index));
        return item;

    }
}
