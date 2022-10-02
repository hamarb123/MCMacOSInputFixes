package com.hamarb123.macos_input_fixes.mixin.gui;

import java.util.function.Function;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.hamarb123.macos_input_fixes.Common;
import net.minecraft.client.gui.widget.CyclingButtonWidget;

@Mixin(CyclingButtonWidget.Builder.class)
public class CyclingButtonWidgetBuilderMixin3
{
	@Inject(method = "<init>(Ljava/util/function/Function;)V", at = @At("TAIL"))
	private void init(Function<?, ?> valueToText, CallbackInfo info)
	{
		//if our omitBuilderKeyText flag is set (which it is for our buttons),
		//call omitKeyText() to omit the prefix based on the key
		if (Common.omitBuilderKeyText())
		{
			((CyclingButtonWidget.Builder<?>)(Object)this).omitKeyText();
		}
	}
}
