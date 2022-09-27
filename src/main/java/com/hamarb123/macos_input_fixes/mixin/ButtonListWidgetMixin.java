package com.hamarb123.macos_input_fixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.ModOptions;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.options.Option;

@Mixin(ButtonListWidget.class)
public class ButtonListWidgetMixin
{
	@ModifyVariable(method = "addAll([Lnet/minecraft/client/options/Option;)V", at = @At("HEAD"), ordinal = 0)
	private Option[] modifyAddAllParameter(Option[] options)
	{
		//if we're not meant to modify anything immediately return it unmodified,
		//otherwise immediately set it to false for the next function call
		if (!Common.modifyAddAllParameter()) return options;
		Common.setModifyAddAllParameter(false);

		//get the mod options so we can add them to the game options
		Option[] modOptions = ModOptions.getModOptions();
		if (modOptions == null) return options;

		//combine the game options and mod options
		Option[] newOptions = new Option[options.length + modOptions.length];
		for (int i = 0; i < options.length; i++) newOptions[i] = options[i];
		for (int i = 0; i < modOptions.length; i++) newOptions[options.length + i] = modOptions[i];
		return newOptions;
	}
}
