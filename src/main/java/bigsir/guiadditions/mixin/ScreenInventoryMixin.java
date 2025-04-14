package bigsir.guiadditions.mixin;

import net.minecraft.client.gui.container.ScreenInventory;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ScreenInventory.class, remap = false)
public abstract class ScreenInventoryMixin {
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/container/ScreenInventory;drawProtectionOverlay(II)V"))
	private void wrapStart(int mx, int my, float partialTick, CallbackInfo ci) {
		GL11.glPushMatrix();
		GL11.glTranslatef(0, 0, 100);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/container/ScreenInventory;drawProtectionOverlay(II)V", shift = At.Shift.AFTER))
	private void wrapEnd(int mx, int my, float partialTick, CallbackInfo ci) {
		GL11.glPopMatrix();
	}
}
