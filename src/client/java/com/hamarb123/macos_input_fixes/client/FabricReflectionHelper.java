package com.hamarb123.macos_input_fixes.client;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.InputUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class FabricReflectionHelper
{
	//Helpers:

	//Looks up a class from Fabric at runtime
	private static Class<?> lookupClass(String unmappedName, String name)
	{
		try
		{
			MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
			return Class.forName(resolver.mapClassName("intermediary", unmappedName));
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to lookup type " + name + " under the mapping " + unmappedName, t);
		}
	}

	//Try to look up a class from Fabric at runtime, returning null instead of throwing any errors
	private static Class<?> tryLookupClass(String unmappedName, String name)
	{
		try
		{
			return lookupClass(unmappedName, name);
		}
		catch (Throwable t)
		{
			return null;
		}
	}

	//Looks up a method from Fabric at runtime
	private static MethodHandle lookupMethod(String unmappedContainingType, String unmappedName, String unmappedDescriptor, boolean isStatic, boolean isPrivate, Class<?> containingType, String name, Class<?> rType, Class<?>... pTypes)
	{
		try
		{
			MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
			String methodName = resolver.mapMethodName("intermediary", unmappedContainingType, unmappedName, unmappedDescriptor);
			if (!isPrivate)
			{
				MethodType methodType = MethodType.methodType(rType, pTypes);
				Lookup lookup = MethodHandles.publicLookup();
				if (isStatic) return lookup.findStatic(containingType, methodName, methodType);
				else return lookup.findVirtual(containingType, methodName, methodType);
			}
			else
			{
				Method m = containingType.getDeclaredMethod(methodName, pTypes);
				if (!m.getReturnType().equals(rType)) throw new RuntimeException("Found method, but with incorrect return type.");
				if (Modifier.isStatic(m.getModifiers()) != isStatic) throw new RuntimeException("Found method, but with incorrect staticity.");
				m.setAccessible(true);
				return MethodHandles.lookup().unreflect(m);
			}
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to lookup method " + name + " in " + containingType + " under the mapping " + unmappedName + unmappedDescriptor, t);
		}
	}

	//Try to look up a method from Fabric at runtime, returning null instead of throwing any errors
	private static MethodHandle tryLookupMethod(String unmappedContainingType, String unmappedName, String unmappedDescriptor, boolean isStatic, boolean isPrivate, Class<?> containingType, String name, Class<?> rType, Class<?>... pTypes)
	{
		try
		{
			return lookupMethod(unmappedContainingType, unmappedName, unmappedDescriptor, isStatic, isPrivate, containingType, name, rType, pTypes);
		}
		catch (Throwable t)
		{
			return null;
		}
	}

	//Invokes a MethodHandle, and deals with any potential exceptions from it
	private static Object invokeMethod(String methodName, MethodHandle handle, Object... args)
	{
		try
		{
			return handle.invokeWithArguments(args);
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Exception thrown while executing " + methodName, t);
		}
	}

	//Looks up a constructor from Fabric at runtime
	private static MethodHandle lookupConstructor(String unmappedDescriptor, boolean isPrivate, Class<?> containingType, Class<?>... pTypes)
	{
		try
		{
			MethodType methodType = MethodType.methodType(void.class, pTypes);
			Lookup lookup = isPrivate ? MethodHandles.lookup() : MethodHandles.publicLookup();
			return lookup.findConstructor(containingType, methodType);
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to lookup constructor in " + containingType + " with the descriptor " + unmappedDescriptor, t);
		}
	}

	//Looks up constructor info from Fabric at runtime
	private static Constructor<?> lookupConstructorInfo(String unmappedDescriptor, boolean isPrivate, Class<?> containingType, Class<?>... pTypes)
	{
		try
		{
			Constructor<?> ctor = isPrivate ? containingType.getDeclaredConstructor(pTypes) : containingType.getConstructor(pTypes);
			ctor.setAccessible(true);
			return ctor;
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to lookup constructor in " + containingType + " with the descriptor " + unmappedDescriptor, t);
		}
	}

	//Try to look up a constructor from Fabric at runtime, returning null instead of throwing any errors
	private static MethodHandle tryLookupConstructor(String unmappedDescriptor, boolean isPrivate, Class<?> containingType, Class<?>... pTypes)
	{
		try
		{
			return lookupConstructor(unmappedDescriptor, isPrivate, containingType, pTypes);
		}
		catch (Throwable t)
		{
			return null;
		}
	}

	//Looks up a field from Fabric at runtime
	private static Field lookupField(String unmappedContainingType, String unmappedName, String unmappedDescriptor, boolean isStatic, boolean isPrivate, Class<?> containingType, String name, Class<?> fieldType)
	{
		try
		{
			MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
			String fieldName = resolver.mapFieldName("intermediary", unmappedContainingType, unmappedName, unmappedDescriptor);
			Field[] fields = isPrivate ? containingType.getDeclaredFields() : containingType.getFields();
			for (Field f : fields)
			{
				if (f.getName().equals(fieldName))
				{
					if (Modifier.isStatic(f.getModifiers()) == isStatic)
					{
						if (f.getType().equals(fieldType))
						{
							f.setAccessible(true);
							return f;
						}
					}
				}
			}
			throw new Exception("Failed to find the field.");
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to lookup " + (isPrivate ? "private" : "public") + (isStatic ? " static" : " instance") + " field " + name + " in " + containingType + " under the mapping " + unmappedName + " with descriptor " + unmappedDescriptor, t);
		}
	}

	//Reads from a field, and deals with any potential exceptions from it
	private static Object readField(String fieldName, Field f, Object instance)
	{
		try
		{
			return f.get(instance);
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Exception thrown while reading " + fieldName, t);
		}
	}

	//Uses LambdaMetafactory to convert a functional interface
	private static MethodHandle makeLambdaConverter(String toUnmapped, String methodName, String methodDescriptor, Class<?> from, Class<?> to, Class<?> returnTypeFrom, Class<?> returnTypeTo, Class<?>[] pTypesFrom, Class<?>[] pTypesTo)
	{
		try
		{
			MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
			String mappedMethodName = resolver.mapMethodName("intermediary", toUnmapped, methodName, methodDescriptor);
			MethodType mTypeTo = MethodType.methodType(returnTypeTo, pTypesTo);
			MethodType mTypeFrom = MethodType.methodType(returnTypeFrom, pTypesFrom);
			Lookup lookup = MethodHandles.lookup();
			return LambdaMetafactory.metafactory
			(
				lookup,
				mappedMethodName,
				MethodType.methodType(to, from),
				mTypeTo,
				lookup.findVirtual(from, methodName, mTypeFrom),
				mTypeTo
			).getTarget();
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Exception while converting lambda expression from " + from + " to " + to, t);
		}
	}

	//Uses LambdaMetafactory to convert a functional interface (simplified)
	private static MethodHandle makeLambdaConverter(String toUnmapped, String methodName, String methodDescriptor, Class<?> from, Class<?> to, Class<?> returnType, Class<?>... pTypes)
	{
		return makeLambdaConverter(toUnmapped, methodName, methodDescriptor, from, to, returnType, returnType, pTypes, pTypes);
	}


	//Classes:

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_316}</p>
	 * <p>Mapped name: {@code net.minecraft.client.option.Option}</p>
	 * <p>Versions: 1.14-1.18.x</p>
	 */
	public static Class<?> Option()
	{
		if (_OptionClass == null) _OptionClass = lookupClass("net.minecraft.class_316", "net.minecraft.client.option.Option");
		return _OptionClass;
	}
	private static Class<?> _OptionClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_349}</p>
	 * <p>Mapped name: {@code net.minecraft.client.gui.widget.OptionButtonWidget}</p>
	 * <p>Versions: 1.14.1-1.16.x</p>
	 */
	public static Class<?> OptionButtonWidget()
	{
		if (_OptionButtonWidgetClass == null) _OptionButtonWidgetClass = lookupClass("net.minecraft.class_349", "net.minecraft.client.gui.widget.OptionButtonWidget");
		return _OptionButtonWidgetClass;
	}
	private static Class<?> _OptionButtonWidgetClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_2585}</p>
	 * <p>Mapped name: {@code net.minecraft.text.LiteralText} (1.14.3+), {@code net.minecraft.text.TextComponent} (1.14-1.14.2)</p>
	 * <p>Versions: 1.14-1.18.x</p>
	 */
	//it was changed to a record in 1.19, and it also doesn't implement Text, so ignore it there for now
	public static Class<?> LiteralText()
	{
		if (_LiteralTextClass == null) _LiteralTextClass = lookupClass("net.minecraft.class_2585", "net.minecraft.text.LiteralText");
		return _LiteralTextClass;
	}
	private static Class<?> _LiteralTextClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_4040}</p>
	 * <p>Mapped name: {@code net.minecraft.client.gui.widget.DoubleOptionSliderWidget}</p>
	 * <p>Versions: 1.14-1.18.x</p>
	 */
	public static Class<?> DoubleOptionSliderWidget()
	{
		if (_DoubleOptionSliderWidgetClass == null) _DoubleOptionSliderWidgetClass = lookupClass("net.minecraft.class_4040", "net.minecraft.client.gui.widget.DoubleOptionSliderWidget");
		return _DoubleOptionSliderWidgetClass;
	}
	private static Class<?> _DoubleOptionSliderWidgetClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_4064}</p>
	 * <p>Mapped name: {@code net.minecraft.client.option.CyclingOption}</p>
	 * <p>Versions: 1.14-1.18.x</p>
	 */
	public static Class<?> CyclingOption()
	{
		if (_CyclingOptionClass == null)
		{
			_triedCyclingOptionClass = true;
			_CyclingOptionClass = lookupClass("net.minecraft.class_4064", "net.minecraft.client.option.CyclingOption");
		}
		return _CyclingOptionClass;
	}
	private static Class<?> _CyclingOptionClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_4064}</p>
	 * <p>Mapped name: {@code net.minecraft.client.option.CyclingOption}</p>
	 * <p>Versions: 1.14-1.18.x</p>
	 */
	public static Class<?> Try_CyclingOption()
	{
		if (!_triedCyclingOptionClass)
		{
			_triedCyclingOptionClass = true;
			_CyclingOptionClass = tryLookupClass("net.minecraft.class_4064", "net.minecraft.client.option.CyclingOption");
		}
		return _CyclingOptionClass;
	}
	private static boolean _triedCyclingOptionClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_4064$class_5675}</p>
	 * <p>Mapped name: {@code net.minecraft.client.option.CyclingOption$Setter}</p>
	 * <p>Versions: 1.17-1.18.x</p>
	 */
	public static Class<?> CyclingOption_Setter()
	{
		if (_CyclingOption_SetterClass == null)
		{
			_triedCyclingOption_SetterClass = true;
			_CyclingOption_SetterClass = lookupClass("net.minecraft.class_4064$class_5675", "net.minecraft.client.option.CyclingOption$Setter");
		}
		return _CyclingOption_SetterClass;
	}
	private static Class<?> _CyclingOption_SetterClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_4064$class_5675}</p>
	 * <p>Mapped name: {@code net.minecraft.client.option.CyclingOption$Setter}</p>
	 * <p>Versions: 1.17-1.18.x</p>
	 */
	public static Class<?> Try_CyclingOption_Setter()
	{
		if (!_triedCyclingOption_SetterClass)
		{
			_triedCyclingOption_SetterClass = true;
			_CyclingOption_SetterClass = tryLookupClass("net.minecraft.class_4064$class_5675", "net.minecraft.client.option.CyclingOption$Setter");
		}
		return _CyclingOption_SetterClass;
	}
	private static boolean _triedCyclingOption_SetterClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_4067}</p>
	 * <p>Mapped name: {@code net.minecraft.client.option.DoubleOption}</p>
	 * <p>Versions: 1.14-1.18.x</p>
	 */
	public static Class<?> DoubleOption()
	{
		if (_DoubleOptionClass == null)
		{
			_triedDoubleOptionClass = true;
			_DoubleOptionClass = lookupClass("net.minecraft.class_4067", "net.minecraft.client.option.DoubleOption");
		}
		return _DoubleOptionClass;
	}
	private static Class<?> _DoubleOptionClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_4067}</p>
	 * <p>Mapped name: {@code net.minecraft.client.option.DoubleOption}</p>
	 * <p>Versions: 1.14-1.18.x</p>
	 */
	public static Class<?> Try_DoubleOption()
	{
		if (!_triedDoubleOptionClass)
		{
			_triedDoubleOptionClass = true;
			_DoubleOptionClass = tryLookupClass("net.minecraft.class_4067", "net.minecraft.client.option.DoubleOption");
		}
		return _DoubleOptionClass;
	}
	private static boolean _triedDoubleOptionClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_5225}</p>
	 * <p>Mapped name: {@code net.minecraft.client.font.TextHandler}</p>
	 * <p>Versions: 1.16+</p>
	 */
	public static Class<?> TextHandler()
	{
		if (_TextHandlerClass == null) _TextHandlerClass = lookupClass("net.minecraft.class_5225", "net.minecraft.client.font.TextHandler");
		return _TextHandlerClass;
	}
	private static Class<?> _TextHandlerClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_5250}</p>
	 * <p>Mapped name: {@code net.minecraft.text.MutableText}</p>
	 * <p>Versions: 1.16+</p>
	 */
	public static Class<?> MutableText()
	{
		if (_MutableTextClass == null)
		{
			_triedMutableTextClass = true;
			_MutableTextClass = lookupClass("net.minecraft.class_5250", "net.minecraft.text.MutableText");
		}
		return _MutableTextClass;
	}
	private static Class<?> _MutableTextClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_5250}</p>
	 * <p>Mapped name: {@code net.minecraft.text.MutableText}</p>
	 * <p>Versions: 1.16+</p>
	 */
	public static Class<?> Try_MutableText()
	{
		if (!_triedMutableTextClass)
		{
			_triedMutableTextClass = true;
			_MutableTextClass = tryLookupClass("net.minecraft.class_5250", "net.minecraft.text.MutableText");
		}
		return _MutableTextClass;
	}
	private static boolean _triedMutableTextClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_5348}</p>
	 * <p>Mapped name: {@code net.minecraft.text.StringVisitable} (1.16.2+), {@code net.minecraft.text.StringRenderable} (1.16-1.16.1)</p>
	 * <p>Versions: 1.16+</p>
	 */
	public static Class<?> StringVisitable()
	{
		if (_StringVisitableClass == null) _StringVisitableClass = lookupClass("net.minecraft.class_5348", "net.minecraft.text.StringVisitable");
		return _StringVisitableClass;
	}
	private static Class<?> _StringVisitableClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_5481}</p>
	 * <p>Mapped name: {@code net.minecraft.text.OrderedText}</p>
	 * <p>Versions: 1.16.2+</p>
	 */
	public static Class<?> OrderedText()
	{
		if (_OrderedTextClass == null)
		{
			_triedOrderedTextClass = true;
			_OrderedTextClass = lookupClass("net.minecraft.class_5481", "net.minecraft.text.OrderedText");
		}
		return _OrderedTextClass;
	}
	private static Class<?> _OrderedTextClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_5481}</p>
	 * <p>Mapped name: {@code net.minecraft.text.OrderedText}</p>
	 * <p>Versions: 1.16.2+</p>
	 */
	public static Class<?> Try_OrderedText()
	{
		if (!_triedOrderedTextClass)
		{
			_triedOrderedTextClass = true;
			_OrderedTextClass = tryLookupClass("net.minecraft.class_5481", "net.minecraft.text.OrderedText");
		}
		return _OrderedTextClass;
	}
	private static boolean _triedOrderedTextClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_5676$class_5679}</p>
	 * <p>Mapped name: {@code net.minecraft.client.gui.widget.CyclingButtonWidget$TooltipFactory}</p>
	 * <p>Versions: 1.17-1.18.2</p>
	 */
	public static Class<?> CyclingButtonWidget_TooltipFactory()
	{
		if (_CyclingButtonWidget_TooltipFactoryClass == null) _CyclingButtonWidget_TooltipFactoryClass = lookupClass("net.minecraft.class_5676$class_5679", "net.minecraft.client.option.CyclingButtonWidget$TooltipFactory");
		return _CyclingButtonWidget_TooltipFactoryClass;
	}
	private static Class<?> _CyclingButtonWidget_TooltipFactoryClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_7172}</p>
	 * <p>Mapped name: {@code net.minecraft.text.SimpleOption}</p>
	 * <p>Versions: 1.19+</p>
	 */
	public static Class<?> SimpleOption()
	{
		if (_SimpleOptionClass == null)
		{
			_triedSimpleOptionClass = true;
			_SimpleOptionClass = lookupClass("net.minecraft.class_7172", "net.minecraft.text.SimpleOption");
		}
		return _SimpleOptionClass;
	}
	private static Class<?> _SimpleOptionClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_7172}</p>
	 * <p>Mapped name: {@code net.minecraft.text.SimpleOption}</p>
	 * <p>Versions: 1.19+</p>
	 */
	public static Class<?> Try_SimpleOption()
	{
		if (!_triedSimpleOptionClass)
		{
			_triedSimpleOptionClass = true;
			_SimpleOptionClass = tryLookupClass("net.minecraft.class_7172", "net.minecraft.text.SimpleOption");
		}
		return _SimpleOptionClass;
	}
	private static boolean _triedSimpleOptionClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_7172$class_7173}</p>
	 * <p>Mapped name: {@code net.minecraft.client.option.SimpleOption$PotentialValuesBasedCallbacks}</p>
	 * <p>Versions: 1.19+</p>
	 */
	public static Class<?> SimpleOption_PotentialValuesBasedCallbacks()
	{
		if (_SimpleOption_PotentialValuesBasedCallbacksClass == null) _SimpleOption_PotentialValuesBasedCallbacksClass = lookupClass("net.minecraft.class_7172$class_7173", "net.minecraft.client.option.SimpleOption$PotentialValuesBasedCallbacks");
		return _SimpleOption_PotentialValuesBasedCallbacksClass;
	}
	private static Class<?> _SimpleOption_PotentialValuesBasedCallbacksClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_7172$class_7177}</p>
	 * <p>Mapped name: {@code net.minecraft.client.option.SimpleOption$DoubleSliderCallbacks}</p>
	 * <p>Versions: 1.19+</p>
	 */
	public static Class<?> SimpleOption_DoubleSliderCallbacks()
	{
		if (_SimpleOption_DoubleSliderCallbacksClass == null) _SimpleOption_DoubleSliderCallbacksClass = lookupClass("net.minecraft.class_7172$class_7177", "net.minecraft.client.option.SimpleOption$DoubleSliderCallbacks");
		return _SimpleOption_DoubleSliderCallbacksClass;
	}
	private static Class<?> _SimpleOption_DoubleSliderCallbacksClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_7172$class_7178}</p>
	 * <p>Mapped name: {@code net.minecraft.client.option.SimpleOption$Callbacks}</p>
	 * <p>Versions: 1.19+</p>
	 */
	public static Class<?> SimpleOption_Callbacks()
	{
		if (_SimpleOption_CallbacksClass == null) _SimpleOption_CallbacksClass = lookupClass("net.minecraft.class_7172$class_7178", "net.minecraft.client.option.SimpleOption$Callbacks");
		return _SimpleOption_CallbacksClass;
	}
	private static Class<?> _SimpleOption_CallbacksClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_7172$class_7277}</p>
	 * <p>Mapped name: {@code net.minecraft.client.option.SimpleOption$TooltipFactoryGetter}</p>
	 * <p>Versions: 1.19-1.19.2</p>
	 */
	public static Class<?> SimpleOption_TooltipFactory()
	{
		if (_SimpleOption_TooltipFactoryClass == null) _SimpleOption_TooltipFactoryClass = lookupClass("net.minecraft.class_7172$class_7277", "net.minecraft.client.option.SimpleOption$TooltipFactory");
		return _SimpleOption_TooltipFactoryClass;
	}
	private static Class<?> _SimpleOption_TooltipFactoryClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_7172$class_7303}</p>
	 * <p>Mapped name: {@code net.minecraft.client.option.SimpleOption$ValueTextGetter}</p>
	 * <p>Versions: 1.19+</p>
	 */
	public static Class<?> SimpleOption_ValueTextGetter()
	{
		if (_SimpleOption_ValueTextGetterClass == null) _SimpleOption_ValueTextGetterClass = lookupClass("net.minecraft.class_7172$class_7303", "net.minecraft.client.option.SimpleOption$ValueTextGetter");
		return _SimpleOption_ValueTextGetterClass;
	}
	private static Class<?> _SimpleOption_ValueTextGetterClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_7172$class_7307}</p>
	 * <p>Mapped name: {@code net.minecraft.client.option.SimpleOption$TooltipFactoryGetter}</p>
	 * <p>Versions: 1.19-1.19.2</p>
	 */
	public static Class<?> SimpleOption_TooltipFactoryGetter()
	{
		if (_SimpleOption_TooltipFactoryGetterClass == null)
		{
			_triedSimpleOption_TooltipFactoryGetterClass = true;
			_SimpleOption_TooltipFactoryGetterClass = lookupClass("net.minecraft.class_7172$class_7307", "net.minecraft.client.option.SimpleOption$TooltipFactoryGetter");
		}
		return _SimpleOption_TooltipFactoryGetterClass;
	}
	private static Class<?> _SimpleOption_TooltipFactoryGetterClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_7172$class_7307}</p>
	 * <p>Mapped name: {@code net.minecraft.client.option.SimpleOption$TooltipFactoryGetter}</p>
	 * <p>Versions: 1.19-1.19.2</p>
	 */
	public static Class<?> Try_SimpleOption_TooltipFactoryGetter()
	{
		if (!_triedSimpleOption_TooltipFactoryGetterClass)
		{
			_triedSimpleOption_TooltipFactoryGetterClass = true;
			_SimpleOption_TooltipFactoryGetterClass = tryLookupClass("net.minecraft.class_7172$class_7307", "net.minecraft.client.option.SimpleOption$TooltipFactoryGetter");
		}
		return _SimpleOption_TooltipFactoryGetterClass;
	}
	private static boolean _triedSimpleOption_TooltipFactoryGetterClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_11908}</p>
	 * <p>Mapped name: {@code net.minecraft.client.input.KeyInput}</p>
	 * <p>Versions: 1.21.9+</p>
	 */
	public static Class<?> KeyInput()
	{
		if (_KeyInputClass == null)
		{
			_triedKeyInputClass = true;
			_KeyInputClass = lookupClass("net.minecraft.class_11908", "net.minecraft.client.input.KeyInput");
		}
		return _KeyInputClass;
	}
	private static Class<?> _KeyInputClass;

	/**
	 * <p>Intermediary name: {@code net.minecraft.class_11908}</p>
	 * <p>Mapped name: {@code net.minecraft.client.input.KeyInput}</p>
	 * <p>Versions: 1.21.9+</p>
	 */
	public static Class<?> Try_KeyInput()
	{
		if (!_triedKeyInputClass)
		{
			_triedKeyInputClass = true;
			_KeyInputClass = tryLookupClass("net.minecraft.class_11908", "net.minecraft.client.input.KeyInput");
		}
		return _KeyInputClass;
	}
	private static boolean _triedKeyInputClass;


	//Methods:

	/**
	 * <p>Intermediary name: {@code method_1466}</p>
	 * <p>Mapped name: {@code onKey}</p>
	 * <p>Containing class: {@code net.minecraft.class_309} ({@code Keyboard})</p>
	 * <p>Descriptor: {@code (JIIII)V}</p>
	 * <p>Return type: {@code void}</p>
	 * <p>Parameters types: (long window, int key, int scancode, int action, int modifiers)</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.14-1.21.8</p>
	 */
	public static void Keyboard_onKey_1(Keyboard instance, long window, int key, int scancode, int action, int modifiers)
	{
		if (_Keyboard_onKey_1Method == null) _Keyboard_onKey_1Method = lookupMethod("net.minecraft.class_309", "method_1466", "(JIIII)V", false, false, Keyboard.class, "onKey", void.class, long.class, int.class, int.class, int.class, int.class);
		invokeMethod("onKey", _Keyboard_onKey_1Method, instance, window, key, scancode, action, modifiers);
	}
	private static MethodHandle _Keyboard_onKey_1Method;

	/**
	 * <p>Intermediary name: {@code method_1466}</p>
	 * <p>Mapped name: {@code onKey}</p>
	 * <p>Containing class: {@code net.minecraft.class_309} ({@code Keyboard})</p>
	 * <p>Descriptor: {@code (JILnet/minecraft/class_11908;)V}</p>
	 * <p>Return type: {@code void}</p>
	 * <p>Parameters types: (long window, int action, KeyInput input)</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.21.9+</p>
	 */
	public static void Keyboard_onKey_2(Keyboard instance, long window, int action, Object input)
	{
		if (_Keyboard_onKey_2Method == null)
		{
			_triedKeyboard_onKey_2Method = true;
			_Keyboard_onKey_2Method = lookupMethod("net.minecraft.class_309", "method_1466", "(JILnet/minecraft/class_11908;)V", false, true, Keyboard.class, "onKey", void.class, long.class, int.class, KeyInput());
		}
		invokeMethod("onKey", _Keyboard_onKey_2Method, instance, window, action, input);
	}
	private static MethodHandle _Keyboard_onKey_2Method;

	/**
	 * <p>Intermediary name: {@code method_1466}</p>
	 * <p>Mapped name: {@code onKey}</p>
	 * <p>Containing class: {@code net.minecraft.class_309} ({@code Keyboard})</p>
	 * <p>Descriptor: {@code (JILnet/minecraft/class_11908;)V}</p>
	 * <p>Return type: {@code void}</p>
	 * <p>Parameters types: (long window, int action, KeyInput input)</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.21.9+</p>
	 */
	public static boolean Has_Keyboard_onKey_2()
	{
		if (!_triedKeyboard_onKey_2Method)
		{
			_triedKeyboard_onKey_2Method = true;
			if (Try_KeyInput() != null) _Keyboard_onKey_2Method = tryLookupMethod("net.minecraft.class_309", "method_1466", "(JILnet/minecraft/class_11908;)V", false, true, Keyboard.class, "onKey", void.class, long.class, int.class, KeyInput());
		}
		return _Keyboard_onKey_2Method != null;
	}
	private static boolean _triedKeyboard_onKey_2Method;

	/**
	 * <p>Intermediary name: {@code method_10558}</p>
	 * <p>Mapped name: {@code getString}</p>
	 * <p>Containing class: {@code net.minecraft.class_2487} ({@code NbtCompound})</p>
	 * <p>Descriptor: {@code (Ljava/lang/String;)Ljava/lang/String;}</p>
	 * <p>Return type: {@code String}</p>
	 * <p>Parameters types: (String key)</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.14-1.21.4</p>
	 */
	public static String NbtCompound_getString_1(NbtCompound instance, String key)
	{
		if (_NbtCompound_getString_1Method == null)
		{
			_NbtCompound_getString_1Method = lookupMethod("net.minecraft.class_2487", "method_10558", "(Ljava/lang/String;)Ljava/lang/String;", false, false, NbtCompound.class, "getString", String.class, String.class);
		}
		return (String)invokeMethod("getString", _NbtCompound_getString_1Method, instance, key);
	}
	private static MethodHandle _NbtCompound_getString_1Method;

	/**
	 * <p>Intermediary name: {@code method_15987}</p>
	 * <p>Mapped name: {@code isKeyPressed}</p>
	 * <p>Containing class: {@code net.minecraft.class_3675} ({@code InputUtil})</p>
	 * <p>Descriptor: {@code (JI)Z}</p>
	 * <p>Return type: {@code boolean}</p>
	 * <p>Parameters types: (long handle, int code)</p>
	 * <p>Static: yes</p>
	 * <p>Versions: 1.14-1.21.8</p>
	 */
	public static boolean InputUtil_isKeyPressed_1(long handle, int code)
	{
		if (_InputUtil_isKeyPressed_1Method == null) _InputUtil_isKeyPressed_1Method = lookupMethod("net.minecraft.class_3675", "method_15987", "(JI)Z", true, false, InputUtil.class, "isKeyPressed", boolean.class, long.class, int.class);
		return (boolean)invokeMethod("isKeyPressed", _InputUtil_isKeyPressed_1Method, handle, code);
	}
	private static MethodHandle _InputUtil_isKeyPressed_1Method;

	/**
	 * <p>Intermediary names: {@code method_25441} (1.16+), {@code hasControlDown} (1.14-1.15.x)</p>
	 * <p>Mapped name: {@code hasControlDown}</p>
	 * <p>Containing class: {@code net.minecraft.class_437} ({@code net.minecraft.client.gui.screen.Screen})</p>
	 * <p>Descriptor: {@code ()Z}</p>
	 * <p>Return type: {@code boolean}</p>
	 * <p>Parameters types: (none)</p>
	 * <p>Static: yes</p>
	 * <p>Versions: 1.14-1.21.8</p>
	 */
	public static boolean Screen_hasControlDown()
	{
		if (_Screen_hasControlDownMethod == null) get_Screen_hasControlDown();
		return (boolean)(Boolean)invokeMethod("hasControlDown", _Screen_hasControlDownMethod);
	}
	private static MethodHandle _Screen_hasControlDownMethod;
	private static void get_Screen_hasControlDown()
	{
		try
		{
			_Screen_hasControlDownMethod = lookupMethod("net.minecraft.class_437", "hasControlDown", "()Z", true, false, Screen.class, "hasControlDown", boolean.class);
		}
		catch (Throwable t1)
		{
			_Screen_hasControlDownMethod = lookupMethod("net.minecraft.class_437", "method_25441", "()Z", true, false, Screen.class, "hasControlDown", boolean.class);
		}
	}

	/**
	 * <p>Intermediary name: {@code method_27498}</p>
	 * <p>Mapped name: {@code wrapLines}</p>
	 * <p>Containing class: {@code net.minecraft.class_5225} ({@code TextHandler})</p>
	 * <p>Descriptor: {@code (Ljava/lang/String;ILnet/minecraft/class_2583;)Ljava/util/List;}</p>
	 * <p>Return type: {@code List<StringVisitable>}</p>
	 * <p>Parameters types: (String text, int maxWidth, Style style)</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.16+</p>
	 */
	public static List<?> TextHandler_wrapLines(Object instance, String text, int maxWidth, Style style)
	{
		if (_TextHandler_wrapLinesMethod == null) _TextHandler_wrapLinesMethod = lookupMethod("net.minecraft.class_5225", "method_27498", "(Ljava/lang/String;ILnet/minecraft/class_2583;)Ljava/util/List;", false, false, TextHandler(), "wrapLines", List.class, String.class, int.class, Style.class);
		return (List<?>)invokeMethod("wrapLines", _TextHandler_wrapLinesMethod, instance, text, maxWidth, style);
	}
	private static MethodHandle _TextHandler_wrapLinesMethod;

	/**
	 * <p>Intermediary name: {@code method_27527}</p>
	 * <p>Mapped name: {@code getTextHandler}</p>
	 * <p>Containing class: {@code net.minecraft.class_327} ({@code TextRenderer})</p>
	 * <p>Descriptor: {@code ()Lnet/minecraft/class_5225;}</p>
	 * <p>Return type: {@code TextHandler}</p>
	 * <p>Parameters types: ()</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.16+</p>
	 */
	public static Object TextRenderer_getTextHandler(TextRenderer instance)
	{
		if (_TextRenderer_getTextHandlerMethod == null) _TextRenderer_getTextHandlerMethod = lookupMethod("net.minecraft.class_327", "method_27527", "()Lnet/minecraft/class_5225;", false, false, TextRenderer.class, "getTextHandler", TextHandler());
		return invokeMethod("getTextHandler", _TextRenderer_getTextHandlerMethod, instance);
	}
	private static MethodHandle _TextRenderer_getTextHandlerMethod;

	/**
	 * <p>Intermediary name: {@code method_29431}</p>
	 * <p>Mapped name: {@code styled}</p>
	 * <p>Containing class: {@code net.minecraft.class_5348} ({@code StringVisitable})</p>
	 * <p>Descriptor: {@code (Ljava/lang/String;Lnet/minecraft/class_2583;)Lnet/minecraft/class_5348;}</p>
	 * <p>Return type: {@code net.minecraft.text.StringVisitable}</p>
	 * <p>Parameters types: {@code (String string, Style style)}</p>
	 * <p>Static: yes</p>
	 * <p>Versions: 1.16+</p>
	 */
	public static Object StringVisitable_styled(String string, Style style)
	{
		if (_StringVisitable_styledMethod == null) _StringVisitable_styledMethod = lookupMethod("net.minecraft.class_5348", "method_29431", "(Ljava/lang/String;Lnet/minecraft/class_2583;)Lnet/minecraft/class_5348;", true, false, StringVisitable(), "styled", StringVisitable(), String.class, Style.class);
		return invokeMethod("styled", _StringVisitable_styledMethod, string, style);
	}
	private static MethodHandle _StringVisitable_styledMethod;

	/**
	 * <p>Intermediary name: {@code method_29618}</p>
	 * <p>Mapped name: {@code setTooltip} (1.16.1+)</p>
	 * <p>Containing class: {@code net.minecraft.class_316} ({@code Option})</p>
	 * <p>Descriptor: {@code ()Ljava/util/Optional;}</p>
	 * <p>Return type: {@code void}</p>
	 * <p>Parameters types: {@code (List<OrderedText> tooltip)} (1.16.2+), {@code (List<StringRenderable> tooltip)} (1.16-1.16.1)</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.16-1.16.x</p>
	 */
	@SuppressWarnings("unchecked")
	public static Optional<List<?>> Option_setTooltip(Object instance, List<?> tooltip)
	{
		if (_Option_setTooltipMethod == null) _Option_setTooltipMethod = lookupMethod("net.minecraft.class_316", "method_29618", "(Ljava/util/List;)V", false, false, Option(), "setTooltip", void.class, List.class);
		return (Optional<List<?>>)invokeMethod("setTooltip", _Option_setTooltipMethod, instance, tooltip);
	}
	private static MethodHandle _Option_setTooltipMethod;

	/**
	 * <p>Intermediary name: {@code method_29619}</p>
	 * <p>Mapped name: {@code getTooltip} (1.16.1+)</p>
	 * <p>Containing class: {@code net.minecraft.class_316} ({@code Option})</p>
	 * <p>Descriptor: {@code ()Ljava/util/Optional;}</p>
	 * <p>Return type: {@code Optional<List<StringRenderable>>} (1.16-1.16.1), {@code Optional<List<OrderedText>>} (1.16.2+)</p>
	 * <p>Parameters types: (none)</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.16-1.16.x</p>
	 */
	@SuppressWarnings("unchecked")
	public static Optional<List<?>> Option_getTooltip(Object instance)
	{
		if (_Option_getTooltipMethod == null) _Option_getTooltipMethod = lookupMethod("net.minecraft.class_316", "method_29619", "()Ljava/util/Optional;", false, false, Option(), "getTooltip", Optional.class);
		return (Optional<List<?>>)invokeMethod("getTooltip", _Option_getTooltipMethod, instance);
	}
	private static MethodHandle _Option_getTooltipMethod;

	/**
	 * <p>Intermediary name: {@code method_29623}</p>
	 * <p>Mapped name: {@code getOption} (1.16.1+)</p>
	 * <p>Containing class: {@code net.minecraft.class_349} ({@code OptionButtonWidget})</p>
	 * <p>Descriptor: {@code ()Lnet/minecraft/class_316;}</p>
	 * <p>Return type: {@code net.minecraft.client.option.Option}</p>
	 * <p>Parameters types: (none)</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.16-1.16.x</p>
	 */
	public static Object OptionButtonWidget_getOption(Object instance)
	{
		if (_OptionButtonWidget_getOptionMethod == null) _OptionButtonWidget_getOptionMethod = lookupMethod("net.minecraft.class_349", "method_29623", "()Lnet/minecraft/class_316;", false, false, OptionButtonWidget(), "getOption", Option());
		return invokeMethod("getOption", _OptionButtonWidget_getOptionMethod, instance);
	}
	private static MethodHandle _OptionButtonWidget_getOptionMethod;

	/**
	 * <p>Intermediary name: {@code method_29624}</p>
	 * <p>Mapped name: {@code getHoveredButton} (1.16.1-1.19.3), {@code getHoveredWidget} (1.19.4+)</p>
	 * <p>Containing class: {@code net.minecraft.class_353} ({@code OptionListWidget})</p>
	 * <p>Descriptor: {@code (DD)Ljava/util/Optional;}</p>
	 * <p>Return type: {@code java.util.Optional}</p>
	 * <p>Parameters types: (double mouseX, double mouseY)</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.16-1.21.8</p>
	 */
	public static Optional<?> OptionButtonWidget_getHoveredWidget(Object instance, double mouseX, double mouseY)
	{
		if (_OptionButtonWidget_getHoveredWidgetMethod == null) _OptionButtonWidget_getHoveredWidgetMethod = lookupMethod("net.minecraft.class_353", "method_29624", "(DD)Ljava/util/Optional;", false, false, OptionListWidget.class, "getHoveredWidget", Optional.class, double.class, double.class);
		return (Optional<?>)invokeMethod("getHoveredWidget", _OptionButtonWidget_getHoveredWidgetMethod, instance, mouseX, mouseY);
	}
	private static MethodHandle _OptionButtonWidget_getHoveredWidgetMethod;

	/**
	 * <p>Intermediary name: {@code method_30747}</p>
	 * <p>Mapped name: {@code styledForwardsVisitedString} (1.16.5+), {@code styledString} (1.16.2-1.16.4)</p>
	 * <p>Containing class: {@code net.minecraft.class_5481} ({@code OrderedText})</p>
	 * <p>Descriptor: {@code (Ljava/lang/String;Lnet/minecraft/class_2583;)Lnet/minecraft/class_5481;}</p>
	 * <p>Return type: {@code net.minecraft.text.OrderedText}</p>
	 * <p>Parameters types: (String string, Style style)</p>
	 * <p>Static: yes</p>
	 * <p>Versions: 1.16.2+</p>
	 */
	public static Object OrderedText_styledForwardsVisitedString(String string, Style style)
	{
		if (_OrderedText_styledForwardsVisitedStringMethod == null) _OrderedText_styledForwardsVisitedStringMethod = lookupMethod("net.minecraft.class_5481", "method_30747", "(Ljava/lang/String;Lnet/minecraft/class_2583;)Lnet/minecraft/class_5481;", true, false, OrderedText(), "styledForwardsVisitedString", OrderedText(), String.class, Style.class);
		return invokeMethod("styledForwardsVisitedString", _OrderedText_styledForwardsVisitedStringMethod, string, style);
	}
	private static MethodHandle _OrderedText_styledForwardsVisitedStringMethod;

	/**
	 * <p>Intermediary name: {@code method_31048}</p>
	 * <p>Mapped name: {@code getHoveredButtonTooltip} (1.16.3+)</p>
	 * <p>Containing class: {@code net.minecraft.class_4667} ({@code GameOptionsScreen})</p>
	 * <p>Descriptor: {@code (Lnet/minecraft/class_353;II)Ljava/util/List;}</p>
	 * <p>Return type: {@code List<OrderedText>}</p>
	 * <p>Parameters types: (OptionListWidget buttonList, int mouseX, int mouseY)</p>
	 * <p>Static: yes</p>
	 * <p>Versions: 1.16.2-1.19.2</p>
	 */
	public static List<?> GameOptionsScreen_getHoveredButtonTooltip(OptionListWidget buttonList, int mouseX, int mouseY)
	{
		if (_GameOptionsScreen_getHoveredButtonTooltipMethod == null) _GameOptionsScreen_getHoveredButtonTooltipMethod = lookupMethod("net.minecraft.class_4667", "method_31048", "(Lnet/minecraft/class_353;II)Ljava/util/List;", true, false, GameOptionsScreen.class, "getHoveredButtonTooltip", List.class, OptionListWidget.class, int.class, int.class);
		return (List<?>)invokeMethod("getHoveredButtonTooltip", _GameOptionsScreen_getHoveredButtonTooltipMethod, buttonList, mouseX, mouseY);
	}
	private static MethodHandle _GameOptionsScreen_getHoveredButtonTooltipMethod;

	/**
	 * <p>Intermediary name: {@code method_32525}</p>
	 * <p>Mapped name: {@code create}</p>
	 * <p>Containing class: {@code net.minecraft.class_4064} ({@code CyclingOption})</p>
	 * <p>Descriptor: {@code (Ljava/lang/String;Lnet/minecraft/class_2561;Lnet/minecraft/class_2561;Ljava/util/function/Function;Lnet/minecraft/class_4064$class_5675;)Lnet/minecraft/class_4064;}</p>
	 * <p>Return type: {@code CyclingOption}</p>
	 * <p>Parameters types: (String key, Text on, Text off, Function<GameOptions, Boolean> getter, CyclingOption.Setter<Boolean> setter)</p>
	 * <p>Static: yes</p>
	 * <p>Versions: 1.17-1.18.x</p>
	 */
	public static Object CyclingOption_create(String key, Text on, Text off, Function<GameOptions, Boolean> getter, Object setter)
	{
		if (_CyclingOption_createMethod == null)
		{
			_CyclingOption_createMethod =
				lookupMethod("net.minecraft.class_4064", "method_32525", "(Ljava/lang/String;Lnet/minecraft/class_2561;Lnet/minecraft/class_2561;Ljava/util/function/Function;Lnet/minecraft/class_4064$class_5675;)Lnet/minecraft/class_4064;",
				true, false, CyclingOption(), "create", CyclingOption(), String.class, Text.class, Text.class, Function.class, CyclingOption_Setter());
		}
		return invokeMethod("create", _CyclingOption_createMethod, key, on, off, getter, setter);
	}
	private static MethodHandle _CyclingOption_createMethod;

	/**
	 * <p>Intermediary name: {@code method_32528}</p>
	 * <p>Mapped name: {@code tooltip}</p>
	 * <p>Containing class: {@code net.minecraft.class_4064} ({@code CyclingOption})</p>
	 * <p>Descriptor: {@code (Ljava/util/function/Function;)Lnet/minecraft/class_4064;}</p>
	 * <p>Return type: {@code CyclingOption}</p>
	 * <p>Parameters types: (Function<MinecraftClient, CyclingButtonWidget.TooltipFactory<T>> tooltips)</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.17-1.18.x</p>
	 */
	public static Object CyclingOption_tooltip(Object instance, Function<MinecraftClient, ?> tooltips)
	{
		if (_CyclingOption_tooltipMethod == null) _CyclingOption_tooltipMethod = lookupMethod("net.minecraft.class_4064", "method_32528", "(Ljava/util/function/Function;)Lnet/minecraft/class_4064;", false, false, CyclingOption(), "tooltip", CyclingOption(), Function.class);
		return invokeMethod("tooltip", _CyclingOption_tooltipMethod, instance, tooltips);
	}
	private static MethodHandle _CyclingOption_tooltipMethod;

	/**
	 * <p>Intermediary name: {@code method_41753}</p>
	 * <p>Mapped name: {@code getValue}</p>
	 * <p>Containing class: {@code net.minecraft.class_7172} ({@code SimpleOption})</p>
	 * <p>Descriptor: {@code ()Ljava/lang/Object;}</p>
	 * <p>Return type: {@code Object (T)}</p>
	 * <p>Parameters types: ()</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.19+</p>
	 */
	public static Object SimpleOption_getValue(Object instance)
	{
		if (_SimpleOption_getValueMethod == null) _SimpleOption_getValueMethod = lookupMethod("net.minecraft.class_7172", "method_41753", "()Ljava/lang/Object;", false, false, SimpleOption(), "getValue", Object.class);
		return invokeMethod("getValue", _SimpleOption_getValueMethod, instance);
	}
	private static MethodHandle _SimpleOption_getValueMethod;

	/**
	 * <p>Intermediary name: {@code method_42717}</p>
	 * <p>Mapped name: {@code constantTooltip}</p>
	 * <p>Containing class: {@code net.minecraft.class_7172} ({@code SimpleOption})</p>
	 * <p>Descriptor: {@code (Lnet/minecraft/class_2561;)Lnet/minecraft/class_7172$class_7307;}</p>
	 * <p>Return type: {@code SimpleOption.TooltipFactoryGetter}</p>
	 * <p>Parameters types: (Text text)</p>
	 * <p>Static: yes</p>
	 * <p>Versions: 1.19-1.19.2</p>
	 */
	public static Object SimpleOption_constantTooltip_1(Text text)
	{
		if (_SimpleOption_constantTooltip1Method == null) _SimpleOption_constantTooltip1Method = lookupMethod("net.minecraft.class_7172", "method_42717", "(Lnet/minecraft/class_2561;)Lnet/minecraft/class_7172$class_7307;", true, false, SimpleOption(), "constantTooltip", SimpleOption_TooltipFactoryGetter(), Text.class);
		return invokeMethod("constantTooltip", _SimpleOption_constantTooltip1Method, text);
	}
	private static MethodHandle _SimpleOption_constantTooltip1Method;

	/**
	 * <p>Intermediary name: {@code method_42717}</p>
	 * <p>Mapped name: {@code constantTooltip}</p>
	 * <p>Containing class: {@code net.minecraft.class_7172} ({@code SimpleOption})</p>
	 * <p>Descriptor: {@code (Lnet/minecraft/class_2561;)Lnet/minecraft/class_7172$class_7277;}</p>
	 * <p>Return type: {@code SimpleOption.TooltipFactory}</p>
	 * <p>Parameters types: (Text text)</p>
	 * <p>Static: yes</p>
	 * <p>Versions: 1.19.3+</p>
	 */
	public static Object SimpleOption_constantTooltip_2(Text text)
	{
		if (_SimpleOption_constantTooltip2Method == null) _SimpleOption_constantTooltip2Method = lookupMethod("net.minecraft.class_7172", "method_42717", "(Lnet/minecraft/class_2561;)Lnet/minecraft/class_7172$class_7277;", true, false, SimpleOption(), "constantTooltip", SimpleOption_TooltipFactory(), Text.class);
		return invokeMethod("constantTooltip", _SimpleOption_constantTooltip2Method, text);
	}
	private static MethodHandle _SimpleOption_constantTooltip2Method;

	/**
	 * <p>Intermediary name: {@code method_42399}</p>
	 * <p>Mapped name: {@code emptyTooltip}</p>
	 * <p>Containing class: {@code net.minecraft.class_7172} ({@code SimpleOption})</p>
	 * <p>Descriptor: {@code ()Lnet/minecraft/class_7172$class_7307;}</p>
	 * <p>Return type: {@code SimpleOption.TooltipFactoryGetter}</p>
	 * <p>Parameters types: ()</p>
	 * <p>Static: yes</p>
	 * <p>Versions: 1.19-1.19.2</p>
	 */
	public static Object SimpleOption_emptyTooltip_1()
	{
		if (_SimpleOption_emptyTooltip1Method == null) _SimpleOption_emptyTooltip1Method = lookupMethod("net.minecraft.class_7172", "method_42399", "()Lnet/minecraft/class_7172$class_7307;", true, false, SimpleOption(), "emptyTooltip", SimpleOption_TooltipFactoryGetter());
		return (List<?>)invokeMethod("emptyTooltip", _SimpleOption_emptyTooltip1Method);
	}
	private static MethodHandle _SimpleOption_emptyTooltip1Method;

	/**
	 * <p>Intermediary name: {@code method_42399}</p>
	 * <p>Mapped name: {@code emptyTooltip}</p>
	 * <p>Containing class: {@code net.minecraft.class_7172} ({@code SimpleOption})</p>
	 * <p>Descriptor: {@code ()Lnet/minecraft/class_7172$class_7277;}</p>
	 * <p>Return type: {@code SimpleOption.TooltipFactory}</p>
	 * <p>Parameters types: ()</p>
	 * <p>Static: yes</p>
	 * <p>Versions: 1.19.3+</p>
	 */
	public static Object SimpleOption_emptyTooltip_2()
	{
		if (_SimpleOption_emptyTooltip2Method == null) _SimpleOption_emptyTooltip2Method = lookupMethod("net.minecraft.class_7172", "method_42399", "()Lnet/minecraft/class_7172$class_7277;", true, false, SimpleOption(), "emptyTooltip", SimpleOption_TooltipFactory());
		return invokeMethod("emptyTooltip", _SimpleOption_emptyTooltip2Method);
	}
	private static MethodHandle _SimpleOption_emptyTooltip2Method;

	/**
	 * <p>Intermediary name: {@code method_42439}</p>
	 * <p>Mapped name: {@code getDiscreteMouseScroll}</p>
	 * <p>Containing class: {@code net.minecraft.class_315} ({@code GameOptions})</p>
	 * <p>Descriptor: {@code ()Lnet/minecraft/class_7172;}</p>
	 * <p>Return type: {@code SimpleOption}</p>
	 * <p>Parameters types: ()</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.19+</p>
	 */
	public static Object GameOptions_getDiscreteMouseScroll(GameOptions instance)
	{
		if (_GameOptions_getDiscreteMouseScrollMethod == null) _GameOptions_getDiscreteMouseScrollMethod = lookupMethod("net.minecraft.class_315", "method_42439", "()Lnet/minecraft/class_7172;", false, false, GameOptions.class, "getDiscreteMouseScroll", SimpleOption());
		return invokeMethod("getDiscreteMouseScroll", _GameOptions_getDiscreteMouseScrollMethod, instance);
	}
	private static MethodHandle _GameOptions_getDiscreteMouseScrollMethod;

	/**
	 * <p>Intermediary name: {@code method_43470}</p>
	 * <p>Mapped name: {@code literal}</p>
	 * <p>Containing class: {@code net.minecraft.class_2561} ({@code Text})</p>
	 * <p>Descriptor: {@code (Ljava/lang/String;)Lnet/minecraft/class_5250;}</p>
	 * <p>Return type: {@code MutableText}</p>
	 * <p>Parameters types: (String string)</p>
	 * <p>Static: yes</p>
	 * <p>Versions: 1.19+</p>
	 */
	public static Text Text_literal(String string)
	{
		if (_Text_literalMethod == null)
		{
			_triedText_literalMethod = true;
			_Text_literalMethod = lookupMethod("net.minecraft.class_2561", "method_43470", "(Ljava/lang/String;)Lnet/minecraft/class_5250;", true, false, Text.class, "literal", MutableText(), String.class);
		}
		return (Text)invokeMethod("literal", _Text_literalMethod, string);
	}
	private static MethodHandle _Text_literalMethod;

	/**
	 * <p>Intermediary name: {@code method_43470}</p>
	 * <p>Mapped name: {@code literal}</p>
	 * <p>Containing class: {@code net.minecraft.class_2561} ({@code Text})</p>
	 * <p>Descriptor: {@code (Ljava/lang/String;)Lnet/minecraft/class_5250;}</p>
	 * <p>Return type: {@code MutableText}</p>
	 * <p>Parameters types: (String string)</p>
	 * <p>Static: yes</p>
	 * <p>Versions: 1.19+</p>
	 */
	public static boolean Has_Text_literal()
	{
		if (!_triedText_literalMethod)
		{
			_triedText_literalMethod = true;
			_Text_literalMethod = tryLookupMethod("net.minecraft.class_2561", "method_43470", "(Ljava/lang/String;)Lnet/minecraft/class_5250;", true, false, Text.class, "literal", Try_MutableText(), String.class);
		}
		return _Text_literalMethod != null;
	}
	private static boolean _triedText_literalMethod;

	/**
	 * <p>Intermediary name: {@code getString}</p>
	 * <p>Mapped name: {@code getString}</p>
	 * <p>Containing class: {@code net.minecraft.class_5348} ({@code StringVisitable})</p>
	 * <p>Descriptor: {@code ()Ljava/lang/String;}</p>
	 * <p>Return type: {@code net.minecraft.text.StringVisitable}</p>
	 * <p>Parameters types: ()</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.16+</p>
	 */
	public static String StringVisitable_getString(Object instance)
	{
		if (_StringVisitable_getStringMethod == null) _StringVisitable_getStringMethod = lookupMethod("net.minecraft.class_5348", "getString", "()Ljava/lang/String;", false, false, StringVisitable(), "getString", String.class);
		return (String)invokeMethod("getString", _StringVisitable_getStringMethod, instance);
	}
	private static MethodHandle _StringVisitable_getStringMethod;

	/**
	 * <p>Intermediary name: {@code method_68564}</p>
	 * <p>Mapped name: {@code getString}</p>
	 * <p>Containing class: {@code net.minecraft.class_2487} ({@code NbtCompound})</p>
	 * <p>Descriptor: {@code (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;}</p>
	 * <p>Return type: {@code String}</p>
	 * <p>Parameters types: (String key, String fallback)</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.21.5+</p>
	 */
	public static String NbtCompound_getString_2(NbtCompound instance, String key, String defaultValue)
	{
		if (_NbtCompound_getString_2Method == null)
		{
			_triedNbtCompound_getString_2Method = true;
			_NbtCompound_getString_2Method = lookupMethod("net.minecraft.class_2487", "method_68564", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false, false, NbtCompound.class, "getString", String.class, String.class, String.class);
		}
		return (String)invokeMethod("getString", _NbtCompound_getString_2Method, instance, key, defaultValue);
	}
	private static MethodHandle _NbtCompound_getString_2Method;

	/**
	 * <p>Intermediary name: {@code method_68564}</p>
	 * <p>Mapped name: {@code getString}</p>
	 * <p>Containing class: {@code net.minecraft.class_2487} ({@code net.minecraft.nbt.NbtCompound})</p>
	 * <p>Descriptor: {@code (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;}</p>
	 * <p>Return type: {@code String}</p>
	 * <p>Parameters types: (String key, String defaultValue)</p>
	 * <p>Static: no</p>
	 * <p>Versions: 1.21.5+</p>
	 */
	public static boolean Has_NbtCompound_getString_2()
	{
		if (!_triedNbtCompound_getString_2Method)
		{
			_triedNbtCompound_getString_2Method = true;
			_NbtCompound_getString_2Method = tryLookupMethod("net.minecraft.class_2487", "method_68564", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false, false, NbtCompound.class, "getString", String.class, String.class, String.class);
		}
		return _NbtCompound_getString_2Method != null;
	}
	private static boolean _triedNbtCompound_getString_2Method;

	//Constructors:

	/**
	 * <p>Class intermediary name: {@code net.minecraft.class_2585}</p>
	 * <p>Class mapped name: {@code net.minecraft.text.LiteralText} (1.14.3+), {@code net.minecraft.text.TextComponent} (1.14-1.14.2)</p>
	 * <p>Constructor descriptor: (Ljava/lang/String;)V</p>
	 * <p>Parameters types: (String string)</p>
	 * <p>Versions: 1.14-1.18.x</p>
	 */
	//it was changed to a record in 1.19, and it also doesn't implement Text, so ignore it there for now
	public static Text new_LiteralText(String string)
	{
		if (_new_LiteralTextCtor1 == null) _new_LiteralTextCtor1 = lookupConstructor("(Ljava/lang/String;)V", false, LiteralText(), String.class);
		return (Text)invokeMethod("LiteralText.<init>(Ljava/lang/String;)V", _new_LiteralTextCtor1, string);
	}
	private static MethodHandle _new_LiteralTextCtor1;

	/**
	 * <p>Class intermediary name: {@code net.minecraft.class_4064}</p>
	 * <p>Class mapped name: {@code net.minecraft.client.option.CyclingOption}</p>
	 * <p>Constructor descriptor: {@code (Ljava/lang/String;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;)V}</p>
	 * <p>Parameters types: {@code (String key, BiConsumer<GameOptions, Integer> setter, BiFunction<GameOptions, CyclingOption, Text> messageProvider)} (1.16+), {@code (String key, BiConsumer<GameOptions, Integer> setter, BiFunction<GameOptions, CyclingOption, String> messageProvider)} (1.14-1.15.x)</p>
	 * <p>Versions: 1.14-1.16.x</p>
	 */
	public static Object new_CyclingOption(String key, BiConsumer<GameOptions, Integer> setter, BiFunction<GameOptions, ?, ?> messageProvider)
	{
		if (_new_CyclingOption3 == null) _new_CyclingOption3 = lookupConstructor("(Ljava/lang/String;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;)V", false, CyclingOption(), String.class, BiConsumer.class, BiFunction.class);
		return invokeMethod("CyclingOption.<init>(Ljava/lang/String;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;)V", _new_CyclingOption3, key, setter, messageProvider);
	}
	private static MethodHandle _new_CyclingOption3;

	/**
	 * <p>Class intermediary name: {@code net.minecraft.class_4064}</p>
	 * <p>Class mapped name: {@code net.minecraft.client.option.CyclingOption}</p>
	 * <p>Constructor descriptor: {@code (Ljava/lang/String;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;)V}</p>
	 * <p>Parameters types: {@code (String key, BiConsumer<GameOptions, Integer> setter, BiFunction<GameOptions, CyclingOption, Text> messageProvider)} (1.16+), {@code (String key, BiConsumer<GameOptions, Integer> setter, BiFunction<GameOptions, CyclingOption, String> messageProvider)} (1.14-1.15.x)</p>
	 * <p>Versions: 1.14-1.16.x</p>
	 */
	public static Constructor<?> Info_new_CyclingOption3()
	{
		if (_new_new_CyclingOption3_Info == null) _new_new_CyclingOption3_Info = lookupConstructorInfo("(Ljava/lang/String;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;)V", false, CyclingOption(), String.class, BiConsumer.class, BiFunction.class);
		return _new_new_CyclingOption3_Info;
	}
	private static Constructor<?> _new_new_CyclingOption3_Info;

	/**
	 * <p>Class intermediary name: {@code net.minecraft.class_4067}</p>
	 * <p>Class mapped name: {@code net.minecraft.client.option.DoubleOption}</p>
	 * <p>Constructor descriptor: {@code (Ljava/lang/String;DDFLjava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;)V}</p>
	 * <p>Parameters types: {@code (String key, double min, double max, float step, Function<GameOptions, Double> getter, BiConsumer<GameOptions, Double> setter, BiFunction<GameOptions, DoubleOption, Text> displayStringGetter)} (1.16+), {@code (String key, double min, double max, float step, Function<GameOptions, Double> getter, BiConsumer<GameOptions, Double> setter, BiFunction<GameOptions, DoubleOption, String> displayStringGetter)} (1.14-1.15.x)</p>
	 * <p>Versions: 1.14-1.18.x</p>
	 */
	public static Object new_DoubleOption(String key, double min, double max, float step, Function<GameOptions, Double> getter, BiConsumer<GameOptions, Double> setter, BiFunction<GameOptions, ?, ?> displayStringGetter)
	{
		if (_new_DoubleOptionCtor7 == null) _new_DoubleOptionCtor7 = lookupConstructor("(Ljava/lang/String;DDFLjava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;)V", false, DoubleOption(), String.class, double.class, double.class, float.class, Function.class, BiConsumer.class, BiFunction.class);
		return invokeMethod("DoubleOption.<init>(Ljava/lang/String;DDFLjava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;)V", _new_DoubleOptionCtor7, key, min, max, step, getter, setter, displayStringGetter);
	}
	private static MethodHandle _new_DoubleOptionCtor7;

	/**
	 * <p>Class intermediary name: {@code net.minecraft.class_4067}</p>
	 * <p>Class mapped name: {@code net.minecraft.client.option.DoubleOption}</p>
	 * <p>Constructor descriptor: {@code (Ljava/lang/String;DDFLjava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;)V}</p>
	 * <p>Parameters types: {@code (String key, double min, double max, float step, Function<GameOptions, Double> getter, BiConsumer<GameOptions, Double> setter, BiFunction<GameOptions, DoubleOption, Text> displayStringGetter)} (1.16+), {@code (String key, double min, double max, float step, Function<GameOptions, Double> getter, BiConsumer<GameOptions, Double> setter, BiFunction<GameOptions, DoubleOption, String> displayStringGetter)} (1.14-1.15.x)</p>
	 * <p>Versions: 1.14-1.18.x</p>
	 */
	public static Constructor<?> Info_new_DoubleOption7()
	{
		if (_new_DoubleOptionCtor7_Info == null) _new_DoubleOptionCtor7_Info = lookupConstructorInfo("(Ljava/lang/String;DDFLjava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;)V", false, DoubleOption(), String.class, double.class, double.class, float.class, Function.class, BiConsumer.class, BiFunction.class);
		return _new_DoubleOptionCtor7_Info;
	}
	private static Constructor<?> _new_DoubleOptionCtor7_Info;

	/**
	 * <p>Class intermediary name: {@code net.minecraft.class_4067}</p>
	 * <p>Class mapped name: {@code net.minecraft.client.option.DoubleOption}</p>
	 * <p>Constructor descriptor: {@code (Ljava/lang/String;DDFLjava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;Ljava/util/function/Function;)V}</p>
	 * <p>Parameters types: {@code (String key, double min, double max, float step, Function<GameOptions, Double> getter, BiConsumer<GameOptions, Double> setter, BiFunction<GameOptions, DoubleOption, Text> displayStringGetter, Function<MinecraftClient, List<OrderedText>> tooltipsGetter)}</p>
	 * <p>Versions: 1.17-1.18.x</p>
	 */
	public static Object new_DoubleOption(String key, double min, double max, float step, Function<GameOptions, Double> getter, BiConsumer<GameOptions, Double> setter, BiFunction<GameOptions, ?, ?> displayStringGetter, Function<MinecraftClient, List<?>> tooltipsGetter)
	{
		if (_new_DoubleOptionCtor8 == null)
		{
			_triednew_DoubleOptionCtor8 = true;
			_new_DoubleOptionCtor8 = lookupConstructor("(Ljava/lang/String;DDFLjava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;Ljava/util/function/Function;)V", false, DoubleOption(), String.class, double.class, double.class, float.class, Function.class, BiConsumer.class, BiFunction.class, Function.class);
		}
		return invokeMethod("DoubleOption.<init>(Ljava/lang/String;DDFLjava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;Ljava/util/function/Function;)V", _new_DoubleOptionCtor8, key, min, max, step, getter, setter, displayStringGetter, tooltipsGetter);
	}
	private static MethodHandle _new_DoubleOptionCtor8;

	/**
	 * <p>Class intermediary name: {@code net.minecraft.class_4067}</p>
	 * <p>Class mapped name: {@code net.minecraft.client.option.DoubleOption}</p>
	 * <p>Constructor descriptor: (Ljava/lang/String;DDFLjava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;Ljava/util/function/Function;)V</p>
	 * <p>Parameters types: (String key, double min, double max, float step, Function<GameOptions, Double> getter, BiConsumer<GameOptions, Double> setter, BiFunction<GameOptions, DoubleOption, Text> displayStringGetter, Function<MinecraftClient, List<OrderedText>> tooltipsGetter)</p>
	 * <p>Versions: 1.17-1.18.x</p>
	 */
	public static boolean Has_new_DoubleOption_8()
	{
		if (!_triednew_DoubleOptionCtor8)
		{
			_triednew_DoubleOptionCtor8 = true;
			_new_DoubleOptionCtor8 = tryLookupConstructor("(Ljava/lang/String;DDFLjava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;Ljava/util/function/Function;)V", false, DoubleOption(), String.class, double.class, double.class, float.class, Function.class, BiConsumer.class, BiFunction.class, Function.class);
		}
		return _new_DoubleOptionCtor8 != null;
	}
	private static boolean _triednew_DoubleOptionCtor8;

	/**
	 * <p>Class intermediary name: {@code net.minecraft.class_4067}</p>
	 * <p>Class mapped name: {@code net.minecraft.client.option.DoubleOption}</p>
	 * <p>Constructor descriptor: (Ljava/lang/String;DDFLjava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;Ljava/util/function/Function;)V</p>
	 * <p>Parameters types: (String key, double min, double max, float step, Function<GameOptions, Double> getter, BiConsumer<GameOptions, Double> setter, BiFunction<GameOptions, DoubleOption, Text> displayStringGetter, Function<MinecraftClient, List<OrderedText>> tooltipsGetter)</p>
	 * <p>Versions: 1.17-1.18.x</p>
	 */
	public static Constructor<?> Info_new_DoubleOption8()
	{
		if (_new_DoubleOptionCtor8_Info == null) _new_DoubleOptionCtor8_Info = lookupConstructorInfo("(Ljava/lang/String;DDFLjava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/function/BiFunction;Ljava/util/function/Function;)V", false, DoubleOption(), String.class, double.class, double.class, float.class, Function.class, BiConsumer.class, BiFunction.class, Function.class);
		return _new_DoubleOptionCtor8_Info;
	}
	private static Constructor<?> _new_DoubleOptionCtor8_Info;

	/**
	 * <p>Class intermediary name: {@code net.minecraft.class_7172}</p>
	 * <p>Class mapped name: {@code net.minecraft.client.option.SimpleOption}</p>
	 * <p>Constructor descriptor: {@code (Ljava/lang/String;Lnet/minecraft/class_7172$class_7307;Lnet/minecraft/class_7172$class_7303;Lnet/minecraft/class_7172$class_7178;Ljava/lang/Object;Ljava/util/function/Consumer;)V}</p>
	 * <p>Parameters types: {@code (String key, SimpleOption.TooltipFactoryGetter<T> tooltipFactoryGetter, SimpleOption.ValueTextGetter<T> valueTextGetter, SimpleOption.Callbacks<T> callbacks, T defaultValue, Consumer<T> changeCallback)}</p>
	 * <p>Versions: 1.19-1.19.2</p>
	 */
	public static Object new_SimpleOption_1(String key, Object tooltipFactoryGetter, Object valueTextGetter, Object callbacks, Object defaultValue, Object changeCallback)
	{
		if (_new_SimpleOptionCtor6_1 == null) _new_SimpleOptionCtor6_1 = lookupConstructor("(Ljava/lang/String;Lnet/minecraft/class_7172$class_7307;Lnet/minecraft/class_7172$class_7303;Lnet/minecraft/class_7172$class_7178;Ljava/lang/Object;Ljava/util/function/Consumer;)V", false, SimpleOption(), String.class, SimpleOption_TooltipFactoryGetter(), SimpleOption_ValueTextGetter(), SimpleOption_Callbacks(), Object.class, Consumer.class);
		return invokeMethod("SimpleObject.<init>(Ljava/lang/String;Lnet/minecraft/class_7172$class_7307;Lnet/minecraft/class_7172$class_7303;Lnet/minecraft/class_7172$class_7178;Ljava/lang/Object;Ljava/util/function/Consumer;)V", _new_SimpleOptionCtor6_1, key, tooltipFactoryGetter, valueTextGetter, callbacks, defaultValue, changeCallback);
	}
	private static MethodHandle _new_SimpleOptionCtor6_1;

	/**
	 * <p>Class intermediary name: {@code net.minecraft.class_7172}</p>
	 * <p>Class mapped name: {@code net.minecraft.client.option.SimpleOption}</p>
	 * <p>Constructor descriptor: {@code (Ljava/lang/String;Lnet/minecraft/class_7172$class_7277;Lnet/minecraft/class_7172$class_7303;Lnet/minecraft/class_7172$class_7178;Ljava/lang/Object;Ljava/util/function/Consumer;)V}</p>
	 * <p>Parameters types: {@code (String key, SimpleOption.TooltipFactory<T> tooltipFactory, SimpleOption.ValueTextGetter<T> valueTextGetter, SimpleOption.Callbacks<T> callbacks, T defaultValue, Consumer<T> changeCallback)}</p>
	 * <p>Versions: 1.19.3+</p>
	 */
	public static Object new_SimpleOption_2(String key, Object tooltipFactory, Object valueTextGetter, Object callbacks, Object defaultValue, Object changeCallback)
	{
		if (_new_SimpleOptionCtor6_2 == null) _new_SimpleOptionCtor6_2 = lookupConstructor("(Ljava/lang/String;Lnet/minecraft/class_7172$class_7277;Lnet/minecraft/class_7172$class_7303;Lnet/minecraft/class_7172$class_7178;Ljava/lang/Object;Ljava/util/function/Consumer;)V", false, SimpleOption(), String.class, SimpleOption_TooltipFactory(), SimpleOption_ValueTextGetter(), SimpleOption_Callbacks(), Object.class, Consumer.class);
		return invokeMethod("SimpleObject.<init>(Ljava/lang/String;Lnet/minecraft/class_7172$class_7277;Lnet/minecraft/class_7172$class_7303;Lnet/minecraft/class_7172$class_7178;Ljava/lang/Object;Ljava/util/function/Consumer;)V", _new_SimpleOptionCtor6_2, key, tooltipFactory, valueTextGetter, callbacks, defaultValue, changeCallback);
	}
	private static MethodHandle _new_SimpleOptionCtor6_2;

	/**
	 * <p>Class intermediary name: {@code net.minecraft.class_11908}</p>
	 * <p>Class mapped name: {@code net.minecraft.client.input.KeyInput}</p>
	 * <p>Constructor descriptor: {@code (III)V}</p>
	 * <p>Parameters types: {@code (int key, int scancode, int modifiers)}</p>
	 * <p>Versions: 1.21.9+</p>
	 */
	public static Object new_KeyInput(int key, int scancode, int modifiers)
	{
		if (_new_KeyInputCtor3 == null) _new_KeyInputCtor3 = lookupConstructor("(III)V", false, KeyInput(), int.class, int.class, int.class);
		return invokeMethod("KeyInput.<init>(III)V", _new_KeyInputCtor3, key, scancode, modifiers);
	}
	private static MethodHandle _new_KeyInputCtor3;


	//Fields:

	/**
	 * <p>Intermediary name: {@code field_18012}</p>
	 * <p>Mapped name: {@code option}</p>
	 * <p>Containing class: {@code net.minecraft.class_4040} ({@code DoubleOptionSliderWidget})</p>
	 * <p>Descriptor: {@code Lnet/minecraft/class_4067;}</p>
	 * <p>Field type: {@code DoubleOption}</p>
	 * <p>Static: no</p>
	 * <p>Private: yes</p>
	 * <p>Versions: 1.14-1.18.x</p>
	 */
	public static Object DoubleOptionSliderWidget_option(Object instance)
	{
		if (_DoubleOptionSliderWidget_optionField == null) _DoubleOptionSliderWidget_optionField = lookupField("net.minecraft.class_4040", "field_18012", "Lnet/minecraft/class_4067;", false, true, DoubleOptionSliderWidget(), "option", DoubleOption());
		return readField("DoubleOptionSliderWidget.option", _DoubleOptionSliderWidget_optionField, instance);
	}
	private static Field _DoubleOptionSliderWidget_optionField;

	/**
	 * <p>Intermediary name: {@code field_19244}</p>
	 * <p>Mapped name: {@code discreteMouseScroll}</p>
	 * <p>Containing class: {@code net.minecraft.class_315} ({@code GameOptions})</p>
	 * <p>Descriptor: {@code Z}</p>
	 * <p>Field type: {@code boolean}</p>
	 * <p>Static: no</p>
	 * <p>Private: no</p>
	 * <p>Versions: 1.14-1.18.x</p>
	 */
	public static boolean GameOptions_discreteMouseScroll(GameOptions instance)
	{
		if (_GameOptions_discreteMouseScrollField == null) _GameOptions_discreteMouseScrollField = lookupField("net.minecraft.class_315", "field_19244", "Z", false, false, GameOptions.class, "discreteMouseScroll", boolean.class);
		return (boolean)(Boolean)readField("GameOptions.discreteMouseScroll", _GameOptions_discreteMouseScrollField, instance);
	}
	private static Field _GameOptions_discreteMouseScrollField;

	/**
	 * <p>Intermediary name: {@code field_24360}</p>
	 * <p>Mapped name: {@code EMPTY}</p>
	 * <p>Containing class: {@code net.minecraft.class_2583} ({@code Style})</p>
	 * <p>Descriptor: {@code Lnet/minecraft/class_2583;}</p>
	 * <p>Field type: {@code Style}</p>
	 * <p>Static: yes</p>
	 * <p>Private: no</p>
	 * <p>Versions: 1.16+</p>
	 */
	public static Style Style_EMPTY()
	{
		if (_Style_EMPTYField == null) _Style_EMPTYField = lookupField("net.minecraft.class_2583", "field_24360", "Lnet/minecraft/class_2583;", true, false, Style.class, "EMPTY", Style.class);
		return (Style)readField("Style.EMPTY", _Style_EMPTYField, null);
	}
	private static Field _Style_EMPTYField;

	/**
	 * <p>Intermediary name: {@code field_37875}</p>
	 * <p>Mapped name: {@code INSTANCE}</p>
	 * <p>Containing class: {@code net.minecraft.class_7172$class_7177} ({@code SimpleOption.DoubleSliderCallbacks})</p>
	 * <p>Descriptor: {@code Lnet/minecraft/class_7172$class_7177;}</p>
	 * <p>Field type: {@code SimpleOption.DoubleSliderCallbacks}</p>
	 * <p>Static: yes</p>
	 * <p>Private: no</p>
	 * <p>Versions: 1.19+</p>
	 */
	public static Object SimpleOption_DoubleSliderCallbacks_INSTANCE()
	{
		if (_SimpleOption_DoubleSliderCallbacks_INSTANCEField == null) _SimpleOption_DoubleSliderCallbacks_INSTANCEField = lookupField("net.minecraft.class_7172$class_7177", "field_37875", "Lnet/minecraft/class_7172$class_7177;", true, false, SimpleOption_DoubleSliderCallbacks(), "INSTANCE", SimpleOption_DoubleSliderCallbacks());
		return readField("SimpleOption.DoubleSliderCallbacks.INSTANCE", _SimpleOption_DoubleSliderCallbacks_INSTANCEField, null);
	}
	private static Field _SimpleOption_DoubleSliderCallbacks_INSTANCEField;

	/**
	 * <p>Intermediary name: {@code field_38278}</p>
	 * <p>Mapped name: {@code BOOLEAN}</p>
	 * <p>Containing class: {@code net.minecraft.class_7172} ({@code SimpleOption})</p>
	 * <p>Descriptor: {@code Lnet/minecraft/class_7172$class_7173;}</p>
	 * <p>Field type: {@code SimpleOption.PotentialValuesBasedCallbacks}</p>
	 * <p>Static: yes</p>
	 * <p>Private: no</p>
	 * <p>Versions: 1.19+</p>
	 */
	public static Object SimpleOption_BOOLEAN()
	{
		if (_SimpleOption_BOOLEANField == null) _SimpleOption_BOOLEANField = lookupField("net.minecraft.class_7172", "field_38278", "Lnet/minecraft/class_7172$class_7173;", true, false, SimpleOption(), "BOOLEAN", SimpleOption_PotentialValuesBasedCallbacks());
		return readField("SimpleOption.BOOLEAN", _SimpleOption_BOOLEANField, null);
	}
	private static Field _SimpleOption_BOOLEANField;


	//Helper functional interfaces that can be converted to the correct functional interface:

	@FunctionalInterface
	public interface ValueTextGetterHelper<T>
	{
		public Text toString(Text var1, T var2);
	}

	@FunctionalInterface
	public interface CyclingOptionSetterHelper<T>
	{
		public void accept(GameOptions gameOptions, /*Option*/ Object option, T value);
	}


	//Methods that take helper functional interfaces:

	/**
	 * Versions: 1.17-1.18.x
	 * From: CyclingOptionSetterHelper
	 * To: net.minecraft.class_4064$class_5675 (CyclingOption.Setter)
	 */
	public static Object convertToCyclingOption_Setter(CyclingOptionSetterHelper<?> method)
	{
		if (_convertToCyclingOption_SetterConverter == null) _convertToCyclingOption_SetterConverter = makeLambdaConverter("net.minecraft.class_4064$class_5675", "accept", "(Lnet/minecraft/class_315;Lnet/minecraft/class_316;Ljava/lang/Object;)V", CyclingOptionSetterHelper.class, CyclingOption_Setter(), void.class, void.class, new Class<?>[] { GameOptions.class, Object.class, Object.class }, new Class<?>[] { GameOptions.class, Option(), Object.class });
		return invokeMethod("convertToCyclingOption_Setter", _convertToCyclingOption_SetterConverter, method);
	}
	private static MethodHandle _convertToCyclingOption_SetterConverter;

	/**
	 * Versions: 1.17-1.18.x
	 * From: Function
	 * To: net.minecraft.class_5676$class_5679 (CyclingButtonWidget.TooltipFactory)
	 */
	public static Object convertToCyclingButtonWidget_TooltipFactory(Function<?, ?> method)
	{
		if (_convertToCyclingButtonWidget_TooltipFactoryConverter == null) _convertToCyclingButtonWidget_TooltipFactoryConverter = makeLambdaConverter("net.minecraft.class_5676$class_5679", "apply", "(Ljava/lang/Object;)Ljava/lang/Object;", Function.class, CyclingButtonWidget_TooltipFactory(), Object.class, Object.class);
		return invokeMethod("convertToCyclingButtonWidget_TooltipFactory", _convertToCyclingButtonWidget_TooltipFactoryConverter, method);
	}
	private static MethodHandle _convertToCyclingButtonWidget_TooltipFactoryConverter;

	/**
	 * Versions: 1.19+
	 * From: ValueTextGetterHelper
	 * To: net.minecraft.class_7172$class_7303 (SimpleOption.ValueTextGetter)
	 */
	public static Object convertToSimpleOption_ValueTextGetter(ValueTextGetterHelper<?> method)
	{
		if (_convertToSimpleOption_ValueTextGetterConverter == null) _convertToSimpleOption_ValueTextGetterConverter = makeLambdaConverter("net.minecraft.class_7172$class_7303", "toString", "(Lnet/minecraft/class_2585;Ljava/lang/Object;)Lnet/minecraft/class_2585;", ValueTextGetterHelper.class, SimpleOption_ValueTextGetter(), Text.class, Text.class, Object.class);
		return invokeMethod("convertToSimpleOption_ValueTextGetter", _convertToSimpleOption_ValueTextGetterConverter, method);
	}
	private static MethodHandle _convertToSimpleOption_ValueTextGetterConverter;
}
