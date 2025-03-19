package org.sobadfish.sales.form;

import cn.nukkit.Player;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.window.FormWindow;
import org.sobadfish.sales.Utils;

import java.util.LinkedHashMap;

/**
 * @author Sobadfish
 * @date 2024/4/28
 */
public abstract class AbstractSaleForm {

    public int getId() {
        return id;
    }

    public static LinkedHashMap<String, AbstractSaleForm> DISPLAY_FROM = new LinkedHashMap<>();

    private final int id;

    private static int getRid(){
        return Utils.rand(1293000,3233000);
    }

    public AbstractSaleForm(){
        id = getRid();
    }


    public void display(Player player){
        FormWindow formWindow = getForm(player);
        player.showFormWindow(formWindow,id);
        DISPLAY_FROM.put(player.getName(),this);
    }

    /**
     * 获取表单
     * @param player 玩家
     * @return 构建的表单
     * */
    abstract public FormWindow getForm(Player player);


    /**
     * 监听表单
     * @param player 玩家
     * @param  responseCustom 表单
     * */
    abstract public void onListener(Player player, FormResponse responseCustom);
}
