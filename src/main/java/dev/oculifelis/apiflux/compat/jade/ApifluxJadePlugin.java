package dev.oculifelis.apiflux.compat.jade;

import dev.oculifelis.apiflux.Apiflux;
import dev.oculifelis.apiflux.ApifluxConfig;
import dev.oculifelis.apiflux.energy.GatedEnergyStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * One Jade line on a machine behind Apiflux's RF gate, when outside FE is switched off. The switch is server
 * config, so the server side of this provider checks it and sends the answer along.
 */
@WailaPlugin
public final class ApifluxJadePlugin implements IWailaPlugin, IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
    private static final ResourceLocation UID = Apiflux.id("machine_power");
    private static final String GATED = "apiflux_rf_refused";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(this, BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(this, Block.class);
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (be == null) return;
        IEnergyStorage energy = accessor.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, be.getBlockPos(),
                be.getBlockState(), be, null);
        if (energy instanceof GatedEnergyStorage && ApifluxConfig.rfInputDisabled()) data.putBoolean(GATED, true);
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getBlockEntity() != null;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.getBoolean(GATED)) {
            tooltip.add(Component.translatable("apiflux.jade.rf_refused").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
