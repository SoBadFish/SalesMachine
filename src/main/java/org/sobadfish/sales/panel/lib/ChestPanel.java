package org.sobadfish.sales.panel.lib;

import cn.nukkit.Player;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.ContainerOpenPacket;
import cn.nukkit.network.protocol.RemoveEntityPacket;
import org.sobadfish.sales.SalesListener;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.panel.button.BasePlayPanelItemInstance;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sobadfish
 * @date 2023/11/18
 */
public class ChestPanel extends DoubleChestFakeInventory implements InventoryHolder,ISalePanel {

    public long id;

    public SalesEntity sales;

    private final Player player;

    public int clickSolt;



    private Map<Integer, BasePlayPanelItemInstance> panel = new LinkedHashMap<>();

    public ChestPanel(Player player, InventoryHolder holder, String name) {
        super(holder,name);
        this.player = player;
        this.setName(name);
    }

    public void setPanel(Map<Integer, BasePlayPanelItemInstance> panel){
        Map<Integer, BasePlayPanelItemInstance> m = new LinkedHashMap<>();
        LinkedHashMap<Integer, Item> map = new LinkedHashMap<>();
        for(Map.Entry<Integer,BasePlayPanelItemInstance> entry : panel.entrySet()){
            Item value = entry.getValue().getPanelItem(getPlayerInfo(),entry.getKey()).clone();
            map.put(entry.getKey(),value);
            m.put(entry.getKey(),entry.getValue());
        }
        setContents(map);
        this.panel = m;
    }

    public void update(){
        setPanel(panel);
    }

    @Override
    public SalesEntity getSales() {
        return sales;
    }

    public Player getPlayerInfo(){
        return player;
    }

    public Player getPlayer() {
        return (Player) player.getPlayer();
    }

    public Map<Integer, BasePlayPanelItemInstance> getPanel() {
        return panel;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public void onSlotChange(int index, Item before, boolean send) {
        super.onSlotChange(index, before, send);
    }


    @Override
    public void onOpen(Player who) {
        super.onOpen(who);
        ContainerOpenPacket pk = new ContainerOpenPacket();
        pk.windowId = who.getWindowId(this);
        pk.entityId = id;
        pk.type = InventoryType.DOUBLE_CHEST.getNetworkType();
        who.dataPacket(pk);
        sales.openIt(who);
        if(!sales.clickPlayers.containsKey(who.getName())){
            sales.clickPlayers.put(who.getName(),this);
        }



    }

    @Override
    public void onClose(Player who) {
        RemoveEntityPacket pk = new RemoveEntityPacket();
        pk.eid = id;
        who.dataPacket(pk);
        super.onClose(who);
        //关闭

        if(sales.clickPlayers.size() <= 1){
            sales.closeIt(who);
        }
        sales.clickPlayers.remove(who.getName());
        SalesListener.chestPanelLinkedHashMap.remove(who.getName());

    }

    @Override
    public Inventory getInventory() {
        return this;
    }


}
