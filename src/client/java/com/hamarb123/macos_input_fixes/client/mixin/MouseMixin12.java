package com.hamarb123.macos_input_fixes.client.mixin;

import net.minecraft.client.Mouse;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.hamarb123.macos_input_fixes.client.Common;
import com.hamarb123.macos_input_fixes.client.ModOptions;

@Mixin(Mouse.class)
public class MouseMixin12
{
	//@Redirect(method = "onMouseButton(JIII)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;IS_SYSTEM_MAC:Z", opcode = Opcodes.GETSTATIC))
	@Redirect(method = "method_1601(JIII)V", at = @At(value = "FIELD", target = "net/minecraft/class_310;field_1703:Z", opcode = Opcodes.GETSTATIC, remap = false), remap = false)
	private boolean leftMouseClick()
	{
		//check if we want to disable the below fix
		if (ModOptions.disableCtrlClickFix) return Common.IS_SYSTEM_MAC;

		//onMouseButton converts left click + control on macOS to right click, simply tell it we're not on macOS so it can't do that (there's no other uses of macOS in the function)
		return false;
	}
}
