package com.hamarb123.macos_input_fixes.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.hamarb123.macos_input_fixes.client.ModOptions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin11
{
	@Shadow
	private Minecraft minecraft;

	@ModifyVariable(at = @At("HEAD"), method = "onScroll(JDD)V", ordinal = 0)
	private double onMouseScroll1(double horizontal)
	{
		if (!ModOptions.reverseHotbarScrolling) return horizontal;
		if (this.minecraft.getOverlay() == null)
		{
			if (this.minecraft.screen == null)
			{
				if (this.minecraft.player != null)
				{
					if (!this.minecraft.player.isSpectator() || this.minecraft.gui.getSpectatorGui().isMenuActive())
					{
						return -horizontal;
					}
				}
			}
		}
		return horizontal;
	}

	@ModifyVariable(at = @At("HEAD"), method = "onScroll(JDD)V", ordinal = 1)
	private double onMouseScroll2(double vertical)
	{
		if (!ModOptions.reverseHotbarScrolling) return vertical;
		if (this.minecraft.getOverlay() == null)
		{
			if (this.minecraft.screen == null)
			{
				if (this.minecraft.player != null)
				{
					if (!this.minecraft.player.isSpectator() || this.minecraft.gui.getSpectatorGui().isMenuActive())
					{
						return -vertical;
					}
				}
			}
		}
		return vertical;
	}
}
