package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.RegisterItemServices;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.panel.lib.ChestPanel;
import org.sobadfish.sales.panel.lib.ISalePanel;


/**
 * @author Sobadfish
 * @date 2023/11/21
 */
public class RemoveSales extends BasePlayPanelItemInstance{

    public int click = 0;

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Item getItem() {
        return RegisterItemServices.CUSTOM_ITEMS.get("remove");
    }

    @Override
    public void onClick(ISalePanel inventory, Player player) {
        if(click == 0){
            click++;
            Server.getInstance().getScheduler().scheduleDelayedTask(SalesMainClass.INSTANCE,() -> click = 0,40);
        }else{
            ((ChestPanel)inventory).onClose(player);
            Item item =  ((ChestPanel)inventory).sales.getShaleItem();
            item.setCount(1);
            ((ChestPanel)inventory).sales.level.dropItem(((ChestPanel)inventory).sales,item);
            ((ChestPanel)inventory).sales.toClose();

        }

    }

    @Override
    public Item getPanelItem(Player info, int index) {
        Item tools = getItem();
        tools.setCustomName(TextFormat.colorize('&',"&r&c&l拆除"));
        tools.setLore(TextFormat.colorize('&',"&r&7&l\n拆除此售货机"));
        tools.setNamedTag(tools.getNamedTag().putInt("index",index));
        return tools;

    }
}
