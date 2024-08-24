package com.hamarb123.macos_input_fixes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

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
		//load certain mixins at runtime depending on whether certain classes are available
		List<String> li = new ArrayList<String>();
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
		if (hasOptionClass)
		{
			li.add("gui.MouseOptionsScreenMixin2");
			if (hasCyclingButtonWidgetClass)
			{
				li.add("gui.CyclingOptionMixin4");
			}
		}
		else
		{
			li.add("gui.SimpleOptionMixin1");
			if (hasMouseOptionsScreen_init)
			{
				li.add("gui.MouseOptionsScreenMixin9");
				li.add("gui.OptionListWidgetMixin9");
			}
			else
			{
				li.add("gui.OptionListWidgetMixin8");
			}
		}
		if (hasCyclingButtonWidgetClass)
		{
			li.add("gui.CyclingButtonWidgetBuilderMixin3");
		}
		if (hasGameOptionsScreen_getHoveredButtonTooltip)
		{
			li.add("gui.MouseOptionsScreenMixin5");
		}
		if (!hasGameOptionsScreen_getHoveredButtonTooltip && hasScreen_renderTooltip && hasOptionClass)
		{
			li.add("gui.MouseOptionsScreenMixin6");
		}
		if (hasGameOptionsScreen_getHoveredButtonTooltip || (hasScreen_renderTooltip && hasOptionClass))
		{
			li.add("gui.ScreenAccessor7");
		}
		if (hasPlayerInventory_scrollInHotbar)
		{
			li.add("PlayerInventoryMixin10");
		}
		else
		{
			li.add("MouseMixin11");
		}
		return li;
	}

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

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{
	}
}
