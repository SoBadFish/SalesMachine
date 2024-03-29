package org.sobadfish.sales.config;

import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sobadfish
 * @date 2024/3/29
 */
public class SalesData {

    public long id;


    public int chunkx;

    public int chunkz;

    public String location;

    public String bf;

    public String master;


    public String itemjson;


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
                list.add(NBTIO.read(
                        entry.toString().getBytes(StandardCharsets.UTF_8)));
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
                strings.add(new String(NBTIO.write(compoundTag),StandardCharsets.UTF_8));
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
                ", itemjson='" + itemjson + '\'' +
                '}';
    }
}
