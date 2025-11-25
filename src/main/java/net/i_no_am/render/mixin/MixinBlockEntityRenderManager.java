package net.i_no_am.render.mixin;

import net.i_no_am.render.NoRenderClient;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderManager.class)
public abstract class MixinBlockEntityRenderManager<S extends BlockEntityRenderState> {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(S renderState, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (NoRenderClient.INSTANCE.shouldSkipRender(renderState.type))
            ci.cancel();
    }
}
