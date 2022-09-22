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
import com.hamarb123.macos_input_fixes.MacOSInputFixesMod;

@Mixin(MinecraftClient.class)
public class MinecraftMixin
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
				MacOSInputFixesMod.registerCallbacks((a, b) -> scrollCallback(a, b), cocoaWindow);
				runOnce = true;
			}
		}
	}

	@SuppressWarnings("resource")
	private void scrollCallback(Double horizontal, Double vertical)
	{
		//recieve the native scrolling callback & convert it into a scroll event

		//enable onMouseScroll
		MacOSInputFixesMod.setAllowedInputOSX(true);

		//on 1.19 (and possibly earlier), it's getWindow(), but the window field still exists so it works
		//combine vertical & horizontal here since it's harder to do in the actual method
		((MouseInvokerMixin)mouse).callOnMouseScroll(MinecraftClient.getInstance().window.getHandle(), 0, vertical + horizontal);

		//disable onMouseScroll
		MacOSInputFixesMod.setAllowedInputOSX(false);
	}
}
