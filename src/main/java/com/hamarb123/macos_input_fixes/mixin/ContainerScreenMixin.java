package com.hamarb123.macos_input_fixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.hamarb123.macos_input_fixes.Common;
import net.minecraft.client.gui.ContainerScreen;

@Mixin(ContainerScreen.class)
public class ContainerScreenMixin
{
	//not mixed in properly
	//dropping stack in container
	@Inject(method = "keyPressed(III)Z", at = @At("HEAD"))
	private void keyPressed_hasControlDownBegin(int a, int b, int c, CallbackInfoReturnable<Boolean> info)
	{
		Common.setInjectHasControlDown(true);
	}
	@Inject(method = "keyPressed(III)Z", at = @At("RETURN"))
	private void keyPressed_hasControlDownEnd(int a, int b, int c, CallbackInfoReturnable<Boolean> info)
	{
		Common.setInjectHasControlDown(false);
	}

	//the following doesn't work for some unknown reason on newer versions
	/*
	@Redirect(method = "keyPressed(III)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ContainerScreen;hasControlDown()Z"))
	private boolean keyPressed_hasControlDown()
	{
		//dropping stack in container
		return Common.hasControlDownInjector();
	}
	*/
}
