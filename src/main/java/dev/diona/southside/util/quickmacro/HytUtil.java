package dev.diona.southside.util.quickmacro;

import net.minecraft.init.Items;

import static dev.diona.southside.Southside.MC.mc;

public class HytUtil {
    public static boolean isInHyt() {
        return mc.player.inventoryContainer.getSlot(36).getStack().getItem() == Items.NETHER_STAR;
    }
    public static boolean isInSW() {
        return mc.player.inventoryContainer.getSlot(44).getStack().getItem() == Items.IRON_DOOR;
    }
}
