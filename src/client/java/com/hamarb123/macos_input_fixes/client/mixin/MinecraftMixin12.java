package com.hamarb123.macos_input_fixes.client.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.client.Common;

@Mixin(Minecraft.class)
public class MinecraftMixin12
{
	//dropping stack in game
	@Inject(method = "handleKeybinds()V", at = @At("HEAD"))
	private void keyPressed_hasControlDownBegin(CallbackInfo info)
	{
		//enable hasControlDown() injector
		Common.setInjectHasControlDown(true);
	}

	@Inject(method = "handleKeybinds()V", at = @At("RETURN"))
	private void keyPressed_hasControlDownEnd(CallbackInfo info)
	{
		//disable hasControlDown() injector
		Common.setInjectHasControlDown(false);
	}
}
