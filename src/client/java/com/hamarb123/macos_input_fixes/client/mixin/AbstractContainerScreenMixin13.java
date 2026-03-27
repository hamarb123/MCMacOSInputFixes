package com.hamarb123.macos_input_fixes.client.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.hamarb123.macos_input_fixes.client.Common;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin13
{
	@ModifyExpressionValue(method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/KeyEvent;hasControlDown()Z"))
	private boolean keyPressedHasCtrlAdjustment(boolean result, @Local(argsOnly = true) KeyEvent input)
	{
		if (!result && Common.IS_SYSTEM_MAC)
		{
			result = (((InputWithModifiers)Common.asObject(input)).modifiers() & 2) != 0;
		}
		return result;
	}
}
