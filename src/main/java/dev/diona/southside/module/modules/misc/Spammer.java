package dev.diona.southside.module.modules.misc;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import dev.diona.southside.event.EventState;
import dev.diona.southside.event.events.MotionEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.util.misc.MathUtil;
import dev.diona.southside.util.misc.TimerUtil;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Spammer extends Module {
    private final Random random = new Random();
    private final TimerUtil timer = new TimerUtil();
    private final List<String> messages = new ArrayList<String>() {{
        add("大家好我是新疆爱摘棉花的曹源，659001201005183237是我的身份证！");
        add("大家好我是曹源亲哥曹诚钦，我的弟弟曹源喜欢在网上耀武扬威！659001199912243213是我的身份证！");
        add("大家好我是曹源亲爹曹新卫，659001197308213211是我的身份证，我宣布人人都是我曹新卫！");
        add("大家好我是曹源亲妈梁晶（死亡版），65400119751202072X是我的身份证！");
        add("ShaoYuNB 我曹源最擅长诈骗背刺拉黑意淫");
        add("曹源曹新卫曹诚钦梁晶全都死了，让我们大家为一家子棉花默哀。");
        add("我曹源最爱在互联网上碰瓷，我天下无敌不服来战！");
        add("你这个客户端给我曹源帝看笑了，什么客户端也敢跟你曹源帝战？");
        add("我曹源最爱摘棉花了，新疆人都是神，大家一起来摘棉花！");
        add("我曹源觉得你漏防了！");
        add("我曹新卫觉得你漏防了！");
        add("我梁晶觉得你漏防了！");
        add("我曹诚钦觉得你漏防了！");
        add("我曹源最擅长诈骗背刺拉黑意淫，你能把我怎么样？");
        add("欢迎大家光临我曹家的佳美超市！");
        add("我是狂笑的蛇陈安健，我是花雨庭反作弊兼Hack Lunar端制作者！");
        add("我是狂笑的蛇陈安健，我使用Hack Lunar不会Ban！因为我制作了花雨庭反作弊！");
        add("你知道吗，我是狂笑的蛇陈安健！我制作了花雨庭反作弊，误封你们！");
        add("不如我狂笑的蛇将写散文制作的Hack Lunar，我很稳定因为花雨庭反作弊就是出自本人之手！");
        add("兄弟，你听我说！我不会被封，因为我是狂笑的蛇，我制作了花雨庭反作弊！");
        add("我是狂笑的蛇陈安健，我疯狂巴结花雨庭的技术！");
        add("陈安健制作的Hack Lunar依然稳定绕过花雨庭，因为我是狂笑的蛇！");
        add("我是大名鼎鼎的狂笑的蛇将写散文本人陈安健，我能无视反作弊飘主播因为我制作了花雨庭反作弊！");
        add("羡慕我不会被封吗？因为我狂笑的蛇本人陈安健制作了花雨庭的反作弊！");
        add("B站搜索狂笑的蛇将写散文，看我陈安健精彩集锦！");
        add("我狂笑的蛇陈安健能无视反作弊，因为花雨庭反作弊就是我做的！");
        add("哈哈你知道你为什么会被误封吗？因为我狂笑的蛇陈安健制作了花雨庭反作弊！");
    }};
    public final Slider delaySlider = new Slider("Delay", 5.0, 1.0, 30.0, 1.0);

    public Spammer(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
    }

    @Override
    public boolean onEnable() {
        super.onEnable();
        timer.reset();
        if (Minecraft.getMinecraft().player == null) {
            return false;
        }

        return true;
    }

    @EventListener
    public void onMotion(MotionEvent event) {
        if (event.getState() != EventState.PRE) return;
        if (Minecraft.getMinecraft().player == null) return;
        double delayValue = delaySlider.getValue().intValue() * 1000;
        if (timer.hasReached((long) delayValue)) {
            sendRandomMessage();
            timer.reset();
        }
    }

    private void sendRandomMessage() {
        if (messages.isEmpty()) return;
        String message = messages.get(random.nextInt(messages.size()));
        Minecraft.getMinecraft().player.sendChatMessage(message);
    }
}