package com.hamarb123.macos_input_fixes.client;

import java.io.IOException;
import net.fabricmc.api.ClientModInitializer;

public class MacOSInputFixesClientMod implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		ModOptions.loadOptions();
	}

	//these functions are defined in Objective C++
	public static native void registerCallbacks(ScrollCallback scrollCallback, KeyCallback keyCallback, long window);
	public static native void setTrackpadSensitivity(double sensitivity);
	public static native void setMomentumScrolling(boolean option);
	public static native void setInterfaceSmoothScroll(boolean option);

	static
	{
		if (Common.IS_SYSTEM_MAC)
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
