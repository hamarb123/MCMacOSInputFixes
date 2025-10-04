package com.hamarb123.macos_input_fixes.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hamarb123.macos_input_fixes.client.Common;

import net.minecraft.client.gui.screen.Screen;

@Mixin(Screen.class)
public class ScreenMixin12
{
	//`hasControlDown()Z` is the intermediary name for 1.14-1.15, and `method_25441()Z` is for 1.16+
	@Inject(method = {"hasControlDown()Z", "method_25441()Z"}, at = @At(value = "HEAD"), cancellable = true, remap = false)
	private static void hasControlDown(CallbackInfoReturnable<Boolean> info)
	{
		//if the hasControlDown() injector is enabled, replace the return value with our own logic
		if (Common.injectHasControlDown())
		{
			info.setReturnValue(Common.hasControlDownInjector());
			return;
		}
	}
}
