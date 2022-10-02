package com.hamarb123.macos_input_fixes.mixin.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.OptionMixinHelper;
import net.minecraft.client.option.GameOptions;

@Mixin(targets = "net.minecraft.class_4064" /*CyclingOption*/, remap = false)
public class CyclingOptionMixin4 implements OptionMixinHelper
{
	private boolean omitBuilderKeyText = false;

	//Inject targets for before_createButton and after_createButton with yarn mappings:
	//`createButton(Lnet/minecraft/client/option/GameOptions;III)Lnet/minecraft/client/gui/widget/ClickableWidget;`

	@Inject(method = "method_18520(Lnet/minecraft/class_315;III)Lnet/minecraft/class_339;", at = @At("HEAD"), remap = false)
	private void before_createButton(GameOptions options, int x, int y, int width, CallbackInfoReturnable<?> info)
	{
		if (omitBuilderKeyText)
		{
			//set the flag for so that when the callee creates the widget it has the correct text
			Common.setOmitBuilderKeyText(true);
		}
	}

	@Inject(method = "method_18520(Lnet/minecraft/class_315;III)Lnet/minecraft/class_339;", at = @At("RETURN"), remap = false)
	private void after_createButton(GameOptions options, int x, int y, int width, CallbackInfoReturnable<?> info)
	{
		if (omitBuilderKeyText)
		{
			//unset the flag for so that when the next callee creates its widget it works how it should
			Common.setOmitBuilderKeyText(false);
		}
	}

	@Override
	public void setOmitBuilderKeyText()
	{
		//this method implements the OptionMixinHelper interface
		//store whether omitKeyText() should be called
		omitBuilderKeyText = true;
	}
}
