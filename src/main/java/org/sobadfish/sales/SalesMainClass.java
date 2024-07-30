package org.sobadfish.sales;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.sobadfish.sales.config.ItemData;
import org.sobadfish.sales.config.SaleSettingConfig;
import org.sobadfish.sales.config.SaleSkinConfig;
import org.sobadfish.sales.config.SalesData;
import org.sobadfish.sales.db.SqliteHelper;
import org.sobadfish.sales.economy.IMoney;
import org.sobadfish.sales.economy.core.EconomyMoney;
import org.sobadfish.sales.economy.core.MoneyCoreMoney;
import org.sobadfish.sales.economy.core.PlayerPointsMoney;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.items.CustomSaleDiscountItem;
import org.sobadfish.sales.items.ISaleItem;
import org.sobadfish.sales.panel.lib.AbstractFakeInventory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Sobadfish
 * @date 2023/11/16
 */
public class SalesMainClass extends PluginBase {

    public static LinkedHashMap<String, SaleSkinConfig> ENTITY_SKIN = new LinkedHashMap<>();

    public static final String PLUGIN_NAME = "&7[&e售货机&7]&r";



    public static SalesMainClass INSTANCE;





    public static List<String> banWorlds = new ArrayList<>();

    public static List<String> OnlyUserAdminCore = new ArrayList<>();


    public SqliteHelper sqliteHelper;

    public static final String DB_TABLE = "salelocation";

    private final static LinkedHashMap<String, IMoney> LOAD_MONEY = new LinkedHashMap<>();


    public static boolean canGiveMoneyItem = true;

    public static String CORE_NAME = "";

    /**
     * 注册物品服务
     * */
    public RegisterItemServices services = new RegisterItemServices();

    public LinkedHashMap<String, ItemData> itemInfoData = new LinkedHashMap<>();

    public String[] lists = new String[]{"v1", "v2","v3","v4","v5","v6"};


    @Override
    public void onLoad() {
        INSTANCE = this;
        //提前注册好
        BlockEntity.registerBlockEntity(SalesEntity.SalesBlockEntity.ENTITY_TYPE,SalesEntity.SalesBlockEntity.class);
        Entity.registerEntity(SalesEntity.ENTITY_TYPE,SalesEntity.class);
        checkServer();
        services.setCoreName(CORE_NAME);
        services.registerItem();
    }

    @Override
    public void onEnable() {

        sendMessageToConsole("&a正在加载售货机插件");
        //检查是否支持自定义物品
        boolean load = true;
        try{
            Class.forName("cn.nukkit.item.customitem.CustomItem");
        }catch (Exception ignore){
            try {
                Class.forName("cn.lanink.customitemapi.CustomItemAPI");

            }catch (Exception ignored){
                load = false;
            }

        }
        if(!load){
            sendMessageToConsole("&c当前核心不支持自定义物品！ 推荐配合CustomItemAPI使用！");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        //加载经济核心
        loadMoneyCore();

        saveDefaultConfig();
        services.config = getConfig();
        //加载配置
        loadConfig();

        //加载物品属性
        sendMessageToConsole("&e 正在加载 &r物品数据信息");

        if(loadItemInfo()){
            sendMessageToConsole("&r物品数据信息 &a加载成功!");
        }else{
            sendMessageToConsole("&r物品数据信息 &c加载失败!");
        }




        saveResource("data.db",false);
        try {
            sqliteHelper = new SqliteHelper(getDataFolder()+"/data.db");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        if(sqliteHelper != null){
            if(!sqliteHelper.exists(DB_TABLE)){
                sqliteHelper.addTable(DB_TABLE, SqliteHelper.DBTable.asDbTable(SalesData.class));
            }
        }

        chunkDb();

        initSkin();


        services.registerBlock();






        this.getServer().getPluginManager().registerEvents(new SalesListener(this),this);


        services.registerCraft();
        sendMessageToConsole("&a加载完成!");

    }

    private boolean loadItemInfo() {

        saveResource("ItemInfoData.json",false);
        File file = new File(this.getDataFolder()+"/ItemInfoData.json");
        try {
            FileReader r = new FileReader(file);
            Gson gson = new Gson();
            ArrayList<ItemData> itemData = gson.fromJson(r, new TypeToken<List<ItemData>>() {}.getType());
            //装载到map
            for(ItemData data: itemData){
                itemInfoData.put(data.id+":"+data.damage,data);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;


    }

    public ItemData getItemDataByItem(Item item){
        String str = item.getId()+":"+item.getDamage();
        if(itemInfoData.containsKey(str)){
            return itemInfoData.get(str);
        }
        return null;
    }

    public static String getFirstMoney(){
        return new ArrayList<>(LOAD_MONEY.keySet()).get(0);
    }

    public static IMoney getMoneyCoreByName(String name){
        if(LOAD_MONEY.containsKey(name)){
            return LOAD_MONEY.get(name);
        }
        return new ArrayList<>(LOAD_MONEY.values()).get(0);
    }

    public static LinkedHashMap<String, IMoney> getLoadMoney() {
        return LOAD_MONEY;
    }

    private void loadMoneyCore() {
        if(isEnableMoneyCore(MoneyType.EconomyAPI)){
            registerMoneyCore("economyapi",EconomyMoney.class);

        }
        if(isEnableMoneyCore(MoneyType.Money)){
            registerMoneyCore("Money", MoneyCoreMoney.class);
        }
        if(isEnableMoneyCore(MoneyType.PlayerPoints)){
            registerMoneyCore("playerPoints", PlayerPointsMoney.class);
        }

        if(LOAD_MONEY.size() == 0){
            sendMessageToConsole("&c无任何经济系统!");
        }
    }


    //加载坐标点
    private void loadConfig() {
        banWorlds = getConfig().getStringList("ban-world");
        OnlyUserAdminCore = getConfig().getStringList("only-use-admin-money-core");
        canGiveMoneyItem = getConfig().getBoolean("can-give-money-item",true);
    }


    public static boolean isEnableMoneyCore(MoneyType type){
        switch (type) {
            case Money:
                return Server.getInstance().getPluginManager().getPlugin("Money") != null;
            case PlayerPoints:
                return Server.getInstance().getPluginManager().getPlugin("playerPoints") != null;
            case EconomyAPI:
                return Server.getInstance().getPluginManager().getPlugin("EconomyAPI") != null;
            default:break;
        }
        return false;
    }

    public enum MoneyType{
        /**Economy PlayerPoints Money*/
        EconomyAPI,PlayerPoints,Money
    }



    private void chunkDb(){
        //检查DB
        if(sqliteHelper != null){
            List<String> columns = sqliteHelper.getColumns(DB_TABLE);
            Field[] fd = SalesData.class.getFields();
            for (Field field : fd){
                if(!columns.contains(field.getName())){
                    //新增...
                    sqliteHelper.addColumns(DB_TABLE,field.getName().toLowerCase(),field);
                    getLogger().info("检测到新字段 "+field.getName()+" 正在写入数据库...");

                }
            }
        }

    }



    public static void registerMoneyCore(String moneyName,Class<? extends IMoney> money){
        if(!LOAD_MONEY.containsKey(moneyName)){
            try{
                IMoney my = money.getDeclaredConstructor().newInstance();
                LOAD_MONEY.put(moneyName, my);
                sendMessageToConsole("&a装载经济核心: &r"+moneyName);
                return;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("当前经济核心已存在！");

    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length > 0){
            switch (args[0]){
                case "help":
                    sendMessageToObject("&a/sa help &7查看帮助",sender);
                    sendMessageToObject("&a/sa give [数量] [玩家(可不填)]  &7给予玩家售货机物品 （放置即可）",sender);
                    sendMessageToObject("&a/sa q [玩家（可不填）]  &7查询玩家售货机坐标信息",sender);
                    sendMessageToObject("&a/sa d <折扣> [玩家（可不填）]  &7给予玩家一个通用优惠券",sender);
                    sendMessageToObject("&a/sa b [模型]  &7将手持物品绑定售货机模型 &c(无法绑定售货机/使用后消耗)",sender);
                    StringBuilder stringBuilder = new StringBuilder();
                    for(SaleSkinConfig saleSkinConfig: ENTITY_SKIN.values()){
                        stringBuilder.append(saleSkinConfig.modelName).append(",");
                    }
                    sendMessageToObject("&a模型列表:&r "+stringBuilder,sender);
                    break;
                case "give":
                    int count = 1;
                    Player p = null;
                    if(sender instanceof Player){
                        p = (Player) sender;
                    }
                    if(args.length > 1){
                        try{
                            count = Integer.parseInt(args[1]);
                        }catch (Exception ignore){}

                    }
                    if(args.length > 2){
                        String pl = args[2];
                        p = Server.getInstance().getPlayer(pl);
                        if(p == null){
                            sendMessageToObject("&c玩家 "+pl+" 不在线",sender);
                            return true;
                        }
                    }
                    if(p != null){
                        Item item =  RegisterItemServices.CUSTOM_ITEMS.get("sale_v1");
                        item.setCount(count);
                        p.getInventory().addItem(item);
                        sendMessageToObject("&b 你获得了 &e 售货机 * &a"+count,p);
                    }else{
                        sendMessageToObject("&c目标玩家为控制台!",sender);
                    }
                    break;
                case "q":
                    if(args.length > 1){
                        String master = args[1];
                        List<SalesData> salesData = SalesMainClass.INSTANCE.sqliteHelper.getDataByString(SalesMainClass.DB_TABLE,
                                "master = ?",new String[]{
                                        master
                                }, SalesData.class);
                        if(salesData.size() > 0){
                            sendMessageToObject("&e找到 &7"+salesData.size()+" &e台 属于 &2"+master+" &e的售货机",sender);
                            for(SalesData salesData1: salesData){
                                sendMessageToObject("&a查询到坐标 &r"+salesData1.location,sender);
                            }
                        }else{
                            sendMessageToObject("&c未找到 "+master+" 相关的售货机",sender);
                        }
                    }else{
                        int allCount = SalesMainClass.INSTANCE.sqliteHelper.countAllData(SalesMainClass.DB_TABLE);
                        sendMessageToObject("&e当前服务器共计 &a"+allCount+" &e台售货机",sender);
                        sendMessageToObject("&7每张地图最多显示 &c40 &7条售货机最多的区块信息",sender);
                        for(Level level: Server.getInstance().getLevels().values()){
                            List<SqliteHelper.DataCount<SalesData>> dataCounts = SalesMainClass.INSTANCE.sqliteHelper.sortDataCount(
                                    SalesMainClass.DB_TABLE,"chunkX,chunkZ","world = '"+level.getFolderName()+"'",40,SalesData.class
                            );
                            if(dataCounts.size() > 0){
                                int mapCount = SalesMainClass.INSTANCE.sqliteHelper.countData(SalesMainClass.DB_TABLE,"world",level.getFolderName());
                                sendMessageToObject("&2地图 &7"+level.getFolderName()+" &2共计 &e"+mapCount+" &2台售货机",sender);
                                for(SqliteHelper.DataCount<SalesData> dataCount: dataCounts){
                                    sendMessageToObject("  &2区块&e("+dataCount.data.chunkx+":"
                                            +dataCount.data.chunkz+") &2存在 &b"+dataCount.count+" &2台售货机 &7坐标: "+dataCount.data.location,sender);
                                }
                            }

                        }
//                        sendMessageToObject("&c未知指令 请执行/sa help 查看帮助",sender);
                    }


                    break;
                case "d":
                    String zks = args[1];
                    float zk = 0;

                    try{
                        zk = Float.parseFloat(zks);
                    }catch (Exception ignore){}

                    String master = args[2];
                    p = Server.getInstance().getPlayer(master);
                    if(p != null){
                        Item item =RegisterItemServices.CUSTOM_ITEMS.get("discount");
                        item.setCustomName(TextFormat.colorize('&',"&r&e&l通用优惠券 ("+zks+"折)"));
                        item.setLore(TextFormat.colorize('&',"\n&r&7 可以用作所有的售货机!"));
                        item.addEnchantment(Enchantment.getEnchantment(0));
                        CompoundTag tag = item.getNamedTag();
                        tag.putString(CustomSaleDiscountItem.USE_TAG,"all");
                        tag.putFloat(CustomSaleDiscountItem.USE_ZK_TAG,zk);
                        tag.putBoolean(CustomSaleDiscountItem.USE_NONE_TAG,true);
                        item.setNamedTag(tag);
                        item.setCount(1);
                        p.getInventory().addItem(item);
                        sendMessageToObject("&a你获得一张 &r"+item.getCustomName(),p);
                    }else{
                        sendMessageToObject("&c玩家 "+master+" 不在线",sender);
                    }

                    break;
                case "b":
                    if(args.length > 1) {
                        String model = args[1];
                        if(ENTITY_SKIN.containsKey(model)){
                            if(sender instanceof Player){
                                Item hand = ((Player) sender).getInventory().getItemInHand();
                                if(hand.getId() == 0){
                                    sendMessageToObject("&c请不要手持空气",sender);
                                }else{
                                    if(hand instanceof ISaleItem){
                                        sendMessageToObject("&c请不要绑定已存在的售货机",sender);
                                    }else{
                                        CompoundTag tag = hand.getNamedTag();
                                        if(tag == null){
                                            tag = new CompoundTag();
                                        }
                                        tag.putBoolean("saleskey",true);
                                        tag.putString("salesmeta",model);
                                        hand.setNamedTag(tag);
                                        ((Player) sender).getInventory().setItemInHand(hand);
                                        sendMessageToObject("&a绑定成功！",sender);
                                    }
                                }
                            }else{
                                sendMessageToObject("&c请不要在控制台执行此指令",sender);
                            }

                        }else{
                            sendMessageToObject("&c不存在 "+model+" 模型",sender);
                        }
                    }
                    break;
                default:
                    sendMessageToObject("&c未知指令 请执行/sa help 查看帮助",sender);
                    break;
            }
        }else{
            sendMessageToObject("&c未知指令 请执行/sa help 查看帮助",sender);
        }

        return true;
    }

    private void initSkin() {
        //先检查文件夹
        BlockFace[] blockFaces = new BlockFace[]{BlockFace.EAST,BlockFace.NORTH,BlockFace.SOUTH,BlockFace.WEST};
        File modelFile = new File(this.getDataFolder()+"/assets");
        File[] nFolders = modelFile.listFiles();
        boolean noExists = true;
        List<File> folders = new ArrayList<>();
        if(nFolders != null && nFolders.length > 0){
            for(File aFile: nFolders){
                if(aFile.isDirectory()){
                    noExists = false;
                    folders.add(aFile);
                }
            }
        }
        //初始化文件夹
        if(noExists){


            for(String verName: lists){
                saveResource("assets/models/" +verName+"/machine.png", "/assets/" +verName+"/machine.png",false);
                saveResource("assets/models/" +verName+"/"+verName+".yml", "/assets/" +verName+"/"+verName+".yml",false);
                for(BlockFace face: blockFaces){
                    saveResource("assets/models/"+verName+"/machine_" +face.getName().toLowerCase()+".json",
                            "/assets/"+verName+"/machine_"+face.getName().toLowerCase()+".json",false);

                }
                //添加到文件列表
                folders.add(new File(this.getDataFolder()+"/assets/"+verName));
            }
        }

        Collections.reverse(folders);
        for (File folder : folders) {
            String name = folder.getName();
            File fc = new File(folder+"/"+name+".yml");
            if(!fc.exists()){
                sendMessageToConsole("&c模型文件夹 "+name+" 缺失关键 "+name+".yml 文件!");
                continue;
            }
            Config config = new Config(fc);
            LinkedHashMap<BlockFace,Skin> hashMap = new LinkedHashMap<>();
            for(BlockFace face: blockFaces){
                hashMap.put(face,loadSkin("machine_"+face.getName().toLowerCase(),folder+"/machine.png",
                        folder+"/machine_"+face.getName().toLowerCase()+".json"));
            }

            SalesMainClass.sendMessageToConsole("加载模型 &e"+name);
            ENTITY_SKIN.put(name,new SaleSkinConfig(name,hashMap,loadSettingConfig(config,name)));
        }




    }

    private SaleSettingConfig loadSettingConfig(Config config,String folder){
        SaleSettingConfig saleSettingConfig = new SaleSettingConfig();
        saleSettingConfig.enableAnim = config.getBoolean("open-door-anim",true);
        saleSettingConfig.enableItem = config.getBoolean("display-item.enable",true);
        int v = 0;
        if(!config.exists("meta")){
            String ve = folder.substring(1);
            try{
                int num = Integer.parseInt(ve);
                v = num - 1;
            }catch (Exception ignore){}
        }
        saleSettingConfig.meta = config.getInt("meta",v);

        saleSettingConfig.entitySize = config.getDouble("entity-size",0.9d);
        SaleSettingConfig.SaleWeight weight = new SaleSettingConfig.SaleWeight();
        weight.width = config.getInt("weight.width",1);
        weight.height = config.getInt("weight.height",2);
        saleSettingConfig.weight = weight;

        Map<?,?> map = (Map<?, ?>) config.get("display-item.position");
        Map<BlockFace, List<Vector3>> linkedListLinkedHashMap = new LinkedHashMap<>();
        for (BlockFace face: BlockFace.values()){
            if(map.containsKey(face.getName().toLowerCase())){
                List<?> sv = (List<?>) map.get(face.getName().toLowerCase());
                List<Vector3> v3 = new ArrayList<>();
                for(Object o:sv){
                    String v3s = o.toString();
                    String[] sp = v3s.split(";");
                    for(String spv1: sp){
                        String[] posv3 = spv1.split(",");
                        v3.add(new Vector3(Double.parseDouble(posv3[0])
                                ,Double.parseDouble(posv3[1])
                                ,Double.parseDouble(posv3[2])));
                    }
                }
                linkedListLinkedHashMap.put(face,v3);
            }
        }
        saleSettingConfig.floatItemPos = linkedListLinkedHashMap;

        return saleSettingConfig;
    }

    private Skin loadSkin(String skinName,String imgPath,String jsonPath){
        Skin skin = new Skin();
        BufferedImage skindata;
        try {
            skindata = ImageIO.read(new File(imgPath));
        } catch (IOException var19) {
            this.getPluginLoader().disablePlugin(this);
            return null;
        }
        if (skindata != null) {
            skin.setSkinData(skindata);
            skin.setSkinId(skinName);
        }
        File skinJsonFile = new File(jsonPath);
        Map<String, Object> skinJson = (new Config(jsonPath, Config.JSON)).getAll();
        String geometryName;
        String formatVersion = (String) skinJson.getOrDefault("format_version", "1.10.0");
        skin.setGeometryDataEngineVersion(formatVersion); //设置皮肤版本，主流格式有1.16.0,1.12.0(Blockbench新模型),1.10.0(Blockbench Legacy模型),1.8.0
        geometryName = getGeometryName(skinJsonFile);
        skin.generateSkinId(skinName);
        skin.setSkinResourcePatch("{\"geometry\":{\"default\":\"" + geometryName + "\"}}");
        skin.setGeometryName(geometryName);
        try {
            skin.setGeometryData(Utils.readFile(skinJsonFile));
        }catch (IOException e){
            this.getPluginLoader().disablePlugin(this);
            return null;
        }
        return skin;
    }

    private static String getGeometryName(File file) {
        Config originGeometry = new Config(file, Config.JSON);
        if (!originGeometry.getString("format_version").equals("1.12.0") && !originGeometry.getString("format_version").equals("1.16.0")) {
            return "nullvalue";
        }
        //先读取minecraft:geometry下面的项目
        List<Map<String, Object>> geometryList = (List<Map<String, Object>>) originGeometry.get("minecraft:geometry");
        //不知道为何这里改成了数组，所以按照示例文件读取第一项
        Map<String, Object> geometryMain = geometryList.get(0);
        //获取description内的所有
        Map<String, Object> descriptions = (Map<String, Object>) geometryMain.get("description");
        return (String) descriptions.getOrDefault("identifier", "geometry.unknown"); //获取identifier
    }

    public static void sendMessageToConsole(String msg){
        sendMessageToObject(msg,null);
    }

    public static void sendMessageToObject(String msg, Object o){
        String message = TextFormat.colorize('&',PLUGIN_NAME+" "+msg);
        if(o != null){
            if(o instanceof Player){
                if(((Player) o).isOnline()) {
                    ((Player) o).sendMessage(message);
                    return;
                }
            }
            if(o instanceof EntityHuman){
                message = ((EntityHuman) o).getName()+"->"+message;
            }
        }
        INSTANCE.getLogger().info(message);

    }

    private void checkServer(){
        boolean ver = false;
        //双核心兼容
        CORE_NAME = "Nukkit";
        try {
            Class<?> c = Class.forName("cn.nukkit.Nukkit");
            c.getField("NUKKIT_PM1E");
            ver = true;
            CORE_NAME = "Nukkit PM1E";


        } catch (ClassNotFoundException | NoSuchFieldException ignore) { }
        try {
            Class<?> c = Class.forName("cn.nukkit.Nukkit");
            CORE_NAME = c.getField("NUKKIT").get(c).toString();

            ver = true;

        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignore) {
        }


        AbstractFakeInventory.IS_PM1E = ver;
        if(ver){
            Server.getInstance().enableExperimentMode = true;
            Server.getInstance().forceResources = true;
        }
        sendMessageToConsole("&e当前核心为 "+CORE_NAME);
    }

    @Override
    public void onDisable() {
        //TODO 保存数据
        sendMessageToConsole("&c正在保存数据...");
        for (SalesEntity sales: SalesListener.cacheEntitys.values()){
            sales.salesData.saveItemSlots(sales.loadItems);
            sales.saveData();
        }

        sendMessageToConsole("&a数据保存成功!");
        if(sqliteHelper != null){
            sqliteHelper.destroyed();
        }
    }
}
