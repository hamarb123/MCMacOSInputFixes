package com.hamarb123.macos_input_fixes.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.client.Common;
import com.hamarb123.macos_input_fixes.client.ModOptions;

//? if >=26.1 {
import com.hamarb123.macos_input_fixes.client.ModernFabricReflectionHelper;
//?}

@Mixin(MouseHandler.class)
public class MouseHandlerMixin
{
	@Shadow
	private Minecraft minecraft;

	//if the lock scroll while attacking option is enabled, ignore scroll events while the attack key is held down in game
	//this stops a small finger movement on a Magic Mouse / trackpad from changing the selected hotbar slot while mining
	//this works on all platforms
	@Inject(at = @At("HEAD"), method = "onScroll(JDD)V", cancellable = true)
	private void lockScrollWhileAttacking(long window, double horizontal, double vertical, CallbackInfo info)
	{
		//only act if the option is on and we're actually in game (no screen or overlay open)
		if (!ModOptions.lockScrollWhileAttacking) return;
		//? if >=26.1 {
		if (ModernFabricReflectionHelper.getOverlay(this.minecraft) != null) return;
		if (ModernFabricReflectionHelper.getScreen(this.minecraft) != null) return;
		//?} else {
		/*
		if (this.minecraft.getOverlay() != null) return;
		if (this.minecraft.screen != null) return;
		*///?}
		if (this.minecraft.player == null) return;

		//cancel the scroll while the attack key is held down (default left click, follows any rebind of the attack key)
		if (this.minecraft.options.keyAttack.isDown() || this.minecraft.options.keyUse.isDown())
		{
			info.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "onScroll(JDD)V", cancellable = true)
	private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo info)
	{
		if (Common.IS_SYSTEM_MAC)
		{
			if (vertical == 0)
			{
				//if vertical is 0 then there is no scroll
				info.cancel();
				return;
			}
			if (!Common.allowInputOSX())
			{
				//only accept scroll event on macOS if it's from the native callback
				info.cancel();
				return;
			}
		}
	}

	@ModifyVariable(method = "onScroll(JDD)V", at = @At("HEAD"), ordinal = 0)
	private double maybeReverseHScroll(double value)
	{
		//if the reverse scrolling option is enabled, reverse the horizontal scroll value
		return ModOptions.reverseScrolling ? -value : value;
	}

	@ModifyVariable(method = "onScroll(JDD)V", at = @At("HEAD"), ordinal = 1)
	private double maybeReverseVScroll(double value)
	{
		//if the reverse scrolling option is enabled, reverse the vertical scroll value
		return ModOptions.reverseScrolling ? -value : value;
	}
}
