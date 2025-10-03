package com.hamarb123.macos_input_fixes.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.hamarb123.macos_input_fixes.client.Common;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin13
{
	@WrapOperation(method = "handleInputEvents()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;isCtrlPressed()Z"))
	private boolean handleInputEventsIsCtrlPressedAdjustment(MinecraftClient instance, Operation<Boolean> original)
	{
		boolean result = original.call(instance);
		if (!result && Common.IS_SYSTEM_MAC)
		{
			Window window = instance.getWindow();
			return InputUtil.isKeyPressed(window, 341) || InputUtil.isKeyPressed(window, 345);
		}
		return result;
	}
}
