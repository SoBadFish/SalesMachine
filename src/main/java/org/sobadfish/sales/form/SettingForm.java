package org.sobadfish.sales.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.entity.SalesEntity;

/**
 * @author Sobadfish
 * @date 2025/3/19
 */
public class SettingForm extends AbstractSaleForm{


    public SalesEntity salesEntity;

    public SettingForm(SalesEntity entity) {
        super();
        this.salesEntity = entity;
    }


    @Override
    public FormWindow getForm(Player player) {
        FormWindowCustom custom = new FormWindowCustom("售货机 ————— 设置");
        custom.addElement(new ElementInput("售货机名称","请输入售货机名称",salesEntity.salesData.customname));
        custom.addElement(new ElementToggle("是否允许交易", salesEntity.salesData.lock == 0));
        if(salesEntity.salesData.netuse == 1) {
            custom.addElement(new ElementToggle("是否被手机搜索", salesEntity.salesData.net == 1));
            custom.addElement(new ElementInput("网店介绍", "描述一下你卖的商品", salesEntity.salesData.netinfo));
        }
        return custom;
    }

    @Override
    public void onListener(Player player, FormResponse responseCustom) {
        if(responseCustom instanceof FormResponseCustom response) {
            salesEntity.salesData.customname = response.getInputResponse(0);
            salesEntity.salesData.lock = (response.getToggleResponse(1) ? 0 : 1);;
            if(salesEntity.salesData.netuse == 1) {
                salesEntity.salesData.net = (response.getToggleResponse(2) ? 1 : 0);
                salesEntity.salesData.netinfo = response.getInputResponse(3);
            }
            salesEntity.saveData();
            SalesMainClass.sendMessageToObject("&a设置成功", player);
        }

    }
}
