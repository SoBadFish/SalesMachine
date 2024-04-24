package org.sobadfish.sales.config;

import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * @author Sobadfish
 * @date 2024/3/29
 */
public class SalesData {

    public long id;

    //唯一ID
    public String uuid;


    public int chunkx;

    public int chunkz;

    public String location;

    public String world = "";

    public String bf;

    public String master;

    public String customname;

    public int width = 1;

    public int height = 2;

    public String itemjson;

    //皮肤模型名称
    public String skinmodel = "";

    //整体打包
    public CompoundTag toPackage(){
        CompoundTag tag = new CompoundTag("sale_data");
        tag.putInt("chunkx",chunkx);
        tag.putInt("chunkz",chunkz);
        tag.putInt("width",width);
        tag.putInt("height",height);


        tag.putString("location",location);
        tag.putString("world",world);
        tag.putString("bf",bf);

        if(world == null || "".equalsIgnoreCase(world)){
            world = location.split(":")[3];
        }

        if(customname != null){
            tag.putString("customname",customname);
        }
        if(skinmodel != null && !"".equalsIgnoreCase(skinmodel)){
            tag.putString("skinmodel",skinmodel);
        }
        if(uuid == null){
            uuid = UUID.randomUUID().toString();
        }

        tag.putString("uuid",uuid);

        tag.putString("master",master);
        tag.putString("itemjson",itemjson);

        return tag;
    }

    public static SalesData getSaleDataByCompoundTag(CompoundTag tag){
        SalesData salesData = new SalesData();
        salesData.chunkx = tag.getInt("chunkx");
        salesData.chunkz = tag.getInt("chunkz");
        if(tag.contains("width")){
            salesData.width = tag.getInt("width");
            salesData.height = tag.getInt("height");
        }


        salesData.location = tag.getString("location");
        salesData.bf = tag.getString("bf");
        if(tag.contains("customname")){
            salesData.customname = tag.getString("customname");
        }
        if(tag.contains("world")){
            salesData.world = tag.getString("world");
        }else{
            salesData.world = salesData.location.split(":")[3];
        }
        if(tag.contains("skinmodel")){
            salesData.skinmodel = tag.getString("skinmodel");
        }
        if(tag.contains("uuid")){
            salesData.uuid = tag.getString("uuid");
        }else{
            salesData.uuid = UUID.randomUUID().toString();
        }


        salesData.master = tag.getString("master");
        salesData.itemjson = tag.getString("itemjson");

        return salesData;

    }


    public Position asPosition(){
        String[] sl = location.split(":");
        return new Position(Integer.parseInt(sl[0]),Integer.parseInt(sl[1]),Integer.parseInt(sl[2]), Server.getInstance().getLevelByName(sl[3]));
    }


    public ListTag<CompoundTag> asItemSlots(){
        ListTag<CompoundTag> list = new ListTag<>();
        Gson gson = new Gson();
        List<?> js = gson.fromJson(itemjson,List.class);
        for(Object entry : js){
            try {
                list.add(NBTIO.read(Base64.getDecoder().decode(entry.toString())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }


    public void saveItemSlots(ListTag<CompoundTag> list){
        List<String> strings = new ArrayList<>();
        for (CompoundTag compoundTag: list.getAll()){
            try {
                strings.add(Base64.getEncoder().encodeToString(NBTIO.write(compoundTag)));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        Gson gson = new Gson();
        itemjson = gson.toJson(strings);
    }

    @Override
    public String toString() {
        return "SalesData{" +
                "id=" + id +
                ", chunkx=" + chunkx +
                ", chunkz=" + chunkz +
                ", location='" + location + '\'' +
                ", bf='" + bf + '\'' +
                ", master='" + master + '\'' +
                ", customname='" + customname + '\'' +
                ", itemjson='" + itemjson + '\'' +
                '}';
    }

    @Override
    public SalesData clone()  {
        try{
            return (SalesData) super.clone();
        }catch (Exception e){
            return new SalesData();
        }
    }
}
