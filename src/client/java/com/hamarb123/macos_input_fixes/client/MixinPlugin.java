package com.hamarb123.macos_input_fixes.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

//? if <26.1 {
/*
// LEGACY LOGIC:

import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.service.MixinService;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

*///?}

public class MixinPlugin implements IMixinConfigPlugin
{
	@Override
	public void onLoad(String mixinPackage)
	{
	}

	@Override
	public String getRefMapperConfig()
	{
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
	{
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
	{
	}

	@Override
	public List<String> getMixins()
	{
		List<String> li = new ArrayList<String>();

		//? if >=26.1 {
		// MODERN LOGIC:

		li.add("gui.OptionInstanceMixin1");
		li.add("gui.OptionsListMixin8");
		li.add("gui.CycleButtonBuilderMixin16");
		li.add("MouseHandlerMixin11");
		li.add("AbstractContainerScreenMixin13");
		li.add("KeyboardHandlerMixin13");
		li.add("MinecraftMixin13");
		li.add("MouseHandlerMixin13");
		li.add("KeyboardHandlerAccessor15");

		//?} else {
		/*
		// LEGACY LOGIC:

		//load certain mixins at runtime depending on whether certain classes are available
		MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
		boolean hasOptionClass = isClassPresent(resolver.mapClassName("intermediary", "net.minecraft.class_316"));
		boolean hasCyclingButtonWidgetClass = isClassPresent(resolver.mapClassName("intermediary", "net.minecraft.class_5676"));
		boolean hasGameOptionsScreen_getHoveredButtonTooltip = isMethodPresent(resolver.mapClassName("intermediary", "net.minecraft.class_4667"),
			resolver.mapMethodName("intermediary", "net.minecraft.class_4667", "method_31048", "(Lnet/minecraft/class_353;II)Ljava/util/List;"),
			"(L" + resolver.mapClassName("intermediary", "net.minecraft.class_353").replace(".", "/") + ";II)Ljava/util/List;");
		boolean hasScreen_renderTooltip = isMethodPresent(resolver.mapClassName("intermediary", "net.minecraft.class_437"),
			resolver.mapMethodName("intermediary", "net.minecraft.class_437", "method_25417", "(Lnet/minecraft/class_4587;Ljava/util/List;II)V"),
			"(L" + resolver.mapClassName("intermediary", "net.minecraft.class_4587").replace(".", "/") + ";Ljava/util/List;II)V");
		boolean hasMouseOptionsScreen_init = isMethodPresent(resolver.mapClassName("intermediary", "net.minecraft.class_4288"),
			resolver.mapMethodName("intermediary", "net.minecraft.class_4288", "method_25426", "()V"),
			"()V");
		boolean hasPlayerInventory_scrollInHotbar = isMethodPresent(resolver.mapClassName("intermediary", "net.minecraft.class_1661"),
			resolver.mapMethodName("intermediary", "net.minecraft.class_1661", "method_7373", "(D)V"),
			"(D)V");
		boolean hasScreen_hasControlDown = isMethodPresent(resolver.mapClassName("intermediary", "net.minecraft.class_437"),
			resolver.mapMethodName("intermediary", "net.minecraft.class_437", "method_25441", "()Z"),
			"()Z") || isMethodPresent(resolver.mapClassName("intermediary", "net.minecraft.class_437"),
			resolver.mapMethodName("intermediary", "net.minecraft.class_437", "hasControlDown", "()Z"),
			"()Z");
		boolean hasCyclingButtonWidget_Builder_TwoParamCtor = isMethodPresent(resolver.mapClassName("intermediary", "net.minecraft.class_5676$class_5677"),
			resolver.mapMethodName("intermediary", "net.minecraft.class_5676$class_5677", "<init>", "(Ljava/util/function/Function;Ljava/util/function/Supplier;)V"),
			"(Ljava/util/function/Function;Ljava/util/function/Supplier;)V");
		if (hasOptionClass)
		{
			li.add("gui.MouseSettingsScreenMixin2");
			if (hasCyclingButtonWidgetClass)
			{
				li.add("gui.CyclingOptionMixin4");
			}
		}
		else
		{
			li.add("gui.OptionInstanceMixin1");
			if (hasMouseOptionsScreen_init)
			{
				li.add("gui.MouseSettingsScreenMixin9");
				li.add("gui.OptionsListMixin9");
			}
			else
			{
				li.add("gui.OptionsListMixin8");
			}
		}
		if (hasCyclingButtonWidgetClass)
		{
			if (hasCyclingButtonWidget_Builder_TwoParamCtor)
			{
				li.add("gui.CycleButtonBuilderMixin16");
			}
			else
			{
				li.add("gui.CycleButtonBuilderMixin3");
			}
		}
		if (hasGameOptionsScreen_getHoveredButtonTooltip)
		{
			li.add("gui.MouseSettingsScreenMixin5");
		}
		if (!hasGameOptionsScreen_getHoveredButtonTooltip && hasScreen_renderTooltip && hasOptionClass)
		{
			li.add("gui.MouseSettingsScreenMixin6");
		}
		if (hasGameOptionsScreen_getHoveredButtonTooltip || (hasScreen_renderTooltip && hasOptionClass))
		{
			li.add("gui.ScreenAccessor7");
		}
		if (hasPlayerInventory_scrollInHotbar)
		{
			li.add("InventoryMixin10");
		}
		else
		{
			li.add("MouseHandlerMixin11");
		}
		if (hasScreen_hasControlDown)
		{
			li.add("AbstractContainerScreenMixin12");
			li.add("KeyboardHandlerMixin12");
			li.add("MinecraftMixin12");
			li.add("MouseHandlerMixin12");
			li.add("ScreenMixin12");
		}
		else
		{
			li.add("AbstractContainerScreenMixin13");
			li.add("KeyboardHandlerMixin13");
			li.add("MinecraftMixin13");
			li.add("MouseHandlerMixin13");
		}

		*///?}

		return li;
	}

	//? if <26.1 {
	/*
	// LEGACY LOGIC:
	private static boolean isClassPresent(String className)
	{
		try
		{
			MixinService.getService().getBytecodeProvider().getClassNode(className);
			return true;
		}
		catch (ClassNotFoundException ignored)
		{
			// Class isn't present, skip this mixin.
			return false;
		}
		catch (Exception e)
		{
			// Something else went wrong which might be more serious.
			e.printStackTrace();
			return false;
		}
	}

	private static boolean isMethodPresent(String className, String methodName, String descriptor)
	{
		try
		{
			ClassNode classNode = MixinService.getService().getBytecodeProvider().getClassNode(className);
			for (MethodNode methodNode : classNode.methods)
			{
				if (methodNode.name.equals(methodName) && methodNode.desc.equals(descriptor)) return true;
			}
			return false;
		}
		catch (ClassNotFoundException ignored)
		{
			// Class isn't present, skip this mixin.
			return false;
		}
		catch (Exception e)
		{
			// Something else went wrong which might be more serious.
			e.printStackTrace();
			return false;
		}
	}
	*///?}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{
	}
}
