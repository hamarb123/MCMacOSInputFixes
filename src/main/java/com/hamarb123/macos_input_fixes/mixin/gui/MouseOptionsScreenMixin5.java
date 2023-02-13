package com.hamarb123.macos_input_fixes.mixin.gui;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.MouseOptionsScreen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;

@Mixin(MouseOptionsScreen.class)
public abstract class MouseOptionsScreenMixin5
{
    @Shadow
	private ButtonListWidget buttonList;

	private static MethodHandle _getHoveredButtonTooltip;
	private static List<OrderedText> getHoveredButtonTooltip(ButtonListWidget buttonList, int mouseX, int mouseY)
	{
		try
		{
			if (_getHoveredButtonTooltip == null)
			{
				MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
				String getHoveredButtonTooltip_MethodName = resolver.mapMethodName("intermediary", "net.minecraft.class_4667", "method_31048", "(Lnet/minecraft/class_353;II)Ljava/util/List;");
				_getHoveredButtonTooltip = MethodHandles.publicLookup().findStatic(GameOptionsScreen.class, getHoveredButtonTooltip_MethodName, MethodType.methodType(List.class, ButtonListWidget.class, int.class, int.class));
			}
			return (List<OrderedText>)_getHoveredButtonTooltip.invoke(buttonList, mouseX, mouseY);
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Failed to run getHoveredButtonTooltip.", t);
		}
	}

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", at = @At("RETURN"), cancellable = true)
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info)
	{
		List<OrderedText> list = getHoveredButtonTooltip(this.buttonList, mouseX, mouseY);
		if (list != null) ((MouseOptionsScreen)(Object)this).renderOrderedTooltip(matrices, list, mouseX, mouseY);
	}
}
