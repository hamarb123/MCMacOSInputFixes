package com.hamarb123.macos_input_fixes.mixin.gui;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.MouseOptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;

@Mixin(MouseOptionsScreen.class)
public abstract class MouseOptionsScreenMixin6
{
    @Shadow
	private ButtonListWidget buttonList;

	private static Class<?> _optionButtonWidget_Class;
	private static Class<?> OptionButtonWidget_Class()
	{
		try
		{
			if (_optionButtonWidget_Class == null)
			{
				MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
				_optionButtonWidget_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_349"));
			}
			return _optionButtonWidget_Class;
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to get OptionButtonWidget class.", t);
		}
	}

	private static Class<?> _doubleOptionSliderWidget_Class;
	private static Class<?> DoubleOptionSliderWidget_Class()
	{
		try
		{
			if (_doubleOptionSliderWidget_Class == null)
			{
				MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
				_doubleOptionSliderWidget_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_4040"));
			}
			return _doubleOptionSliderWidget_Class;
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to get DoubleOptionSliderWidget class.", t);
		}
	}

	private static MethodHandle _getOption;
	private static Object getOption(Object optionButtonWidget)
	{
		try
		{
			if (_getOption == null)
			{
				MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
				String getOption_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_349", "method_29623", "()Lnet/minecraft/class_316;");
				Class<?> Option_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_316"));
				_getOption = MethodHandles.publicLookup().findVirtual(OptionButtonWidget_Class(), getOption_MethodName, MethodType.methodType(Option_Class));
			}
			return _getOption.invoke(optionButtonWidget);
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to run getOption.", t);
		}
	}

	private static MethodHandle _getTooltip;
	private static Optional<List<?>> getTooltip(Object option)
	{
		try
		{
			if (_getTooltip == null)
			{
				MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
				String getTooltip_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_316", "method_29619", "()Ljava/util/Optional;");
				Class<?> Option_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_316"));
				_getTooltip = MethodHandles.publicLookup().findVirtual(Option_Class, getTooltip_MethodName, MethodType.methodType(Optional.class));
			}
			return (Optional<List<?>>)_getTooltip.invoke(option);
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to run getTooltip.", t);
		}
	}

	private static Field _DoubleOptionSliderWidget_option_field;
	private static Object DoubleOptionSliderWidget_getOptionField(Object widget)
	{
		try
		{
			if (_getTooltip == null)
			{
				MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
				String option_FieldName = resolver.mapFieldName("intermediary", "net.minecraft.class_4040", "field_18012", "Lnet/minecraft/class_4067;");
				Class<?> DoubleOption_Class = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_4067"));
				Class<?> DoubleOptionSliderWidget_Class = DoubleOptionSliderWidget_Class();
				Field f = DoubleOptionSliderWidget_Class.getDeclaredField(option_FieldName);
				if (!f.getType().equals(DoubleOption_Class)) throw new Exception("Failed to find field");
				f.setAccessible(true);
				_DoubleOptionSliderWidget_option_field = f;
			}
			return _DoubleOptionSliderWidget_option_field.get(widget);
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to run DoubleOptionSliderWidget_getOptionField.", t);
		}
	}

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", at = @At("RETURN"), cancellable = true)
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info)
	{
		Optional<?> optional = buttonList.getHoveredButton(mouseX, mouseY);
		if (optional.isPresent())
		{
			Object option;
			if (OptionButtonWidget_Class().isInstance(optional.get()))
			{
				option = getOption(optional.get());
			}
			else if (DoubleOptionSliderWidget_Class().isInstance(optional.get()))
			{
				option = DoubleOptionSliderWidget_getOptionField(optional.get());
			}
			else
			{
				return;
			}
			Optional<List<?>> optional2 = getTooltip(option);
			if (optional2.isPresent())
			{
				//cast is purely to satisfy syntax
				((MouseOptionsScreen)(Object)this).renderOrderedTooltip(matrices, (List<? extends OrderedText>)optional2.get(), mouseX, mouseY);
			}
		}
	}
}
