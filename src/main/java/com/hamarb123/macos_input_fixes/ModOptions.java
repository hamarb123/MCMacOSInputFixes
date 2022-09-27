package com.hamarb123.macos_input_fixes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.IOUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.CyclingOption;
import net.minecraft.client.options.DoubleOption;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.nbt.CompoundTag;

@Environment(EnvType.CLIENT)
public class ModOptions
{
	//todo: make this work properly on all versions

	public static Option[] getModOptions()
	{
		return null;
	}
	public static boolean reverseHotbarScrolling = false;
	public static boolean reverseScrolling = false;
	public static void loadOptions() { }

	/*
	public static Option[] getModOptions()
	{
		if (MinecraftClient.IS_SYSTEM_MAC)
		{
			//on macOS show reverse scrolling, reverse hotbar scrolling, and trackpad sensitivity options
			Option[] arr = new Option[3];
			arr[0] = REVERSE_SCROLLING;
			arr[1] = REVERSE_HOTBAR_SCROLLING;
			arr[2] = TRACKPAD_SENSITIVITY;
			return arr;
		}
		else
		{
			//otherwise show reverse scrolling, and reverse hotbar scrolling options only
			Option[] arr = new Option[2];
			arr[0] = REVERSE_SCROLLING;
			arr[1] = REVERSE_HOTBAR_SCROLLING;
			return arr;
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
			CompoundTag compoundTag = new CompoundTag();
            for (String line : lines) //read the lines into a tag
			{
                try
				{
                    Iterator<String> iterator = GameOptions.COLON_SPLITTER.omitEmptyStrings().limit(2).split(line).iterator();
                    compoundTag.putString(iterator.next(), iterator.next());
                }
                catch (Exception ex1)
				{
                    ex1.printStackTrace(System.err); //failed to parse
                }
            }
			if (compoundTag.containsKey("trackpadSensitivity")) //read trackpadSensitivity option
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
				setTrackpadSensitivity(actualValue, false);
			}
			if (compoundTag.containsKey("reverseHotbarScrolling")) //read reverseHotbarScrolling option
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
			if (compoundTag.containsKey("reverseScrolling")) //read reverseScrolling option
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
		}
		catch (Exception ex2)
		{
			ex2.printStackTrace(System.err); //failed to do some sort of IO or something
		}
	}

	//todo: use lang files for the below

	//trackpad sensitivity option code:

	public static double trackpadSensitivity = 20.0;

	public static void setTrackpadSensitivity(double value, boolean save)
	{
		trackpadSensitivity = value;
		if (save) saveOptions();

		//set the value in the native library also, ensure the value is clamped here
		if (value < 0) value = 0.0;
		else if (value > 100.0) value = 100.0;
		MacOSInputFixesClientMod.setTrackpadSensitivity(value);
	}

	public static final DoubleOption TRACKPAD_SENSITIVITY = new DoubleOption("options.macos_input_fixes.trackpad_sensitivity", 0.0, 100.0, 1.0f,
			gameOptions -> trackpadSensitivity,
			(gameOptions, double_) -> {
				setTrackpadSensitivity(double_, true);
			}, (gameOptions, doubleOption) -> {
				return "Trackpad Sensitivity: " + trackpadSensitivity;
			});

	//reverse hotbar scrolling option code:
	
	public static boolean reverseHotbarScrolling = false;

	public static final CyclingOption REVERSE_HOTBAR_SCROLLING = new CyclingOption("options.macos_input_fixes.reverse_hotbar_scrolling",
			(gameOptions, integer) -> {
				reverseHotbarScrolling = !reverseHotbarScrolling;
				saveOptions();
			}, (gameOption, integer) -> {
				return reverseHotbarScrolling ? "Reverse Hotbar Scroll: ON" : "Reverse Hotbar Scroll: OFF";
			});

	//reverse scrolling code:

	public static boolean reverseScrolling = false;

	public static final CyclingOption REVERSE_SCROLLING = new CyclingOption("options.macos_input_fixes.reverse_scrolling",
			(gameOptions, integer) -> {
				reverseScrolling = !reverseScrolling;
				saveOptions();
			}, (gameOptions, integer) -> {
				return reverseScrolling ? "Reverse Scrolling: ON" : "Reverse Scrolling: OFF";
			});
	*/
}
