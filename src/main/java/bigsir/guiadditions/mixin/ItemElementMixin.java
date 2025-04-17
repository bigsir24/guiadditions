package bigsir.guiadditions.mixin;

import bigsir.guiadditions.GuiAdditions;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ItemElement;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.player.inventory.slot.Slot;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemElement.class, remap = false)
public abstract class ItemElementMixin extends Gui {

	@Inject(method = "render(Lnet/minecraft/core/item/ItemStack;IIZLnet/minecraft/core/player/inventory/slot/Slot;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ItemElement;drawRect(IIIII)V"))
	public void fix(ItemStack itemStack, int x, int y, boolean isSelected, Slot slot, CallbackInfo ci) {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		this.zLevel += 25;
	}

	@Redirect(method = "render(Lnet/minecraft/core/item/ItemStack;IIZLnet/minecraft/core/player/inventory/slot/Slot;)V", at = @At(value = "INVOKE",  target = "Lnet/minecraft/client/gui/ItemElement;drawRect(IIIII)V", ordinal = 0))
	public void draw(ItemElement e, int x, int y, int maxX, int maxY, int argb) {
		if(GuiAdditions.customSlotSelector.value == 0) {
			this.zLevel -= 5;
			this.drawRect(x, y, maxX, maxY, argb);
			this.zLevel += 5;
			return;
		}

		Tessellator t = Tessellator.instance;
		GL11.glEnable(GL11.GL_BLEND);

		GuiAdditions.getSlotTexture().bind();
		GL11.glColor4f(1.0f,1.0f,1.0f,1.0f);

		int xp = x - 8;
		int yp = y - 8;

		int offset = 25;

		t.startDrawingQuads();
		t.addVertexWithUV(xp, yp, this.zLevel + offset, 0, 0);
		t.addVertexWithUV(xp, yp + 32, this.zLevel + offset, 0, 1);
		t.addVertexWithUV(xp + 32, yp + 32, this.zLevel + offset, 1, 1);
		t.addVertexWithUV(xp + 32, yp, this.zLevel + offset, 1, 0);
		t.draw();
	}

	@Inject(method = "render(Lnet/minecraft/core/item/ItemStack;IIZLnet/minecraft/core/player/inventory/slot/Slot;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ItemElement;drawRect(IIIII)V", shift = At.Shift.AFTER))
	public void fix2(ItemStack itemStack, int x, int y, boolean isSelected, Slot slot, CallbackInfo ci) {
		this.zLevel -= 25;
	}

	@Inject(method = "render(Lnet/minecraft/core/item/ItemStack;IIZLnet/minecraft/core/player/inventory/slot/Slot;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/model/ItemModel;renderItemOverlayIntoGUI(Lnet/minecraft/client/render/tessellator/Tessellator;Lnet/minecraft/client/render/Font;Lnet/minecraft/client/render/TextureManager;Lnet/minecraft/core/item/ItemStack;IILjava/lang/String;F)V", ordinal = 2))
	public void matrix(ItemStack itemStack, int x, int y, boolean isSelected, Slot slot, CallbackInfo ci) {
		if(isSelected) {
			GL11.glPushMatrix();
			GL11.glTranslatef(0,0,50);
		}
	}

	@Inject(method = "render(Lnet/minecraft/core/item/ItemStack;IIZLnet/minecraft/core/player/inventory/slot/Slot;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/model/ItemModel;renderItemOverlayIntoGUI(Lnet/minecraft/client/render/tessellator/Tessellator;Lnet/minecraft/client/render/Font;Lnet/minecraft/client/render/TextureManager;Lnet/minecraft/core/item/ItemStack;IILjava/lang/String;F)V", ordinal = 2, shift = At.Shift.AFTER))
	public void matrixEnd(ItemStack itemStack, int x, int y, boolean isSelected, Slot slot, CallbackInfo ci) {
		if(isSelected) {
			GL11.glTranslatef(0,0,-50);
			GL11.glPopMatrix();
		}
	}
}
