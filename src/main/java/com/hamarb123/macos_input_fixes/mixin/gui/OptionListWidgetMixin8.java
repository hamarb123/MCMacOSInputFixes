package com.hamarb123.macos_input_fixes.mixin.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.hamarb123.macos_input_fixes.ModOptions;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.MouseOptionsScreen;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.SimpleOption;

@Mixin(OptionListWidget.class)
public class OptionListWidgetMixin8
{
	@Shadow(remap = false, aliases = {"field_49483", "optionsScreen"})
	private GameOptionsScreen optionsScreen;

	//this is where we add additional menu options
	@ModifyVariable(method = "addAll([Lnet/minecraft/client/option/SimpleOption;)V", at = @At("HEAD"), ordinal = 0)
	private SimpleOption<?>[] modifyAddAllParameter1(SimpleOption<?>[] options)
	{
		//check if it's a MouseOptionsScreen, otherwise we don't want to modify
		if (!(optionsScreen instanceof MouseOptionsScreen)) return options;

		//get the mod options so we can add them to the game options
		Object[] modOptions = ModOptions.getModOptions();
		if (modOptions == null) return options;

		//combine the game options and mod options
		SimpleOption<?>[] newOptions = new SimpleOption<?>[options.length + modOptions.length];
		for (int i = 0; i < options.length; i++) newOptions[i] = options[i];
		for (int i = 0; i < modOptions.length; i++) newOptions[options.length + i] = (SimpleOption<?>)modOptions[i];
		return newOptions;
	}
}
