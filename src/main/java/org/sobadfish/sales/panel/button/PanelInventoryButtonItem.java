package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.BlockChest;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.items.SaleItem;
import org.sobadfish.sales.panel.lib.ChestPanel;
import org.sobadfish.sales.panel.lib.ISalePanel;

import java.util.LinkedHashMap;

/**
 * @author Sobadfish
 * @date 2024/4/6
 */
public class PanelInventoryButtonItem extends BasePlayPanelItemInstance{
    public int click = 0;

    public SalesEntity sales;

    public PanelInventoryButtonItem(SalesEntity sales){
        this.sales = sales;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Item getItem() {
        return new BlockChest().toItem();
    }

    @Override
    public void onClick(ISalePanel inventory, Player player) {
        if(click == 0){
            click++;
            Server.getInstance().getScheduler().scheduleDelayedTask(() -> click = 0,40);
        }else {
            LinkedHashMap<Integer,BasePlayPanelItemInstance> items = new LinkedHashMap<>();
            int i = 0;
            for(SaleItem item: sales.items){
                items.put(i++, new PanelSettingItem(item,true));

            }
            ((ChestPanel)inventory).setPanel(items);

        }
    }

    @Override
    public Item getPanelItem(Player info, int index) {
        Item item = getItem();
        item.setCustomName(TextFormat.colorize('&',"&r&l&e库存"));
        item.setLore(TextFormat.colorize('&',"&r&7&l\n查看库存"));
        item.setNamedTag(item.getNamedTag().putInt("index",index));
        return item;

    }

}
