package dev.oculifelis.apiflux;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server config (per world, serverconfig/apiflux-server.toml). Rates are not here: they are a data map, so a
 * datapack can change them (docs/DATA.md).
 */
public final class ApifluxConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue RF_INPUT_DISABLED;
    private static final ModConfigSpec.BooleanValue KINETIC_ENABLED;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        RF_INPUT_DISABLED = b.comment(
                        "When true, Complicated Bees machines (centrifuge, gyrofuge, mellarium energy cells) refuse FE",
                        "from every other source, Complicated Bees' own generators included, and run only on a Kinetic",
                        "Dynamo (or, with Manacomb installed, on mana too). Off by default: rotation is then an extra",
                        "way to power them.")
                .define("rfInputDisabled", false);
        KINETIC_ENABLED = b.comment("The Kinetic Dynamo turns rotation into FE (needs Create).")
                .define("kineticEnabled", true);
        SPEC = b.build();
    }

    private ApifluxConfig() {}

    // The server config exists only on a server (or the integrated one); before it loads, the defaults apply.
    public static boolean rfInputDisabled() {
        return SPEC.isLoaded() && RF_INPUT_DISABLED.get();
    }

    /** Flips the switch for the running server (GameTests use it); saved to the config file. */
    public static void setRfInputDisabled(boolean value) {
        RF_INPUT_DISABLED.set(value);
    }

    public static boolean kineticEnabled() {
        return !SPEC.isLoaded() || KINETIC_ENABLED.get();
    }
}
