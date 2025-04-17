package bigsir.guiadditions.mixin;

import bigsir.guiadditions.GuiAdditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.container.ScreenContainerAbstract;
import net.minecraft.client.render.tessellator.Tessellator;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ScreenContainerAbstract.class, remap = false)
public abstract class ScreenContainerAbstractMixin extends Screen {

	@Shadow
	public int xSize;

	@Shadow
	public int ySize;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V", shift = At.Shift.AFTER))
	public void offset(int mx, int my, float partialTick, CallbackInfo ci) {
		//GL11.glTranslatef(0, 0, 0); I don't think this is needed anymore
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V", ordinal = 1, shift = At.Shift.AFTER))
	public void t(int mx, int my, float partialTick, CallbackInfo ci) {
		if(GuiAdditions.selectorStyle.value == 0) return;

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GuiAdditions.getHotbarTexture().bind();
		GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
		Tessellator t = Tessellator.instance;

		int centerX = (this.width - this.xSize) / 2;
		int centerY = (this.height - this.ySize) / 2;

		int hotbarOffset = Minecraft.getMinecraft().thePlayer.inventory.getHotbarOffset();

		int x = centerX - 40;
		int y = centerY + this.ySize - 33 + (hotbarOffset > 1 ? -80 + (hotbarOffset * 2 + 4) : 0) + 1;
		int offset = 20;

		t.startDrawingQuads();
		if(GuiAdditions.showBackground.value) {
			t.addVertexWithUV(x, y, this.zLevel + 0.01, 0, 0.5);
			t.addVertexWithUV(x, y + 32, this.zLevel + 0.01, 0, 1);
			t.addVertexWithUV(x + 256, y + 32, this.zLevel + 0.01, 1, 1);
			t.addVertexWithUV(x + 256, y, this.zLevel + 0.01, 1, 0.5);
		}
		t.draw();

		t.startDrawingQuads();
		t.addVertexWithUV(x, y, this.zLevel + offset, 0, 0);
		t.addVertexWithUV(x, y + 32, this.zLevel + offset, 0, 0.5);
		t.addVertexWithUV(x + 256, y + 32, this.zLevel + offset, 1, 0.5);
		t.addVertexWithUV(x + 256, y, this.zLevel + offset, 1, 0);
		t.draw();
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}
}
