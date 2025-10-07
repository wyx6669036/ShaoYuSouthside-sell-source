package dev.diona.southside.module.modules.misc;

import com.google.common.collect.Lists;
import dev.diona.southside.Southside;
import dev.diona.southside.event.events.EventPacket;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.util.misc.TimeHelper;
import jnic.JNICInclude;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.play.server.SPacketPlayerListItem;

import java.util.List;

@JNICInclude
public class AntiStaff extends Module {
    public boolean autoLeaveGame = true;
    public boolean keepArmor = true;

    private final TimeHelper timer = new TimeHelper();
    private static final List<String> staff = Lists.newArrayList("weiler_", "小布丁qwq", "艾森啊", "gotnumb", "swemOG", "桃子OTM", "Wighterr", "China丶旭梦", "血樱丶星梦", "Toxic_AslGy", "仙阁灬特色", "TNT丶UFO", "小符xfu360", "xPir4te_", "落花榭",
            "Bir__yezi138", "欲生北茶丿年糕", "抖音搜MC小饭", "LaoZiKaiG_QS", "CN_HYP_印花", "刀客塔", "CK_87", "Toxic_Yuuki", "MxyxPlays", "zoay", "抖音搜兴龙睡不着", "Cloudy_C", "Lucky陌北");
    public AntiStaff(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
    }
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SPacketPlayerListItem) {
            SPacketPlayerListItem packet = (SPacketPlayerListItem) e.getPacket();

            if (packet.getAction() == SPacketPlayerListItem.Action.ADD_PLAYER) {
                for (SPacketPlayerListItem.AddPlayerData data : packet.getEntries()) {
                    if (data.getProfile() != null) {
                        String name = data.getProfile().getName();
                        if (staff.contains(name) && timer.delay(1000)) {
                            Southside.INSTANCE.showNotification(name + " is watching you!", 5000);

                            if (autoLeaveGame) {
                                if (keepArmor) {
                                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 5, 0, ClickType.PICKUP, mc.player);
                                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
                                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 7, 0, ClickType.PICKUP, mc.player);
                                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 8, 0, ClickType.PICKUP, mc.player);
                                }

                                mc.player.sendChatMessage("/hub");
                            }
                            timer.reset();
                            break;
                        }
                    }
                }
            }
        }
    }
}