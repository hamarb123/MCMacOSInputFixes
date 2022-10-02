package com.hamarb123.macos_input_fixes.mixin.gui;

import java.lang.reflect.Array;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import com.hamarb123.macos_input_fixes.ModOptions;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.gui.screen.option.MouseOptionsScreen;

@Mixin(MouseOptionsScreen.class)
public class MouseOptionsScreenMixin2
{
	//caching for the Option class value
	private static Class<?> _optionClass;

	//ModifyArgs targets for init with yarn mappings:
	//`init()V`, note: we mixin to both `init()V` (for 1.14-1.15) and `method_25426()V` (for 1.16+) as the intemediary name was changed

	//other target for init (invoke at's target) with yarn mappings:
	//`net/minecraft/client/gui/widget/ButtonListWidget;addAll([Lnet/minecraft/client/option/Option;)V`

	@ModifyArgs(method = {"init()V", "method_25426()V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_353;method_20408([Lnet/minecraft/class_316;)V", remap = false), remap = false)
	private void init(Args args)
	{
		//get the mod options so we can add them to the game options
		Object[] modOptions = ModOptions.getModOptions();
		if (modOptions == null) return;

		//get the array element type
		if (_optionClass == null)
		{
			MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
			String OptionClassName = resolver.mapClassName("intermediary", "net.minecraft.class_316");
			try
			{
				_optionClass = Class.forName(OptionClassName);
			}
			catch (ClassNotFoundException e)
			{
				throw new RuntimeException("Failed to load Option type.", e);
			}
		}

		//get the method call argument
		Object[] options = args.get(0);

		//combine the game and mod options into 1 array
		Object[] newOptions = (Object[])Array.newInstance(_optionClass, options.length + modOptions.length);
		for (int i = 0; i < options.length; i++) newOptions[i] = options[i];
		for (int i = 0; i < modOptions.length; i++) newOptions[options.length + i] = modOptions[i];
		args.set(0, newOptions);
	}
}
