package com.hamarb123.macos_input_fixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.hamarb123.macos_input_fixes.Common;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

@Mixin(HandledScreen.class)
public class HandledScreenMixin
{
	//`keyPressed(III)Z` is the intemediary name for 1.14/1.15, and `method_25404(III)Z` is for 1.16+
	@Inject(method = {"keyPressed(III)Z", "method_25404(III)Z"}, at = @At("HEAD"), remap = false)
	private void keyPressed_hasControlDownBegin(int a, int b, int c, CallbackInfoReturnable<Boolean> info)
	{
		//enable hasControlDown() injector
		Common.setInjectHasControlDown(true);
	}
	@Inject(method = {"keyPressed(III)Z", "method_25404(III)Z"}, at = @At("RETURN"), remap = false)
	private void keyPressed_hasControlDownEnd(int a, int b, int c, CallbackInfoReturnable<Boolean> info)
	{
		//disable hasControlDown() injector
		Common.setInjectHasControlDown(false);
	}
}
