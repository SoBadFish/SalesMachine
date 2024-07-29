package org.sobadfish.sales;

import cn.lanink.customitemapi.CustomItemAPI;
import cn.nukkit.block.Block;
import cn.nukkit.inventory.CraftingManager;
import cn.nukkit.inventory.ShapedRecipe;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import org.sobadfish.sales.block.BarrierBlock;
import org.sobadfish.sales.block.BarrierBlock_Nukkit;
import org.sobadfish.sales.block.IBarrier;
import org.sobadfish.sales.items.*;
import org.sobadfish.sales.items.ct.*;
import org.sobadfish.sales.items.sales.*;
import org.sobadfish.sales.panel.lib.AbstractFakeInventory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 尝试兼容PNX 结果发现PNX改动过大...
 * @author Sobadfish
 * @date 2024/7/29
 */
public class RegisterItemServices {

    public static boolean LOAD_CUSTOM = false;

    public IBarrier iBarrier;

    public String coreName = "";

    public static LinkedHashMap<String,Item> CUSTOM_ITEMS = new LinkedHashMap<>();

    public void registerItem(){
        initItem();

    }

    public Config config;

    public void setCoreName(String coreName) {
        this.coreName = coreName;
    }



    public void registerBlock(){
        if(Block.list.length <= 256){
            iBarrier = new BarrierBlock_Nukkit();
            //TODO 放弃了 使用Nkx后好多都没法用 比如实体点击不到
//            sendMessageToConsole("&c当前核心不支持此插件！");
//            this.getServer().getPluginManager().disablePlugin(this);
//            return;

        }else{
            Block.list[416] = BarrierBlock.class;
            iBarrier = new BarrierBlock();
        }
    }

    private void initItem() {
        try{
            Class.forName("cn.lanink.customitemapi.item.ItemCustom");
            LOAD_CUSTOM = true;
        }catch (Exception ignore){}
        if(LOAD_CUSTOM){


            CustomItemAPI.getInstance().registerCustomItem(1993, org.sobadfish.sales.items.custom.CustomSaleSettingItem.class);
            CustomItemAPI.getInstance().registerCustomItem(1994, org.sobadfish.sales.items.custom.CustomSaleRemoveItem.class);
            CustomItemAPI.getInstance().registerCustomItem(1995, org.sobadfish.sales.items.custom.CustomSaleMoneyItem.class);

            CustomItemAPI.getInstance().registerCustomItem(1996, org.sobadfish.sales.items.custom.CustomCtItem.class);

            CustomItemAPI.getInstance().registerCustomItem(1998, org.sobadfish.sales.items.custom.CustomWrench.class);

            CustomItemAPI.getInstance().registerCustomItem(1999, org.sobadfish.sales.items.custom.CustomSalePanelLeftItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2000, org.sobadfish.sales.items.custom.CustomSalePanelRightItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2001, org.sobadfish.sales.items.custom.CustomSaleDiscountItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2002, org.sobadfish.sales.items.custom.CustomSalePanelWallItem.class);


            CustomItemAPI.getInstance().registerCustomItem(2003, org.sobadfish.sales.items.custom.sales.CustomV1SaleItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2004, org.sobadfish.sales.items.custom.sales.CustomV2SaleItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2005, org.sobadfish.sales.items.custom.sales.CustomV3SaleItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2006, org.sobadfish.sales.items.custom.sales.CustomV4SaleItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2007, org.sobadfish.sales.items.custom.sales.CustomV5SaleItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2008, org.sobadfish.sales.items.custom.sales.CustomV6SaleItem.class);

            CustomItemAPI.getInstance().registerCustomItem(2009, org.sobadfish.sales.items.custom.ct.CustomV1CtSaleItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2010, org.sobadfish.sales.items.custom.ct.CustomV2CtSaleItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2011, org.sobadfish.sales.items.custom.ct.CustomV3CtSaleItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2012, org.sobadfish.sales.items.custom.ct.CustomV4CtSaleItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2013, org.sobadfish.sales.items.custom.ct.CustomV5CtSaleItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2014, org.sobadfish.sales.items.custom.ct.CustomV6CtSaleItem.class);


            CUSTOM_ITEMS.put("sale_v1",new  org.sobadfish.sales.items.custom.sales.CustomV1SaleItem());
            CUSTOM_ITEMS.put("sale_v2",new  org.sobadfish.sales.items.custom.sales.CustomV2SaleItem());
            CUSTOM_ITEMS.put("sale_v3",new  org.sobadfish.sales.items.custom.sales.CustomV3SaleItem());
            CUSTOM_ITEMS.put("sale_v4",new  org.sobadfish.sales.items.custom.sales.CustomV4SaleItem());
            CUSTOM_ITEMS.put("sale_v5",new  org.sobadfish.sales.items.custom.sales.CustomV5SaleItem());
            CUSTOM_ITEMS.put("sale_v6",new  org.sobadfish.sales.items.custom.sales.CustomV6SaleItem());

            CUSTOM_ITEMS.put("ct_sale_v1",new org.sobadfish.sales.items.custom.ct.CustomV1CtSaleItem());
            CUSTOM_ITEMS.put("ct_sale_v2",new org.sobadfish.sales.items.custom.ct.CustomV2CtSaleItem());
            CUSTOM_ITEMS.put("ct_sale_v3",new org.sobadfish.sales.items.custom.ct.CustomV3CtSaleItem());
            CUSTOM_ITEMS.put("ct_sale_v4",new org.sobadfish.sales.items.custom.ct.CustomV4CtSaleItem());
            CUSTOM_ITEMS.put("ct_sale_v5",new org.sobadfish.sales.items.custom.ct.CustomV5CtSaleItem());
            CUSTOM_ITEMS.put("ct_sale_v6",new org.sobadfish.sales.items.custom.ct.CustomV6CtSaleItem());


            CUSTOM_ITEMS.put("setting",new org.sobadfish.sales.items.custom.CustomSaleSettingItem());
            CUSTOM_ITEMS.put("remove",new org.sobadfish.sales.items.custom.CustomSaleRemoveItem());
            CUSTOM_ITEMS.put("money",new org.sobadfish.sales.items.custom.CustomSaleMoneyItem());
            CUSTOM_ITEMS.put("ct",new org.sobadfish.sales.items.custom.CustomCtItem());

            CUSTOM_ITEMS.put("pipe_wrench",new org.sobadfish.sales.items.custom.CustomWrench());

            CUSTOM_ITEMS.put("left",new org.sobadfish.sales.items.custom.CustomSalePanelLeftItem());
            CUSTOM_ITEMS.put("right",new org.sobadfish.sales.items.custom.CustomSalePanelRightItem());
            CUSTOM_ITEMS.put("discount",new org.sobadfish.sales.items.custom.CustomSaleDiscountItem());

            CUSTOM_ITEMS.put("wall",new org.sobadfish.sales.items.custom.CustomSalePanelWallItem());

        }else{
            Item.registerCustomItem(CustomV1SaleItem.class);
            Item.registerCustomItem(CustomV2SaleItem.class);
            Item.registerCustomItem(CustomV3SaleItem.class);
            Item.registerCustomItem(CustomV4SaleItem.class);
            Item.registerCustomItem(CustomV5SaleItem.class);
            Item.registerCustomItem(CustomV6SaleItem.class);
            //

            Item.registerCustomItem(CustomSaleSettingItem.class,false);
            Item.registerCustomItem(CustomSaleRemoveItem.class,false);
            Item.registerCustomItem(CustomSaleMoneyItem.class);

            Item.registerCustomItem(CustomCtItem.class);
            Item.registerCustomItem(CustomV1CtSaleItem.class,false);
            Item.registerCustomItem(CustomV2CtSaleItem.class,false);
            Item.registerCustomItem(CustomV3CtSaleItem.class,false);
            Item.registerCustomItem(CustomV4CtSaleItem.class,false);
            Item.registerCustomItem(CustomV5CtSaleItem.class,false);
            Item.registerCustomItem(CustomV6CtSaleItem.class,false);
            Item.registerCustomItem(CustomWrench.class);

            Item.registerCustomItem(CustomSalePanelLeftItem.class,false);
            Item.registerCustomItem(CustomSalePanelRightItem.class,false);
            Item.registerCustomItem(CustomSaleDiscountItem.class);

            Item.registerCustomItem(CustomSalePanelWallItem.class,false);

            CUSTOM_ITEMS.put("sale_v1",new CustomV1SaleItem());
            CUSTOM_ITEMS.put("sale_v2",new CustomV2SaleItem());
            CUSTOM_ITEMS.put("sale_v3",new CustomV3SaleItem());
            CUSTOM_ITEMS.put("sale_v4",new CustomV4SaleItem());
            CUSTOM_ITEMS.put("sale_v5",new CustomV5SaleItem());
            CUSTOM_ITEMS.put("sale_v6",new CustomV6SaleItem());

            CUSTOM_ITEMS.put("ct_sale_v1",new CustomV1CtSaleItem());
            CUSTOM_ITEMS.put("ct_sale_v2",new CustomV2CtSaleItem());
            CUSTOM_ITEMS.put("ct_sale_v3",new CustomV3CtSaleItem());
            CUSTOM_ITEMS.put("ct_sale_v4",new CustomV4CtSaleItem());
            CUSTOM_ITEMS.put("ct_sale_v5",new CustomV5CtSaleItem());
            CUSTOM_ITEMS.put("ct_sale_v6",new CustomV6CtSaleItem());

            CUSTOM_ITEMS.put("setting",new CustomSaleSettingItem());
            CUSTOM_ITEMS.put("remove",new CustomSaleRemoveItem());
            CUSTOM_ITEMS.put("money",new CustomSaleMoneyItem());
            CUSTOM_ITEMS.put("ct",new CustomCtItem());
            CUSTOM_ITEMS.put("pipe_wrench",new CustomWrench());

            CUSTOM_ITEMS.put("left",new CustomSalePanelLeftItem());
            CUSTOM_ITEMS.put("right",new CustomSalePanelRightItem());
            CUSTOM_ITEMS.put("discount",new CustomSaleDiscountItem());
            CUSTOM_ITEMS.put("wall",new CustomSalePanelWallItem());
        }

//        Item.removeCreativeItem(CUSTOM_ITEMS.get("ct_sale"));







    }

    public void registerCraft(){
        registerCraftMot();

    }

    private void registerCraftMot() {
        //注册合成配方 通过这个可以合成优惠券.
        if(config.getBoolean("craft-discount",true)){


            //一张纸合成一个空白优惠券
            Map<Character, Item> ingredients = new HashMap<>();
            ingredients.put('A', Item.get(Item.PAPER));
            ShapedRecipe result = new ShapedRecipe(CUSTOM_ITEMS.get("discount"),new String[]{"AA"},ingredients,new LinkedList<>());

            registerRecipeMot(result);
            //搬运器合成


            SalesMainClass.sendMessageToConsole("&a成功注册 &r"+SalesMainClass.CORE_NAME+" &a核心合成配方");


        }

    }

    private void registerRecipeMot(ShapedRecipe result){
        CraftingManager manager = SalesMainClass.INSTANCE.getServer().getCraftingManager();
        if(AbstractFakeInventory.IS_PM1E){
            manager.registerRecipe(313,result);
            manager.registerRecipe(332,result);
            manager.registerRecipe(388,result);
            manager.registerRecipe(419,result);
            manager.registerRecipe(527,result);
            manager.registerRecipe(649,result);

        }else{
            manager.registerRecipe(result);
        }
        manager.rebuildPacket();
    }
}
