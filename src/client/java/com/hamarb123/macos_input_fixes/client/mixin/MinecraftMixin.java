package com.hamarb123.macos_input_fixes.client.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.client.Common;
import com.hamarb123.macos_input_fixes.client.KeyCallback;
import com.hamarb123.macos_input_fixes.client.MacOSInputFixesClientMod;
import com.hamarb123.macos_input_fixes.client.ModOptions;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.main.GameConfig;

//? if >=26.1 {
import org.spongepowered.asm.mixin.injection.At.Shift;
import com.hamarb123.macos_input_fixes.client.ModernFabricReflectionHelper;
//?} else {
/*
import com.hamarb123.macos_input_fixes.client.FabricReflectionHelper;
import org.lwjgl.glfw.GLFWNativeCocoa;
*///?}

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

	//? if >=26.1 {
	@Inject(method = "<init>(Lnet/minecraft/client/main/GameConfig;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;window:Lcom/mojang/blaze3d/platform/Window;", opcode = Opcodes.PUTFIELD, shift = Shift.AFTER), cancellable = false)
	private void onWindowSet(GameConfig config, CallbackInfo info)
	//?} else {
	/*
	@Inject(at = @At("HEAD"), method = "setWindowActive(Z)V", cancellable = true)
	private void onWindowFocusChanged(boolean focused, CallbackInfo info)
	*///?}
	{
		if (Common.IS_SYSTEM_MAC)
		{
			if (!runOnce)
			{
				//register the native callback for scrolling
				long windowHandle = window.handle();
				long cocoaWindow;
				KeyCallback keyCallback = this::keyCallback;
				//? if >=26.1 {
				if (ModernFabricReflectionHelper.CLASS_GLFW.isPresent())
				{
					cocoaWindow = ModernFabricReflectionHelper.METHOD_GLFWNativeCocoa_glfwGetCocoaWindow.invoke(windowHandle);
				}
				else
				{
					int properties = ModernFabricReflectionHelper.METHOD_SDLVideo_SDL_GetWindowProperties.invoke(windowHandle);
					cocoaWindow = ModernFabricReflectionHelper.METHOD_SDLProperties_SDL_GetPointerProperty.invoke(properties, ModernFabricReflectionHelper.FIELD_SDLVideo_SDL_PROP_WINDOW_COCOA_WINDOW_POINTER.getValue(null), 0L);
					keyCallback = null;
				}
				//?} else {
				/*
				cocoaWindow = GLFWNativeCocoa.glfwGetCocoaWindow(windowHandle);
				*///?}
				MacOSInputFixesClientMod.registerCallbacks(this::scrollCallback, keyCallback, cocoaWindow);
				runOnce = true;
			}
		}
	}

	private static final long SCROLL_GROUPING_TIME_NS = 100_000_000; //100ms in nanoseconds
	private static long lastIncreasingDiscreteHorizontalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;
	private static long lastIncreasingDiscreteVerticalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;
	private static long lastDecreasingDiscreteHorizontalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;
	private static long lastDecreasingDiscreteVerticalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;
	private static long lastIncreasingHotbarHorizontalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;
	private static long lastDecreasingHotbarHorizontalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;
	private static long lastIncreasingHotbarVerticalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;
	private static long lastDecreasingHotbarVerticalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;

	private void scrollCallback(double horizontal, double vertical, double horizontalWithMomentum, double verticalWithMomentum, double horizontalUngrouped, double verticalUngrouped)
	{
		//recieve the native scrolling callback & convert it into a scroll event

		//? if >=26.1 {
		//SDL can deliver scroll events while Minecraft is still constructing, before gui is set
		if (((Minecraft)(Object)this).gui == null) return;
		//?}

		//determine if discrete scroll is enabled
		//? if >=26.1 {
		boolean discreteScroll = options.discreteMouseScroll().get();
		//?} else {
		/*
		boolean discreteScroll = FabricReflectionHelper.Try_SimpleOption() != null
			? (boolean)(Boolean)FabricReflectionHelper.SimpleOption_getValue(FabricReflectionHelper.GameOptions_getDiscreteMouseScroll(options)) //1.19+
			: FabricReflectionHelper.GameOptions_discreteMouseScroll(options); //1.14-1.18
		*///?}

		//replace ungrouped values with grouped values if discrete scroll is enabled
		if (discreteScroll)
		{
			horizontalUngrouped = horizontalWithMomentum;
			verticalUngrouped = verticalWithMomentum;
		}

		//use ungrouped values if not scrolling on hotbar
		//? if >=26.1 {
		if (ModernFabricReflectionHelper.getOverlay((Minecraft)(Object)this) != null || ModernFabricReflectionHelper.getScreen((Minecraft)(Object)this) != null || ((Minecraft)(Object)this).player == null)
		//?} else {
		/*
		if (((Minecraft)(Object)this).getOverlay() != null || ((Minecraft)(Object)this).screen != null || ((Minecraft)(Object)this).player == null)
		*///?}
		{
			//apply discrete grouping if enabled:
			if (ModOptions.groupDiscreteScrolls && discreteScroll)
			{
				if (horizontalUngrouped > 0)
				{
					if (System.nanoTime() - lastIncreasingDiscreteHorizontalScrollTime >= SCROLL_GROUPING_TIME_NS)
					{
						lastIncreasingDiscreteHorizontalScrollTime = System.nanoTime();
					}
					else
					{
						horizontalUngrouped = 0;
					}
					lastDecreasingDiscreteHorizontalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;
				}
				else if (horizontalUngrouped < 0)
				{
					if (System.nanoTime() - lastDecreasingDiscreteHorizontalScrollTime >= SCROLL_GROUPING_TIME_NS)
					{
						lastDecreasingDiscreteHorizontalScrollTime = System.nanoTime();
					}
					else
					{
						horizontalUngrouped = 0;
					}
					lastIncreasingDiscreteHorizontalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;
				}

				if (verticalUngrouped > 0)
				{
					if (System.nanoTime() - lastIncreasingDiscreteVerticalScrollTime >= SCROLL_GROUPING_TIME_NS)
					{
						lastIncreasingDiscreteVerticalScrollTime = System.nanoTime();
					}
					else
					{
						verticalUngrouped = 0;
					}
					lastDecreasingDiscreteVerticalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;
				}
				else if (verticalUngrouped < 0)
				{
					if (System.nanoTime() - lastDecreasingDiscreteVerticalScrollTime >= SCROLL_GROUPING_TIME_NS)
					{
						lastDecreasingDiscreteVerticalScrollTime = System.nanoTime();
					}
					else
					{
						verticalUngrouped = 0;
					}
					lastIncreasingDiscreteVerticalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;
				}
			}

			horizontal = horizontalUngrouped;
			vertical = verticalUngrouped;
		}

		//combine vertical & horizontal here since it's harder to do in the actual method (when scrolling for hotbar)
		else
		{
			//apply discrete grouping if enabled:
			if (ModOptions.groupDiscreteScrolls)
			{
				if (horizontal > 0)
				{
					if (System.nanoTime() - lastIncreasingHotbarHorizontalScrollTime >= SCROLL_GROUPING_TIME_NS)
					{
						lastIncreasingHotbarHorizontalScrollTime = System.nanoTime();
					}
					else
					{
						horizontal = 0;
					}
					lastDecreasingHotbarHorizontalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;
				}
				else if (horizontal < 0)
				{
					if (System.nanoTime() - lastDecreasingHotbarHorizontalScrollTime >= SCROLL_GROUPING_TIME_NS)
					{
						lastDecreasingHotbarHorizontalScrollTime = System.nanoTime();
					}
					else
					{
						horizontal = 0;
					}
					lastIncreasingHotbarHorizontalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;
				}

				if (vertical > 0)
				{
					if (System.nanoTime() - lastIncreasingHotbarVerticalScrollTime >= SCROLL_GROUPING_TIME_NS)
					{
						lastIncreasingHotbarVerticalScrollTime = System.nanoTime();
					}
					else
					{
						vertical = 0;
					}
					lastDecreasingHotbarVerticalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;
				}
				else if (vertical < 0)
				{
					if (System.nanoTime() - lastDecreasingHotbarVerticalScrollTime >= SCROLL_GROUPING_TIME_NS)
					{
						lastDecreasingHotbarVerticalScrollTime = System.nanoTime();
					}
					else
					{
						vertical = 0;
					}
					lastIncreasingHotbarVerticalScrollTime = System.nanoTime() - SCROLL_GROUPING_TIME_NS;
				}

				//since we're dropping events effectively randomly, let's just always make it discrete here also:
				horizontal = Math.signum(horizontal);
				vertical = Math.signum(vertical);
			}

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
			((MouseHandlerInvokerMixin)mouseHandler).callOnScroll(((MinecraftAccessor)Minecraft.getInstance()).getWindow().handle(), horizontalCopy, verticalCopy);

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

			//? if >=26.1 {
			((KeyboardHandlerAccessor15)keyboardHandler).callKeyPress(Minecraft.getInstance().getWindow().handle(), action, new KeyEvent(key, scancode, modifiers));
			//?} else {
			/*
			if (FabricReflectionHelper.Has_Keyboard_onKey_2())
			{
				FabricReflectionHelper.Keyboard_onKey_2(keyboardHandler, Minecraft.getInstance().getWindow().handle(), action, FabricReflectionHelper.new_KeyInput(key, scancode, modifiers));
			}
			else
			{
				//on 1.14 we need to use the window field, on 1.19 the field still exists
				FabricReflectionHelper.Keyboard_onKey_1(keyboardHandler, ((MinecraftAccessor)Minecraft.getInstance()).getWindow().handle(), key, scancode, action, modifiers);
			}
			*///?}

			//disable onKey
			Common.setAllowedInputOSX2(false);
		});
	}
}
