package dev.oculifelis.apiflux.compat.create;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

/**
 * Takes rotation through the shaft on its FACING side and gives FE out of the other five. A casing body with
 * a bearing collar; the half shaft is drawn by Create (renderer or Flywheel visual).
 */
public class KineticDynamoBlock extends DirectionalKineticBlock implements IBE<KineticDynamoBlockEntity> {
    // Modelled facing UP: body 0..12, collar 12..14, the shaft shows from 14 to the face.
    private static final VoxelShaper SHAPE = VoxelShaper.forDirectional(Shapes.or(
            box(0, 0, 0, 16, 12, 16),
            box(4, 12, 4, 12, 14, 12)), Direction.UP);

    public KineticDynamoBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(FACING));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Point the shaft at a neighbouring shaft or cog if there is one, unless sneaking.
        Direction preferred = getPreferredFacing(context);
        if (preferred != null && (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown())) {
            return defaultBlockState().setValue(FACING, preferred);
        }
        return super.getStateForPlacement(context);
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public Class<KineticDynamoBlockEntity> getBlockEntityClass() {
        return KineticDynamoBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KineticDynamoBlockEntity> getBlockEntityType() {
        return CreateCompat.KINETIC_DYNAMO_BE.get();
    }
}
