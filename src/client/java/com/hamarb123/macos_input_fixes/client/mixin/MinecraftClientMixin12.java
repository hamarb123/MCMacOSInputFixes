package com.hamarb123.macos_input_fixes.client.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.client.Common;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin12
{
	//dropping stack in game
	@Inject(method = "handleInputEvents()V", at = @At("HEAD"))
	private void keyPressed_hasControlDownBegin(CallbackInfo info)
	{
		//enable hasControlDown() injector
		Common.setInjectHasControlDown(true);
	}

	@Inject(method = "handleInputEvents()V", at = @At("RETURN"))
	private void keyPressed_hasControlDownEnd(CallbackInfo info)
	{
		//disable hasControlDown() injector
		Common.setInjectHasControlDown(false);
	}
}
