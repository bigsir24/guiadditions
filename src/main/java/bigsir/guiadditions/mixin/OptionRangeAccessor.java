package bigsir.guiadditions.mixin;

import net.minecraft.client.option.OptionRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = OptionRange.class, remap = false)
public interface OptionRangeAccessor {
	@Accessor
	void setHighest(int highest);
}
