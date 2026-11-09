package dev.oculifelis.apiflux.gametest;

import com.accbdd.complicated_bees.block.entity.CentrifugeBlockEntity;
import dev.oculifelis.apiflux.compat.create.CreateCompat;
import dev.oculifelis.apiflux.compat.create.KineticDynamoBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The one client check, run only with -Dapiflux.clienttest=1 (tools/run_client_check.sh). Makes a flat
 * creative world, builds creative motor -> Kinetic Dynamo -> centrifuge, photographs the dynamo turning and
 * the centrifuge with Jade's line, logs AFTEST lines, and quits.
 */
public final class ClientCheck {
    static final Logger LOG = LoggerFactory.getLogger("apiflux_clientcheck");
    private static final String WORLD = "aftest";
    private static int step;
    private static int wait;
    private static boolean creating;
    private static boolean done;
    private static BlockPos origin;

    private ClientCheck() {}

    static void init() {
        NeoForge.EVENT_BUS.addListener(ClientCheck::tick);
    }

    private static void tick(ClientTickEvent.Post event) {
        if (done) return;
        Minecraft mc = Minecraft.getInstance();
        try {
            drive(mc);
        } catch (Throwable t) {
            LOG.error("AFTEST FAIL exception at step {}", step, t);
            finish(mc);
        }
    }

    private static void drive(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            if (!creating && mc.screen instanceof TitleScreen title && mc.getOverlay() == null) {
                creating = true;
                LOG.info("AFTEST title reached, window visible={} focused={}",
                        GLFW.glfwGetWindowAttrib(mc.getWindow().getWindow(), GLFW.GLFW_VISIBLE),
                        GLFW.glfwGetWindowAttrib(mc.getWindow().getWindow(), GLFW.GLFW_FOCUSED));
                mc.options.pauseOnLostFocus = false;
                mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(0.0);
                mc.options.renderDistance().set(4);
                LevelStorageSource source = mc.getLevelSource();
                if (source.levelExists(WORLD)) {
                    try (LevelStorageSource.LevelStorageAccess access = source.createAccess(WORLD)) {
                        access.deleteLevel();
                    } catch (Exception e) {
                        LOG.warn("AFTEST could not delete old world", e);
                    }
                }
                LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
                        new GameRules(), WorldDataConfiguration.DEFAULT);
                mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(20261104L, false, false),
                        ra -> ra.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value()
                                .createWorldDimensions(), title);
            }
            return;
        }
        if (mc.screen instanceof PauseScreen) mc.setScreen(null);
        if (wait > 0) {
            wait--;
            return;
        }
        MinecraftServer server = mc.getSingleplayerServer();
        switch (step) {
            case 0 -> {
                server.execute(() -> buildScene(server));
                step = 1;
                wait = 100;
            }
            case 1 -> {
                // Look at the dynamo from the side, slightly above.
                look(server, origin.offset(1, 0, 0), 0.0, 1.6, 3.2);
                step = 2;
                wait = 40;
            }
            case 2 -> {
                mc.options.hideGui = true;
                step = 3;
                wait = 3;
            }
            case 3 -> {
                shot(mc, "aftest_1_dynamo");
                KineticDynamoBlockEntity dynamo = (KineticDynamoBlockEntity) mc.level.getBlockEntity(origin.offset(1, 0, 0));
                LOG.info("AFTEST client dynamo speed={} delivered(synced)={}", dynamo == null ? "null" : dynamo.getSpeed(),
                        dynamo == null ? "null" : dynamo.getDelivered());
                server.execute(() -> {
                    ServerLevel level = server.overworld();
                    CentrifugeBlockEntity be = (CentrifugeBlockEntity) level.getBlockEntity(origin.offset(2, 0, 0));
                    KineticDynamoBlockEntity d = (KineticDynamoBlockEntity) level.getBlockEntity(origin.offset(1, 0, 0));
                    LOG.info("AFTEST server dynamo speed={} delivered={} centrifuge FE={}", d.getSpeed(), d.getDelivered(),
                            be.getStoredPower());
                });
                mc.options.hideGui = false;
                look(server, origin.offset(2, 0, 0), 0.0, 1.0, 2.2);
                step = 4;
                wait = 60;
            }
            case 4 -> {
                shot(mc, "aftest_2_centrifuge_jade");
                step = 5;
                wait = 10;
            }
            default -> {
                LOG.info("AFTEST done");
                finish(mc);
            }
        }
    }

    private static void buildScene(MinecraftServer server) {
        ServerLevel level = server.overworld();
        ServerPlayer player = server.getPlayerList().getPlayers().get(0);
        origin = player.blockPosition().offset(-1, 0, -3);
        Block motor = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("create", "creative_motor"));
        Block centrifuge = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("complicated_bees", "centrifuge"));
        level.setBlockAndUpdate(origin, motor.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.EAST));
        level.setBlockAndUpdate(origin.offset(1, 0, 0), CreateCompat.KINETIC_DYNAMO.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.WEST));
        level.setBlockAndUpdate(origin.offset(2, 0, 0), centrifuge.defaultBlockState());
        level.setDayTime(6000);
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        LOG.info("AFTEST scene built at {}", origin);
    }

    /** Stands the player south of {@code target} by {@code back} blocks, {@code up} above, facing it. */
    private static void look(MinecraftServer server, BlockPos target, double side, double up, double back) {
        server.execute(() -> {
            ServerPlayer p = server.getPlayerList().getPlayers().get(0);
            double x = target.getX() + 0.5 + side, y = target.getY() + up - 1.62 + 0.5, z = target.getZ() + 0.5 + back;
            double dy = (target.getY() + 0.5) - (y + 1.62);
            float pitch = (float) Math.toDegrees(-Math.atan2(dy, back));
            p.getAbilities().flying = true;
            p.onUpdateAbilities();
            p.teleportTo(server.overworld(), x, y, z, 180f, pitch);
        });
    }

    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(),
                msg -> LOG.info("AFTEST shot {}: {}", name, msg.getString()));
    }

    private static void finish(Minecraft mc) {
        done = true;
        mc.stop();
    }
}
