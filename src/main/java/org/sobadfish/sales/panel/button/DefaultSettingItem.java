package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.RegisterItemServices;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.form.SettingForm;
import org.sobadfish.sales.panel.lib.ChestPanel;
import org.sobadfish.sales.panel.lib.ISalePanel;

/**
 * @author Sobadfish
 * @date 2025/3/19
 */
public class DefaultSettingItem extends BasePlayPanelItemInstance{

    public int click = 0;

    public SalesEntity sales;

    public DefaultSettingItem(SalesEntity sales){
        this.sales = sales;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Item getItem() {
        return RegisterItemServices.CUSTOM_ITEMS.get("defsetting");
    }

    @Override
    public void onClick(ISalePanel inventory, Player player) {
        if(click == 0){
            click++;
            Server.getInstance().getScheduler().scheduleDelayedTask(SalesMainClass.INSTANCE,() -> click = 0,40);
        }else {
            ((ChestPanel)inventory).close(player);
            Server.getInstance().getScheduler().scheduleDelayedTask(SalesMainClass.INSTANCE,() -> {
                SettingForm settingForm = new SettingForm(sales);
                settingForm.display(player);
            },10);
        }
    }

    @Override
    public Item getPanelItem(Player info, int index) {
        Item item = getItem();
        item.setCustomName(TextFormat.colorize('&',"&r&l设置"));
        item.setLore(TextFormat.colorize('&',"&r&7&l\n设置此售货机"));
        item.setNamedTag(item.getNamedTag().putInt("index",index));
        return item;

    }
}
