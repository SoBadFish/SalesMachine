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
import org.sobadfish.sales.panel.button.LastPageItem;
import org.sobadfish.sales.panel.button.NextPageItem;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sobadfish
 * @date 2024/4/6
 */
public class DoubleChestPanel extends DoubleChestFakeInventory implements InventoryHolder,ISalePanel {

    public long id;

    public SalesEntity sales;

    private final Player player;


    public int page = 1;

    public int pageSize = 0;



    private SalesEntity.ItemStack items;

    public Item choseItem;

    private final Map<Integer, BasePlayPanelItemInstance> panel = new LinkedHashMap<>();


    public DoubleChestPanel(Item choseItem,SalesEntity sales,Player player, InventoryHolder holder, String name) {
        super(holder,name);
        this.player = player;
        this.choseItem = choseItem;
        this.sales = sales;
        this.setName(name);

    }

    @Override
    public SalesEntity getSales() {
        return sales;
    }

    @Override
    public void onOpen(Player who) {
        super.onOpen(who);
        ContainerOpenPacket pk = new ContainerOpenPacket();
        pk.windowId = who.getWindowId(this);
        pk.entityId = id;
        pk.type = InventoryType.DOUBLE_CHEST.getNetworkType();
        who.dataPacket(pk);

    }


    //
    public void setPanel(SalesEntity.ItemStack items){
        Map<Integer,Item> map = new LinkedHashMap<>();
        if(items.pageSize > (InventoryType.DOUBLE_CHEST.getDefaultSize() - 1)){
            //首页不用显示墙
            if(page == 1){
                int ps = 0;
                int i = 0;
                for(; i < InventoryType.DOUBLE_CHEST.getDefaultSize() - 1; i++){
                    map.put(i,items.item);
                    ps += items.item.getCount();
                }
                pageSize = ps;
                NextPageItem nx = new NextPageItem();
                panel.put(i,nx);
                map.put(i,nx.getPanelItem(getPlayerInfo(),i));

            }else{
                int ip = (items.pageSize - ((InventoryType.DOUBLE_CHEST.getDefaultSize() - 1) * (page - 1)));
                if(ip > (InventoryType.DOUBLE_CHEST.getDefaultSize() - 1)){
                    //还能往下翻
                    int i = 0;
                    int ps = 0;
                    LastPageItem lx = new LastPageItem();
                    map.put(i,lx.getPanelItem(getPlayerInfo(),i));
                    panel.put(i++,lx);
                    for(; i < InventoryType.DOUBLE_CHEST.getDefaultSize() - 1; i++){
                        map.put(i,items.item);
                        ps += items.item.getCount();
                    }
                    pageSize = ps;
                    NextPageItem nx = new NextPageItem();
                    panel.put(i,nx);
                    map.put(i,nx.getPanelItem(getPlayerInfo(),i));

                }else{
                    //只能往上翻
                    int i = 0;
                    int ps = 0;
                    LastPageItem lx = new LastPageItem();
                    map.put(i,lx.getPanelItem(getPlayerInfo(),i));
                    panel.put(i++,lx);
                    for(; i < ip - 1; i++){
                        map.put(i,items.item);
                        ps += items.item.getCount();
                    }
                    Item cl2 = items.item.clone();
                    cl2.setCount(items.endCount);
                    map.put(i,cl2);
                    ps += cl2.getCount();

                    pageSize = ps;
                }
            }

        }else{
            int i = 0;
            int ps = 0;
            for(; i < items.pageSize - 1; i++){
                map.put(i,items.item);
                ps += items.item.getCount();
            }
            Item cl2 = items.item.clone();
            cl2.setCount(items.endCount);
            map.put(i,cl2);
            ps += items.endCount;

            pageSize = ps;
        }
        setContents(map);
        this.items = items;

    }



    public Map<Integer, BasePlayPanelItemInstance> getPanel() {
        return panel;
    }

    public void update(Inventory inventory){
        int count = 0;
        for (Item item : inventory.getContents().values()) {
            if(item.equals(choseItem,true,true)){
                count += item.getCount();
            }
        }

        //获取当前页数
        sales.setItem(choseItem,count,pageSize);

    }

    public void update(boolean reset){
        if(reset){
            page = 1;
        }
        setPanel(items);
    }



    public Player getPlayerInfo(){
        return player;
    }

    public Player getPlayer() {
        return (Player) player.getPlayer();
    }




    @Override
    public void onClose(Player who) {
        update(this);
        clearAll();
        RemoveEntityPacket pk = new RemoveEntityPacket();
        pk.eid = id;
        who.dataPacket(pk);
        super.onClose(who);
        //关闭
        sales.clickInvPlayers.remove(who.getName());
        SalesListener.chestPanelLinkedHashMap.remove(who.getName());


    }

    @Override
    public Inventory getInventory() {
        return this;
    }

    public void updateItem() {
        this.items = sales.getItemInventoryByItem(choseItem);
    }
}
