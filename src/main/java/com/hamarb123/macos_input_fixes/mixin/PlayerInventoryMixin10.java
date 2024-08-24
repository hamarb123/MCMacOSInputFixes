package com.hamarb123.macos_input_fixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.hamarb123.macos_input_fixes.ModOptions;
import net.minecraft.entity.player.PlayerInventory;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin10
{
	@ModifyVariable(method = "scrollInHotbar(D)V", at = @At("HEAD"), ordinal = 0)
	private double fixHotbarScrollDirection(double d)
	{
		//if the reverse hotbar scrolling option is enabled, reverse the scroll value
		return ModOptions.reverseHotbarScrolling ? -d : d;
	}
}
