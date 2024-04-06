package org.sobadfish.sales.panel;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.item.Item;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.panel.lib.AbstractFakeInventory;
import org.sobadfish.sales.panel.lib.DoubleChestPanel;
import org.sobadfish.sales.panel.lib.IDisplayPanel;

/**
 * @author Sobadfish
 * @date 2024/4/6
 */
public class DisplaySaleInventoryPanel  implements InventoryHolder, IDisplayPanel {

    public AbstractFakeInventory inventory;

    public SalesEntity sales;

    public Item clickedItem;

    public DisplaySaleInventoryPanel(SalesEntity sales,Item clickedItem){
        this.sales = sales;
        this.clickedItem = clickedItem;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }


    @Override
    public void close(){
        if(inventory != null){
            DoubleChestPanel chestPanel = (DoubleChestPanel) inventory;
            chestPanel.close(chestPanel.getPlayer());
        }
    }


    public void displayPlayer(Player player, String name){

        DoubleChestPanel panel = new DoubleChestPanel(clickedItem,sales,player,this,name);
        panel.sales = sales;
        panel.setPanel(sales.getItemInventoryByItem(clickedItem));
        panel.id = ++Entity.entityCount;
        inventory = panel;
        sales.clickInvPlayers.put(player.getName(),panel);
        panel.getPlayer().addWindow(panel);


    }

    @Override
    public SalesEntity getSales() {
        return sales;
    }

    @Override
    public void open(Player player) {
        displayPlayer(player,"库存");
    }
}
