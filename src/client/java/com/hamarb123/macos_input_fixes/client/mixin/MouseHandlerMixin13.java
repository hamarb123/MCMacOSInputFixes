package com.hamarb123.macos_input_fixes.client.mixin;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.InputQuirks;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.hamarb123.macos_input_fixes.client.Common;
import com.hamarb123.macos_input_fixes.client.ModOptions;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin13
{
	@Redirect(method = "simulateRightClick(Lnet/minecraft/client/input/MouseButtonInfo;Z)Lnet/minecraft/client/input/MouseButtonInfo;", at = @At(value = "FIELD", target = "Lnet/minecraft/client/input/InputQuirks;SIMULATE_RIGHT_CLICK_WITH_LONG_LEFT_CLICK:Z", opcode = Opcodes.GETSTATIC))
	private static boolean modifyMouseInput_USE_LONG_LEFT_PRESS_Adjustment()
	{
		// Ensure control + left click doesn't get converted into right click if we don't want to do that
		if (Common.IS_SYSTEM_MAC && !ModOptions.disableCtrlClickFix) return false;
		return InputQuirks.SIMULATE_RIGHT_CLICK_WITH_LONG_LEFT_CLICK;
	}
}
