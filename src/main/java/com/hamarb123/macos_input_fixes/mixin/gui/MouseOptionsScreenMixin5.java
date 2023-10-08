package com.hamarb123.macos_input_fixes.mixin.gui;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.hamarb123.macos_input_fixes.FabricReflectionHelper;
import net.minecraft.client.gui.screen.option.MouseOptionsScreen;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(MouseOptionsScreen.class)
public abstract class MouseOptionsScreenMixin5
{
    @Shadow
	private OptionListWidget buttonList;

	@Inject(method = "method_25394(Lnet/minecraft/class_4587;IIF)V", at = @At("RETURN"), cancellable = true, remap = false)
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info)
	{
		List<?> list = (List<?>)FabricReflectionHelper.GameOptionsScreen_getHoveredButtonTooltip(this.buttonList, mouseX, mouseY);
		if (list != null) ((ScreenAccessor7)this).renderOrderedTooltip(matrices, list, mouseX, mouseY);
	}
}
