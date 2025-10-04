package com.hamarb123.macos_input_fixes.client.mixin.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import com.google.common.base.Splitter;
import net.minecraft.client.option.GameOptions;

@Mixin(GameOptions.class)
public interface GameOptionsAccessor
{
	//allows the COLON_SPLITTER field to be accessed since with these mappings it is private
	@Accessor("COLON_SPLITTER")
	public static Splitter COLON_SPLITTER()
	{
		throw new AssertionError();
	}
}
