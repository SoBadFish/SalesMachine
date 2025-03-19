package org.sobadfish.sales.form;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;

/**
 * @author Sobadfish
 * @date 2025/3/19
 */
public class SearchForm extends AbstractSaleForm{

    public SearchForm() {
        super();
    }
    @Override
    public FormWindow getForm(Player player) {
        FormWindowCustom custom = new FormWindowCustom("查找");
        custom.addElement(new ElementInput("请输入你要查找的信息","查询信息"));
        custom.addElement(new ElementLabel("* 可根据售货机介绍查询"));
        custom.addElement(new ElementLabel("* 可根据店主信息查询"));
        custom.addElement(new ElementLabel("* 可根据商品名称查询"));

        return custom;
    }

    @Override
    public void onListener(Player player, FormResponse responseCustom) {
        if(responseCustom instanceof FormResponseCustom) {
            PhoneForm discountForm = new PhoneForm();
            discountForm.search = ((FormResponseCustom) responseCustom).getInputResponse(0);
            discountForm.display(player);
        }

    }
}
