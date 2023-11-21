package org.sobadfish.sales.config;

import cn.nukkit.item.Item;

import java.util.List;

/**
 * @author Sobadfish
 * @date 2023/11/18
 */
public class SaleInfoConfig {

    /**
     * 名称
     * */
    public String name;

    /**
     * 位置
     * */
    public String position;

    /**
     * 朝向
     * */
    public String face;

    /**
     * 物品
     * */
    public List<Item> items;

    public SaleInfoConfig(String name, String position, String face, List<Item> items){
        this.name = name;
        this.position = position;
        this.face = face;
        this.items = items;
    }


    

}
