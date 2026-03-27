package com.hamarb123.macos_input_fixes.client.mixin;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.hamarb123.macos_input_fixes.client.Common;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(Minecraft.class)
public class MinecraftMixin13
{
	@WrapOperation(method = "handleKeybinds()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;hasControlDown()Z"))
	private boolean handleInputEventsIsCtrlPressedAdjustment(Minecraft instance, Operation<Boolean> original)
	{
		boolean result = original.call(instance);
		if (!result && Common.IS_SYSTEM_MAC)
		{
			Window window = instance.getWindow();
			return InputConstants.isKeyDown(window, 341) || InputConstants.isKeyDown(window, 345);
		}
		return result;
	}
}
