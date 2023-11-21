package org.sobadfish.sales;

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
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.utils.Utils;
import org.sobadfish.sales.block.BarrierBlock;
import org.sobadfish.sales.block.BarrierBlock_Nukkit;
import org.sobadfish.sales.block.IBarrier;
import org.sobadfish.sales.entity.SalesEntity;

import org.sobadfish.sales.manager.SalesManager;
import org.sobadfish.sales.panel.lib.AbstractFakeInventory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sobadfish
 * @date 2023/11/16
 */
public class SalesMainClass extends PluginBase {

    public static LinkedHashMap<BlockFace,Skin> ENTITY_SKIN = new LinkedHashMap<>();

    public static final String PLUGIN_NAME = "&7[&e售货机&7]&r";

    public IBarrier iBarrier;

    public static SalesMainClass INSTANCE;

    @Override
    public void onLoad() {

        Entity.registerEntity(SalesEntity.ENTITY_TYPE,SalesEntity.class);
        BlockEntity.registerBlockEntity(SalesEntity.SalesBlockEntity.ENTITY_TYPE,SalesEntity.SalesBlockEntity.class);
        if(Block.list.length <= 256){
            Block.list[95] = BarrierBlock_Nukkit.class;
            iBarrier = new BarrierBlock_Nukkit();
        }else{
            Block.list[416] = BarrierBlock.class;
            iBarrier = new BarrierBlock();
        }




    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        sendMessageToConsole("&a正在加载售卖机插件");
        checkServer();
        initSkin();
        this.getServer().getPluginManager().registerEvents(new SalesListener(this),this);

//
        sendMessageToConsole("&a加载完成!");

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length > 0){
            switch (args[0]){
                case "help":
                    sendMessageToObject("&a/sa help &7查看帮助",sender);
                    sendMessageToObject("&a/sa give [数量] [玩家(可不填)]  &7给予玩家售货机物品 （放置即可）",sender);
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
        saveResource("assets/machine.png","/assets/machine.png",false);
        BlockFace[] blockFaces = new BlockFace[]{BlockFace.EAST,BlockFace.NORTH,BlockFace.SOUTH,BlockFace.WEST};
        for(BlockFace face: blockFaces){
            saveResource("assets/machine_"+face.getName().toLowerCase()+".json","/assets/machine_"+face.getName().toLowerCase()+".json",false);
            ENTITY_SKIN.put(face,loadSkin("machine_"+face.getName().toLowerCase()));
        }

    }

    private Skin loadSkin(String skinName){
        Skin skin = new Skin();
        BufferedImage skindata = null;
        try {
            skindata = ImageIO.read(new File(this.getDataFolder()+"/assets/machine.png"));
        } catch (IOException var19) {
            this.getPluginLoader().disablePlugin(this);
            return null;
        }
        if (skindata != null) {
            skin.setSkinData(skindata);
            skin.setSkinId(skinName);
        }
        File skinJsonFile = new File(this.getDataFolder() + "/assets/" + skinName + ".json");
        Map<String, Object> skinJson = (new Config(this.getDataFolder()+"/assets/"+skinName+".json", Config.JSON)).getAll();
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
            sendMessageToConsole("&e当前核心为 Nukkit PM1E");
        }else{
            sendMessageToConsole("&e当前核心为 Nukkit");
        }
    }
}
