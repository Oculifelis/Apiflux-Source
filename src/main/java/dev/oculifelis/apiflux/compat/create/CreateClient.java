package dev.oculifelis.apiflux.compat.create;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Draws the dynamo's half shaft: a Flywheel visual when Flywheel is on, the plain renderer when it is off. */
final class CreateClient {
    private CreateClient() {}

    static void init(IEventBus modBus) {
        modBus.addListener(CreateClient::renderers);
        modBus.addListener(CreateClient::clientSetup);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CreateCompat.KINETIC_DYNAMO_BE.get(), DynamoRenderer::new);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> SimpleBlockEntityVisualizer.builder(CreateCompat.KINETIC_DYNAMO_BE.get())
                .factory(OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF))
                .skipVanillaRender(be -> true)
                .apply());
    }

    static final class DynamoRenderer extends KineticBlockEntityRenderer<KineticDynamoBlockEntity> {
        DynamoRenderer(BlockEntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        protected SuperByteBuffer getRotatedModel(KineticDynamoBlockEntity be, BlockState state) {
            return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state);
        }
    }
}
