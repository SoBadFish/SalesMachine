package org.sobadfish.sales.config;

import cn.nukkit.entity.data.Skin;
import cn.nukkit.math.BlockFace;

import java.util.Map;

/**
 * @author Sobadfish
 * @date 2024/4/3
 */
public class SaleSkinConfig {

    public String modelName;

    public Map<BlockFace, Skin> skinLinkedHashMap;

    public SaleSettingConfig config;

    public SaleSkinConfig(String modelName, Map<BlockFace, Skin> skin,SaleSettingConfig config){
        this.modelName = modelName;
        this.skinLinkedHashMap = skin;
        this.config = config;
    }

}
