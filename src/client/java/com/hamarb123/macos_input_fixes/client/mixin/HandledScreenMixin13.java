package com.hamarb123.macos_input_fixes.client.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.input.KeyInput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.hamarb123.macos_input_fixes.client.Common;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(HandledScreen.class)
public class HandledScreenMixin13
{
	@ModifyExpressionValue(method = "keyPressed(Lnet/minecraft/client/input/KeyInput;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/KeyInput;hasCtrl()Z"))
    private boolean keyPressedHasCtrlAdjustment(boolean result, @Local(argsOnly = true) KeyInput input)
	{
		if (!result && Common.IS_SYSTEM_MAC)
		{
			result = (((AbstractInput)Common.asObject(input)).modifiers() & 2) != 0;
		}
		return result;
    }
}
