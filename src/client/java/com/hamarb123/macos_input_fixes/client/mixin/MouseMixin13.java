package com.hamarb123.macos_input_fixes.client.mixin;

import net.minecraft.client.Mouse;
import net.minecraft.client.input.SystemKeycodes;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.hamarb123.macos_input_fixes.client.Common;
import com.hamarb123.macos_input_fixes.client.ModOptions;

@Mixin(Mouse.class)
public class MouseMixin13
{
	@Redirect(method = "modifyMouseInput(Lnet/minecraft/client/input/MouseInput;Z)Lnet/minecraft/client/input/MouseInput;", at = @At(value = "FIELD", target = "Lnet/minecraft/client/input/SystemKeycodes;USE_LONG_LEFT_PRESS:Z", opcode = Opcodes.GETSTATIC))
	private static boolean modifyMouseInput_USE_LONG_LEFT_PRESS_Adjustment()
	{
		// Ensure control + left click doesn't get converted into right click if we don't want to do that
		if (Common.IS_SYSTEM_MAC && !ModOptions.disableCtrlClickFix) return false;
		return SystemKeycodes.USE_LONG_LEFT_PRESS;
	}
}
