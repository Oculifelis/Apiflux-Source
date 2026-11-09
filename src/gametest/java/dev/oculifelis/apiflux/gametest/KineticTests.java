package dev.oculifelis.apiflux.gametest;

import com.accbdd.complicated_bees.block.entity.CentrifugeBlockEntity;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import dev.oculifelis.apiflux.ApifluxConfig;
import dev.oculifelis.apiflux.compat.create.CreateCompat;
import dev.oculifelis.apiflux.compat.create.KineticDynamoBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Rotation power: creative motor -> Kinetic Dynamo -> centrifuge. */
@GameTestHolder("apiflux_gametest")
@PrefixGameTestTemplate(false)
public final class KineticTests {
    private static final BlockPos MOTOR = new BlockPos(1, 1, 3);
    private static final BlockPos DYNAMO = new BlockPos(2, 1, 3);
    private static final BlockPos MACHINE = new BlockPos(3, 1, 3);

    /** NeoForge registers every holder class, so with Create absent these pass without running. */
    private static boolean skip(GameTestHelper helper) {
        if (ModList.get().isLoaded("create")) return false;
        helper.succeed();
        return true;
    }

    private static void build(GameTestHelper helper) {
        helper.setBlock(MOTOR, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("create", "creative_motor"))
                .defaultBlockState().setValue(DirectionalKineticBlock.FACING, Direction.EAST));
        helper.setBlock(DYNAMO, CreateCompat.KINETIC_DYNAMO.get().defaultBlockState()
                .setValue(DirectionalKineticBlock.FACING, Direction.WEST));
        helper.setBlock(MACHINE, GateTests.cb("centrifuge"));
    }

    @GameTest(template = "arena", timeoutTicks = 200)
    public static void dynamoFeedsCentrifuge(GameTestHelper helper) {
        if (skip(helper)) return;
        build(helper);
        helper.succeedWhen(() -> {
            KineticDynamoBlockEntity dynamo = helper.getBlockEntity(DYNAMO);
            CentrifugeBlockEntity be = helper.getBlockEntity(MACHINE);
            helper.assertTrue(dynamo.getSpeed() != 0, "dynamo not turning");
            // Creative motor default 16 RPM x 0.5 FE/RPM = 8 FE/t.
            helper.assertTrue(dynamo.getDelivered() == 8, "dynamo gives " + dynamo.getDelivered() + " FE/t, expected 8");
            helper.assertTrue(be.getStoredPower() >= 80, "centrifuge has only " + be.getStoredPower() + " FE");
        });
    }

    /** The dynamo still feeds the machine with outside FE refused. Own batch: it holds the global switch on. */
    @GameTest(template = "arena", timeoutTicks = 200, batch = "apiflux_gate_held")
    public static void dynamoPassesGate(GameTestHelper helper) {
        if (skip(helper)) return;
        build(helper);
        ApifluxConfig.setRfInputDisabled(true);
        helper.succeedWhen(() -> {
            CentrifugeBlockEntity be = helper.getBlockEntity(MACHINE);
            helper.assertTrue(be.getStoredPower() >= 80, "centrifuge has only " + be.getStoredPower() + " FE");
            ApifluxConfig.setRfInputDisabled(false);
        });
    }
}
