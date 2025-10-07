package dev.diona.southside.module.modules.combat;

import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.diona.southside.event.events.Render3DEvent;
import dev.diona.southside.event.events.UpdateEvent;
import dev.diona.southside.module.Category;
import dev.diona.southside.module.Module;
import dev.diona.southside.util.misc.MathUtil;
import dev.diona.southside.util.misc.TimerUtil;
import dev.diona.southside.util.player.InventoryUtil;
import dev.diona.southside.util.player.RayCastUtil;
import dev.diona.southside.util.player.Rotation;
import dev.diona.southside.util.player.RotationUtil;
import dev.diona.southside.util.render.RenderUtil;
import me.bush.eventbus.annotation.EventListener;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;

import java.awt.*;

/**
 * @author EzDiaoL
 * @since 6/4/2024
 */
public class Surround extends Module {
    public final Slider delay = new Slider("Delay", 50, 0, 1000, 10);
    public final Slider layers = new Slider("Layers", 1, 1, 5, 1);
    public final Dropdown rotationMode = new Dropdown("Rotation Mode", "Normal", "Normal", "Smooth");
    public final Slider minRotationSpeed = new Slider("Min Rotation Speed", 180, 0, 180, 0.1);
    public final Slider maxRotationSpeed = new Slider("Max Rotation Speed", 180, 0, 180, 0.1);
    public final Switch rotation = new Switch("Rotation", true);
    public final Switch rayCast = new Switch("Ray cast", true);
    public final Switch render = new Switch("Render", true);
    public final Switch legit = new Switch("Legit", true);

    public Surround(String name, String description, Category category, boolean visible) {
        super(name, description, category, visible);
    }

    private final TimerUtil placeTimer = new TimerUtil();
    private BlockPos[] surroundPositions;

    @Override
    public boolean onEnable() {
        return super.onEnable();
    }

    @Override
    public boolean onDisable() {
        return super.onDisable();
    }

    @EventListener
    public void onUpdate(UpdateEvent event) {
        BlockPos playerPos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
        int layersToPlace = this.layers.getValue().intValue();

        if (placeTimer.hasReached(delay.getValue().longValue())) {
            for (int currentLayer = 0; currentLayer < layersToPlace; currentLayer++) {
                this.surroundPositions = new BlockPos[]{
                        playerPos.add(0, currentLayer, -1), // North
                        playerPos.add(0, currentLayer, 1),  // South
                        playerPos.add(1, currentLayer, 0),  // East
                        playerPos.add(-1, currentLayer, 0)  // West
                };

                for (BlockPos targetPos : this.surroundPositions) {
                    if (!canPlaceBlock(targetPos)) continue;

                    EnumFacing bestFacing = findBestFacing(targetPos);
                    if (bestFacing == null) continue;

                    Rotation rotation = RotationUtil.getRotationBlock(targetPos, 0);
                    if (this.rotation.getValue() && InventoryUtil.switchBlock()) {
                        float speed = MathUtil.getRandomInRange(minRotationSpeed.getValue().floatValue(), maxRotationSpeed.getValue().floatValue());
                        rotation = RotationUtil.turn(rotation, rotationMode.getMode(), speed);
                        if (rotation != null) {
                            RotationUtil.setTargetRotation(rotation, 0);
                        }
                    }

                    if (rayCast.getValue() && RayCastUtil.overBlock((this.rotation.getValue() && rotation != null) ? RotationUtil.serverRotation : new Rotation(mc.player.rotationYaw, mc.player.rotationPitch), bestFacing, targetPos, true)) {
                        continue;
                    }

                    if (legit.getValue()) {
                        mc.rightClickMouse();
                    } else {
                        if (mc.playerController.processRightClickBlock(mc.player, mc.world, targetPos, bestFacing, getHitVector(targetPos, bestFacing), EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS) {
                            mc.player.swingArm(EnumHand.MAIN_HAND);
                        }
                    }
                    placeTimer.reset();
                }
            }
        }
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {
        for (BlockPos targetPos : this.surroundPositions) {
            if (render.getValue()) {
                if (mc.world.getBlockState(targetPos).getBlock() instanceof BlockAir) {
                    RenderUtil.drawOutlinedBoundingBox(targetPos, 2, new Color(255, 0, 0, 120));
                } else {
                    RenderUtil.boundingESPBoxFilled(mc.world.getBlockState(targetPos).getSelectedBoundingBox(mc.world, targetPos), new Color(0, 255, 0, 120));
                }
            }
        }
    }

    public Vec3d getHitVector(BlockPos pos, EnumFacing facing) {
        double x = (double) pos.getX() + 0.5, y = (double) pos.getY() + 0.5, z = (double) pos.getZ() + 0.5;

        switch (facing) {
            case DOWN: y -= 0.5; break;
            case UP: y += 0.5; break;
            case NORTH: z -= 0.5; break;
            case SOUTH: z += 0.5; break;
            case WEST: x -= 0.5; break;
            case EAST: x += 0.5; break;
        }

        return new Vec3d(x, y, z);
    }

    private boolean canPlaceBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().isReplaceable(mc.world, pos) && (mc.world.getBlockState(pos).getBlock() instanceof BlockAir);
    }

    private EnumFacing findBestFacing(BlockPos targetPos) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos adjacentPos = targetPos.offset(facing);
            if (!mc.world.getBlockState(adjacentPos).getBlock().isReplaceable(mc.world, adjacentPos) && !(mc.world.getBlockState(adjacentPos).getBlock() instanceof BlockAir)) {
                return facing;
            }
        }
        return null;
    }
}
