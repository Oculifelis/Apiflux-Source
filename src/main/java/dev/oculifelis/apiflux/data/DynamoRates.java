package dev.oculifelis.apiflux.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

/**
 * The {@code apiflux:kinetic_dynamo} data map entry (registry: blocks), read by the Kinetic Dynamo.
 *
 * @param stressImpact stress units drawn per RPM, as Create counts impact
 * @param fePerRpm     FE delivered per tick for each RPM of input speed
 * @param maxFePerTick cap on FE per tick, whatever the speed
 * @param minRpm       below this speed the dynamo delivers nothing
 */
public record DynamoRates(float stressImpact, float fePerRpm, int maxFePerTick, float minRpm) {
    public static final DynamoRates DEFAULT = new DynamoRates(2.0f, 0.5f, 256, 0f);

    private static final Codec<Float> NON_NEGATIVE = Codec.floatRange(0f, Float.MAX_VALUE);

    public static final Codec<DynamoRates> CODEC = RecordCodecBuilder.create(i -> i.group(
            NON_NEGATIVE.optionalFieldOf("stress_impact", DEFAULT.stressImpact).forGetter(DynamoRates::stressImpact),
            NON_NEGATIVE.optionalFieldOf("fe_per_rpm", DEFAULT.fePerRpm).forGetter(DynamoRates::fePerRpm),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("max_fe_per_tick", DEFAULT.maxFePerTick).forGetter(DynamoRates::maxFePerTick),
            NON_NEGATIVE.optionalFieldOf("min_rpm", DEFAULT.minRpm).forGetter(DynamoRates::minRpm)
    ).apply(i, DynamoRates::new));

    /** FE per tick at the given speed (direction ignored). */
    public int fePerTick(float rpm) {
        float speed = Math.abs(rpm);
        if (speed <= 0 || speed < minRpm) return 0;
        return Math.min(maxFePerTick, Math.round(speed * fePerRpm));
    }
}
