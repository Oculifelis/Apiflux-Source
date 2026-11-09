package dev.oculifelis.apiflux.energy;

import dev.oculifelis.apiflux.ApifluxConfig;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * What other mods see of a Complicated Bees machine's energy: the machine's own storage, except that with
 * {@code rfInputDisabled} on it accepts nothing. The Kinetic Dynamo gets past it through
 * {@link EnergyAccess#unwrap}.
 */
public record GatedEnergyStorage(IEnergyStorage inner) implements IEnergyStorage {
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return ApifluxConfig.rfInputDisabled() ? 0 : inner.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public boolean canReceive() {
        return !ApifluxConfig.rfInputDisabled() && inner.canReceive();
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return inner.extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        return inner.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return inner.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return inner.canExtract();
    }
}
