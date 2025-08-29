package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.FabricReflectionHelper;
import com.hamarb123.macos_input_fixes.MacOSInputFixesClientMod;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin
{
	@Shadow
	private Window window;

	@Shadow
	private Mouse mouse;

	@Shadow
	private Keyboard keyboard;

	@Shadow
	private GameOptions options;

	private boolean runOnce = false;

	//function that is called immediately after the window is created on both versions
	@Inject(at = @At("HEAD"), method = "onWindowFocusChanged(Z)V", cancellable = true)
	private void onWindowFocusChanged(boolean focused, CallbackInfo info)
	{
		if (MinecraftClient.IS_SYSTEM_MAC)
		{
			if (!runOnce)
			{
				//register the native callback for scrolling
				long glfwWindow = window.getHandle();
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
		if (((MinecraftClient)(Object)this).getOverlay() != null || ((MinecraftClient)(Object)this).currentScreen != null || ((MinecraftClient)(Object)this).player == null)
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

		double finalHorizontal = horizontal;
		double finalVertical = vertical;
		Common.runOnRenderThread(() ->
		{
			//enable onMouseScroll
			Common.setAllowedInputOSX(true);

			//on 1.14 we need to use the window field, on 1.19 the field still exists
			((MouseInvokerMixin)mouse).callOnMouseScroll(((MinecraftClientAccessor)MinecraftClient.getInstance()).getWindow().getHandle(), finalHorizontal, finalVertical);

			//disable onMouseScroll
			Common.setAllowedInputOSX(false);
		});
	}

	private void keyCallback(int key, int scancode, int action, int modifiers)
	{
		Common.runOnRenderThread(() ->
        {
            //enable onKey
            Common.setAllowedInputOSX2(true);

            //on 1.14 we need to use the window field, on 1.19 the field still exists
            keyboard.onKey(((MinecraftClientAccessor)MinecraftClient.getInstance()).getWindow().getHandle(), key, scancode, action, modifiers);

            //disable onKey
            Common.setAllowedInputOSX2(false);
        });
	}

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
