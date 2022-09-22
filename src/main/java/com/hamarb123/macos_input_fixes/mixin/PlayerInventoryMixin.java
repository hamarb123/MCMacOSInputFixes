package com.hamarb123.macos_input_fixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin
{
	@ModifyVariable(method = "scrollInHotbar(D)V", at = @At("HEAD"), ordinal = 0)
	private double fixHotbarScrollDirection(double d)
	{
		if (MinecraftClient.IS_SYSTEM_MAC)
		{
			//for hotbar events, the scrolling seems to be reversed, so we'll just reverse it again ourselves
			return -d;
		}
		else
		{
			return d;
		}
	}
}
