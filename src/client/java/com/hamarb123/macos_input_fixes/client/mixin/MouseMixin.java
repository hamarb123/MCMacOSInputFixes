package com.hamarb123.macos_input_fixes.client.mixin;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.client.Common;
import com.hamarb123.macos_input_fixes.client.ModOptions;

@Mixin(Mouse.class)
public class MouseMixin
{
	@Inject(at = @At("HEAD"), method = "onMouseScroll(JDD)V", cancellable = true)
	private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo info)
	{
		if (Common.IS_SYSTEM_MAC)
		{
			if (vertical == 0)
			{
				//if vertical is 0 then there is no scroll
				info.cancel();
				return;
			}
			if (!Common.allowInputOSX())
			{
				//only accept scroll event on macOS if it's from the native callback
				info.cancel();
				return;
			}
		}
	}

	@ModifyVariable(method = "onMouseScroll(JDD)V", at = @At("HEAD"), ordinal = 0)
	private double maybeReverseHScroll(double value)
	{
		//if the reverse scrolling option is enabled, reverse the horizontal scroll value
		return ModOptions.reverseScrolling ? -value : value;
	}

	@ModifyVariable(method = "onMouseScroll(JDD)V", at = @At("HEAD"), ordinal = 1)
	private double maybeReverseVScroll(double value)
	{
		//if the reverse scrolling option is enabled, reverse the vertical scroll value
		return ModOptions.reverseScrolling ? -value : value;
	}
}
