package com.hamarb123.macos_input_fixes.client.mixin;

import org.lwjgl.glfw.GLFWNativeCocoa;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.hamarb123.macos_input_fixes.client.Common;
import com.hamarb123.macos_input_fixes.client.FabricReflectionHelper;
import com.hamarb123.macos_input_fixes.client.MacOSInputFixesClientMod;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;

@Mixin(Minecraft.class)
public class MinecraftMixin
{
	@Shadow
	private Window window;

	@Shadow
	private MouseHandler mouseHandler;

	@Shadow
	private KeyboardHandler keyboardHandler;

	@Shadow
	private Options options;

	private boolean runOnce = false;

	//function that is called immediately after the window is created on both versions
	@Inject(at = @At("HEAD"), method = "setWindowActive(Z)V", cancellable = true)
	private void onWindowFocusChanged(boolean focused, CallbackInfo info)
	{
		if (Common.IS_SYSTEM_MAC)
		{
			if (!runOnce)
			{
				//register the native callback for scrolling
				long glfwWindow = window.handle();
				long cocoaWindow = GLFWNativeCocoa.glfwGetCocoaWindow(glfwWindow);
				MacOSInputFixesClientMod.registerCallbacks(this::scrollCallback, this::keyCallback, cocoaWindow);
				runOnce = true;
			}
		}
	}

	private void scrollCallback(double horizontal, double vertical, double horizontalWithMomentum, double verticalWithMomentum, double horizontalUngrouped, double verticalUngrouped)
	{
		//recieve the native scrolling callback & convert it into a scroll event

		//determine if discrete scroll is enabled
		boolean discreteScroll = FabricReflectionHelper.Try_SimpleOption() != null
			? (boolean)(Boolean)FabricReflectionHelper.SimpleOption_getValue(FabricReflectionHelper.GameOptions_getDiscreteMouseScroll(options)) //1.19+
			: FabricReflectionHelper.GameOptions_discreteMouseScroll(options); //1.14-1.18

		//replace ungrouped values with grouped values if discrete scroll is enabled
		if (discreteScroll)
		{
			horizontalUngrouped = horizontalWithMomentum;
			verticalUngrouped = verticalWithMomentum;
		}

		//use ungrouped values if not scrolling on hotbar
		if (((Minecraft)(Object)this).getOverlay() != null || ((Minecraft)(Object)this).screen != null || ((Minecraft)(Object)this).player == null)
		{
			horizontal = horizontalUngrouped;
			vertical = verticalUngrouped;
		}

		//combine vertical & horizontal here since it's harder to do in the actual method (when scrolling for hotbar)
		else
		{
			vertical += horizontal;
			horizontal = 0;
		}

		//check if we actually have an event still
		if (horizontal == 0 && vertical == 0) return;

		double horizontalCopy = horizontal;
		double verticalCopy = vertical;
		Common.runOnRenderThreadHelper(() ->
		{
			//enable onMouseScroll
			Common.setAllowedInputOSX(true);

			//on 1.14 we need to use the window field, on 1.19 the field still exists
			((MouseHandlerInvokerMixin) mouseHandler).callOnScroll(((MinecraftAccessor)Minecraft.getInstance()).getWindow().handle(), horizontalCopy, verticalCopy);

			//disable onMouseScroll
			Common.setAllowedInputOSX(false);
		});
	}

	private void keyCallback(int key, int scancode, int action, int modifiers)
	{
		Common.runOnRenderThreadHelper(() ->
		{
			//enable onKey
			Common.setAllowedInputOSX2(true);

			if (FabricReflectionHelper.Has_Keyboard_onKey_2())
			{
				FabricReflectionHelper.Keyboard_onKey_2(keyboardHandler, Minecraft.getInstance().getWindow().handle(), action, FabricReflectionHelper.new_KeyInput(key, scancode, modifiers));
			}
			else
			{
				//on 1.14 we need to use the window field, on 1.19 the field still exists
				FabricReflectionHelper.Keyboard_onKey_1(keyboardHandler, ((MinecraftAccessor)Minecraft.getInstance()).getWindow().handle(), key, scancode, action, modifiers);
			}

			//disable onKey
			Common.setAllowedInputOSX2(false);
		});
	}
}
