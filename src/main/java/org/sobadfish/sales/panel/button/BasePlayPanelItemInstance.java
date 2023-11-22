package org.sobadfish.sales.panel.button;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import org.sobadfish.sales.panel.lib.ChestPanel;


/**
 * @author SoBadFish
 * 2022/1/2
 */
public abstract class BasePlayPanelItemInstance {

    
    /**
     * 消费数量
     * @return 数量
     * */
    public abstract int getCount();
    /**
     * 游戏内物品
     * @return 物品
     * */
    public abstract Item getItem();
    /**
     * 当玩家触发
     *
     * @param inventory 商店
     * @param player 玩家
     *
     * */
    public abstract void onClick(ChestPanel inventory, Player player);


    /**
     * 展示物品
     * @param index 位置
     * @param info 玩家信息
     * @return 物品
     *
     * */
    public abstract Item getPanelItem(Player info, int index);



    @Override
    public String toString() {
        return getItem()+" count: "+getCount();
    }
}
