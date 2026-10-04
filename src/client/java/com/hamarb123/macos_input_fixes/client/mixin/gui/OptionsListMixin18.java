package com.hamarb123.macos_input_fixes.client.mixin.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.hamarb123.macos_input_fixes.client.ModOptions;

import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.OptionInstance;

@Mixin(OptionsList.class)
public class OptionsListMixin18
{
	@Shadow
	private OptionsSubScreen screen;

	//this is where we add additional menu options
	@ModifyVariable(method = "addSmall([Lnet/minecraft/client/OptionInstance;)V", at = @At("HEAD"), ordinal = 0)
	private OptionInstance<?>[] modifyAddAllParameter1(OptionInstance<?>[] options)
	{
		//check if it's a VideoSettingsScreen, otherwise we don't want to modify
		if (!(screen instanceof VideoSettingsScreen)) return options;

		//combine the game options and mod options
		ModOptions.loadInterface();
		OptionInstance<?>[] newOptions = new OptionInstance<?>[options.length + 1];
		for (int i = 0; i < options.length; i++) newOptions[i] = options[i];
		newOptions[options.length] = (OptionInstance<?>)ModOptions.USE_P3_COLOR_SPACE;
		return newOptions;
	}
}
