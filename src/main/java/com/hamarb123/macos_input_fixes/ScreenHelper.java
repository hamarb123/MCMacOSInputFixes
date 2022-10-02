package com.hamarb123.macos_input_fixes;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.gui.screen.Screen;

public class ScreenHelper
{
	//since the intemediary mapping for hasControlDown changed between 1.14-1.15 and 1.16+,
	//we need to load the method using reflection and call it that way

	private static MethodHandle _hasControlDown;

	private static MethodHandle hasControlDownInvoker()
	{
		if (_hasControlDown == null)
		{
			MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();

			Class<Screen> t = Screen.class;
			try
			{
				_hasControlDown = MethodHandles.publicLookup().findStatic(t, resolver.mapMethodName("intermediary", "net.minecraft.class_437", "method_25441", "()Z)"), MethodType.methodType(boolean.class));
			}
			catch (NoSuchMethodException | SecurityException | IllegalAccessException e1)
			{
				try
				{
					_hasControlDown = MethodHandles.publicLookup().findStatic(t, resolver.mapMethodName("intermediary", "net.minecraft.class_437", "hasControlDown", "()Z)"), MethodType.methodType(boolean.class));
				}
				catch (NoSuchMethodException | SecurityException | IllegalAccessException e2)
				{
					e2.printStackTrace();
				}
			}
		}
		return _hasControlDown;
	}

	//public method that is used by other types to call the hasControlDown() method
	public static boolean hasControlDown()
	{
		try
		{
			return (boolean)(Boolean)hasControlDownInvoker().invoke();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			throw new RuntimeException("Failed to find hasControlDown method.", e);
		}
	}
}
