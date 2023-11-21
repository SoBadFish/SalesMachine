package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.block.BarrierBlock;
import org.sobadfish.sales.panel.lib.ChestPanel;

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
        return null;
    }

    @Override
    public void onClick(ChestPanel inventory, Player player) {
        if(click == 0){
            click++;
            Server.getInstance().getScheduler().scheduleDelayedTask(() -> click = 0,40);
        }else{
            inventory.onClose(player);
            inventory.sales.level.dropItem(inventory.sales,new BarrierBlock().getShaleItem());
            inventory.sales.toClose();

        }

    }

    @Override
    public Item getPanelItem(Player info, int index) {
        Item tools = Item.get(35,14);
        tools.setCustomName(TextFormat.colorize('&',"&r&c&l拆除"));
        tools.setLore(TextFormat.colorize('&',"&r&7&l\n拆除此售卖机"));
        tools.setNamedTag(tools.getNamedTag().putInt("index",index));
        return tools;

    }
}
