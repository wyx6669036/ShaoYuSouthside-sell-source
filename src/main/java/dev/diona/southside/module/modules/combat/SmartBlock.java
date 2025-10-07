package dev.diona.southside.module.modules.combat;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.diona.southside.Southside;
import dev.diona.southside.event.events.NewRender2DEvent;
import dev.diona.southside.event.events.UpdateEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.module.modules.render.OldHitting;
import dev.diona.southside.util.chat.Chat;
import dev.diona.southside.util.player.ChatUtil;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class SmartBlock extends Module {
    private static SmartBlock INSTANCE;
    private boolean blocking = false;
    public SmartBlock(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
        INSTANCE = this;
    }
    public Slider RangeValue = new Slider("Range", 5, 1, 16, 1);
    private final Switch textValue = new Switch("Display Text", true);
    public final Switch DebugValue = new Switch("Debug", false);


    @EventListener
    public void onUpdate(UpdateEvent event) {
        blocking = false;
        if (mc.player.isHandActive() || !(mc.player.inventory.getCurrentItem().getItem() instanceof ItemSword)) return;

        double detectionRange = RangeValue.getValue().intValue();

        AxisAlignedBB detectionBox = new AxisAlignedBB(
                mc.player.posX - detectionRange, mc.player.posY - detectionRange, mc.player.posZ - detectionRange,
                mc.player.posX + detectionRange, mc.player.posY + detectionRange, mc.player.posZ + detectionRange
        );

        if (!mc.world.getEntitiesWithinAABB(EntityTNTPrimed.class, detectionBox).isEmpty() || !mc.world.getEntitiesWithinAABB(EntityArrow.class, detectionBox).isEmpty()) {
            mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
            blocking = true;
            if ((Boolean) this.DebugValue.getValue()) {
                ChatUtil.info("Block");
            }
        }
    }

    public static boolean isBlocking() {
        return INSTANCE.isEnabled() && INSTANCE.blocking;
    }

    @EventListener
    public void onRender2D(NewRender2DEvent event) {
        ScaledResolution sr = event.getScaledResolution();
        if (blocking && textValue.getValue()) {
            String text = String.format(TextFormatting.YELLOW + "Smart Blocking");
            mc.fontRenderer.drawStringWithShadow(text, (float) sr.getScaledWidth() / 2 - (float) mc.fontRenderer.getStringWidth(text) / 2, (float) sr.getScaledHeight() / 2 - 20, -1);
        }
    }
}
