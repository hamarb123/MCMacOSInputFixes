package com.hamarb123.macos_input_fixes.mixin.gui;

import java.util.List;
import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.hamarb123.macos_input_fixes.FabricReflectionHelper;
import net.minecraft.client.gui.screen.option.MouseOptionsScreen;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;

@Mixin(MouseOptionsScreen.class)
public class MouseOptionsScreenMixin6
{
    @Shadow
	private OptionListWidget buttonList;

	@SuppressWarnings("unchecked")
	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", at = @At("RETURN"), cancellable = true)
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info)
	{
		Optional<?> optional = buttonList.getHoveredWidget(mouseX, mouseY);
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
				//cast is purely to satisfy syntax
				((MouseOptionsScreen)(Object)this).renderOrderedTooltip(matrices, (List<? extends OrderedText>)optional2.get(), mouseX, mouseY);
			}
		}
	}
}
