package dev.oculifelis.apiflux.gametest;

import com.accbdd.complicated_bees.block.entity.CentrifugeBlockEntity;
import dev.oculifelis.apiflux.ApifluxConfig;
import dev.oculifelis.apiflux.energy.GatedEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The "instead of RF" switch: outside FE is taken while it is off and refused while it is on. */
@GameTestHolder("apiflux_gametest")
@PrefixGameTestTemplate(false)
public final class GateTests {
    static final BlockPos MACHINE = new BlockPos(3, 1, 3);

    static Block cb(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("complicated_bees", id));
    }

    @GameTest(template = "arena")
    public static void rfGate(GameTestHelper helper) {
        helper.setBlock(MACHINE, cb("centrifuge"));
        BlockPos abs = helper.absolutePos(MACHINE);
        IEnergyStorage outside = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, abs, null);
        helper.assertTrue(outside instanceof GatedEnergyStorage, "centrifuge FE capability is not gated: " + outside);
        CentrifugeBlockEntity be = helper.getBlockEntity(MACHINE);

        ApifluxConfig.setRfInputDisabled(false);
        int taken = outside.receiveEnergy(100, false);
        helper.assertTrue(taken == 100 && be.getStoredPower() == 100, "gate off: took " + taken + ", stored " + be.getStoredPower());

        ApifluxConfig.setRfInputDisabled(true);
        try {
            int refused = outside.receiveEnergy(100, false);
            helper.assertTrue(refused == 0 && !outside.canReceive(), "gate on: outside FE still taken (" + refused + ")");
            helper.assertTrue(be.getStoredPower() == 100, "gate on: stored changed to " + be.getStoredPower());
        } finally {
            ApifluxConfig.setRfInputDisabled(false);
        }
        helper.succeed();
    }
}
