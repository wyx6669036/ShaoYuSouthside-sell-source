package dev.diona.southside.util.player;

import dev.diona.southside.util.world.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class PlaceInfo {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final BlockPos blockPos;
    private EnumFacing enumFacing;
    private Vec3d vec3;

    public PlaceInfo(BlockPos blockPos, EnumFacing enumFacing) {
        this.blockPos = blockPos;
        this.enumFacing = enumFacing;
    }
    public PlaceInfo(BlockPos blockPos, EnumFacing enumFacing, Vec3d vec3) {
        this.blockPos = blockPos;
        this.enumFacing = enumFacing;
        this.vec3 = vec3;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public EnumFacing getEnumFacing() {
        return enumFacing;
    }

    public void setEnumFacing(EnumFacing enumFacing1) {
        enumFacing = enumFacing1;
    }

    public Vec3d getVec3() {
        return vec3;
    }

    public void setVec3(Vec3d vec3) {
        this.vec3 = vec3;
    }

    public PlaceInfo(BlockPos blockPos, EnumFacing enumFacing, Vec3d vec3, int n) {
        this(blockPos, enumFacing, vec3);
        if ((n & 4) != 0) {
            vec3 = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
        }
    }

    public static PlaceInfo get(BlockPos blockPos) {
        if (BlockUtil.isValidBock(blockPos.add(0, -1, 0))) {
            return new PlaceInfo(blockPos.add(0, -1, 0), EnumFacing.UP, null, 4);
        } else if (BlockUtil.isValidBock(blockPos.add(0, 0, 1))) {
            return new PlaceInfo(blockPos.add(0, 0, 1), EnumFacing.NORTH, null, 4);
        } else if (BlockUtil.isValidBock(blockPos.add(-1, 0, 0))) {
            return new PlaceInfo(blockPos.add(-1, 0, 0), EnumFacing.EAST, null, 4);
        } else if (BlockUtil.isValidBock(blockPos.add(0, 0, -1))) {
            return new PlaceInfo(blockPos.add(0, 0, -1), EnumFacing.SOUTH, null, 4);
        } else {
            return BlockUtil.isValidBock(blockPos.add(1, 0, 0)) ? new PlaceInfo(blockPos.add(1, 0, 0), EnumFacing.WEST, null, 4) : null;
        }
    }


    public static Block getBlock(BlockPos blockPos) {
        IBlockState blockState;
        if (mc.world != null && (blockState = mc.world.getBlockState(blockPos)) != null) {
            return blockState.getBlock();
        }
        return null;
    }

    public static IBlockState getState(BlockPos blockPos) {
        return mc.world.getBlockState(blockPos);
    }

    public static boolean isValidBock(BlockPos blockPos) {
        Block var10000 = getBlock(blockPos);
        if (var10000 != null && var10000.canCollideCheck(getState(blockPos), false)) {
            return mc.world.getWorldBorder().contains(blockPos);
        }
        return false;
    }

    public void setFacing(EnumFacing enumFacing) {
        this.enumFacing = enumFacing;
    }

}

