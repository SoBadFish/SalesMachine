package org.sobadfish.sales;


import cn.nukkit.block.Block;

import cn.nukkit.block.BlockBarrier;
import cn.nukkit.inventory.CraftingManager;
import cn.nukkit.inventory.ShapedRecipe;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.Plugin;

import cn.nukkit.utils.Config;

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

    public BlockBarrier iBarrier = new BlockBarrier();

    public Plugin plugin;

    public String coreName = "";

    public RegisterItemServices(Plugin plugin) {
        this.plugin = plugin;
    }

    public static LinkedHashMap<String,Item> CUSTOM_ITEMS = new LinkedHashMap<>();

    public void registerItem(){
        initItem();

    }

    public Config config;

    public void setCoreName(String coreName) {
        this.coreName = coreName;
    }



    public void registerBlock(){
//        if(Block.list.length <= 256){

//            iBarrier = new BarrierBlock_Nukkit();
//            //TODO 放弃了 使用Nkx后好多都没法用 比如实体点击不到
////            sendMessageToConsole("&c当前核心不支持此插件！");
////            this.getServer().getPluginManager().disablePlugin(this);
////            return;
//
//        }else{
//            Block.list[416] = BarrierBlock.class;
//            iBarrier = new BarrierBlock();
//        }
    }

    private void initItem() {


//        Registries.ITEM.registerCustomItem(plugin,CustomV1SaleItem.class);
            Item.registerCustomItem(CustomV1SaleItem.class);
            Item.registerCustomItem(CustomV2SaleItem.class);
            Item.registerCustomItem(CustomV3SaleItem.class);
            Item.registerCustomItem(CustomV4SaleItem.class);
            Item.registerCustomItem(CustomV5SaleItem.class);
            Item.registerCustomItem(CustomV6SaleItem.class);
            //

            Item.registerCustomItem(CustomSaleSettingItem.class);
            Item.registerCustomItem(CustomSaleRemoveItem.class);
            Item.registerCustomItem(CustomSaleMoneyItem.class);

            Item.registerCustomItem(CustomCtItem.class);


            Item.registerCustomItem(CustomV1CtSaleItem.class);
            Item.registerCustomItem(CustomV2CtSaleItem.class);
            Item.registerCustomItem(CustomV3CtSaleItem.class);
            Item.registerCustomItem(CustomV4CtSaleItem.class);
            Item.registerCustomItem(CustomV5CtSaleItem.class);
            Item.registerCustomItem(CustomV6CtSaleItem.class);
            Item.registerCustomItem(CustomWrench.class);

            Item.registerCustomItem(CtChestItem.class);

            Item.registerCustomItem(CustomSalePanelLeftItem.class);
            Item.registerCustomItem(CustomSalePanelRightItem.class);
            Item.registerCustomItem(CustomSaleDiscountItem.class);

            Item.registerCustomItem(CustomSalePanelWallItem.class);

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
            CUSTOM_ITEMS.put("ct_chest",new CtChestItem());


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
        manager.registerRecipe(result);
        manager.rebuildPacket();
    }
}
