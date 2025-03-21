package org.sobadfish.sales.items;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.block.BlockChest;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.sales.RegisterItemServices;
import org.sobadfish.sales.SalesListener;
import org.sobadfish.sales.SalesMainClass;
import org.sobadfish.sales.config.SaleSkinConfig;
import org.sobadfish.sales.config.SalesData;
import org.sobadfish.sales.entity.SalesEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sobadfish
 * 23:01
 */
public class ItemAction {

    /**
     * 放置箱子..
     * */
    public static boolean onChestPlace(Item handItem,Level level, Player player, Block block, Block target, BlockFace face, double fx, double fy, double fz){
        BlockChest blockChest = new BlockChest();
        BlockPlaceEvent event = new BlockPlaceEvent(player,blockChest,block,target,handItem);
        Server.getInstance().getPluginManager().callEvent(event);
        if(event.isCancelled()){
            return false;
        }
        int[] faces = new int[]{2, 5, 3, 4};

        BlockEntityChest chest = null;
        blockChest.setDamage(faces[player.getDirection().getHorizontalIndex()]);

        for(int side = 2; side <= 5; ++side) {
            if ((blockChest.getDamage() != 4 && blockChest.getDamage() != 5 || side != 4 && side != 5) && (blockChest.getDamage() != 3 && blockChest.getDamage() != 2 || side != 2 && side != 3)) {
                Block c = block.getSide(BlockFace.fromIndex(side));
                if (c instanceof BlockChest && c.getDamage() == blockChest.getDamage()) {
                    BlockEntity blockEntity = block.getLevel().getBlockEntity(c);
                    if (blockEntity instanceof BlockEntityChest && !((BlockEntityChest)blockEntity).isPaired()) {
                        chest = (BlockEntityChest)blockEntity;
                        break;
                    }
                }
            }
        }
        level.setBlock(block, blockChest, true, true);
        CompoundTag nbt = (new CompoundTag("")).putList(handItem.getNamedTag().getList("Items")).putString("id", "Chest").putInt("x", (int)block.x).putInt("y", (int)block.y).putInt("z", (int)block.z);

        BlockEntityChest blockEntity = (BlockEntityChest)BlockEntity.createBlockEntity("Chest", block.getChunk(), nbt, new Object[0]);
        if (chest != null) {
            chest.pairWith(blockEntity);
            blockEntity.pairWith(chest);
        }
        Item cc = RegisterItemServices.CUSTOM_ITEMS.get("ct").clone();
        if(handItem.getDamage() > handItem.getMaxDurability()){
            player.getInventory().removeItem(handItem);
            //添加粒子
            player.level.addParticle(new ItemBreakParticle(player.add(0, player.getEyeY()),handItem));
            player.level.addSound(player,Sound.RANDOM_BREAK);
        }else{
            cc.setDamage(handItem.getDamage());
            player.getInventory().setItemInHand(cc);
            level.addSound(block, Sound.MOB_ZOMBIE_WOODBREAK);
        }




        return true;
    }

    /**
     * 放置售货机
     * */
    public static boolean onSalePlace(Item handItem,Block block, Player player, int meta){
        if(!handItem.hasCompoundTag() || !handItem.getNamedTag().contains("salesmeta")){
            String sm = null;
            for(SaleSkinConfig saleSkinConfig: SalesMainClass.ENTITY_SKIN.values()){
                if(saleSkinConfig.config.meta == meta){
                    sm = saleSkinConfig.modelName;
                }
            }

            if(SalesEntity.spawnToAll(block,player.getDirection(),player.getName(),null,sm,handItem) != null){
                if (player.isSurvival() || player.isAdventure()) {
                    Item item = player.getInventory().getItemInHand();
                    item.setCount(item.getCount() - 1);
                    player.getInventory().setItemInHand(item);
                }
                return true;
            }
            SalesMainClass.sendMessageToObject("&c生成失败！ 请保证周围没有其他方块",player);
            return false;
        }
        return false;

    }

    public static boolean onCtActivate(Item i, Player player, Block target,BlockFace face) {

        if(SalesMainClass.usedCtChest) {
            //把箱子搬起来
            if (target instanceof BlockChest) {
                //箱子
                BlockBreakEvent event = new BlockBreakEvent(player,target, i, null, false);
                Server.getInstance().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return false;
                }
                BlockEntity blockEntityChest = target.level.getBlockEntity(target);
                if (blockEntityChest instanceof BlockEntityChest) {
                    ((BlockEntityChest) blockEntityChest).unpair();
                    CompoundTag ctm = i.getNamedTag();
                    if (ctm == null) {
                        ctm = new CompoundTag();
                    }
                    Inventory inventory = ((BlockEntityChest) blockEntityChest).getInventory();
                    ListTag<CompoundTag> cl = new ListTag<>("Items");
                    for (int index = 0; index < inventory.getSize(); ++index) {
                        cl.add(index, NBTIO.putItemHelper(inventory.getItem(index), index));
                    }
//     
                    ctm.putList(cl);
                    i.setDamage(i.getDamage() + 1);
                    Item sitem = RegisterItemServices.CUSTOM_ITEMS.get("ct_chest");
                    sitem.setDamage(i.getDamage());
                    sitem.setNamedTag(ctm);

                    player.getInventory().removeItem(i);
                    player.getInventory().setItemInHand(sitem);
                    target.level.setBlock(target, new BlockAir());

                    return true;
                }

                return false;
            }
        }
        //编写拿取的逻辑
        if(player.isSneaking()){
            return false;
        }


        SalesEntity salesEntity = SalesListener.getEntityByPos(target);
        if(salesEntity != null){
            if(player.isOp() || salesEntity.master.equalsIgnoreCase(player.getName())){

                SaleSkinConfig saleSkinConfig = SalesMainClass.ENTITY_SKIN.get(salesEntity.salesData.skinmodel);
                String name = "ct_sale_v"+(saleSkinConfig.config.meta+1);

                if(!RegisterItemServices.CUSTOM_ITEMS.containsKey(name)){
                    name = "ct_sale_v1";
                }

                Item sitem = RegisterItemServices.CUSTOM_ITEMS.get(name);
                i.setDamage(i.getDamage() + 1);
                sitem.setDamage(i.getDamage());
                sitem.setCount(1);
                //sitem.setDamage(saleSkinConfig.config.meta);
                String nm = "&r&e"+salesEntity.master+" 的售货机";
                if(salesEntity.salesData.customname != null){
                    nm = salesEntity.salesData.customname;
                }
//                SalesMainClass.sendMessageToObject("&e售货机朝向: &a"+salesEntity.blockFace.getName(),player);

                sitem.setCustomName(TextFormat.colorize('&',nm));
                sitem.addEnchantment(Enchantment.getEnchantment(0));
                CompoundTag compoundTag = sitem.getNamedTag();
                compoundTag.putCompound(salesEntity.toPackage());
                //防止过快使用锁
                compoundTag.putLong("lock",System.currentTimeMillis());
                sitem.setCompoundTag(compoundTag);
                //i.setCount(i.getCount() - 1);

                player.getInventory().removeItem(i);
                player.getInventory().setItemInHand(sitem);
            }


        }

        return true;
    }



    public static boolean onSaleModelChange(Item item,Level level, Player player, Block block, Block target, BlockFace face, double fx, double fy, double fz){
        if(player.isSneaking()){
            return false;
        }
        SalesEntity salesEntity = SalesListener.getEntityByPos(target);
        if(salesEntity != null){
            if(player.isOp() || salesEntity.master.equalsIgnoreCase(player.getName())){
                String model = salesEntity.salesData.skinmodel;
                List<String> sk = new ArrayList<>(SalesMainClass.ENTITY_SKIN.keySet());
                int index = sk.indexOf(model);
                index++;
                if(index >= sk.size()){
                    index = 0;
                }
                if(!salesEntity.setModel(sk.get(index))){
                    SalesMainClass.sendMessageToObject("&c切换模型失败",player);
                }else{
                    item.setDamage(item.getDamage() + 1);
                    if(item.getDamage() > item.getMaxDurability()){
                        player.getInventory().removeItem(item);
                        //添加粒子
                        level.addParticle(new ItemBreakParticle(player.add(0, player.getEyeY()),item));
                        level.addSound(player,Sound.RANDOM_BREAK);
                    }
                }
            }else{
                SalesMainClass.sendMessageToObject("&c这不是你的售货机",player);
            }
        }
        return true;

    }


    public static boolean onSaleActivate(Item item,Level level, Player player, Block block) {
        //编写放置的逻辑
        if(player.isSneaking()){
            return false;
        }
        if (item.hasCompoundTag()){
            CompoundTag tag = item.getNamedTag();
            //这段代码防止 win10 右键放置多次触发
            if(tag.contains("lock")){
                long lockTime = tag.getLong("lock");
                if(System.currentTimeMillis() - lockTime < 1000){
                    return false;
                }
            }
            if(SalesMainClass.banWorlds.contains(level.getFolderName()) && !player.isOp()){
                return false;
            }
            if(tag.contains("sale_data")){

              //  Server.getInstance().getScheduler().scheduleDelayedTask(SalesMainClass.INSTANCE, () -> {
                CompoundTag data = tag.getCompound("sale_data");
                SalesData salesData = SalesData.getSaleDataByCompoundTag(data);
                salesData.bf = player.getDirection().getName();
                salesData.location = SalesEntity.asLocation(block);
                SalesEntity entity = SalesEntity.spawnToAll(salesData.asPosition(),
                        BlockFace.valueOf(salesData.bf.toUpperCase()), salesData.master, salesData,false,true,null,null);
                if(entity != null){

                    SalesMainClass.INSTANCE.sqliteHelper.add(SalesMainClass.DB_TABLE, salesData);
                    if(item.getDamage() >= item.getMaxDurability()){
                        player.getInventory().removeItem(item);
                        //添加粒子
                        player.level.addParticle(new ItemBreakParticle(player.add(0, player.getEyeY()),item));
                        player.level.addSound(player,Sound.RANDOM_BREAK);
                    }else{
                        Item cc = RegisterItemServices.CUSTOM_ITEMS.get("ct").clone();
                        cc.setDamage(item.getDamage());
                        player.getInventory().setItemInHand(cc);
                        level.addSound(block, Sound.MOB_ZOMBIE_WOODBREAK);
                    }



                }else{
                    SalesMainClass.sendMessageToObject("&c生成失败！ 请保证周围没有其他方块",player);
                }


              //  },1);



            }
        }


        return false;
    }
}
