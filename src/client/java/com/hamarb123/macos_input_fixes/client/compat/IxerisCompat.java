package com.hamarb123.macos_input_fixes.client.compat;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

// Utility class for Ixeris mod compatibility (see https://github.com/hamarb123/MCMacOSInputFixes/issues/48)
public class IxerisCompat
{
	private static final boolean _isModPresent = FabricLoader.getInstance().isModLoaded("ixeris");
	private static final Object _ixerisApi;
	private static final MethodHandle _runOnRenderThreadHandle;

	static
	{
		if (_isModPresent)
		{
			// initialize Ixeris compatibility fields
			try
			{
				Class<?> clazz = Class.forName("me.decce.ixeris.api.IxerisApi");
				_ixerisApi = clazz.getMethod("getInstance").invoke(null);
				MethodHandles.Lookup lookup = MethodHandles.publicLookup();
				_runOnRenderThreadHandle = lookup.unreflect(clazz.getMethod("runLaterOnRenderThread", Runnable.class));
			}
			catch (Throwable t)
			{
				throw new RuntimeException("Exception initializing Ixeris compat!", t);
			}
		}
		else
		{
			_ixerisApi = null;
			_runOnRenderThreadHandle = null;
		}
	}

	public static boolean isModPresent()
	{
		return _isModPresent;
	}

	public static void runOnRenderThread(Runnable runnable)
	{
		try
		{
			_runOnRenderThreadHandle.invoke(_ixerisApi, runnable);
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Exception invoking Ixeris method!", e);
		}
	}
}
