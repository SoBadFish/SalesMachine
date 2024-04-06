package org.sobadfish.sales.panel.lib;

import cn.nukkit.Player;
import org.sobadfish.sales.entity.SalesEntity;

/**
 * @author Sobadfish
 * @date 2024/4/6
 */
public interface IDisplayPanel {

    void close();

    void open(Player player);

    SalesEntity getSales();
}
