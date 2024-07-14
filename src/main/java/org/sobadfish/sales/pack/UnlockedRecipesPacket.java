package org.sobadfish.sales.pack;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

/**
 * @author Sobadfish
 * 22:58
 */
public class UnlockedRecipesPacket extends DataPacket {

    public static final byte NETWORK_ID = ProtocolInfo.UNLOCKED_RECIPES_PACKET;
    public ActionType action;
    public final List<String> unlockedRecipes = new ObjectArrayList<>();

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        if (this.protocol >= ProtocolInfo.v1_20_0) {
            this.action = ActionType.values()[this.getLInt()];
        } else {
            this.action = this.getBoolean() ? ActionType.NEWLY_UNLOCKED : ActionType.INITIALLY_UNLOCKED;
        }
        int count = (int) this.getUnsignedVarInt();
        for (int i = 0; i < count; i++) {
            this.unlockedRecipes.add(this.getString());
        }
    }

    @Override
    public void encode() {
        this.reset();
        if (this.protocol >= ProtocolInfo.v1_20_0) {
            this.putLInt(this.action.ordinal());
        } else {
            this.putBoolean(this.action == ActionType.NEWLY_UNLOCKED);
        }
        this.putUnsignedVarInt(this.unlockedRecipes.size());
        for (String recipe : this.unlockedRecipes) {
            this.putString(recipe);
        }
    }

    public enum ActionType {
        EMPTY,
        INITIALLY_UNLOCKED,
        NEWLY_UNLOCKED,
        REMOVE_UNLOCKED,
        REMOVE_ALL
    }
}

