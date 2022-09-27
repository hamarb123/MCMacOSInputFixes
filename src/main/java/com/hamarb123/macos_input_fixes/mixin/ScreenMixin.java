package com.hamarb123.macos_input_fixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.hamarb123.macos_input_fixes.Common;
import net.minecraft.client.gui.Screen;

@Mixin(Screen.class)
public class ScreenMixin
{
	//not mixed in properly
	@Inject(method = "hasControlDown()Z", at = @At(value = "HEAD"), cancellable = true)
	private static void hasControlDown(CallbackInfoReturnable<Boolean> info)
	{
		if (Common.injectHasControlDown())
		{
			info.setReturnValue(Common.hasControlDownInjector());
			return;
		}
	}
}
