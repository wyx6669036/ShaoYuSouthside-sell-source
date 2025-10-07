package dev.diona.southside.module.modules.movement;

import dev.diona.southside.event.events.UpdateEvent;
import dev.diona.southside.event.events.WorldEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.diona.southside.util.player.PlaceInfo.getBlock;

public class FastLadder extends Module {

    private static FastLadder INSTANCE;
    public FastLadder(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
        INSTANCE = this;
    }

    private List<BlockPos> blockLadder = new ArrayList<>();
    public boolean cancel = false;
    private boolean normalClimb = false;

    @EventListener
    public void onUpdate(UpdateEvent event) {
        if (!mc.player.isOnLadder()) {
            normalClimb = false;
            cancel = false;
            blockLadder.clear();
        }
        if (!mc.player.isOnLadder() && !cancel) {
            return;
        }
        if (normalClimb && mc.player.isOnLadder()) {
            return;
        }
        if (mc.player.isOnLadder() && mc.gameSettings.keyBindJump.isKeyDown()) {
            blockLadder.clear();
            cancel = false;
            normalClimb = true;
            return;
        }
        for (Map.Entry<BlockPos, Block> entry : searchBlocks(4).entrySet()) {
            BlockPos block = entry.getKey();
            Block value = entry.getValue();
            if (value instanceof BlockLadder) {
                if (!blockLadder.contains(block)) {
                    blockLadder.add(block);
                }
            }
        }
        if (!blockLadder.isEmpty()) {
            for (BlockPos block : blockLadder) {
                mc.getConnection().getNetworkManager().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, block, EnumFacing.DOWN));
            }
            if (mc.player.isOnLadder()) {
                cancel = true;
            }
        }
    }

    @EventListener
    public void onWorld(WorldEvent event) {
        blockLadder.clear();
    }

    public Map<BlockPos, Block> searchBlocks(int radius) {
        Map<BlockPos, Block> blocks = new HashMap<>();

        EntityPlayerSP thePlayer = mc.player;
        if (thePlayer == null) {
            return blocks;
        }

        for (int x = radius; x >= -radius + 1; x--) {
            for (int y = radius; y >= -radius + 1; y--) {
                for (int z = radius; z >= -radius + 1; z--) {
                    BlockPos blockPos = new BlockPos(thePlayer.posX + x, thePlayer.posY + y, thePlayer.posZ + z);
                    Block block = getBlock(blockPos);
                    if (block != null) {
                        blocks.put(blockPos, block);
                    }
                }
            }
        }

        return blocks;
    }
}
