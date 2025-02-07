package com.hamarb123.macos_input_fixes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.io.IOUtils;
import com.hamarb123.macos_input_fixes.FabricReflectionHelper.CyclingOptionSetterHelper;
import com.hamarb123.macos_input_fixes.FabricReflectionHelper.ValueTextGetterHelper;
import com.hamarb123.macos_input_fixes.mixin.MinecraftClientAccessor;
import com.hamarb123.macos_input_fixes.mixin.gui.GameOptionsAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

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

	//todo: update above comments to reflect changes from tooltips


	//here's the implementation for creating the different interface elements:

	private static Text createLiteralText(String value)
	{
		if (FabricReflectionHelper.Has_Text_literal())
		{
			//1.19+
			return FabricReflectionHelper.Text_literal(value);
		}
		else
		{
			//1.14-1.18
			return FabricReflectionHelper.new_LiteralText(value);
		}
	}

	@SuppressWarnings("unchecked")
	private static Object doubleOption(String key, String prefix, double min, double max, float step, Supplier<Double> getter, Consumer<Double> setter, String tooltip)
	{
		try
		{
			if (FabricReflectionHelper.Try_DoubleOption() != null)
			{
				//1.14-1.18
				Constructor<?> ctorM;
				boolean is117Plus = false;
				int version = 1160;
				if (tooltip == null || !FabricReflectionHelper.Has_new_DoubleOption_8())
				{
					ctorM = FabricReflectionHelper.Info_new_DoubleOption7();
				}
				else
				{
					ctorM = FabricReflectionHelper.Info_new_DoubleOption8();
					is117Plus = true;
					version = 1170;
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
					return FabricReflectionHelper.new_DoubleOption(key, min, max, step, _getter, _setter, displayStringGetter);
				}
				else
				{
					Object tooltipObject = createTooltip(true, tooltip, version);
					if (is117Plus)
					{
						return FabricReflectionHelper.new_DoubleOption(key, min, max, step, _getter, _setter, displayStringGetter, (Function<MinecraftClient, List<?>>)tooltipObject);
					}
					else
					{
						Object returnValue = FabricReflectionHelper.new_DoubleOption(key, min, max, step, _getter, _setter, displayStringGetter);
						returnValue = writeTooltip(returnValue, tooltipObject, version);
						return returnValue;
					}
				}
			}
			else
			{
				//1.19+
				double step2 = (max - min) / step;
				ValueTextGetterHelper<Double> valueTextGetterImpl = (optionText, value) ->
				{
					double result = Math.round(value * step2) * step + min;
					return (Text)createLiteralText(prefix + ": " + result);
				};
				Object valueTextGetter = FabricReflectionHelper.convertToSimpleOption_ValueTextGetter(valueTextGetterImpl);

				Double defaultValue = (getter.get() - min) / (max - min);

				Consumer<Double> changeCallback = (value) ->
				{
					double result = Math.round(value * step2) * step + min;
					setter.accept(result);
					saveOptions();
				};
				if (FabricReflectionHelper.Try_SimpleOption_TooltipFactoryGetter() != null)
				{
					//1.19-1.19.2
					Object tooltipParameter = createTooltip(true, tooltip, 1190);
					return FabricReflectionHelper.new_SimpleOption_1(key, tooltipParameter, valueTextGetter, FabricReflectionHelper.SimpleOption_DoubleSliderCallbacks_INSTANCE(), defaultValue, changeCallback);
				}
				else
				{
					//1.19.3+
					Object tooltipParameter = createTooltip(true, tooltip, 1193);
					return FabricReflectionHelper.new_SimpleOption_2(key, tooltipParameter, valueTextGetter, FabricReflectionHelper.SimpleOption_DoubleSliderCallbacks_INSTANCE(), defaultValue, changeCallback);
				}
			}
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to create the double option interface element.", t);
		}
	}

	private static Object booleanOption(String key, String prefix, Supplier<Boolean> getter, Consumer<Boolean> setter, String tooltip)
	{
		try
		{
			if (FabricReflectionHelper.Try_CyclingOption() != null)
			{
				//1.14-1.18
				if (FabricReflectionHelper.Try_CyclingOption_Setter() != null)
				{
					//1.17-1.18
					Function<GameOptions, Boolean> _getter = (gameOptions) -> getter.get();
					CyclingOptionSetterHelper<Boolean> _setter = (gameOptions, option, value) ->
					{
						setter.accept(value);
						saveOptions();
					};
					Object _setter2 = FabricReflectionHelper.convertToCyclingOption_Setter(_setter);
					Object returnValue = FabricReflectionHelper.CyclingOption_create(key, createLiteralText(prefix + ": ON"), createLiteralText(prefix + ": OFF"), _getter, _setter2);
					((OptionMixinHelper)returnValue).setOmitBuilderKeyText();
					if (tooltip != null)
					{
						Object tooltipObject = createTooltip(false, tooltip, 1170);
						returnValue = writeTooltip(returnValue, tooltipObject, 1170);
					}
					return returnValue;
				}
				else
				{
					//1.14-1.16
					BiConsumer<GameOptions, Integer> _setter = (gameOptions, value) ->
					{
						setter.accept(!getter.get());
						saveOptions();
					};
					BiFunction<GameOptions, ?, ?> displayStringGetter;
					int version = 1160;
					if (((ParameterizedType)FabricReflectionHelper.Info_new_CyclingOption3().getGenericParameterTypes()[2]).getActualTypeArguments()[2] == String.class)
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
					Object returnValue = FabricReflectionHelper.new_CyclingOption(key, _setter, displayStringGetter);
					if (tooltip != null)
					{
						Object tooltipObject = createTooltip(false, tooltip, version);
						returnValue = writeTooltip(returnValue, tooltipObject, version);
					}
					return returnValue;
				}
			}
			else
			{
				//1.19+
				ValueTextGetterHelper<Boolean> valueTextGetterImpl = (optionText, value) ->
				{
					return (Text)createLiteralText(prefix + ": " + (value ? "ON" : "OFF"));
				};
				Object valueTextGetter = FabricReflectionHelper.convertToSimpleOption_ValueTextGetter(valueTextGetterImpl);

				Consumer<Boolean> changeCallback = (value) ->
				{
					setter.accept(value);
					saveOptions();
				};

				Object returnValue;
				if (FabricReflectionHelper.Try_SimpleOption_TooltipFactoryGetter() != null)
				{
					//1.19-1.19.2
					Object tooltipParameter = createTooltip(false, tooltip, 1190);
					returnValue = FabricReflectionHelper.new_SimpleOption_1(key, tooltipParameter, valueTextGetter, FabricReflectionHelper.SimpleOption_BOOLEAN(), getter.get(), changeCallback);
				}
				else
				{
					//1.19.3+
					Object tooltipParameter = createTooltip(false, tooltip, 1193);
					returnValue = FabricReflectionHelper.new_SimpleOption_2(key, tooltipParameter, valueTextGetter, FabricReflectionHelper.SimpleOption_BOOLEAN(), getter.get(), changeCallback);
				}

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

	private static Object createTooltip(boolean isDouble, String tooltip, int version) throws Throwable
	{
		//version is in the following format: 1.19.3 = 1193
		//rounded down to what we know (e.g. 1.16.5 may be just 1160, or 1.15 may be just 1140)

		if (version >= 1193)
		{
			//1.19.3+
			return tooltip == null ? FabricReflectionHelper.SimpleOption_emptyTooltip_2() : FabricReflectionHelper.SimpleOption_constantTooltip_2(createLiteralText(tooltip));
		}

		if (version >= 1190)
		{
			//1.19-1.19.2
			return tooltip == null ? FabricReflectionHelper.SimpleOption_emptyTooltip_1() : FabricReflectionHelper.SimpleOption_constantTooltip_1(createLiteralText(tooltip));
		}

		if (version >= 1170)
		{
			//1.17-1.18
			ArrayList<Object> tooltipList = new ArrayList<>();
			String[] strings = tooltip.split("\\n");
			for (String str : strings)
			{
				for (String str2 : splitTooltipLine(str))
				{
					tooltipList.add(FabricReflectionHelper.OrderedText_styledForwardsVisitedString(str2, FabricReflectionHelper.Style_EMPTY()));
				}
			}
			Function<?, ?> tooltipFunc = (Object o) -> tooltipList;

			if (isDouble) return tooltipFunc;

			Object tooltipFunc2 = FabricReflectionHelper.convertToCyclingButtonWidget_TooltipFactory(tooltipFunc);
			Function<?, ?> tooltips = (MinecraftClient client) -> tooltipFunc2;
			return tooltips;
		}

		if (version >= 1160)
		{
			//1.16
			if (FabricReflectionHelper.Try_OrderedText() != null)
			{
				//1.16.2-1.16.5
				String[] strings = tooltip.split("\\n");
				ArrayList<Object> tooltipList = new ArrayList<>();
				for (String str : strings)
				{
					for (String str2 : splitTooltipLine(str))
					{
						tooltipList.add(FabricReflectionHelper.OrderedText_styledForwardsVisitedString(str2, FabricReflectionHelper.Style_EMPTY()));
					}
				}
				return tooltipList;
			}
			else
			{
				//1.16-1.16.1
				String[] strings = tooltip.split("\\n");
				ArrayList<Object> tooltipList = new ArrayList<>();
				for (String str : strings)
				{
					for (String str2 : splitTooltipLine(str))
					{
						tooltipList.add(FabricReflectionHelper.StringVisitable_styled(str2, FabricReflectionHelper.Style_EMPTY()));
					}
				}
				return tooltipList;
			}
		}

		return null;
	}

	private static List<String> splitTooltipLine(String line) throws Throwable
	{
		List<?> listVisitable = FabricReflectionHelper.TextHandler_wrapLines(FabricReflectionHelper.TextRenderer_getTextHandler(((MinecraftClientAccessor)MinecraftClient.getInstance()).getTextRenderer()), line, 200, FabricReflectionHelper.Style_EMPTY());
		ArrayList<String> resultList = new ArrayList<>();
		for (Object o : listVisitable)
		{
			resultList.add(FabricReflectionHelper.StringVisitable_getString(o));
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	private static Object writeTooltip(Object option, Object tooltipObject, int version) throws Throwable
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
			return FabricReflectionHelper.CyclingOption_tooltip(option, (Function<MinecraftClient, ?>)tooltipObject);
		}

		if (version >= 1160)
		{
			//1.16
			FabricReflectionHelper.Option_setTooltip(option, (List<?>)tooltipObject);
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
			//on macOS show reverse scrolling, reverse hotbar scrolling, trackpad sensitivity, momentum scrolling, interface smooth scroll options, disable ctrl+click fix
			Object[] arr = new Object[6];
			arr[0] = REVERSE_SCROLLING;
			arr[1] = REVERSE_HOTBAR_SCROLLING;
			arr[2] = TRACKPAD_SENSITIVITY;
			arr[3] = MOMENTUM_SCROLLING;
			arr[4] = INTERFACE_SMOOTH_SCROLL;
			arr[5] = DISABLE_CTRL_CLICK_FIX;
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

				DISABLE_CTRL_CLICK_FIX = booleanOption(
					"options.macos_input_fixes.disable_ctrl_click_fix",
					"Disable Ctrl+Click Fix",
					() -> disableCtrlClickFix,
					(value) -> disableCtrlClickFix = value,
					"When enabled, disables the fix for the bug which causes Minecraft\nto map Control + Left Click to Right Click.");
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
			if (compoundTag.contains("disableCtrlClickFix")) //read disableCtrlClickFix option
			{
				boolean actualValue = false; //default value
				try
				{
					Boolean value = Boolean.parseBoolean(compoundTag.getString("disableCtrlClickFix"));
					actualValue = value;
				}
				catch (Exception ex1)
				{
					ex1.printStackTrace(System.err); //failed to parse
				}
				disableCtrlClickFix = actualValue;
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
			printWriter.println("disableCtrlClickFix:" + disableCtrlClickFix);
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
		if (Util.getOperatingSystem() != Util.OperatingSystem.OSX) return;

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
		if (Util.getOperatingSystem() != Util.OperatingSystem.OSX) return;

		//set the value in the native library also
		MacOSInputFixesClientMod.setMomentumScrolling(value);
	}

	public static boolean interfaceSmoothScroll = false;
	public static Object INTERFACE_SMOOTH_SCROLL;

	public static void setInterfaceSmoothScroll(boolean value)
	{
		interfaceSmoothScroll = value;
		if (Util.getOperatingSystem() != Util.OperatingSystem.OSX) return;

		//set the value in the native library also
		MacOSInputFixesClientMod.setInterfaceSmoothScroll(value);
	}

	public static boolean disableCtrlClickFix = false;
	public static Object DISABLE_CTRL_CLICK_FIX;
}
