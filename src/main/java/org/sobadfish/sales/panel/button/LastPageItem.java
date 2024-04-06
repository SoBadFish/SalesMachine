package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.panel.lib.DoubleChestPanel;
import org.sobadfish.sales.panel.lib.ISalePanel;

/**
 * @author Sobadfish
 * @date 2024/4/6
 */
public class LastPageItem extends BasePlayPanelItemInstance{
    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Item getItem() {
        return SalesMainClass.CUSTOM_ITEMS.get("left");
    }

    @Override
    public void onClick(ISalePanel inventory, Player player) {
        ((DoubleChestPanel)inventory).update((Inventory) inventory);
        ((DoubleChestPanel)inventory).page--;

        ((DoubleChestPanel)inventory).update(false);
    }


    @Override
    public Item getPanelItem(Player info, int index) {
        Item tools = getItem();
        tools.setCustomName(TextFormat.colorize('&',"&r&l&a上一页"));
        tools.setLore(TextFormat.colorize('&',"&r&7&l\n翻到上一页"));
        tools.setNamedTag(tools.getNamedTag().putInt("index",index));
        return tools;
    }
}
