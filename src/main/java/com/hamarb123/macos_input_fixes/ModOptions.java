package com.hamarb123.macos_input_fixes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import com.hamarb123.macos_input_fixes.mixin.gui.GameOptionsAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ModOptions
{
	//note: most of the comments for this code are at the top


	//here are the different methods that we target for different versions to create option instances:

	//1.19:
	/*
		SimpleOption(
			String key,
			SimpleOption.TooltipFactoryGetter<T> tooltipFactoryGetter,
			SimpleOption.ValueTextGetter<T> valueTextGetter,
			SimpleOption.Callbacks<T> callbacks,
			T defaultValue,
			Consumer<T> changeCallback)
	*/

	//1.18 or 1.17:
	/*
		Option - base type
		DoubleOption(
			String key,
			double min,
			double max,
			float step,
			Function<GameOptions,Double> getter,
			BiConsumer<GameOptions,Double> setter,
			BiFunction<GameOptions,DoubleOption,Text> displayStringGetter)
		static CyclingOption<Boolean> create(
			String key,
			Text on,
			Text off,
			Function<GameOptions,Boolean> getter,
			CyclingOption.Setter<Boolean> setter)
	*/

	//1.16:
	/*
		Same Option and DoubleOption
		CyclingOption(
			String key,
			BiConsumer<GameOptions,Integer> setter,
			BiFunction<GameOptions,CyclingOption,Text> messageProvider)
	*/

	//1.15 or 1.14:
	/*
		String instead of Text.
	*/

	//1.19 4th: SimpleOption.DoubleSliderCallbacks.INSTANCE
	//1.19 4th: SimpleOption.BOOLEAN


	//here's the implementation for creating the different interface elements:

	private static MethodHandle _createLiteralText;
	private static MethodHandle createLiteralTextInvoker()
	{
		if (_createLiteralText == null)
		{
			MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
			try
			{
				//1.19+
				String textLiteralMethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_2561", "method_43470", "(Ljava/lang/String;)Lnet/minecraft/class_5250;");
				_createLiteralText = MethodHandles.publicLookup().findStatic(Text.class, textLiteralMethodName, MethodType.methodType(MutableText.class, String.class));
			}
			catch (NoSuchMethodException | IllegalAccessException e1)
			{
				try
				{
					//1.14-1.18
					String LiteralTextContentName = resolver.mapClassName("intermediary", "net.minecraft.class_2585");
					Class<?> literalTextContentClass = Class.forName(LiteralTextContentName);
					_createLiteralText = MethodHandles.publicLookup().findConstructor(literalTextContentClass, MethodType.methodType(void.class, String.class));
				}
				catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException e2)
				{
					e2.printStackTrace();
				}
			}
		}
		return _createLiteralText;
	}

	private static Object createLiteralText(String value)
	{
		try
		{
			return createLiteralTextInvoker().invoke(value);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			throw new RuntimeException("Failed to either find LiteralText constructor or call Text.literal", e);
		}
	}

	//helper functional interface that can be converted to the correct functional interface
	@FunctionalInterface
	private interface ValueTextGetterHelper<T>
	{
		public Text toString(Text var1, T var2);
	}

	//helper functional interface that can be converted to the correct functional interface
	@FunctionalInterface
	private interface CyclingOptionSetterHelper<T>
	{
		public void accept(GameOptions gameOptions, /*Option*/ Object option, T value);	 
	}

	private static Object doubleOption(String key, String prefix, double min, double max, float step, Supplier<Double> getter, Consumer<Double> setter, String tooltip)
	{
		MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
		String doubleOptionClassName = resolver.mapClassName("intermediary", "net.minecraft.class_4067");
		Class<?> doubleOptionClass = null;
		try
		{
			doubleOptionClass = Class.forName(doubleOptionClassName);
		}
		catch (Throwable e1)
		{
		}
		try
		{
			if (doubleOptionClass != null)
			{
				//1.14-1.18
				MethodHandle ctor;
				Constructor<?> ctorM;
				boolean is117Plus = false;
				int version = 1160;
				if (tooltip == null)
				{
					ctor = MethodHandles.publicLookup().findConstructor(doubleOptionClass, MethodType.methodType(void.class, String.class, double.class, double.class, float.class, Function.class, BiConsumer.class, BiFunction.class));
					ctorM = doubleOptionClass.getConstructor(String.class, double.class, double.class, float.class, Function.class, BiConsumer.class, BiFunction.class);
				}
				else
				{
					try
					{
						ctor = MethodHandles.publicLookup().findConstructor(doubleOptionClass, MethodType.methodType(void.class, String.class, double.class, double.class, float.class, Function.class, BiConsumer.class, BiFunction.class, Function.class));
						ctorM = doubleOptionClass.getConstructor(String.class, double.class, double.class, float.class, Function.class, BiConsumer.class, BiFunction.class, Function.class);
						is117Plus = true;
						version = 1170;
					}
					catch (Throwable t)
					{
						ctor = MethodHandles.publicLookup().findConstructor(doubleOptionClass, MethodType.methodType(void.class, String.class, double.class, double.class, float.class, Function.class, BiConsumer.class, BiFunction.class));
						ctorM = doubleOptionClass.getConstructor(String.class, double.class, double.class, float.class, Function.class, BiConsumer.class, BiFunction.class);
					}
				}
				Function<GameOptions, Double> _getter = (gameOptions) -> getter.get();
				BiConsumer<GameOptions, Double> _setter = (gameOptions, value) ->
				{
					setter.accept(value);
					saveOptions();
				};
				BiFunction<GameOptions, ?, ?> displayStringGetter;
				if (((ParameterizedType)ctorM.getGenericParameterTypes()[6]).getActualTypeArguments()[2] == String.class)
				{
					//1.14-1.15
					displayStringGetter = (gameOptions, doubleOption) -> prefix + ": " + getter.get();
					version = 1140;
				}
				else
				{
					//1.16-1.18
					displayStringGetter = (gameOptions, doubleOption) -> createLiteralText(prefix + ": " + getter.get());
				}
				if (tooltip == null)
				{
					return ctor.invoke(key, min, max, step, _getter, _setter, displayStringGetter);
				}
				else
				{
					Object tooltipObject = createTooltip(true, tooltip, version, resolver);
					if (is117Plus)
					{
						return ctor.invoke(key, min, max, step, _getter, _setter, displayStringGetter, tooltipObject);
					}
					else
					{
						Object returnValue = ctor.invoke(key, min, max, step, _getter, _setter, displayStringGetter);
						returnValue = writeTooltip(returnValue, tooltipObject, version, resolver);
						return returnValue;
					}
				}
			}
			else
			{
				//1.19+
				String SimpleOption_TooltipFactoryGetter_ClassName = resolver.mapClassName("intermediary", "net.minecraft.class_7172$class_7307");
				Class<?> SimpleOption_TooltipFactoryGetter_Class = null;
				try
				{
					SimpleOption_TooltipFactoryGetter_Class = Class.forName(SimpleOption_TooltipFactoryGetter_ClassName);
				}
				catch (Throwable e2)
				{
				}
				Class<?> SimpleOption_TooltipParameter_Class;

				String SimpleOption_ValueTextGetter_ClassName = resolver.mapClassName("intermediary", "net.minecraft.class_7172$class_7303");
				Class<?> SimpleOption_ValueTextGetter_Class = Class.forName(SimpleOption_ValueTextGetter_ClassName);
				Class<?> SimpleOption_Callbacks_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_7172$class_7178"));
				Class<?> SimpleOption_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_7172"));

				Object tooltipParameter;

				if (SimpleOption_TooltipFactoryGetter_Class != null)
				{
					//1.19-1.19.2
					SimpleOption_TooltipParameter_Class = SimpleOption_TooltipFactoryGetter_Class;
					tooltipParameter = createTooltip(true, tooltip, 1190, resolver);
				}
				else
				{
					//1.19.3+
					String SimpleOption_TooltipFactory_ClassName = resolver.mapClassName("intermediary", "net.minecraft.class_7172$class_7277");
					Class<?> SimpleOption_TooltipFactory_Class = Class.forName(SimpleOption_TooltipFactory_ClassName);
					SimpleOption_TooltipParameter_Class = SimpleOption_TooltipFactory_Class;
					tooltipParameter = createTooltip(true, tooltip, 1193, resolver);
				}

				double step2 = (max - min) / step;
				ValueTextGetterHelper<Double> valueTextGetterImpl = (optionText, value) ->
				{
					double result = Math.round(value * step2) * step + min;
					return (Text)createLiteralText(prefix + ": " + result);
				};
				String toString_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_7172$class_7303", "toString", "(Lnet/minecraft/class_2585)Lnet/minecraft/class_2585;");
				Lookup lookup = MethodHandles.lookup();
				MethodType mType = MethodType.methodType(Text.class, Text.class, Object.class);
				Object valueTextGetter = LambdaMetafactory.metafactory(
					lookup,
					toString_MethodName,
					MethodType.methodType(SimpleOption_ValueTextGetter_Class, ValueTextGetterHelper.class),
					mType,
					lookup.findVirtual(ValueTextGetterHelper.class, "toString", mType),
					mType
				).getTarget().invoke(valueTextGetterImpl);

				String SimpleOption_DoubleSliderCallbacks_ClassName = resolver.mapClassName("intermediary", "net.minecraft.class_7172$class_7177");
				String DoubleSliderCallbacks_INSTANCE_FieldName = resolver.mapFieldName("intermediary", "net.minecraft.class_7172$class_7177", "field_37875", "Lnet/minecraft/class_7172$class_7177;");
				Object callbacks = Class.forName(SimpleOption_DoubleSliderCallbacks_ClassName).getField(DoubleSliderCallbacks_INSTANCE_FieldName).get(null);

				Double defaultValue = (getter.get() - min) / (max - min);

				Consumer<Double> changeCallback = (value) ->
				{
					double result = Math.round(value * step2) * step + min;
					setter.accept(result);
					saveOptions();
				};
				MethodHandle mh = MethodHandles.publicLookup().findConstructor(SimpleOption_Class, MethodType.methodType(void.class, String.class, SimpleOption_TooltipParameter_Class, SimpleOption_ValueTextGetter_Class, SimpleOption_Callbacks_Class, Object.class, Consumer.class));
				return mh.invoke(key, tooltipParameter, valueTextGetter, callbacks, defaultValue, changeCallback);
			}
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to create the double option interface element.", t);
		}
	}

	private static Object booleanOption(String key, String prefix, Supplier<Boolean> getter, Consumer<Boolean> setter, String tooltip)
	{
		MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
		String cyclingOptionClassName = resolver.mapClassName("intermediary", "net.minecraft.class_4064");
		Class<?> cyclingOptionClass = null;
		try
		{
			cyclingOptionClass = Class.forName(cyclingOptionClassName);
		}
		catch (Throwable e1)
		{
		}
		try
		{
			if (cyclingOptionClass != null)
			{
				//1.14-1.18
				String createMethodName = null;
				Class<?> CyclingOption_Setter_Class = null;
				MethodHandle createMethod = null;
				try
				{
					CyclingOption_Setter_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_4064$class_5675"));
					createMethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_4064", "method_32525",
						"(Ljava/lang/String;Lnet/minecraft/class_2561;Lnet/minecraft/class_2561;Ljava/util/function/Function;Lnet/minecraft/class_4064$class_5675;)Lnet/minecraft/class_4064;");
					createMethod = MethodHandles.publicLookup().findStatic(cyclingOptionClass, createMethodName, MethodType.methodType(
						cyclingOptionClass, String.class, Text.class, Text.class, Function.class, CyclingOption_Setter_Class
					));
				}
				catch (Throwable e2)
				{
					if (CyclingOption_Setter_Class != null)
					{
						throw new Exception("Failed to load CyclingOption.create, but CyclingOption.Setter was found.", e2);
					}
				}

				if (createMethod != null)
				{
					//1.17-1.18
					String accept_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_4064$class_5675", "accept", "(Lnet/minecraft/class_315;Lnet/minecraft/class_316;Ljava/lang/Object;)V");
					Lookup lookup = MethodHandles.lookup();
					Class<?> OptionClass = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_316"));
					MethodType mType1 = MethodType.methodType(void.class, GameOptions.class, OptionClass, Object.class);
					MethodType mType2 = MethodType.methodType(void.class, GameOptions.class, Object.class, Object.class);
					Function<GameOptions, Boolean> _getter = (gameOptions) -> getter.get();
					CyclingOptionSetterHelper<Boolean> _setter = (gameOptions, option, value) ->
					{
						setter.accept(value);
						saveOptions();
					};
					Object _setter2 = LambdaMetafactory.metafactory(
						lookup,
						accept_MethodName,
						MethodType.methodType(CyclingOption_Setter_Class, CyclingOptionSetterHelper.class),
						mType1,
						lookup.findVirtual(CyclingOptionSetterHelper.class, "accept", mType2),
						mType1
					).getTarget().invoke(_setter);
					Object returnValue = createMethod.invoke(key, createLiteralText(prefix + ": ON"), createLiteralText(prefix + ": OFF"), _getter, _setter2);
					((OptionMixinHelper)returnValue).setOmitBuilderKeyText();
					if (tooltip != null)
					{
						Object tooltipObject = createTooltip(false, tooltip, 1170, resolver);
						returnValue = writeTooltip(returnValue, tooltipObject, 1170, resolver);
					}
					return returnValue;
				}
				else
				{
					//1.14-1.16
					MethodHandle ctor = MethodHandles.publicLookup().findConstructor(cyclingOptionClass, MethodType.methodType(void.class, String.class, BiConsumer.class, BiFunction.class));
					Constructor<?> ctorM = cyclingOptionClass.getConstructor(String.class, BiConsumer.class, BiFunction.class);
					BiConsumer<GameOptions, Integer> _setter = (gameOptions, value) ->
					{
						setter.accept(!getter.get());
						saveOptions();
					};
					BiFunction<GameOptions, ?, ?> displayStringGetter;
					int version = 1160;
					if (((ParameterizedType)ctorM.getGenericParameterTypes()[2]).getActualTypeArguments()[2] == String.class)
					{
						//1.14-1.15
						displayStringGetter = (gameOptions, booleanOption) -> prefix + (getter.get() ? ": ON" : ": OFF");
						version = 1140;
					}
					else
					{
						//1.16
						displayStringGetter = (gameOptions, booleanOption) -> createLiteralText(prefix + (getter.get() ? ": ON" : ": OFF"));
					}
					Object returnValue = ctor.invoke(key, _setter, displayStringGetter);
					if (tooltip != null)
					{
						Object tooltipObject = createTooltip(false, tooltip, version, resolver);
						returnValue = writeTooltip(returnValue, tooltipObject, version, resolver);
					}
					return returnValue;
				}
			}
			else
			{
				//1.19+
				String SimpleOption_TooltipFactoryGetter_ClassName = resolver.mapClassName("intermediary", "net.minecraft.class_7172$class_7307");
				Class<?> SimpleOption_TooltipFactoryGetter_Class = null;
				try
				{
					SimpleOption_TooltipFactoryGetter_Class = Class.forName(SimpleOption_TooltipFactoryGetter_ClassName);
				}
				catch (Throwable e2)
				{
				}
				Class<?> SimpleOption_TooltipParameter_Class;

				String SimpleOption_ValueTextGetter_ClassName = resolver.mapClassName("intermediary", "net.minecraft.class_7172$class_7303");
				Class<?> SimpleOption_ValueTextGetter_Class = Class.forName(SimpleOption_ValueTextGetter_ClassName);
				Class<?> SimpleOption_Callbacks_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_7172$class_7178"));
				Class<?> SimpleOption_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_7172"));

				Object tooltipParameter;

				if (SimpleOption_TooltipFactoryGetter_Class != null)
				{
					//1.19-1.19.2
					SimpleOption_TooltipParameter_Class = SimpleOption_TooltipFactoryGetter_Class;
					tooltipParameter = createTooltip(false, tooltip, 1190, resolver);
				}
				else
				{
					//1.19.3+
					String SimpleOption_TooltipFactory_ClassName = resolver.mapClassName("intermediary", "net.minecraft.class_7172$class_7277");
					Class<?> SimpleOption_TooltipFactory_Class = Class.forName(SimpleOption_TooltipFactory_ClassName);
					SimpleOption_TooltipParameter_Class = SimpleOption_TooltipFactory_Class;
					tooltipParameter = createTooltip(false, tooltip, 1193, resolver);
				}

				ValueTextGetterHelper<Boolean> valueTextGetterImpl = (optionText, value) ->
				{
					return (Text)createLiteralText(prefix + ": " + (value ? "ON" : "OFF"));
				};
				String toString_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_7172$class_7303", "toString", "(Lnet/minecraft/class_2585)Lnet/minecraft/class_2585;");
				Lookup lookup = MethodHandles.lookup();
				MethodType mType = MethodType.methodType(Text.class, Text.class, Object.class);
				Object valueTextGetter = LambdaMetafactory.metafactory(
					lookup,
					toString_MethodName,
					MethodType.methodType(SimpleOption_ValueTextGetter_Class, ValueTextGetterHelper.class),
					mType,
					lookup.findVirtual(ValueTextGetterHelper.class, "toString", mType),
					mType
				).getTarget().invoke(valueTextGetterImpl);

				String SimpleOption_BOOLEAN_FieldName = resolver.mapFieldName("intermediary", "net.minecraft.class_7172", "field_38278", "Lnet/minecraft/class_7172$class_7173;");
				Object callbacks = SimpleOption_Class.getField(SimpleOption_BOOLEAN_FieldName).get(null);

				Consumer<Boolean> changeCallback = (value) ->
				{
					setter.accept(value);
					saveOptions();
				};
				MethodHandle mh = MethodHandles.publicLookup().findConstructor(SimpleOption_Class, MethodType.methodType(void.class, String.class, SimpleOption_TooltipParameter_Class, SimpleOption_ValueTextGetter_Class, SimpleOption_Callbacks_Class, Object.class, Consumer.class));
				Object returnValue = mh.invoke(key, tooltipParameter, valueTextGetter, callbacks, getter.get(), changeCallback);
				((OptionMixinHelper)returnValue).setOmitBuilderKeyText();
				return returnValue;
			}
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to create the boolean option interface element.", t);
		}
	}

	//there doesn't seem to be a way to do tooltips on 1.14 and 1.15, so for now we won't

	private static Object createTooltip(boolean isDouble, String tooltip, int version, MappingResolver resolver) throws Throwable
	{
		//version is in the following format: 1.19.3 = 1193
		//rounded down to what we know (e.g. 1.16.5 may be just 1160, or 1.15 may be just 1140)

		if (version >= 1190)
		{
			Class<?> SimpleOption_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_7172"));
			Class<?> Text_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_2561"));

			if (version >= 1193)
			{
				//1.19.3+
				Class<?> SimpleOption_TooltipFactory_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_7172$class_7277"));
				if (tooltip == null)
				{
					String emptyTooltip_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_7172", "method_42399", "()Lnet/minecraft/class_7172$class_7277;");
					MethodHandle emptyTooltipMethod = MethodHandles.publicLookup().findStatic(SimpleOption_Class, emptyTooltip_MethodName, MethodType.methodType(SimpleOption_TooltipFactory_Class));
					Object tooltipFactory = emptyTooltipMethod.invoke();
					return tooltipFactory;
				}
				else
				{
					String constantTooltip_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_7172", "method_42717", "(Lnet/minecraft/class_2561;)Lnet/minecraft/class_7172$class_7277;");
					MethodHandle constantTooltipMethod = MethodHandles.publicLookup().findStatic(SimpleOption_Class, constantTooltip_MethodName, MethodType.methodType(SimpleOption_TooltipFactory_Class, Text_Class));
					Object tooltipFactory = constantTooltipMethod.invoke(createLiteralText(tooltip));
					return tooltipFactory;
				}
			}
			else
			{
				//1.19-1.19.2
				Class<?> SimpleOption_TooltipFactoryGetter_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_7172$class_7307"));
				if (tooltip == null)
				{
					String emptyTooltip_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_7172", "method_42399", "()Lnet/minecraft/class_7172$class_7307;");
					MethodHandle emptyTooltipMethod = MethodHandles.publicLookup().findStatic(SimpleOption_Class, emptyTooltip_MethodName, MethodType.methodType(SimpleOption_TooltipFactoryGetter_Class));
					Object tooltipFactoryGetter = emptyTooltipMethod.invoke();
					return tooltipFactoryGetter;
				}
				else
				{
					String constantTooltip_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_7172", "method_42717", "(Lnet/minecraft/class_2561;)Lnet/minecraft/class_7172$class_7307;");
					MethodHandle constantTooltipMethod = MethodHandles.publicLookup().findStatic(SimpleOption_Class, constantTooltip_MethodName, MethodType.methodType(SimpleOption_TooltipFactoryGetter_Class, Text_Class));
					Object tooltipFactoryGetter = constantTooltipMethod.invoke(createLiteralText(tooltip));
					return tooltipFactoryGetter;
				}
			}
		}

		if (version >= 1160)
		{
			//1.16-1.18
			Class<?> Style_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_2583"));
			String Style_EMPTY_Field = resolver.mapFieldName("intermediary", "net.minecraft.class_2583", "field_24360", "Lnet/minecraft/class_2583;");
			Object Style_EMPTY = Style_Class.getField(Style_EMPTY_Field).get(null);

			if (version >= 1170)
			{
				//1.17-1.18
				Class<?> OrderedText_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_5481"));
				String styledForwardsVisitedString_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_5481", "method_30747", "(Ljava/lang/String;Lnet/minecraft/class_2583;)Lnet/minecraft/class_5481;");
				MethodHandle styledForwardsVisitedStringMethod = MethodHandles.publicLookup().findStatic(OrderedText_Class, styledForwardsVisitedString_MethodName, MethodType.methodType(OrderedText_Class, String.class, Style_Class));

				ArrayList<Object> tooltipList = new ArrayList<>();
				String[] strings = tooltip.split("\\n");
				for (String str : strings)
				{
					for (String str2 : splitTooltipLine(str, resolver))
					{
						tooltipList.add(styledForwardsVisitedStringMethod.invoke(str2, Style_EMPTY));
					}
				}
				Function<?, ?> tooltipFunc = (Object o) -> tooltipList;

				if (isDouble) return tooltipFunc;

				//convert tooltipFunc to a CyclingButtonWidget.TooltipFactory
				Lookup lookup = MethodHandles.lookup();
				Class<?> CyclingButtonWidget_TooltipFactory_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_5676$class_5679"));
				MethodType mType = MethodType.methodType(Object.class, Object.class);
				Object tooltipFunc2 = LambdaMetafactory.metafactory(
					lookup,
					"apply",
					MethodType.methodType(CyclingButtonWidget_TooltipFactory_Class, Function.class),
					mType,
					lookup.findVirtual(Function.class, "apply", mType),
					mType
				).getTarget().invoke(tooltipFunc);

				Function<?, ?> tooltips = (MinecraftClient client) -> tooltipFunc2;

				return tooltips;
			}

			else
			{
				//1.16
				Class<?> OrderedText_Class = null;
				try
				{
					OrderedText_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_5481"));
				}
				catch (Throwable t)
				{
				}
				if (OrderedText_Class != null)
				{
					//1.16.2-1.16.5
					String styledString_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_5481", "method_30747", "(Ljava/lang/String;Lnet/minecraft/class_2583;)Lnet/minecraft/class_5481;");
					MethodHandle styledStringMethod = MethodHandles.publicLookup().findStatic(OrderedText_Class, styledString_MethodName, MethodType.methodType(OrderedText_Class, String.class, Style_Class));

					String[] strings = tooltip.split("\\n");
					ArrayList<Object> tooltipList = new ArrayList<>();
					for (String str : strings)
					{
						for (String str2 : splitTooltipLine(str, resolver))
						{
							tooltipList.add(styledStringMethod.invoke(str2, Style_EMPTY));
						}
					}
					return tooltipList;
				}
				else
				{
					//1.16-1.16.1
					Class<?> StringRenderable_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_5348"));
					String styled_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_5348", "method_29431", "(Ljava/lang/String;Lnet/minecraft/class_2583;)Lnet/minecraft/class_5348;");
					MethodHandle styledMethod = MethodHandles.publicLookup().findStatic(StringRenderable_Class, styled_MethodName, MethodType.methodType(StringRenderable_Class, String.class, Style_Class));

					String[] strings = tooltip.split("\\n");
					ArrayList<Object> tooltipList = new ArrayList<>();
					for (String str : strings)
					{
						for (String str2 : splitTooltipLine(str, resolver))
						{
							tooltipList.add(styledMethod.invoke(str2, Style_EMPTY));
						}
					}
					return tooltipList;
				}
			}
		}

		return null;
	}

	private static List<String> splitTooltipLine(String line, MappingResolver resolver) throws Throwable
	{
		Class<?> StringVisitable_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_5348"));
		Class<?> TextHandler_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_5225"));
		Class<?> Style_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_2583"));
		Class<?> TextRenderer_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_327"));
		String wrapLines_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_5225", "method_27498", "(Ljava/lang/String;ILnet/minecraft/class_2583;)Ljava/util/List;");
		MethodHandle wrapLinesMethod = MethodHandles.publicLookup().findVirtual(TextHandler_Class, wrapLines_MethodName, MethodType.methodType(List.class, String.class, int.class, Style_Class));
		MethodHandle getStringMethod = MethodHandles.publicLookup().findVirtual(StringVisitable_Class, "getString", MethodType.methodType(String.class));
		String getTextHandler_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_327", "method_27527", "()Lnet/minecraft/class_5225;");
		MethodHandle getTextHandlerMethod = MethodHandles.publicLookup().findVirtual(TextRenderer_Class, getTextHandler_MethodName, MethodType.methodType(TextHandler_Class));
		Field textRenderer_Field = MinecraftClient.class.getField(resolver.mapFieldName("intermediary", "net.minecraft.class_310", "field_1772", "Lnet/minecraft/class_327;"));
		textRenderer_Field.setAccessible(true);
		String Style_EMPTY_Field = resolver.mapFieldName("intermediary", "net.minecraft.class_2583", "field_24360", "Lnet/minecraft/class_2583;");
		Object Style_EMPTY = Style_Class.getField(Style_EMPTY_Field).get(null);
		List<?> listVisitable = (List<?>)wrapLinesMethod.invoke(getTextHandlerMethod.invoke(textRenderer_Field.get(MinecraftClient.getInstance())), line, 200, Style_EMPTY);
		ArrayList<String> resultList = new ArrayList<>();
		for (Object o : listVisitable)
		{
			resultList.add((String)getStringMethod.invoke(o));
		}
		return resultList;
	}

	private static Object writeTooltip(Object option, Object tooltipObject, int version, MappingResolver resolver) throws Throwable
	{
		//version is in the following format: 1.19.3 = 1193
		//rounded down to what we know (e.g. 1.16.5 may be just 1160, or 1.15 may be just 1140)

		if (version >= 1190)
		{
			//unsupported for 1.19+
			throw new RuntimeException("writeTooltip is not implemented for 1.19+");
		}

		if (version >= 1170)
		{
			//1.17-1.18
			//runs on CyclingOption only
			Class<?> CyclingOption_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_4064"));
			String tooltip_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_4064", "method_32528", "(Ljava/util/function/Function;)Lnet/minecraft/class_4064;");
			MethodHandle tooltipMethod = MethodHandles.publicLookup().findVirtual(CyclingOption_Class, tooltip_MethodName, MethodType.methodType(CyclingOption_Class, Function.class));

			return tooltipMethod.invoke(option, tooltipObject);
		}

		if (version >= 1160)
		{
			//1.16
			Class<?> Option_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_316"));
			String setTooltip_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_316", "method_29618", "(Ljava/util/List;)V");
			MethodHandle setTooltipMethod = MethodHandles.publicLookup().findVirtual(Option_Class, setTooltip_MethodName, MethodType.methodType(void.class, List.class));

			setTooltipMethod.invoke(option, tooltipObject);
			return option;
		}

		return option;
	}


	//here is the rest of the class that selects the interface options, and loads & stores the options:

	//return the list of interface options to show
	public static Object[] getModOptions()
	{
		loadInterface(); //load the elements if they are not loaded yet
		if (MinecraftClient.IS_SYSTEM_MAC)
		{
			//on macOS show reverse scrolling, reverse hotbar scrolling, trackpad sensitivity, momentum scrolling, and interface smooth scroll options
			Object[] arr = new Object[5];
			arr[0] = REVERSE_SCROLLING;
			arr[1] = REVERSE_HOTBAR_SCROLLING;
			arr[2] = TRACKPAD_SENSITIVITY;
			arr[3] = MOMENTUM_SCROLLING;
			arr[4] = INTERFACE_SMOOTH_SCROLL;
			return arr;
		}
		else
		{
			//otherwise show reverse scrolling, and reverse hotbar scrolling options only
			Object[] arr = new Object[2];
			arr[0] = REVERSE_SCROLLING;
			arr[1] = REVERSE_HOTBAR_SCROLLING;
			return arr;
		}
	}

	private static boolean loadedInterface = false;
	private static void loadInterface()
	{
		//load the option instances if they are not already loaded
		if (loadedInterface) return;
		try
		{
			//this is only used on macOS, so only load it here so we don't accidentally call any of these on other platforms
			if (MinecraftClient.IS_SYSTEM_MAC)
			{
				TRACKPAD_SENSITIVITY = doubleOption(
					"options.macos_input_fixes.trackpad_sensitivity",
					"Trackpad Sensitivity",
					0.0, 100.0, 1.0f,
					() -> trackpadSensitivity,
					(value) -> setTrackpadSensitivity(value),
					"The grouping feature only affects hotbar scrolling.\nThis feature only affects scrolling from the trackpad (and other high precision devices).\nDefault: 20.0\n0.0: Disable custom trackpad scroll processing.\nOther: group scrolls together to make scrolling speed much more reasonable on hotbar, scroll amount is divided by the value chosen here.");

				MOMENTUM_SCROLLING = booleanOption(
					"options.macos_input_fixes.momentum_scrolling",
					"Momentum Scrolling",
					() -> momentumScrolling,
					(value) -> setMomentumScrolling(value),
					"Only affects hotbar scrolling.\nA momentum scroll is when macOS keeps scrolling after you release the wheel.\nDefault: OFF\nOFF: ignore 'momentum scroll' events.\nON: process 'momentum scroll' events.");

				INTERFACE_SMOOTH_SCROLL = booleanOption(
					"options.macos_input_fixes.smooth_scroll",
					"Interface Smooth Scroll",
					() -> interfaceSmoothScroll,
					(value) -> setInterfaceSmoothScroll(value),
					"Affects all scrolling from legacy input devices (except for the hotbar).\nmacOS sometimes adjusts how much a single scroll does to make it feel 'smoother', but this can cause scroll amounts to feel random sometimes.\nDefault: OFF\nOFF: Modify smooth scrolling events to all be the same scroll amount.\nON: Keep smooth scrolling events as-is.");
			}

			REVERSE_HOTBAR_SCROLLING = booleanOption(
				"options.macos_input_fixes.reverse_hotbar_scrolling",
				"Reverse Hotbar Scroll",
				() -> reverseHotbarScrolling,
				(value) -> reverseHotbarScrolling = value,
				"Reverses the direction that scrolling goes for the hotbar when enabled.");

			REVERSE_SCROLLING = booleanOption(
				"options.macos_input_fixes.reverse_scrolling",
				"Reverse Scrolling",
				() -> reverseScrolling,
				(value) -> reverseScrolling = value,
				"Reverses the direction of all scrolling when enabled.");

			loadedInterface = true;
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to initialise option interface elements.", t);
		}
	}

	//saving and loading of options:

	public static File optionsFile;

	@SuppressWarnings("resource")
	public static void loadOptions()
	{
		//load options similarly to how minecraft does
		optionsFile = new File(MinecraftClient.getInstance().runDirectory, "options_macos_input_fixes.txt");
		try
		{
			if (!optionsFile.exists())
			{
				return;
			}
			List<String> lines = IOUtils.readLines(new FileInputStream(optionsFile), StandardCharsets.UTF_8); //split by lines
			NbtCompound compoundTag = new NbtCompound();
			for (String line : lines) //read the lines into a tag
			{
				try
				{
					Iterator<String> iterator = GameOptionsAccessor.COLON_SPLITTER().omitEmptyStrings().limit(2).split(line).iterator();
					compoundTag.putString(iterator.next(), iterator.next());
				}
				catch (Exception ex1)
				{
					ex1.printStackTrace(System.err); //failed to parse
				}
			}
			if (compoundTag.contains("trackpadSensitivity")) //read trackpadSensitivity option
			{
				double actualValue = 20.0; //default value
				try
				{
					Double value = Double.parseDouble(compoundTag.getString("trackpadSensitivity"));
					actualValue = value;
				}
				catch (Exception ex1)
				{
					ex1.printStackTrace(System.err); //failed to parse
				}
				setTrackpadSensitivity(actualValue);
			}
			if (compoundTag.contains("reverseHotbarScrolling")) //read reverseHotbarScrolling option
			{
				boolean actualValue = false; //default value
				try
				{
					Boolean value = Boolean.parseBoolean(compoundTag.getString("reverseHotbarScrolling"));
					actualValue = value;
				}
				catch (Exception ex1)
				{
					ex1.printStackTrace(System.err); //failed to parse
				}
				reverseHotbarScrolling = actualValue;
			}
			if (compoundTag.contains("reverseScrolling")) //read reverseScrolling option
			{
				boolean actualValue = false; //default value
				try
				{
					Boolean value = Boolean.parseBoolean(compoundTag.getString("reverseScrolling"));
					actualValue = value;
				}
				catch (Exception ex1)
				{
					ex1.printStackTrace(System.err); //failed to parse
				}
				reverseScrolling = actualValue;
			}
			if (compoundTag.contains("momentumScrolling")) //read momentumScrolling option
			{
				boolean actualValue = false; //default value
				try
				{
					Boolean value = Boolean.parseBoolean(compoundTag.getString("momentumScrolling"));
					actualValue = value;
				}
				catch (Exception ex1)
				{
					ex1.printStackTrace(System.err); //failed to parse
				}
				setMomentumScrolling(actualValue);
			}
			if (compoundTag.contains("interfaceSmoothScroll")) //read interfaceSmoothScroll option
			{
				boolean actualValue = false; //default value
				try
				{
					Boolean value = Boolean.parseBoolean(compoundTag.getString("interfaceSmoothScroll"));
					actualValue = value;
				}
				catch (Exception ex1)
				{
					ex1.printStackTrace(System.err); //failed to parse
				}
				setInterfaceSmoothScroll(actualValue);
			}

			loadedInterface = false;
		}
		catch (Exception ex2)
		{
			ex2.printStackTrace(System.err); //failed to do some sort of IO or something
		}
	}

	public static void saveOptions()
	{
		//write the options to the file in a similar way to minecraft
		try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(optionsFile), StandardCharsets.UTF_8)))
		{
			printWriter.println("trackpadSensitivity:" + trackpadSensitivity);
			printWriter.println("reverseHotbarScrolling:" + reverseHotbarScrolling);
			printWriter.println("reverseScrolling:" + reverseScrolling);
			printWriter.println("momentumScrolling:" + momentumScrolling);
			printWriter.println("interfaceSmoothScroll:" + interfaceSmoothScroll);
		}
		catch (Exception ex2)
		{
			ex2.printStackTrace(System.err); //failed to do some sort of IO or something
		}
	}

	//todo: use lang files for the below

	//trackpad sensitivity option code:

	public static double trackpadSensitivity = 20.0;

	public static void setTrackpadSensitivity(double value)
	{
		trackpadSensitivity = value;

		//set the value in the native library also, ensure the value is clamped here
		if (value < 0) value = 0.0;
		else if (value > 100.0) value = 100.0;
		MacOSInputFixesClientMod.setTrackpadSensitivity(value);
	}

	public static Object TRACKPAD_SENSITIVITY;

	//other options code:

	public static boolean reverseHotbarScrolling = false;
	public static Object REVERSE_HOTBAR_SCROLLING;

	public static boolean reverseScrolling = false;
	public static Object REVERSE_SCROLLING;

	public static boolean momentumScrolling = false;
	public static Object MOMENTUM_SCROLLING;

	public static void setMomentumScrolling(boolean value)
	{
		momentumScrolling = value;

		//set the value in the native library also
		MacOSInputFixesClientMod.setMomentumScrolling(value);
	}

	public static boolean interfaceSmoothScroll = false;
	public static Object INTERFACE_SMOOTH_SCROLL;

	public static void setInterfaceSmoothScroll(boolean value)
	{
		interfaceSmoothScroll = value;

		//set the value in the native library also
		MacOSInputFixesClientMod.setInterfaceSmoothScroll(value);
	}
}
