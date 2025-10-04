package com.hamarb123.macos_input_fixes.client.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.client.Common;

import net.minecraft.client.Keyboard;

@Mixin(Keyboard.class)
public class KeyboardMixin12
{
	//@Inject(at = @At("HEAD"), method = "onKey(JIIII)V", cancellable = true)
	@Inject(at = @At("HEAD"), method = "method_1466(JIIII)V", cancellable = true, remap = false)
	public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo info)
	{
		if (Common.IS_SYSTEM_MAC)
		{
			//disable built-in callback for tab and escape
			// - these are the keys which don't get registered properly when control is pressed in some configurations
			// - space can seemingly ONLY be fixed by changing macOS settings
			if (key == GLFW.GLFW_KEY_TAB || key == GLFW.GLFW_KEY_ESCAPE)
			{
				if (!Common.allowInputOSX2())
				{
					//only accept key event on macOS if it's from the native callback
					info.cancel();
					return;
				}
			}
		}
	}
}
