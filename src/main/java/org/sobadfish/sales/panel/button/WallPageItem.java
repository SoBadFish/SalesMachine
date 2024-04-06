package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.panel.lib.ISalePanel;

/**
 * @author Sobadfish
 * @date 2024/4/6
 */
public class WallPageItem extends BasePlayPanelItemInstance{

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Item getItem() {
        return SalesMainClass.CUSTOM_ITEMS.get("wall");
    }

    @Override
    public void onClick(ISalePanel inventory, Player player) {
    }

    @Override
    public Item getPanelItem(Player info, int index) {
        Item tools = getItem();
        tools.setCustomName(TextFormat.colorize('&',"&r&l&a无"));
        tools.setLore(TextFormat.colorize('&',"&r&7&l\n暂无"));
        tools.setNamedTag(tools.getNamedTag().putInt("index",index));
        return tools;
    }
}
