package dev.oculifelis.apiflux.energy;

import com.accbdd.complicated_bees.registry.BlockEntitiesRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/**
 * Puts a {@link GatedEnergyStorage} in front of every Complicated Bees block entity that takes FE.
 *
 * Registered at HIGHEST priority, so NeoForge asks this provider before Complicated Bees' own and keeps its
 * answer. It hands back the very storage Complicated Bees would have, wrapped; with the config off the wrapper
 * changes nothing. These are the four FE inputs Complicated Bees 3.6 for 1.21.1 registers. The generators only
 * give FE out and are left alone.
 *
 * <p>Manacomb (Botania mana for the same machines) carries its own copy of this gate but does not register it
 * when Apiflux is loaded, so there is only ever one gate in front of a machine and this switch is the one in
 * use. Manacomb's mana takes the machine's storage straight from the block entity, past this gate.
 */
public final class CbEnergyGate {
    private CbEnergyGate() {}

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BlockEntitiesRegistration.CENTRIFUGE_ENTITY.get(),
                (be, side) -> gate(be.getEnergyHandler()));
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BlockEntitiesRegistration.GYROFUGE_CONTROLLER_BLOCK_ENTITY.get(),
                (be, side) -> gate(be.getEnergyHandler()));
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BlockEntitiesRegistration.GYROFUGE_ENERGY_CELL_BLOCK_ENTITY.get(),
                (be, side) -> gate(be.getEnergyHandler()));
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BlockEntitiesRegistration.MELLARIUM_ENERGY_CELL_BLOCK_ENTITY.get(),
                (be, side) -> gate(be.getEnergyHandler()));
    }

    @Nullable
    private static IEnergyStorage gate(@Nullable IEnergyStorage storage) {
        return storage == null ? null : new GatedEnergyStorage(storage);
    }
}
