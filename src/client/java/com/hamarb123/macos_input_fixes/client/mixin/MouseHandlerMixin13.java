package com.hamarb123.macos_input_fixes.client.mixin;

import net.minecraft.client.MouseHandler;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.hamarb123.macos_input_fixes.client.Common;
import com.hamarb123.macos_input_fixes.client.ModOptions;

//? if >=26.1 {
import com.hamarb123.macos_input_fixes.client.ModernFabricReflectionHelper;
//?} else {
/*
import net.minecraft.client.input.InputQuirks;
*///?}

@Mixin(MouseHandler.class)
public class MouseHandlerMixin13
{
	@Redirect(method = "simulateRightClick(Lnet/minecraft/client/input/MouseButtonInfo;Z)Lnet/minecraft/client/input/MouseButtonInfo;", require = 0, at = @At(value = "FIELD", target = "Lnet/minecraft/client/input/InputQuirks;SIMULATE_RIGHT_CLICK_WITH_LONG_LEFT_CLICK:Z", opcode = Opcodes.GETSTATIC))
	private static boolean modifyMouseInput_USE_LONG_LEFT_PRESS_Adjustment()
	{
		return impl();
	}

	//? if >=26.1 {
	@Redirect(method = "simulateRightClick(Lnet/minecraft/client/input/MouseButtonInfo;Z)Lnet/minecraft/client/input/MouseButtonInfo;", require = 0, at = @At(value = "FIELD", target = "Lnet/minecraft/client/input/InputQuirks;EMULATE_RIGHT_CLICK_WITH_CTRL_KEY:Z", opcode = Opcodes.GETSTATIC))
	private static boolean modifyMouseInput_EMULATE_RIGHT_CLICK_WITH_CTRL_KEY_Adjustment()
	{
		return impl();
	}
	//?}

	private static boolean impl()
	{
		// Ensure control + left click doesn't get converted into right click if we don't want to do that
		if (Common.IS_SYSTEM_MAC && !ModOptions.disableCtrlClickFix) return false;
		//? if >=26.1 {
		if (ModernFabricReflectionHelper.FIELD_InputQuirks_EMULATE_RIGHT_CLICK_WITH_CTRL_KEY.isPresent())
		{
			return ModernFabricReflectionHelper.FIELD_InputQuirks_EMULATE_RIGHT_CLICK_WITH_CTRL_KEY.getValue(null);
		}
		else
		{
			return ModernFabricReflectionHelper.FIELD_InputQuirks_SIMULATE_RIGHT_CLICK_WITH_LONG_LEFT_CLICK.getValue(null);
		}
		//?} else {
		/*
		return InputQuirks.SIMULATE_RIGHT_CLICK_WITH_LONG_LEFT_CLICK;
		*///?}
	}
}
