package org.sobadfish.sales.panel;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.items.SaleItem;
import org.sobadfish.sales.panel.button.AdminSettingItem;
import org.sobadfish.sales.panel.button.BasePlayPanelItemInstance;
import org.sobadfish.sales.panel.button.PanelItem;
import org.sobadfish.sales.panel.button.RemoveSales;
import org.sobadfish.sales.panel.lib.AbstractFakeInventory;
import org.sobadfish.sales.panel.lib.ChestPanel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sobadfish
 * @date 2023/11/20
 */
public class DisplayPlayerPanel implements InventoryHolder {

    public AbstractFakeInventory inventory;

    public SalesEntity sales;

    public DisplayPlayerPanel(SalesEntity sales){
        this.sales = sales;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player){

        LinkedHashMap<Integer,BasePlayPanelItemInstance> items = new LinkedHashMap<>();
        int i = 0;
        for(SaleItem item: new ArrayList<>(sales.items)){
            if(item.isRemove){
                sales.items.remove(item);
                continue;
            }
            items.put(i++, new PanelItem(item));

        }
        if(player.isOp() || (sales.master != null && sales.master.equalsIgnoreCase(player.getName()))){
            i = InventoryType.CHEST.getDefaultSize() - 1;

            items.put(i,new RemoveSales());

        }
        if(player.isOp()){
            items.put(i - 1,new AdminSettingItem(sales));
        }


        displayPlayer(player,items,"售货机");
    }

    public void close(){
        if(inventory != null){
            ChestPanel chestPanel = (ChestPanel) inventory;
            chestPanel.close(chestPanel.getPlayer());
        }
    }


    public void displayPlayer(Player player, Map<Integer, BasePlayPanelItemInstance> itemMap, String name){
        ChestPanel panel = new ChestPanel(player,this,name);
        panel.sales = sales;
        panel.setPanel(itemMap);
        panel.id = ++Entity.entityCount;
        inventory = panel;

        panel.getPlayer().addWindow(panel);


    }
}
