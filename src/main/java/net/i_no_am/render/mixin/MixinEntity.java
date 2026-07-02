package net.i_no_am.render.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.i_no_am.render.NoRenderClient;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class MixinEntity<T extends Entity> {

    @ModifyReturnValue(method = "shouldRender", at = @At("RETURN"))
    private boolean disableRender(boolean original, T entity) {
        return !NoRenderClient.INSTANCE.shouldSkipRender(entity);
    }
}
