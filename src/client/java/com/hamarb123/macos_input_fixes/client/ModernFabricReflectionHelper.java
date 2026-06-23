package com.hamarb123.macos_input_fixes.client;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;

public class ModernFabricReflectionHelper
{
	public final static class ClassImpl<T>
	{
		private Class<T> clazz;
		private Exception exception;
		private final String className;
		private final String expectedVersions;

		public ClassImpl(String expectedVersions, String className)
		{
			this.expectedVersions = expectedVersions;
			this.className = className;
		}

		public ClassImpl(Class<T> clazz)
		{
			this.clazz = clazz;
			this.className = null;
			this.expectedVersions = null;
		}

		public static ClassImpl<?> of(String expectedVersions, String className)
		{
			return new ClassImpl<>(expectedVersions, className);
		}

		public static <T> ClassImpl<T> of(Class<T> clazz)
		{
			return new ClassImpl<>(clazz);
		}

		@SuppressWarnings("unchecked")
		public <TTo> ClassImpl<TTo> into()
		{
			return (ClassImpl<TTo>)this;
		}

		@SuppressWarnings("unchecked")
		public Class<T> tryGet()
		{
			if (this.clazz != null) return this.clazz;
			if (this.exception != null) return null;

			try
			{
				this.clazz = (Class<T>)Class.forName(this.className);
				return this.clazz;
			}
			catch (Exception e)
			{
				this.exception = e;
				return null;
			}
		}

		public Class<T> get()
		{
			Class<T> clazz = this.tryGet();
			if (clazz != null) return clazz;
			else throw new RuntimeException("Failed to find class '" + this.className + "', which was expected on minecraft versions " + this.expectedVersions + ".", this.exception);
		}

		public boolean isPresent()
		{
			return this.tryGet() != null;
		}

		public TypeImpl<T> asType()
		{
			return new TypeImpl<T>(this);
		}
	}

	public final static class TypeImpl<T>
	{
		private final ClassImpl<?> classImpl;
		private Class<T> type;
		private final int arrayCount;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static final TypeImpl<Byte> BYTE = new TypeImpl(byte.class);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static final TypeImpl<Character> CHAR = new TypeImpl(char.class);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static final TypeImpl<Double> DOUBLE = new TypeImpl(double.class);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static final TypeImpl<Float> FLOAT = new TypeImpl(float.class);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static final TypeImpl<Integer> INT = new TypeImpl(int.class);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static final TypeImpl<Long> LONG = new TypeImpl(long.class);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static final TypeImpl<Short> SHORT = new TypeImpl(short.class);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static final TypeImpl<Boolean> BOOLEAN = new TypeImpl(boolean.class);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static final TypeImpl<Void> VOID = new TypeImpl(void.class);

		public TypeImpl(ClassImpl<T> classType)
		{
			this.classImpl = classType;
			this.type = null;
			this.arrayCount = 0;
		}

		public TypeImpl(Class<T> type)
		{
			this.classImpl = null;
			this.type = type;
			this.arrayCount = 0;
		}

		@SuppressWarnings("unchecked")
		private TypeImpl(ClassImpl<?> classImpl, int arrayCount)
		{
			this.classImpl = (ClassImpl<T>)classImpl;
			this.type = null;
			this.arrayCount = arrayCount;
		}

		@SuppressWarnings("unchecked")
		public TypeImpl<T[]> makeArrayType()
		{
			if (type != null) return (TypeImpl<T[]>)new TypeImpl<>(type.arrayType());
			else return new TypeImpl<T[]>(classImpl, arrayCount + 1);
		}

		@SuppressWarnings("unchecked")
		public Class<T> tryGet()
		{
			if (type != null) return type;
			Class<?> clazz = this.classImpl.tryGet();
			if (clazz != null)
			{
				Class<?> arrayClass = clazz;
				for (int i = 0; i < this.arrayCount; i++) arrayClass = arrayClass.arrayType();
				type = (Class<T>)arrayClass;
				return type;
			}
			else return null;
		}

		public Class<?> get()
		{
			if (type != null) return type;
			if (classImpl != null) return classImpl.get();
			return tryGet();
		}

		public boolean isPresent()
		{
			return this.tryGet() != null;
		}

		@SuppressWarnings("unchecked")
		public <TTo> TypeImpl<TTo> into()
		{
			return (TypeImpl<TTo>)this;
		}
	}

	public final static class FieldImpl<TContainer, TField>
	{
		private final ClassImpl<TContainer> containingType;
		private final TypeImpl<TField> fieldType;
		private final String fieldName;
		private Exception exception;
		private final boolean isStatic, isPrivate;
		private Field field;
		private final String expectedVersions;

		public FieldImpl(String expectedVersions, ClassImpl<TContainer> containingClass, TypeImpl<TField> fieldType, String fieldName, boolean isStatic, boolean isPrivate)
		{
			this.expectedVersions = expectedVersions;
			this.containingType = containingClass;
			this.fieldType = fieldType;
			this.fieldName = fieldName;
			this.isStatic = isStatic;
			this.isPrivate = isPrivate;
		}

		public FieldImpl(String expectedVersions, ClassImpl<TContainer> containingClass, ClassImpl<TField> fieldType, String fieldName, boolean isStatic, boolean isPrivate)
		{
			this.expectedVersions = expectedVersions;
			this.containingType = containingClass;
			this.fieldType = new TypeImpl<TField>(fieldType);
			this.fieldName = fieldName;
			this.isStatic = isStatic;
			this.isPrivate = isPrivate;
		}

		public FieldImpl(String expectedVersions, Class<TContainer> containingClass, TypeImpl<TField> fieldType, String fieldName, boolean isStatic, boolean isPrivate)
		{
			this.expectedVersions = expectedVersions;
			this.containingType = new ClassImpl<TContainer>(containingClass);
			this.fieldType = fieldType;
			this.fieldName = fieldName;
			this.isStatic = isStatic;
			this.isPrivate = isPrivate;
		}

		public FieldImpl(String expectedVersions, Class<TContainer> containingClass, ClassImpl<TField> fieldType, String fieldName, boolean isStatic, boolean isPrivate)
		{
			this.expectedVersions = expectedVersions;
			this.containingType = new ClassImpl<TContainer>(containingClass);
			this.fieldType = new TypeImpl<TField>(fieldType);
			this.fieldName = fieldName;
			this.isStatic = isStatic;
			this.isPrivate = isPrivate;
		}

		public Field tryGet()
		{
			if (field != null) return field;
			if (exception != null) return null;

			Class<?> containingClass, fieldTypeClass;
			try
			{
				containingClass = containingType.get();
				fieldTypeClass = fieldType.get();
			}
			catch (Exception e)
			{
				this.exception = e;
				return null;
			}

			Field[] fields = isPrivate ? containingClass.getDeclaredFields() : containingClass.getFields();
			Field foundField = null;
			for (Field f : fields)
			{
				if (f.getName().equals(fieldName))
				{
					if (Modifier.isStatic(f.getModifiers()) == isStatic)
					{
						if (f.getType().equals(fieldTypeClass))
						{
							f.setAccessible(true);
							foundField = f;
							break;
						}
					}
				}
			}

			if (foundField != null)
			{
				field = foundField;
				return foundField;
			}
			else
			{
				exception = new NoSuchFieldException();
				return null;
			}
		}

		public Field get()
		{
			Field f = this.tryGet();
			if (f != null) return f;
			else throw new RuntimeException("Failed to find field '" + fieldName + "' of type '" + fieldType.get() + "' in class '" + containingType.get() + "', expected in versions: " + expectedVersions + ".", exception);
		}

		public boolean isPresent()
		{
			return this.tryGet() != null;
		}

		@SuppressWarnings("unchecked")
		public TField getValue(TContainer instance)
		{
			Field f = this.get();
			try
			{
				return (TField)f.get(instance);
			}
			catch (IllegalAccessException e)
			{
				throw new RuntimeException("Failed to access field '" + fieldName + "' of type '" + fieldType.get() + "' in class '" + containingType.get() + "'.", e);
			}
		}

		public void setValue(TContainer instance, TField value)
		{
			Field f = this.get();
			try
			{
				f.set(instance, value);
			}
			catch (IllegalAccessException e)
			{
				throw new RuntimeException("Failed to access field '" + fieldName + "' of type '" + fieldType.get() + "' in class '" + containingType.get() + "'.", e);
			}
		}
	}

	public final static class MethodImpl<T>
	{
		private final ClassImpl<?> containingType;
		private final TypeImpl<?> returnType;
		private final TypeImpl<?>[] parameterTypes;
		private final String methodName;
		private Exception exception;
		private final boolean isStatic, isPrivate, isConstructor;
		private MethodHandle methodHandle;
		private final String expectedVersions;

		public MethodImpl(String expectedVersions, ClassImpl<?> containingClass, String methodName, boolean isStatic, boolean isPrivate, TypeImpl<T> returnType, TypeImpl<?>... parameterTypes)
		{
			this.expectedVersions = expectedVersions;
			this.containingType = containingClass;
			this.returnType = returnType;
			this.parameterTypes = parameterTypes;
			this.methodName = methodName;
			this.isStatic = isStatic;
			this.isPrivate = isPrivate;
			this.isConstructor = "<init>".equals(methodName);
		}

		public static <T> MethodImpl<T> forMethod(String expectedVersions, ClassImpl<?> containingClass, String methodName, boolean isStatic, boolean isPrivate, TypeImpl<T> returnType, TypeImpl<?>... parameterTypes)
		{
			return new MethodImpl<T>(expectedVersions, containingClass, methodName, isStatic, isPrivate, returnType, parameterTypes);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static <T> MethodImpl<T> forConstructor(String expectedVersions, ClassImpl<T> containingClass, boolean isPrivate, TypeImpl<?>... parameterTypes)
		{
			return new MethodImpl<T>(expectedVersions, containingClass, "<init>", false, isPrivate, new TypeImpl(void.class), parameterTypes);
		}

		public MethodHandle tryGet()
		{
			if (methodHandle != null) return methodHandle;
			if (exception != null) return null;

			Class<?> containingClass, returnTypeClass;
			Class<?>[] parameterTypeClasses = new Class<?>[parameterTypes.length];
			try
			{
				containingClass = containingType.get();
				returnTypeClass = returnType.get();
				for (int i = 0; i < parameterTypes.length; i++)
				{
					parameterTypeClasses[i] = parameterTypes[i].get();
				}
			}
			catch (Exception e)
			{
				this.exception = e;
				return null;
			}

			if (isConstructor)
			{
				try
				{
					MethodType methodType = MethodType.methodType(returnTypeClass, parameterTypeClasses);
					Lookup lookup = isPrivate ? MethodHandles.lookup() : MethodHandles.publicLookup();
					methodHandle = lookup.findConstructor(containingClass, methodType);
					return methodHandle;
				}
				catch (Exception e)
				{
					this.exception = e;
					return null;
				}
			}
			else
			{
				try
				{
					if (!isPrivate)
					{
						MethodType methodType = MethodType.methodType(returnTypeClass, parameterTypeClasses);
						Lookup lookup = MethodHandles.publicLookup();
						if (isStatic) methodHandle = lookup.findStatic(containingClass, methodName, methodType);
						else methodHandle = lookup.findVirtual(containingClass, methodName, methodType);
					}
					else
					{
						Method m = containingClass.getDeclaredMethod(methodName, parameterTypeClasses);
						if (!m.getReturnType().equals(returnTypeClass)) throw new RuntimeException("Found method, but with incorrect return type.");
						if (Modifier.isStatic(m.getModifiers()) != isStatic) throw new RuntimeException("Found method, but with incorrect staticity.");
						m.setAccessible(true);
						methodHandle = MethodHandles.lookup().unreflect(m);
					}
					return methodHandle;
				}
				catch (Exception e)
				{
					this.exception = e;
					return null;
				}
			}
		}

		public MethodHandle get()
		{
			MethodHandle mh = this.tryGet();
			if (mh != null) return mh;
			else throw new RuntimeException("Failed to find method '" + methodName + "' with return type '" + returnType.get() + "' and parameter types " + java.util.Arrays.toString(parameterTypes) + " in class '" + containingType.get() + "', expected on versions: " + expectedVersions + ".", exception);
		}

		public boolean isPresent()
		{
			return this.tryGet() != null;
		}

		@SuppressWarnings("unchecked")
		public T invoke(Object... args)
		{
			MethodHandle mh = this.get();
			try
			{
				return (T)mh.invokeWithArguments(args);
			}
			catch (Throwable e)
			{
				throw new RuntimeException("Exception while invoking method '" + methodName + "'.", e);
			}
		}
	}

	public final static class LambdaConverterImpl<TFrom, TTo>
	{
		private ClassImpl<TFrom> from;
		private ClassImpl<TTo> to;
		private TypeImpl<?> returnTypeFrom, returnTypeTo;
		private TypeImpl<?>[] parameterTypesFrom, parameterTypesTo;
		private String fromMethodName, toMethodName;
		private Exception exception;
		private MethodHandle methodHandle;

		public LambdaConverterImpl(ClassImpl<TFrom> from, String fromMethodName, TypeImpl<?> returnTypeFrom, TypeImpl<?>[] parameterTypesFrom, ClassImpl<TTo> to, String toMethodName, TypeImpl<?> returnTypeTo, TypeImpl<?>[] parameterTypesTo)
		{
			this.from = from;
			this.fromMethodName = fromMethodName;
			this.returnTypeFrom = returnTypeFrom;
			this.parameterTypesFrom = parameterTypesFrom;
			this.to = to;
			this.toMethodName = toMethodName;
			this.returnTypeTo = returnTypeTo;
			this.parameterTypesTo = parameterTypesTo;
		}

		public MethodHandle tryGet()
		{
			if (methodHandle != null) return methodHandle;
			if (exception != null) return null;

			Class<?> fromClass, toClass, returnTypeFromClass, returnTypeToClass;
			Class<?>[] parameterTypesFromClass = new Class<?>[parameterTypesFrom.length];
			Class<?>[] parameterTypesToClass = new Class<?>[parameterTypesTo.length];
			try
			{
				fromClass = from.get();
				toClass = to.get();
				returnTypeFromClass = returnTypeFrom.get();
				returnTypeToClass = returnTypeTo.get();
				for (int i = 0; i < parameterTypesFrom.length; i++)
				{
					parameterTypesFromClass[i] = parameterTypesFrom[i].get();
				}
				for (int i = 0; i < parameterTypesTo.length; i++)
				{
					parameterTypesToClass[i] = parameterTypesTo[i].get();
				}
			}
			catch (Exception e)
			{
				this.exception = e;
				return null;
			}

			MethodType methodTypeFrom = MethodType.methodType(returnTypeFromClass, parameterTypesFromClass);
			MethodType methodTypeTo = MethodType.methodType(returnTypeToClass, parameterTypesToClass);
			Lookup lookup = MethodHandles.lookup();
			try
			{
				methodHandle = LambdaMetafactory.metafactory
				(
					lookup,
					toMethodName,
					MethodType.methodType(toClass, fromClass),
					methodTypeTo,
					lookup.findVirtual(fromClass, fromMethodName, methodTypeFrom),
					methodTypeTo
				).getTarget();
			}
			catch (Exception e)
			{
				this.exception = e;
				return null;
			}

			return methodHandle;
		}

		public MethodHandle get()
		{
			MethodHandle mh = this.tryGet();
			if (mh != null) return mh;
			else throw new RuntimeException("Failed to create lambda converter from '" + fromMethodName + "' in '" + from.get() + "' to '" + toMethodName + "' in '" + to.get() + "'.", exception);
		}

		public boolean isPresent()
		{
			return this.tryGet() != null;
		}

		public TTo convert(TFrom instance)
		{
			MethodHandle mh = this.get();
			try
			{
				return (TTo)mh.invoke(instance);
			}
			catch (Throwable e)
			{
				throw new RuntimeException("Exception while converting lambda.", e);
			}
		}

		@SuppressWarnings("unchecked")
		public <TNewFrom, TNewTo> LambdaConverterImpl<TNewFrom, TNewTo> into(ClassImpl<TNewFrom> newFrom, String newFromMethodName, TypeImpl<?> newReturnTypeFrom, TypeImpl<?>[] newParameterTypesFrom, ClassImpl<TNewTo> newTo, String newToMethodName, TypeImpl<?> newReturnTypeTo, TypeImpl<?>[] newParameterTypesTo)
		{
			return (LambdaConverterImpl<TNewFrom, TNewTo>)this;
		}
	}


	// System classes:
	public static final ClassImpl<Consumer<?>> CLASS_Consumer = ClassImpl.of(Consumer.class).into();
	public static final ClassImpl<Object> CLASS_Object = ClassImpl.of(Object.class);
	public static final ClassImpl<String> CLASS_String = ClassImpl.of(String.class);

	// Minecraft classes (not by reflection):
	public static final ClassImpl<Gui> CLASS_Gui = ClassImpl.of(Gui.class);
	public static final ClassImpl<Minecraft> CLASS_Minecraft = ClassImpl.of(Minecraft.class);
	public static final ClassImpl<OptionInstance<?>> CLASS_OptionInstance = ClassImpl.of(OptionInstance.class).into();
	public static final ClassImpl<OptionInstance.CaptionBasedToString<?>> CLASS_OptionInstance_CaptionBasedToString = ClassImpl.of(OptionInstance.CaptionBasedToString.class).into();
	public static final ClassImpl<OptionInstance.TooltipSupplier<?>> CLASS_OptionInstance_TooltipSupplier = ClassImpl.of(OptionInstance.TooltipSupplier.class).into();
	public static final ClassImpl<Overlay> CLASS_Overlay = ClassImpl.of(Overlay.class);
	public static final ClassImpl<Screen> CLASS_Screen = ClassImpl.of(Screen.class);
	public static final ClassImpl<SpectatorGui> CLASS_SpectatorGui = ClassImpl.of(SpectatorGui.class);

	// Minecraft classes (by reflection):
	public static final ClassImpl<?> CLASS_Hud = ClassImpl.of("26.2+", "net.minecraft.client.gui.Hud");
	public static final ClassImpl<?> CLASS_OptionInstance_ValueSet = ClassImpl.of("26.1+", "net.minecraft.client.OptionInstance$ValueSet");
	public static final ClassImpl<?> CLASS_OptionInstance_ValueUpdateListener = ClassImpl.of("26.2+", "net.minecraft.client.OptionInstance$ValueUpdateListener");

	// Minecraft constructors / methods (by reflection):
	public static final MethodImpl<Overlay> METHOD_Gui_overlay = MethodImpl.forMethod("26.2+", CLASS_Gui, "overlay", false, false, CLASS_Overlay.asType().into());
	public static final MethodImpl<Screen> METHOD_Gui_screen = MethodImpl.forMethod("26.2+", CLASS_Gui, "screen", false, false, CLASS_Screen.asType().into());
	public static final MethodImpl<SpectatorGui> METHOD_Gui_getSpectatorGui = MethodImpl.forMethod("26.1.x", CLASS_Gui, "getSpectatorGui", false, false, CLASS_SpectatorGui.asType().into());
	public static final MethodImpl<SpectatorGui> METHOD_Hud_getSpectatorGui = MethodImpl.forMethod("26.2+", CLASS_Hud, "getSpectatorGui", false, false, CLASS_SpectatorGui.asType().into());
	public static final MethodImpl<Overlay> METHOD_Minecraft_getOverlay = MethodImpl.forMethod("26.1.x", CLASS_Minecraft, "getOverlay", false, false, CLASS_Overlay.asType().into());
	public static final MethodImpl<OptionInstance<Boolean>> METHOD_OptionInstance_createBoolean_1 = MethodImpl.forMethod("26.1.x", CLASS_OptionInstance, "createBoolean", true, false, CLASS_OptionInstance.asType().into(), CLASS_String.asType(), CLASS_OptionInstance_TooltipSupplier.asType(), CLASS_OptionInstance_CaptionBasedToString.asType(), TypeImpl.BOOLEAN, CLASS_Consumer.asType());
	public static final MethodImpl<OptionInstance<Boolean>> METHOD_OptionInstance_createBoolean_2 = MethodImpl.forMethod("26.2+", CLASS_OptionInstance, "createBoolean", true, false, CLASS_OptionInstance.asType().into(), CLASS_String.asType(), CLASS_OptionInstance_TooltipSupplier.asType(), CLASS_OptionInstance_CaptionBasedToString.asType(), TypeImpl.BOOLEAN, CLASS_OptionInstance_ValueUpdateListener.asType());
	public static final MethodImpl<OptionInstance<Double>> METHOD_OptionInstance_ctor_1 = MethodImpl.forConstructor("26.1.x", CLASS_OptionInstance.into(), false, CLASS_String.asType(), CLASS_OptionInstance_TooltipSupplier.asType(), CLASS_OptionInstance_CaptionBasedToString.asType(), CLASS_OptionInstance_ValueSet.asType(), CLASS_Object.asType(), CLASS_Consumer.asType());
	public static final MethodImpl<OptionInstance<Double>> METHOD_OptionInstance_ctor_2 = MethodImpl.forConstructor("26.2+", CLASS_OptionInstance.into(), false, CLASS_String.asType(), CLASS_OptionInstance_TooltipSupplier.asType(), CLASS_OptionInstance_CaptionBasedToString.asType(), CLASS_OptionInstance_ValueSet.asType(), CLASS_Object.asType(), CLASS_OptionInstance_ValueUpdateListener.asType());

	// Minecraft fields (by reflection):
	public static final FieldImpl<Gui, ?> FIELD_Gui_hud = new FieldImpl<>("26.2+", CLASS_Gui, CLASS_Hud.asType(), "hud", false, false);
	public static final FieldImpl<Minecraft, Screen> FIELD_Minecraft_screen = new FieldImpl<>("26.1.x", CLASS_Minecraft, CLASS_Screen.asType(), "screen", false, false);

	// Lambda converters:
	public static final LambdaConverterImpl<Consumer<?>, ?> CONVERTER_Consumer_TO_OptionInstance_ValueUpdateListener = new LambdaConverterImpl<>(CLASS_Consumer, "accept", TypeImpl.VOID, new TypeImpl[] { CLASS_Object.asType() }, CLASS_OptionInstance_ValueUpdateListener, "valueChanged", TypeImpl.VOID, new TypeImpl[] { CLASS_Object.asType() });


	// Common helpers:

	public static Overlay getOverlay(Minecraft minecraft)
	{
		if (METHOD_Gui_overlay.isPresent())
		{
			return METHOD_Gui_overlay.invoke(minecraft.gui);
		}
		else
		{
			return METHOD_Minecraft_getOverlay.invoke(minecraft);
		}
	}

	public static Screen getScreen(Minecraft minecraft)
	{
		if (METHOD_Gui_screen.isPresent())
		{
			return METHOD_Gui_screen.invoke(minecraft.gui);
		}
		else
		{
			return FIELD_Minecraft_screen.getValue(minecraft);
		}
	}

	public static SpectatorGui getSpectatorGui(Gui gui)
	{
		if (METHOD_Gui_getSpectatorGui.isPresent())
		{
			return METHOD_Gui_getSpectatorGui.invoke(gui);
		}
		else
		{
			Object hud = FIELD_Gui_hud.getValue(gui);
			return METHOD_Hud_getSpectatorGui.invoke(hud);
		}
	}
}
