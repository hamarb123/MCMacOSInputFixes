package com.hamarb123.macos_input_fixes.client.mixin.gui;

import java.util.List;
import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.client.FabricReflectionHelper;

import net.minecraft.client.gui.screen.option.MouseOptionsScreen;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(MouseOptionsScreen.class)
public class MouseOptionsScreenMixin6
{
    @Shadow
	private OptionListWidget buttonList;

	@Inject(method = "method_25394(Lnet/minecraft/class_4587;IIF)V", at = @At("RETURN"), cancellable = true, remap = false)
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info)
	{
		Optional<?> optional = FabricReflectionHelper.OptionButtonWidget_getHoveredWidget(buttonList, mouseX, mouseY);
		if (optional.isPresent())
		{
			Object option;
			if (FabricReflectionHelper.OptionButtonWidget().isInstance(optional.get()))
			{
				option = FabricReflectionHelper.OptionButtonWidget_getOption(optional.get());
			}
			else if (FabricReflectionHelper.DoubleOptionSliderWidget().isInstance(optional.get()))
			{
				option = FabricReflectionHelper.DoubleOptionSliderWidget_option(optional.get());
			}
			else
			{
				return;
			}
			Optional<List<?>> optional2 = FabricReflectionHelper.Option_getTooltip(option);
			if (optional2.isPresent())
			{
				((ScreenAccessor7)this).renderOrderedTooltip(matrices, optional2.get(), mouseX, mouseY);
			}
		}
	}
}
