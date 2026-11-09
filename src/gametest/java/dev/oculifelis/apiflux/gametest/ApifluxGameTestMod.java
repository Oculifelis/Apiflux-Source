package dev.oculifelis.apiflux.gametest;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Dev-only test mod: GameTests (found by NeoForge from @GameTestHolder) and the client check. Never shipped. */
@Mod("apiflux_gametest")
public final class ApifluxGameTestMod {
    public ApifluxGameTestMod(IEventBus modBus, Dist dist) {
        if (dist.isClient() && Boolean.getBoolean("apiflux.clienttest")) {
            ClientCheck.init();
        }
    }
}
