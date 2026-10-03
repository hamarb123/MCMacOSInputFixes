package com.hamarb123.macos_input_fixes.client.mixin;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.Window;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.hamarb123.macos_input_fixes.client.Common;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

//? if >=26.1 {
import com.hamarb123.macos_input_fixes.client.ModernFabricReflectionHelper;
//?} else {
/*
import com.mojang.blaze3d.platform.InputConstants;
*///?}

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
			//? if >=26.1 {
			if (ModernFabricReflectionHelper.METHOD_InputConstants_isKeyDown_1.isPresent())
			{
				result = ModernFabricReflectionHelper.METHOD_InputConstants_isKeyDown_1.invoke(null, 341) || ModernFabricReflectionHelper.METHOD_InputConstants_isKeyDown_1.invoke(null, 345);
			}
			else
			{
				result = ModernFabricReflectionHelper.METHOD_InputConstants_isKeyDown_2.invoke(null, window, 341) || ModernFabricReflectionHelper.METHOD_InputConstants_isKeyDown_2.invoke(null, window, 345);
			}
			//?} else {
			/*
			return InputConstants.isKeyDown(window, 341) || InputConstants.isKeyDown(window, 345);
			*///?}
		}
		return result;
	}
}
