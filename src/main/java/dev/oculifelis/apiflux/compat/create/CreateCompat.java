package dev.oculifelis.apiflux.compat.create;

import com.simibubi.create.api.stress.BlockStressValues;
import dev.oculifelis.apiflux.Apiflux;
import dev.oculifelis.apiflux.data.ApifluxDataMaps;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Rotation power: the Kinetic Dynamo. Only touched when Create is loaded, so nothing of Create is loaded
 * otherwise.
 *
 * <p>Why a block and not a capability on the machines: a Create kinetic network is made of kinetic blocks
 * with their own block entities (speed, stress, network id). A Complicated Bees centrifuge cannot join one
 * without replacing its block and block entity. A small dynamo placed against the machine joins the network
 * itself and hands FE to whatever it touches, Complicated Bees or not.
 */
public final class CreateCompat {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Apiflux.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Apiflux.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Apiflux.MOD_ID);

    public static final DeferredBlock<KineticDynamoBlock> KINETIC_DYNAMO = BLOCKS.register("kinetic_dynamo",
            () -> new KineticDynamoBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PODZOL)
                    .strength(1.5f, 6f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));
    public static final DeferredItem<BlockItem> KINETIC_DYNAMO_ITEM = ITEMS.registerSimpleBlockItem(KINETIC_DYNAMO, new Item.Properties());

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KineticDynamoBlockEntity>> KINETIC_DYNAMO_BE =
            BLOCK_ENTITIES.register("kinetic_dynamo", () -> BlockEntityType.Builder
                    .of((pos, state) -> new KineticDynamoBlockEntity(CreateCompat.KINETIC_DYNAMO_BE.get(), pos, state), KINETIC_DYNAMO.get())
                    .build(null));

    private CreateCompat() {}

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(CreateCompat::commonSetup);
        modBus.addListener(CreateCompat::creativeTabs);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CreateClient.init(modBus);
        }
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Block dynamo = KINETIC_DYNAMO.get();
            // Read on every lookup, so a datapack reload changes the impact at once.
            BlockStressValues.IMPACTS.register(dynamo, () -> ApifluxDataMaps.dynamoRates(dynamo).stressImpact());
        });
    }

    private static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(KINETIC_DYNAMO_ITEM);
        }
    }
}
