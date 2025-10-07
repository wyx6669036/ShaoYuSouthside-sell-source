package dev.diona.southside.module.modules.world;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.diona.southside.event.EventState;
import dev.diona.southside.event.events.*;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.util.misc.MathUtil;
import dev.diona.southside.util.player.*;
import dev.diona.southside.util.render.RenderUtil;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Scaffold extends Module {
    public static Scaffold INSTANCE;
    public final Switch fullSprint = new Switch("Full Sprint", false);
    public final Switch keepFov = new Switch("Keep fov", false);
    public final Switch switchBack = new Switch("Switch Back", true);
    public final Slider fovValue = new Slider("Fov", 1.2, 0.8, 1.5, 0.05);
    public final Switch bw = new Switch("Bed Wars", false);
    public final Switch dbgV = new Switch("Debug", false);
    public final Switch renderTargetPos = new Switch("Render Target Pos", true);
    public final Switch renderClickPos = new Switch("Render Click Pos", false);

    public static final List<Block> invalidBlocks = Arrays.asList(
            Blocks.ENCHANTING_TABLE, Blocks.CHEST, Blocks.ENDER_CHEST,
            Blocks.TRAPPED_CHEST, Blocks.ANVIL, Blocks.SAND, Blocks.WEB, Blocks.TORCH,
            Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.WATERLILY, Blocks.DISPENSER,
            Blocks.STONE_PRESSURE_PLATE, Blocks.WOODEN_PRESSURE_PLATE, Blocks.NOTEBLOCK,
            Blocks.DROPPER, Blocks.TNT, Blocks.STANDING_BANNER, Blocks.WALL_BANNER,
            Blocks.REDSTONE_TORCH, Blocks.CRAFTING_TABLE
    );

    public int baseY = -1;
    private int slot;
    private boolean canPlace;
    public int bigVelocityTick = 0;
    private BlockData blockPos;
    private BlockPos lastBlockPos;
    private EnumFacing lastEnumFacing;
    private int rotateCount = 0;

    public Scaffold(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
        INSTANCE = this;
    }

    @Override
    public boolean onEnable() {
        if (mc.player == null) return true;
        lastBlockPos = null;
        blockPos = null;
        this.slot = mc.player.inventory.currentItem;
        baseY = -1;
        canPlace = true;
        bigVelocityTick = 0;
        return true;
    }

    @Override
    public boolean onDisable() {
        if (mc.player == null) return true;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        mc.player.inventory.currentItem = slot;
        return true;
    }

    @EventListener
    public void onRender2D(NewRender2DEvent event) {
        ScaledResolution sr = event.getScaledResolution();
        int count = getBlockCount();
        String text = String.format(TextFormatting.WHITE + "Blocks: %s",
                (count > 64 ? TextFormatting.GREEN : count > 0 ? TextFormatting.YELLOW : TextFormatting.RED) + String.valueOf(count));
        mc.fontRenderer.drawStringWithShadow(text,
                (float) sr.getScaledWidth() / 2 - (float) mc.fontRenderer.getStringWidth(text) / 2,
                (float) sr.getScaledHeight() / 2 - 30, -1);
    }

    @EventListener
    public void onMoveInput(MoveInputEvent event) {
        if (mc.player.onGround && event.getMoveForward() > 0 && !mc.gameSettings.keyBindJump.isKeyDown()) {
            event.setJump(true);
        }
    }

    private void place(boolean rotate) {
        if (!canPlace) return;
        if (!InventoryUtil.switchBlock()) return;

        if (blockPos != null) {
            if (mc.playerController.processRightClickBlock(mc.player, mc.world, blockPos.pos(),
                    blockPos.facing(), getVec3(blockPos.pos(), blockPos.facing()), EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }
            if (this.blockPos != null) {
                this.lastBlockPos = this.blockPos.pos();
            }
            if (this.blockPos.facing() != null) {
                this.lastEnumFacing = this.blockPos.facing();
            }
            blockPos = null;
            if (rotate) {
                RotationUtil.setTargetRotation(new Rotation(mc.player.rotationYaw, mc.player.rotationPitch), 0);
            }
        }
    }

    @EventListener
    public void onPacket(PacketEvent event) {
        if (mc.player == null) return;
        if (event.getPacket() instanceof SPacketEntityVelocity velocity &&
                velocity.getEntityID() == mc.player.getEntityId()) {
            double strength = new Vec3d(velocity.getMotionX() / 8000D, 0, velocity.getMotionZ() / 8000D).length();
            if (strength >= 1.5D) {
                ChatUtil.info("你也是要飞了: " + strength);
                bigVelocityTick = 60;
            }
        }
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        BlockPos playerPos = new BlockPos(mc.player);
        IBlockState state = mc.world.getBlockState(playerPos);
        if (state.getBlock() != Blocks.AIR && state.getBlock().isPassable(mc.world, playerPos)) return;
        if (mc.player.ticksExisted <= 5) return;

        if (bigVelocityTick > 0) bigVelocityTick--;
        if (mc.player.onGround && bigVelocityTick <= 30) bigVelocityTick = 0;

        double motion = Math.max(Math.abs(mc.player.motionX), Math.abs(mc.player.motionZ));
        if (!fullSprint.getValue()) place(true);

        // Movement condition checks
        if (!fullSprint.getValue() && motion <= 0.4) {
            if (Math.abs(mc.player.motionX) < 0.03 || Math.abs(mc.player.motionZ) < 0.03) {
                if (!mc.player.onGround && mc.player.offGroundTicks <= 2) return;
            } else {
                if (!mc.player.onGround && mc.player.offGroundTicks <= 1) return;
            }
        }

        if (mc.player.onGround) {
            baseY = (int) Math.floor(mc.player.posY - 1);
        }

        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            baseY = (int) (mc.player.posY - 1);
        }

        // Find and place blocks
        blockPos = mc.world.getBlockState(mc.player.getPos().down()).getBlock() instanceof BlockAir ? getBlockData(new BlockPos(mc.player.posX, baseY, mc.player.posZ)) : null;

        if (!InventoryUtil.switchBlock()) return;
        canPlace = !mc.gameSettings.keyBindJump.isKeyDown() || mc.player.offGroundTicks >= 2;
        if (mc.gameSettings.keyBindJump.isKeyDown() && !canPlace) return;

        if (blockPos != null) {
            boolean reachable = true;
            if (mc.player.motionY < -0.1) {
                FallingPlayer fallingPlayer = new FallingPlayer(mc.player);
                fallingPlayer.calculate(2);
                if (blockPos.pos().getY() > fallingPlayer.getY()) reachable = false;
            }

            // Rotation handling
            if ((!reachable || bigVelocityTick > 0 || fullSprint.getValue()) && rotateCount <= 8) {
                Rotation rotation = RotationUtil.getRotationBlock(blockPos.pos(), 0F);
                if (dbgV.getValue()) ChatUtil.info("working " + rotateCount);

                mc.playerStuckTicks++;
                rotateCount++;
                mc.getConnection().sendPacket(new CPacketPlayer.Rotation(
                        rotation.yaw, rotation.pitch, mc.player.onGround
                ));
                place(false);
                this.onUpdate(event); // Recursive call for fast placement
            } else {
                Rotation rotation = RotationUtil.getRotationBlock(blockPos.pos(), 1F);
                rotateCount = 0;
                RotationUtil.setTargetRotation(rotation, 0);
            }
        }

        // Disable if spectator
        if (mc.player.isSpectator()) this.setEnable(false);
    }

    @EventListener
    public void onMotion(MotionEvent event) {
        if (event.getState() == EventState.POST && switchBack.getValue() &&
                mc.player.inventory.currentItem != slot) {
            mc.player.connection.sendPacket(new CPacketKeepAlive(0));
            mc.player.inventory.currentItem = slot;
        }
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        if ((blockPos != null || lastBlockPos != null) && (renderTargetPos.getValue() || renderClickPos.getValue())) {
            // Render target position (green/red box)
            if (renderTargetPos.getValue()) {
                BlockPos targetPos = lastBlockPos == null ?
                        blockPos.pos().offset(blockPos.facing()) :
                        lastBlockPos.offset(lastEnumFacing);

                if (targetPos != null) {
                    if (mc.world.getBlockState(targetPos).getBlock() instanceof BlockAir) {
                        RenderUtil.drawOutlinedBoundingBox(targetPos, 2, new Color(255, 0, 0, 120));
                    } else {
                        RenderUtil.boundingESPBoxFilled(
                                mc.world.getBlockState(targetPos).getSelectedBoundingBox(mc.world, targetPos),
                                new Color(0, 255, 0, 120)
                        );
                    }
                }
            }

            // Render click position (red box)
            if (renderClickPos.getValue() && blockPos != null) {
                RenderUtil.boundingESPBoxFilled(
                        mc.world.getBlockState(blockPos.pos()).getSelectedBoundingBox(mc.world, blockPos.pos()),
                        new Color(255, 10, 10, 120)
                );
            }
        }
    }

    private BlockData getBlockData(BlockPos pos) {
        if (getPos(pos) == null) {
            if (getBlockPos() == null) return null;

            if (getPlaceSide(getBlockPos()) == null) return null;

            return new BlockData(getBlockPos(), getPlaceSide(getBlockPos()));
        } else {
            return getPos(pos);
        }
    }

    private EnumFacing getPlaceSide(BlockPos blockPos) {
        List<BlockData> blockData = new ArrayList<>();

        BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);

        if (isAirBlock(blockPos.east()) && !blockPos.east().equals(pos)) {
            blockData.add(new BlockData(blockPos.east(), EnumFacing.EAST));
        }


        if (isAirBlock(blockPos.north()) && !blockPos.north().equals(pos)) {
            blockData.add(new BlockData(blockPos.north(), EnumFacing.NORTH));
        }

        if (isAirBlock(blockPos.south()) && !blockPos.south().equals(pos)) {
            blockData.add(new BlockData(blockPos.south(), EnumFacing.SOUTH));
        }

        if (isAirBlock(blockPos.west()) && !blockPos.west().equals(pos)) {
            blockData.add(new BlockData(blockPos.west(), EnumFacing.WEST));
        }

        if (blockData.isEmpty()) return null;

        blockData.sort(Comparator.comparingDouble(vec3 -> {
            final double d0 = pos.getX() - vec3.pos().getX();
            final double d1 = pos.getY() - vec3.pos().getY();
            final double d2 = pos.getZ() - vec3.pos().getZ();
            return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
        }));

        return blockData.get(0).facing();
    }


    private BlockPos getBlockPos() {

        BlockPos playerPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);

        ArrayList<BlockPos> positions = new ArrayList<>();

        Map<BlockPos, Block> searchBlock = searchBlocks(5);
        for (Map.Entry<BlockPos, Block> block : searchBlock.entrySet()) {
            if (isPosSolid(block.getKey())) {
                positions.add(block.getKey());
            }
        }

        positions.removeIf(pos -> mc.player.getDistance(pos) > mc.playerController.getBlockReachDistance() || pos.getY() >= playerPos.getY());

        if (positions.isEmpty()) return null;

        positions.sort(Comparator.comparingDouble(vec3 -> {
            final double d0 = playerPos.getX() - vec3.getX();
            final double d1 = playerPos.getY() - vec3.getY();
            final double d2 = playerPos.getZ() - vec3.getZ();
            return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
        }));

        return positions.get(0);
    }

    public boolean isAirBlock(BlockPos blockPos) {
        Block block = Minecraft.getMinecraft().world.getBlockState(blockPos).getBlock();
        return block instanceof BlockAir;
    }

    public Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    public Map<BlockPos, Block> searchBlocks(int radius) {
        Map<BlockPos, Block> blocks = new HashMap<>();
        EntityPlayer player = mc.player;
        if (player == null) {
            return blocks;
        }
        for (int x = radius; x >= -radius + 1; x--) {
            for (int y = radius; y >= -radius + 1; y--) {
                for (int z = radius; z >= -radius + 1; z--) {
                    BlockPos blockPos = new BlockPos(player.posX + x, player.posY + y, player.posZ + z);
                    Block block = getBlock(blockPos);
                    if (block == null) {
                        continue;
                    }
                    blocks.put(blockPos, block);
                }
            }
        }
        return blocks;
    }

    public BlockData getPos(BlockPos pos) {
        if (isPosSolid(pos.add(-1, 0, 0))) {
            return new BlockData(pos.add(-1, 0, 0), EnumFacing.EAST);
        } else if (isPosSolid(pos.add(1, 0, 0))) {
            return new BlockData(pos.add(1, 0, 0), EnumFacing.WEST);
        } else if (isPosSolid(pos.add(0, 0, 1))) {
            return new BlockData(pos.add(0, 0, 1), EnumFacing.NORTH);
        } else if (isPosSolid(pos.add(0, 0, -1))) {
            return new BlockData(pos.add(0, 0, -1), EnumFacing.SOUTH);
        } else if (isPosSolid(pos.add(0, -1, 0))) {
            return new BlockData(pos.add(0, -1, 0), EnumFacing.UP);
        }
        return null;
    }

    public boolean isPosSolid(BlockPos pos) {
        final Block block = mc.world.getBlockState(pos).getBlock();
        return !Arrays.asList(
                Blocks.ANVIL,
                Blocks.AIR,
                Blocks.WATER,
                Blocks.FIRE,
                Blocks.FLOWING_WATER,
                Blocks.LAVA,
                Blocks.SKULL,
                Blocks.TRAPPED_CHEST,
                Blocks.FLOWING_LAVA,
                Blocks.CHEST,
                Blocks.ENCHANTING_TABLE,
                Blocks.ENDER_CHEST,
                Blocks.CRAFTING_TABLE
        ).contains(block);
    }

    public static Vec3d getVec3(BlockPos pos, EnumFacing face) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        // Add randomness based on facing direction
        if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
            x += MathUtil.getRandomInRange(0.3, -0.3);
            z += MathUtil.getRandomInRange(0.3, -0.3);
        } else {
            y += MathUtil.getRandomInRange(0.3, -0.3);
        }
        if (face == EnumFacing.WEST || face == EnumFacing.EAST) {
            z += MathUtil.getRandomInRange(0.3, -0.3);
        }
        if (face == EnumFacing.SOUTH || face == EnumFacing.NORTH) {
            x += MathUtil.getRandomInRange(0.3, -0.3);
        }
        return new Vec3d(x, y, z);
    }

    private boolean isValid(final Item item) {
        return item instanceof ItemBlock && !invalidBlocks.contains(((ItemBlock) item).getBlock());
    }

    public void getBlock(int switchSlot) {
        for (int i = 9; i < 45; ++i) {
            if (mc.player.inventoryContainer.getSlot(i).getHasStack() &&
                    (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory)) {

                ItemStack is = mc.player.inventoryContainer.getSlot(i).getStack();
                if (is.getItem() instanceof ItemBlock) {
                    ItemBlock block = (ItemBlock) is.getItem();
                    if (isValid(block)) {
                        if (36 + switchSlot != i) {
                            InventoryUtil.swap(i, switchSlot);
                        }
                        break;
                    }
                }
            }
        }
    }

    public int getBlockCount() {
        int blockCount = 0;
        for (int i = 9; i < 45; ++i) {
            if (mc.player.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.player.inventoryContainer.getSlot(i).getStack();
                if (is.getItem() instanceof ItemBlock) {
                    ItemBlock block = (ItemBlock) is.getItem();
                    if (isValid(block)) blockCount += is.getCount();
                }
            }
        }
        return blockCount;
    }
}
record BlockData(BlockPos pos, EnumFacing facing) {
}
