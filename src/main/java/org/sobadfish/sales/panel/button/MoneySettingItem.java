package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.RegisterItemServices;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.items.SaleItem;
import org.sobadfish.sales.panel.lib.ChestPanel;
import org.sobadfish.sales.panel.lib.ISalePanel;

import java.util.LinkedHashMap;

/**
 * @author Sobadfish
 * @date 2024/8/1
 */
public class MoneySettingItem  extends BasePlayPanelItemInstance{

    public int click = 0;

    public SalesEntity sales;

    public MoneySettingItem(SalesEntity sales){
        this.sales = sales;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Item getItem() {

        return RegisterItemServices.CUSTOM_ITEMS.get("money");
    }

    @Override
    public void onClick(ISalePanel inventory, Player player) {
        if(click == 0){
            click++;
            Server.getInstance().getScheduler().scheduleDelayedTask(SalesMainClass.INSTANCE,() -> click = 0,40);
        }else {
            LinkedHashMap<Integer,BasePlayPanelItemInstance> items = new LinkedHashMap<>();
            int i = 0;
            for(SaleItem item: sales.items){
                if(!item.visable){
                    items.put(i++, new PanelSettingItem(item,true));
                }
            }
            ((ChestPanel)inventory).setPanel(items);
        }
    }

    @Override
    public Item getPanelItem(Player info, int index) {
        Item item = getItem();
        item.setCustomName(TextFormat.colorize('&',"&r&e钱罐"));
        item.setLore(TextFormat.colorize('&',"&r&7用于存储收购获得的物品货币\n&r&7也可用作消耗&c(仅限物品货币)"));
        item.setNamedTag(item.getNamedTag().putInt("index",index));
        return item;
    }
}
