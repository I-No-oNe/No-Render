package net.i_no_am.render.mixin;

import net.i_no_am.render.NoRenderClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class MixinEntity {
    @Inject(method = "shouldRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;shouldRender(DDD)Z"), cancellable = true)
    <T extends Entity> void render(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (NoRenderClient.INSTANCE.shouldSkipRender(entity)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
