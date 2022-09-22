package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.hamarb123.macos_input_fixes.MacOSInputFixesMod;

@Mixin(Mouse.class)
public class MouseMixin
{
	@Inject(at = @At("HEAD"), method = "onMouseScroll(JDD)V", cancellable = true)
	private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo info)
	{
		if (MinecraftClient.IS_SYSTEM_MAC)
		{
			if (vertical == 0)
			{
				//if vertical is 0 then there is no scroll
				info.cancel();
				return;
			}
			if (!MacOSInputFixesMod.allowInputOSX())
			{
				//only accept scroll event on macOS if it's from the native callback
				info.cancel();
				return;
			}
		}
	}

	@Redirect(method = "onMouseButton(JIII)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;IS_SYSTEM_MAC:Z", opcode = Opcodes.GETSTATIC))
	private boolean leftMouseClick()
	{
		//onMouseButton converts left click + control on macOS to right click, simply tell it we're not on macOS so it can't do that (there's no other uses of macOS in the function)
		return false;
	}
}
