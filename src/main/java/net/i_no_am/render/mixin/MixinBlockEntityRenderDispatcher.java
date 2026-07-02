package net.i_no_am.render.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.i_no_am.render.NoRenderClient;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class MixinBlockEntityRenderDispatcher {

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private <S extends BlockEntityRenderState> void onSubmit(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        if (NoRenderClient.INSTANCE.shouldSkipRender(state.blockEntityType)) {
            ci.cancel();
        }
    }
}