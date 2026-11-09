package dev.oculifelis.apiflux.data;

import dev.oculifelis.apiflux.Apiflux;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/** The data map that holds the Kinetic Dynamo's rates. Defaults ship in data/apiflux/data_maps/. */
public final class ApifluxDataMaps {
    /** The Kinetic Dynamo's stress and FE rates. Synced, so goggles and tooltips show the real numbers. */
    public static final DataMapType<Block, DynamoRates> KINETIC_DYNAMO = DataMapType
            .builder(Apiflux.id("kinetic_dynamo"), Registries.BLOCK, DynamoRates.CODEC)
            .synced(DynamoRates.CODEC, false)
            .build();

    private ApifluxDataMaps() {}

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(KINETIC_DYNAMO);
    }

    @SuppressWarnings("deprecation")
    public static DynamoRates dynamoRates(Block block) {
        DynamoRates rates = block.builtInRegistryHolder().getData(KINETIC_DYNAMO);
        return rates != null ? rates : DynamoRates.DEFAULT;
    }
}
