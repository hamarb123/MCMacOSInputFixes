package com.hamarb123.macos_input_fixes.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.hamarb123.macos_input_fixes.Common;
import com.hamarb123.macos_input_fixes.MacOSInputFixesClientMod;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin
{
	@Shadow
	private Window window;

	@Shadow
	private Mouse mouse;

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
				MacOSInputFixesClientMod.registerCallbacks(this::scrollCallback, cocoaWindow);
				runOnce = true;
			}
		}
	}

	private void scrollCallback(Double horizontal, Double vertical)
	{
		//recieve the native scrolling callback & convert it into a scroll event

		//enable onMouseScroll
		Common.setAllowedInputOSX(true);

		//on 1.14 we need to use the window field, on 1.19 the field still exists
		//combine vertical & horizontal here since it's harder to do in the actual method
		((MouseInvokerMixin)mouse).callOnMouseScroll(((MinecraftClientAccessor)MinecraftClient.getInstance()).getWindow().getHandle(), 0, vertical + horizontal);

		//disable onMouseScroll
		Common.setAllowedInputOSX(false);
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
