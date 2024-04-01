package org.sobadfish.sales.config;

import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;

import java.util.*;

/**
 * @author Sobadfish
 * @date 2024/4/1
 */
public class SaleSettingConfig {

    public boolean enableAnim = true;


    public boolean enableItem = true;

    public double entitySize = 0.9f;

    public List<String> banWorlds = new ArrayList<>();


    /**
     * 浮空物品
     * */
    public Map<BlockFace, List<Vector3>> floatItemPos = new LinkedHashMap<>();
}
