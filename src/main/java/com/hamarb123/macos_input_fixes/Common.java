package com.hamarb123.macos_input_fixes;

import com.hamarb123.macos_input_fixes.compat.IxerisCompat;
import com.hamarb123.macos_input_fixes.mixin.MinecraftClientAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
public class Common
{
	public static boolean hasControlDownInjector()
	{
		//disable the injector for this call but put back after
		boolean oldValue = injectHasControlDown();
		setInjectHasControlDown(false);
		boolean returnValue;

		//if not on macOS, use normal implementation
		if (!MinecraftClient.IS_SYSTEM_MAC)
		{
			returnValue = FabricReflectionHelper.Screen_hasControlDown();
		}
		else
		{
			//replace hasControlDown() on macOS with hasControlDown() (which tests command) or 'actual control down' for this function only
			returnValue = FabricReflectionHelper.Screen_hasControlDown() ||
				//ctrl key check
				InputUtil.isKeyPressed(((MinecraftClientAccessor)MinecraftClient.getInstance()).getWindow().getHandle(), 341) ||
				InputUtil.isKeyPressed(((MinecraftClientAccessor)MinecraftClient.getInstance()).getWindow().getHandle(), 345);
		}

		//restore injector and return
		setInjectHasControlDown(oldValue);
		return returnValue;
	}

	//enable and disable the onMouseScroll function
	private static ThreadLocal<Boolean> _allowInputOSX = new ThreadLocal<Boolean>();
	public static boolean allowInputOSX()
	{
		Boolean value = _allowInputOSX.get();
		return value != null && value;
	}
	public static void setAllowedInputOSX(boolean value)
	{
		_allowInputOSX.set(value);
	}

	//enable and disable the onKey function (for some specific key codes)
	private static ThreadLocal<Boolean> _allowInputOSX2 = new ThreadLocal<Boolean>();
	public static boolean allowInputOSX2()
	{
		Boolean value = _allowInputOSX2.get();
		return value != null && value;
	}
	public static void setAllowedInputOSX2(boolean value)
	{
		_allowInputOSX2.set(value);
	}

	//enable and disable the addAll parameter modification mixin
	private static ThreadLocal<Boolean> _modifyAddAllParameter = new ThreadLocal<Boolean>();
	public static boolean modifyAddAllParameter()
	{
		Boolean value = _modifyAddAllParameter.get();
		return value != null && value;
	}
	public static void setModifyAddAllParameter(boolean value)
	{
		_modifyAddAllParameter.set(value);
	}

	//enable and disable the hasControlDown mixin
	private static ThreadLocal<Boolean> _injectHasControlDown = new ThreadLocal<Boolean>();
	public static boolean injectHasControlDown()
	{
		Boolean value = _injectHasControlDown.get();
		return value != null && value;
	}
	public static void setInjectHasControlDown(boolean value)
	{
		_injectHasControlDown.set(value);
	}

	//enable and disable the CyclingButtonWidgetMixin3 builder mixin
	private static ThreadLocal<Boolean> _omitBuilderKeyText = new ThreadLocal<Boolean>();
	public static boolean omitBuilderKeyText()
	{
		Boolean value = _omitBuilderKeyText.get();
		return value != null && value;
	}
	public static void setOmitBuilderKeyText(boolean value)
	{
		_omitBuilderKeyText.set(value);
	}

	//assumes we are on the main/event thread
	public static void runOnRenderThreadHelper(Runnable runnable)
	{
		if (IxerisCompat.isModPresent())
		{
			IxerisCompat.runOnRenderThread(runnable);
		}
		else
		{
			runnable.run();
		}
	}
}
