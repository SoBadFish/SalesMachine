package org.sobadfish.sales.items;

import cn.nukkit.item.customitem.CustomItemDefinition;
import cn.nukkit.item.customitem.ItemCustom;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;

/**
 * @author Sobadfish
 * @date 2023/11/27
 */
public class CustomSaleDiscountItem extends ItemCustom {

    /**
     * 使用标签 标志着应用的售货机 uuid 或 店主名称
     * */
    public static final String USE_TAG = "use";

    /**
     * 标志着这是通用优惠券 可以对全部售货机使用
     * */
    public static final String USE_NONE_TAG = "use-none";

    /**
     * 标志着仅对某一个物品优惠
     * */
    public static final String USE_ONLY_ITEM_TAG = "use-only-item";

    /**
     * 标志着有效期 -1未不限制
     * */
    public static final String USE_TIME_TAG = "use_time";

    /**
     * 标志着折扣
     * */
    public static final String USE_ZK_TAG = "zk";

    /**
     * 标志着创建时间
     * */
    public static final String CRETE_TIME_TAG = "create_time";

    /**
     * use
     * */

    public CustomSaleDiscountItem() {
        super("minecraft:sale_discount", "空白优惠券", "sale_discount");
    }


    @Override
    public CustomItemDefinition getDefinition() {
        return CustomItemDefinition.customBuilder(this, CreativeItemCategory.ITEMS).build();
    }

}
