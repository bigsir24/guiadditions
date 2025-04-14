package bigsir.guiadditions;

import bigsir.guiadditions.mixin.OptionRangeAccessor;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.OptionRange;

import java.util.function.Function;

public class OptionChoice extends OptionRange {
	private final Function<Integer, String> displayStringFunction;
	public OptionChoice(GameSettings gameSettings, String name, int defaultValue, int values, Function<Integer, String> displayStringFunction) {
		super(gameSettings, name, defaultValue, values);
		this.displayStringFunction = displayStringFunction;
	}

	void setValues(int values) {
		this.values = getValueArray(values);
		((OptionRangeAccessor)this).setHighest(values - 1);
	}

	@Override
	public String getDisplayStringValue() {
		return this.displayStringFunction.apply(this.value);
	}
}
