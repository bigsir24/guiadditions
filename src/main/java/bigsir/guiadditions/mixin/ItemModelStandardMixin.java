package bigsir.guiadditions.mixin;

import net.minecraft.client.render.Font;
import net.minecraft.client.render.TextureManager;
import net.minecraft.client.render.item.model.ItemModelStandard;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemModelStandard.class, remap = false)
public abstract class ItemModelStandardMixin {
	@Redirect(method = "renderTexturedQuad(Lnet/minecraft/client/render/tessellator/Tessellator;IILnet/minecraft/client/render/texture/stitcher/IconCoordinate;ZZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/tessellator/Tessellator;addVertexWithUV(DDDDD)V"))
	private void addV(Tessellator t, double x, double y, double z, double u, double v) {
		t.addVertexWithUV(x, y, 1, u, v);
	}

	@Redirect(method = "renderItemOverlayIntoGUI", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V", ordinal = 1))
	private void fix2(int target) {
		GL11.glPushMatrix();
		GL11.glTranslatef(0, 0, 35);
	}

	@Inject(method = "renderItemOverlayIntoGUI", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Font;drawStringWithShadow(Ljava/lang/String;III)V", shift = At.Shift.AFTER))
	private void fix3(Tessellator tessellator, Font font, TextureManager textureManager, ItemStack itemstack, int x, int y, String override, float alpha, CallbackInfo ci) {
		GL11.glPopMatrix();
	}

	@Redirect(method = "renderItemOverlayIntoGUI", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V", ordinal = 3))
	private void fix(int target) {

	}

	@Redirect(method = "renderColoredQuad", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/tessellator/Tessellator;addVertex(DDD)V"))
	private void addV(Tessellator t, double x, double y, double z) {
		t.addVertex(x, y, 2);
	}
}
