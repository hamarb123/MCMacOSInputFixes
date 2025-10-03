package com.hamarb123.macos_input_fixes.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.hamarb123.macos_input_fixes.client.ModOptions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;

@Mixin(Mouse.class)
public class MouseMixin11
{
	@Shadow
	private MinecraftClient client;

	@ModifyVariable(at = @At("HEAD"), method = "onMouseScroll(JDD)V", ordinal = 0)
	private double onMouseScroll1(double horizontal)
	{
		if (!ModOptions.reverseHotbarScrolling) return horizontal;
		if (this.client.getOverlay() == null)
		{
			if (this.client.currentScreen == null)
			{
				if (this.client.player != null)
				{
					if (!this.client.player.isSpectator() || this.client.inGameHud.getSpectatorHud().isOpen())
					{
						return -horizontal;
					}
				}
			}
		}
		return horizontal;
	}

	@ModifyVariable(at = @At("HEAD"), method = "onMouseScroll(JDD)V", ordinal = 1)
	private double onMouseScroll2(double vertical)
	{
		if (!ModOptions.reverseHotbarScrolling) return vertical;
		if (this.client.getOverlay() == null)
		{
			if (this.client.currentScreen == null)
			{
				if (this.client.player != null)
				{
					if (!this.client.player.isSpectator() || this.client.inGameHud.getSpectatorHud().isOpen())
					{
						return -vertical;
					}
				}
			}
		}
		return vertical;
	}
}
