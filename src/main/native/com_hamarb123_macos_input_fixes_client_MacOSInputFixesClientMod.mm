#include <memory>
#include <functional>
#include <iostream>
#include <list>
#include <tuple>
#include <cmath>

#include <jni.h>
#include "com_hamarb123_macos_input_fixes_client_MacOSInputFixesClientMod.h"

#import <Cocoa/Cocoa.h>

//global fields that represent the library state
bool added = false;
jobject _scrollCallback = NULL;
jlong _window = NULL;
jmethodID _ScrollCallbackAccept = NULL;
JavaVM* jvm = NULL;
double trackpadSensitivity = 20.0;
bool momentumScrolling = false;
bool interfaceSmoothScroll = false;
jobject _keyCallback = NULL;
jmethodID _KeyCallbackAccept = NULL;

//gets a jenv from the currently cached jvm
JNIEnv* get_jenv()
{
	JNIEnv *env;
	jint rs = jvm->AttachCurrentThread((void**)&env, NULL);
	assert(rs == JNI_OK);
	return env;
}

//sign function like in c#
double sgn(double x)
{
	if (x == 0.0) return 0.0;
	else if (x > 0.0) return 1.0;
	else if (x < 0.0) return -1.0;
	else return NAN;
}

double scrollX = 0.0;
double scrollY = 0.0;

void processScroll(NSEvent* event, double& x, double& y, double& xWithMomentum, double& yWithMomentum, double& ungroupedX, double& ungroupedY)
{
	//if shift is down, macos adds vertical axis to horizontal axis and sets vertical to 0 for 'legacy scroll events' (indicated by NSEventPhaseNone)
	if ((event.modifierFlags & NSEventModifierFlagShift) != 0 && event.phase == NSEventPhaseNone)
	{
		//note: this conversion is slightly incorrect for horizontal scrolling on legacy input devices when shift is down (doesn't affect the trackpad),
		//using (x, y) = (y, x) doesn't fix it, so we do the below instead since it would work if someone is running an app that converts horizontal scrolls to vertical always
		//(which is currently illegal for speedrunning, and no it's not a good solution to the original bug)
		y += x;
		x = 0.0;
		ungroupedX = x;
		ungroupedY = y;
	}

	if (event.hasPreciseScrollingDeltas)
	{
		//calculate ungrouped scroll
		if (trackpadSensitivity != 0.0)
		{
			ungroupedX = x / trackpadSensitivity;
			ungroupedY = y / trackpadSensitivity;
		}

		//if it is a non-legacy scrolling event (meaning it has a beginning and an end), and it is on the trackpad or other high precision scroll,
		//then we don't want to interpret it as a million scroll events for a very small movement.
		if (event.phase != NSEventPhaseNone)
		{
			if (trackpadSensitivity == 0.0)
			{
				//if the user sets it to 0.0, disable custom trackpad handling
			}
			else if (event.phase == NSEventPhaseBegan)
			{
				//reset scroll counter, and ensure that the first event generates a scroll
				scrollX = sgn(x) * std::max(std::abs(x) - 1, 0.0);
				scrollY = sgn(y) * std::max(std::abs(y) - 1, 0.0);
				x = sgn(x);
				y = sgn(y);
			}
			else
			{
				//group scrolls together up to a magnitude of the trackpadSensitivity
				scrollX += x;
				scrollY += y;
				x = 0.0;
				y = 0.0;
				if (std::abs(scrollX) >= trackpadSensitivity)
				{
					x = sgn(scrollX) * (int)(std::abs(scrollX) / trackpadSensitivity);
					scrollX = sgn(scrollX) * (std::fmod(std::abs(scrollX), trackpadSensitivity));
					scrollY = 0; //reset y partial scroll since it was probably not intended
				}
				if (std::abs(scrollY) >= trackpadSensitivity)
				{
					y = sgn(scrollY) * (int)(std::abs(scrollY) / trackpadSensitivity);
					scrollY = sgn(scrollY) * (std::fmod(std::abs(scrollY), trackpadSensitivity));
					scrollX = 0; //reset x partial scroll since it was probably not intended
				}
			}
		}
	}

	//if there isn't precise deltas (ie. mouse), macos often causes sketchy smooth scrolling
	else
	{
		x = sgn(x);
		y = sgn(y);
		if (!interfaceSmoothScroll)
		{
			ungroupedX = x;
			ungroupedY = y;
		}
	}

	//copy x and y to the ones with momentum, before removing it
	xWithMomentum = x;
	yWithMomentum = y;

	//check that it wasn't caused by momentum
	if (!momentumScrolling && event.momentumPhase != NSEventPhaseNone)
	{
		x = 0.0;
		y = 0.0;
	}
}

void handleScroll(NSEvent* event)
{
	//check that we have a function to actually call
	if (_scrollCallback != NULL)
	{
		//extract the x and y
		double x = event.scrollingDeltaX;
		double y = event.scrollingDeltaY;

		//grouped variables that include momentum scrolls
		double xWithMomentum = x;
		double yWithMomentum = y;

		//variables that don't group x and y scrolls
		double ungroupedX = x;
		double ungroupedY = y;

		//modify x and y based on information available from the event
		processScroll(event, x, y, xWithMomentum, yWithMomentum, ungroupedX, ungroupedY);

		//send the event to java
		if (x != 0 || y != 0 || xWithMomentum != 0 || yWithMomentum != 0 || ungroupedX != 0 || ungroupedY != 0)
		{
			JNIEnv* jenv = get_jenv();
			jenv->CallVoidMethod(_scrollCallback, _ScrollCallbackAccept, x, y, xWithMomentum, yWithMomentum, ungroupedX, ungroupedY);
		}
	}
}

void handleKey(NSEvent* event)
{
	//check that we have a function to actually call
	if (_keyCallback != NULL)
	{
		//check if we have tab or escape
		unsigned short scancode = event.keyCode;
		if (scancode == 0x30 /*kVK_Tab*/ || scancode == 0x35 /*kVK_Escape*/)
		{
			//convert the key to a glfw key (see org.lwjgl.glfw.GLFW class for values)
			int key;
			if (scancode == 0x30 /*kVK_Tab*/) key = 258 /*GLFW_KEY_TAB*/;
			else if (scancode == 0x35 /*kVK_Escape*/) key = 256 /*GLFW_KEY_ESCAPE*/;
			else return;

			//determine the action
			int action;
			NSEventType eventType = event.type;
			if (eventType == NSEventTypeKeyDown)
			{
				if (event.ARepeat) action = 2 /*GLFW.GLFW_REPEAT*/;
				else action = 1 /*GLFW.GLFW_PRESS*/;
			}
			else if (eventType == NSEventTypeKeyUp) action = 0 /*GLFW.GLFW_RELEASE*/;
			else return;

			//determine the modifiers
			int modifiers = 0;
			NSEventModifierFlags modifierFlags = event.modifierFlags;
			if (modifierFlags & NSEventModifierFlagShift) modifiers |= 0x01 /*GLFW_MOD_SHIFT*/;
			if (modifierFlags & NSEventModifierFlagControl) modifiers |= 0x02 /*GLFW_MOD_CONTROL*/;
			if (modifierFlags & NSEventModifierFlagOption) modifiers |= 0x04 /*GLFW_MOD_ALT*/;
			if (modifierFlags & NSEventModifierFlagCommand) modifiers |= 0x08 /*GLFW_MOD_SUPER*/;
			if (modifierFlags & NSEventModifierFlagCapsLock) modifiers |= 0x10 /*GLFW_MOD_CAPS_LOCK*/;
			//GLFW_MOD_NUM_LOCK - not sent by glfw

			//send the event to java
			JNIEnv* jenv = get_jenv();
			jenv->CallVoidMethod(_keyCallback, _KeyCallbackAccept, key, (int)scancode, action, modifiers);
		}
	}
}

template<typename T>
void UpdateGlobalRef(JNIEnv* old_jenv, JNIEnv* new_jenv, T& storage, T value)
{
	//we need to make a global ref of these java objects since otherwise they become invalid at the end of the function call
	//we also need to free the last value if it exists to allow the java GC to collect it
	if (storage != NULL) old_jenv->DeleteGlobalRef(storage);
	storage = value == NULL ? NULL : (T)new_jenv->NewGlobalRef(value);
}

/*
 * Class:     com_hamarb123_macos_input_fixes_client_MacOSInputFixesClientMod
 * Method:    registerCallbacks
 * Signature: (Lcom/hamarb123/macos_input_fixes/client/ScrollCallback;Lcom/hamarb123/macos_input_fixes/client/KeyCallback;J)V
 */
JNIEXPORT void JNICALL Java_com_hamarb123_macos_1input_1fixes_client_MacOSInputFixesClientMod_registerCallbacks
  (JNIEnv* jenv, jclass, jobject scrollCallback, jobject keyCallback, jlong window)
{
	//this a function that is called from java
	//we only store 1 state at once, if the function is called more than once then we replace old state

	JNIEnv* oldJenv;

	//get the old jenv for cleaning purposes
	if (jvm != NULL)
	{
		oldJenv = get_jenv();
	}
	else
	{
		oldJenv = jenv;
	}

	//cache relevant java classes and methods
	UpdateGlobalRef(oldJenv, jenv, _scrollCallback, scrollCallback);
	_ScrollCallbackAccept = jenv->GetMethodID(jenv->FindClass("com/hamarb123/macos_input_fixes/client/ScrollCallback"), "accept", "(DDDDDD)V");
	UpdateGlobalRef(oldJenv, jenv, _keyCallback, keyCallback);
	_KeyCallbackAccept = jenv->GetMethodID(jenv->FindClass("com/hamarb123/macos_input_fixes/client/KeyCallback"), "accept", "(IIII)V");

	//store the cocoa window id
	_window = window;

	//store the new jvm so we can use it to call the callback
	jint rs = jenv->GetJavaVM(&jvm);
	assert(rs == JNI_OK);

	//add the cocoa callback for the scroll event if we have not already done so (only done once since it only supports 1 state anyway)
	if (!added)
	{
		added = true;
		[NSEvent addLocalMonitorForEventsMatchingMask: NSEventMaskScrollWheel handler: ^NSEvent *(NSEvent *event)
		{
			//check it is the relevant window and the call our handle function
			if (event.window == (__bridge void*)_window)
			{
				handleScroll(event);
			}
			return event;
		}];
		[NSEvent addLocalMonitorForEventsMatchingMask: (NSEventMaskKeyDown | NSEventMaskKeyUp) handler: ^NSEvent *(NSEvent *event)
		{
			//check it is the relevant window and the call our handle function
			if (event.window == (__bridge void*)_window)
			{
				handleKey(event);
			}
			return event;
		}];
	}
}

/*
 * Class:     com_hamarb123_macos_input_fixes_client_MacOSInputFixesClientMod
 * Method:    setTrackpadSensitivity
 * Signature: (D)V
 */
JNIEXPORT void JNICALL Java_com_hamarb123_macos_1input_1fixes_client_MacOSInputFixesClientMod_setTrackpadSensitivity
  (JNIEnv *, jclass, jdouble value)
{
	//this a function that is called from java
	//it updates the trackpad sensitivity option
	trackpadSensitivity = value;
	scrollX = 0.0;
	scrollY = 0.0;
}

/*
 * Class:     com_hamarb123_macos_input_fixes_client_MacOSInputFixesClientMod
 * Method:    setMomentumScrolling
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_hamarb123_macos_1input_1fixes_client_MacOSInputFixesClientMod_setMomentumScrolling
  (JNIEnv *, jclass, jboolean value)
{
	//this a function that is called from java
	//it updates the momentum scrolling option
	momentumScrolling = value != JNI_FALSE;
	scrollX = 0.0;
	scrollY = 0.0;
}

/*
 * Class:     com_hamarb123_macos_input_fixes_client_MacOSInputFixesClientMod
 * Method:    setInterfaceSmoothScroll
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_hamarb123_macos_1input_1fixes_client_MacOSInputFixesClientMod_setInterfaceSmoothScroll
  (JNIEnv *, jclass, jboolean value)
{
	//this a function that is called from java
	//it updates the momentum scrolling option
	interfaceSmoothScroll = value != JNI_FALSE;
}
