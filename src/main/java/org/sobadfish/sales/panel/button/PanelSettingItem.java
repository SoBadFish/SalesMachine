package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import org.sobadfish.sales.form.AdminForm;
import org.sobadfish.sales.items.SaleItem;
import org.sobadfish.sales.panel.lib.ChestPanel;

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
        i.setNamedTag(i.getNamedTag().putInt("index",index));
        return i;
    }
}
