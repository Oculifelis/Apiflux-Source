package dev.oculifelis.apiflux.energy;

import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/** How the Kinetic Dynamo's FE reaches a machine's storage, past the RF gate. */
public final class EnergyAccess {
    private EnergyAccess() {}

    @Nullable
    public static IEnergyStorage unwrap(@Nullable IEnergyStorage storage) {
        return storage instanceof GatedEnergyStorage gated ? gated.inner() : storage;
    }
}
