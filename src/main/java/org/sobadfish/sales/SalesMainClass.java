package org.sobadfish.sales;

import cn.lanink.customitemapi.CustomItemAPI;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.utils.Utils;
import org.sobadfish.sales.block.BarrierBlock;
import org.sobadfish.sales.block.BarrierBlock_Nukkit;
import org.sobadfish.sales.block.IBarrier;
import org.sobadfish.sales.config.SaleSettingConfig;
import org.sobadfish.sales.config.SaleSkinConfig;
import org.sobadfish.sales.config.SalesData;
import org.sobadfish.sales.db.SqliteHelper;
import org.sobadfish.sales.entity.SalesEntity;
import org.sobadfish.sales.items.*;
import org.sobadfish.sales.panel.lib.AbstractFakeInventory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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

    public IBarrier iBarrier;

    public static SalesMainClass INSTANCE;

    public static LinkedHashMap<String,Item> CUSTOM_ITEMS = new LinkedHashMap<>();

    public static boolean LOAD_CUSTOM = false;

    public static List<String> banWorlds = new ArrayList<>();


    public SqliteHelper sqliteHelper;

    public static final String DB_TABLE = "salelocation";



    @Override
    public void onLoad() {
        //提前注册好
        BlockEntity.registerBlockEntity(SalesEntity.SalesBlockEntity.ENTITY_TYPE,SalesEntity.SalesBlockEntity.class);
        Entity.registerEntity(SalesEntity.ENTITY_TYPE,SalesEntity.class);
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
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

        saveDefaultConfig();
        //加载配置
        loadConfig();

        checkServer();

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
        initItem();


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




        this.getServer().getPluginManager().registerEvents(new SalesListener(this),this);


        sendMessageToConsole("&a加载完成!");

    }


    //加载坐标点
    private void loadConfig() {
        banWorlds = getConfig().getStringList("ban-world");

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

    private void initItem() {
        try{
            Class.forName("cn.lanink.customitemapi.item.ItemCustom");
            LOAD_CUSTOM = true;
        }catch (Exception ignore){}
        if(LOAD_CUSTOM){

            CustomItemAPI.getInstance().registerCustomItem(1992, org.sobadfish.sales.items.custom.CustomSaleItem.class);
            CustomItemAPI.getInstance().registerCustomItem(1993, org.sobadfish.sales.items.custom.CustomSaleSettingItem.class);
            CustomItemAPI.getInstance().registerCustomItem(1994, org.sobadfish.sales.items.custom.CustomSaleRemoveItem.class);
            CustomItemAPI.getInstance().registerCustomItem(1995, org.sobadfish.sales.items.custom.CustomSaleMoneyItem.class);

            CustomItemAPI.getInstance().registerCustomItem(1996, org.sobadfish.sales.items.custom.CustomCtItem.class);
            CustomItemAPI.getInstance().registerCustomItem(1997, org.sobadfish.sales.items.custom.CustomCtSaleItem.class);
            CustomItemAPI.getInstance().registerCustomItem(1998, org.sobadfish.sales.items.custom.CustomWrench.class);

            CustomItemAPI.getInstance().registerCustomItem(1999, org.sobadfish.sales.items.custom.CustomSalePanelLeftItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2000, org.sobadfish.sales.items.custom.CustomSalePanelRightItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2001, org.sobadfish.sales.items.custom.CustomSalePanelResetItem.class);
            CustomItemAPI.getInstance().registerCustomItem(2002, org.sobadfish.sales.items.custom.CustomSalePanelWallItem.class);



            CUSTOM_ITEMS.put("sale",new org.sobadfish.sales.items.custom.CustomSaleItem());
            CUSTOM_ITEMS.put("setting",new org.sobadfish.sales.items.custom.CustomSaleSettingItem());
            CUSTOM_ITEMS.put("remove",new org.sobadfish.sales.items.custom.CustomSaleRemoveItem());
            CUSTOM_ITEMS.put("money",new org.sobadfish.sales.items.custom.CustomSaleMoneyItem());
            CUSTOM_ITEMS.put("ct",new org.sobadfish.sales.items.custom.CustomCtItem());
            CUSTOM_ITEMS.put("ct_sale",new org.sobadfish.sales.items.custom.CustomCtSaleItem());
            CUSTOM_ITEMS.put("pipe_wrench",new org.sobadfish.sales.items.custom.CustomWrench());

            CUSTOM_ITEMS.put("left",new org.sobadfish.sales.items.custom.CustomSalePanelLeftItem());
            CUSTOM_ITEMS.put("right",new org.sobadfish.sales.items.custom.CustomSalePanelRightItem());
            CUSTOM_ITEMS.put("reset",new org.sobadfish.sales.items.custom.CustomSalePanelResetItem());

            CUSTOM_ITEMS.put("wall",new org.sobadfish.sales.items.custom.CustomSalePanelWallItem());

        }else{
            Item.registerCustomItem(CustomSaleItem.class);
            Item.registerCustomItem(CustomSaleSettingItem.class);
            Item.registerCustomItem(CustomSaleRemoveItem.class);
            Item.registerCustomItem(CustomSaleMoneyItem.class);

            Item.registerCustomItem(CustomCtItem.class);
            Item.registerCustomItem(CustomCtSaleItem.class,false);
            Item.registerCustomItem(CustomWrench.class);

            Item.registerCustomItem(CustomSalePanelLeftItem.class,false);
            Item.registerCustomItem(CustomSalePanelRightItem.class,false);
            Item.registerCustomItem(CustomSalePanelResetItem.class,false);

            Item.registerCustomItem(CustomSalePanelWallItem.class,false);

            CUSTOM_ITEMS.put("sale",new CustomSaleItem());
            CUSTOM_ITEMS.put("setting",new CustomSaleSettingItem());
            CUSTOM_ITEMS.put("remove",new CustomSaleRemoveItem());
            CUSTOM_ITEMS.put("money",new CustomSaleMoneyItem());
            CUSTOM_ITEMS.put("ct",new CustomCtItem());
            CUSTOM_ITEMS.put("ct_sale",new CustomCtSaleItem());
            CUSTOM_ITEMS.put("pipe_wrench",new CustomWrench());

            CUSTOM_ITEMS.put("left",new CustomSalePanelLeftItem());
            CUSTOM_ITEMS.put("right",new CustomSalePanelRightItem());
            CUSTOM_ITEMS.put("reset",new CustomSalePanelResetItem());
            CUSTOM_ITEMS.put("wall",new CustomSalePanelWallItem());
        }
//        Item.removeCreativeItem(CUSTOM_ITEMS.get("ct_sale"));




    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length > 0){
            switch (args[0]){
                case "help":
                    sendMessageToObject("&a/sa help &7查看帮助",sender);
                    sendMessageToObject("&a/sa give [数量] [玩家(可不填)]  &7给予玩家售货机物品 （放置即可）",sender);
                    sendMessageToObject("&a/sa q [玩家（可不填）]  &7查询玩家售货机坐标信息",sender);
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
                        Item item =  SalesMainClass.INSTANCE.iBarrier.getShaleItem();
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
                        sendMessageToObject("&7每张地图最多显示 &c5 &7条售货机最多的区块信息",sender);
                        for(Level level: Server.getInstance().getLevels().values()){
                            List<SqliteHelper.DataCount<SalesData>> dataCounts = SalesMainClass.INSTANCE.sqliteHelper.sortDataCount(
                                    SalesMainClass.DB_TABLE,"chunkX,chunkZ","world = '"+level.getFolderName()+"'",5,SalesData.class
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

            String[] lists = new String[]{"v1", "v2","v3","v4","v5"};
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
            ENTITY_SKIN.put(name,new SaleSkinConfig(name,hashMap,loadSettingConfig(config)));
        }




    }

    private SaleSettingConfig loadSettingConfig(Config config){
        SaleSettingConfig saleSettingConfig = new SaleSettingConfig();
        saleSettingConfig.enableAnim = config.getBoolean("open-door-anim",true);
        saleSettingConfig.enableItem = config.getBoolean("display-item.enable",true);

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
        try {
            Class<?> c = Class.forName("cn.nukkit.Nukkit");
            c.getField("NUKKIT_PM1E");
            ver = true;


        } catch (ClassNotFoundException | NoSuchFieldException ignore) { }
        try {
            Class<?> c = Class.forName("cn.nukkit.Nukkit");
            c.getField("NUKKIT").get(c).toString().equalsIgnoreCase("Nukkit PetteriM1 Edition");
            ver = true;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignore) {
        }

        AbstractFakeInventory.IS_PM1E = ver;
        if(ver){
            sendMessageToConsole("&e当前核心为 Nukkit MOT");
            Server.getInstance().enableExperimentMode = true;
            Server.getInstance().forceResources = true;
        }else{
            sendMessageToConsole("&e当前核心为 Nukkit");
        }
    }

    @Override
    public void onDisable() {
        if(sqliteHelper != null){
            sqliteHelper.destroyed();
        }
    }
}
