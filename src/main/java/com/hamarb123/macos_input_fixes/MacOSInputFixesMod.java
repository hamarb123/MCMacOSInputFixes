package com.hamarb123.macos_input_fixes;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.SystemUtil;
import java.io.IOException;
import java.util.function.BiConsumer;

public class MacOSInputFixesMod implements ModInitializer
{
	@Override
	public void onInitialize()
	{
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

	//this function is defined in Objective C++
	public static native void registerCallbacks(BiConsumer<Double, Double> scrollCallback, long window);

	static
	{
		if (SystemUtil.getOperatingSystem() == SystemUtil.OperatingSystem.MAC)
		{
			try
			{
				//load the Objective C++ function's library
				NativeUtils.loadLibraryFromJar("/natives/macos_input_fixes.dylib");
			}
			catch (IOException e2)
			{
				//uncomment below line and replace with project path if it fails to load from jar e.g. you're running in an ide. also comment the throw line if you do this
				//System.load("<path to project>/native/macos_input_fixes.dylib");
				e2.printStackTrace();
				throw new RuntimeException(e2);
			}
		}
	}
}
