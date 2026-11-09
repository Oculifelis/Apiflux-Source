package dev.oculifelis.apiflux;

import dev.oculifelis.apiflux.compat.create.CreateCompat;
import dev.oculifelis.apiflux.data.ApifluxDataMaps;
import dev.oculifelis.apiflux.energy.CbEnergyGate;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Apiflux lets Complicated Bees' FE machines run on Create rotation.
 *
 * {@link CreateCompat} adds the Kinetic Dynamo and loads only when Create is present. {@link CbEnergyGate} is
 * the optional "instead of RF" switch.
 *
 * <p>Botania mana for the same machines is a separate mod, Manacomb. It has the same switch, but stands its
 * own gate down when Apiflux is installed, so this gate and this switch then cover both.
 */
@Mod(Apiflux.MOD_ID)
public final class Apiflux {
    public static final String MOD_ID = "apiflux";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    public static final boolean CREATE = ModList.get().isLoaded("create");

    public Apiflux(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, ApifluxConfig.SPEC);
        modBus.addListener(ApifluxDataMaps::register);
        // HIGHEST so the gate answers before Complicated Bees' own (NORMAL) provider would.
        modBus.addListener(EventPriority.HIGHEST, CbEnergyGate::registerCapabilities);
        if (CREATE) {
            CreateCompat.init(modBus);
        }
        LOG.info("Apiflux: rotation power {}", CREATE ? "on" : "off (no Create)");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
