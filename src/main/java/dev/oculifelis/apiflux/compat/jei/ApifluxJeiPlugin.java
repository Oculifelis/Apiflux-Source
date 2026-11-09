package dev.oculifelis.apiflux.compat.jei;

import dev.oculifelis.apiflux.Apiflux;
import dev.oculifelis.apiflux.compat.create.CreateCompat;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Info page: what the Kinetic Dynamo does. */
@JeiPlugin
public final class ApifluxJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return Apiflux.id("info");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Apiflux.CREATE) {
            registration.addIngredientInfo(new ItemStack(CreateCompat.KINETIC_DYNAMO_ITEM.get()), VanillaTypes.ITEM_STACK,
                    Component.translatable("apiflux.jei.dynamo"));
        }
    }
}
