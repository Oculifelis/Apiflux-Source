package dev.oculifelis.apiflux.compat.create;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.oculifelis.apiflux.ApifluxConfig;
import dev.oculifelis.apiflux.data.ApifluxDataMaps;
import dev.oculifelis.apiflux.data.DynamoRates;
import dev.oculifelis.apiflux.energy.EnergyAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.EnumMap;
import java.util.List;

/**
 * Each tick turns the shaft's speed into FE ({@code fe_per_rpm} per RPM, up to {@code max_fe_per_tick}) and
 * offers it to the blocks on its five non-shaft sides. It stores nothing: what no neighbour takes is never
 * made, so an idle machine costs no stress beyond the impact Create already counts.
 *
 * <p>Complicated Bees' machines work whenever they have enough FE in store, so a dynamo too slow for a
 * machine's draw runs it part of the time: speed scales processing up to the machine's full rate.
 */
public class KineticDynamoBlockEntity extends KineticBlockEntity {
    private final EnumMap<Direction, BlockCapabilityCache<IEnergyStorage, Direction>> targets = new EnumMap<>(Direction.class);
    /** FE handed out last tick, synced every half second for goggles. */
    private int delivered;
    private int syncedDelivered;

    public KineticDynamoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public DynamoRates rates() {
        return ApifluxDataMaps.dynamoRates(getBlockState().getBlock());
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level instanceof ServerLevel serverLevel)) return;

        int budget = ApifluxConfig.kineticEnabled() && !isOverStressed() && isSpeedRequirementFulfilled()
                ? rates().fePerTick(getSpeed()) : 0;
        int given = 0;
        if (budget > 0) {
            Direction shaft = getBlockState().getValue(KineticDynamoBlock.FACING);
            for (Direction side : Direction.values()) {
                if (side == shaft) continue;
                IEnergyStorage target = EnergyAccess.unwrap(target(serverLevel, side).getCapability());
                if (target == null) continue;
                given += target.receiveEnergy(budget - given, false);
                if (given >= budget) break;
            }
        }
        delivered = given;
        if (delivered != syncedDelivered && serverLevel.getGameTime() % 10 == 0) {
            syncedDelivered = delivered;
            sendData();
        }
    }

    private BlockCapabilityCache<IEnergyStorage, Direction> target(ServerLevel level, Direction side) {
        return targets.computeIfAbsent(side, s -> BlockCapabilityCache.create(
                Capabilities.EnergyStorage.BLOCK, level, worldPosition.relative(s), s.getOpposite()));
    }

    public int getDelivered() {
        return delivered;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (clientPacket) tag.putInt("Delivered", syncedDelivered);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (clientPacket) delivered = tag.getInt("Delivered");
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        int possible = rates().fePerTick(getSpeed());
        tooltip.add(Component.literal("    ").append(
                Component.translatable("apiflux.goggles.dynamo").withStyle(ChatFormatting.GRAY)));
        tooltip.add(Component.literal("    ").append(
                Component.translatable("apiflux.goggles.delivering", delivered, possible).withStyle(ChatFormatting.AQUA)));
        return true;
    }
}
