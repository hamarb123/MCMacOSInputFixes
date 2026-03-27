package com.hamarb123.macos_input_fixes.client.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.client.Common;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin13
{
	@Inject(at = @At("HEAD"), method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V", cancellable = true)
	public void onKey(long window, int action, KeyEvent input, CallbackInfo info)
	{
		if (Common.IS_SYSTEM_MAC)
		{
			//disable built-in callback for tab and escape
			// - these are the keys which don't get registered properly when control is pressed in some configurations
			// - space can seemingly ONLY be fixed by changing macOS settings
			if (((InputWithModifiers)Common.asObject(input)).input() == GLFW.GLFW_KEY_TAB || ((InputWithModifiers)Common.asObject(input)).input() == GLFW.GLFW_KEY_ESCAPE)
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
