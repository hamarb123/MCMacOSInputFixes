#include <memory>
#include <functional>
#include <iostream>
#include <list>
#include <tuple>
#include <cmath>

#include <jni.h>
#include "com_hamarb123_macos_input_fixes_MacOSInputFixesMod.h"

#import <Cocoa/Cocoa.h>

//global fields that represent the library state
bool added = false;
jobject _scrollCallback = NULL;
jlong _window = NULL;
jmethodID _BiConsumerAccept = NULL;
jclass _Double = NULL;
jmethodID _DoubleCtor = NULL;
jclass _Integer = NULL;
jmethodID _IntegerCtor = NULL;
jclass _Boolean = NULL;
jmethodID _BooleanCtor = NULL;
JavaVM* jvm = NULL;

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

void processScroll(NSEvent* event, double& x, double& y)
{
	//check that it wasn't caused by momentum
	if (event.momentumPhase != NSEventPhaseNone)
	{
		x = 0.0;
		y = 0.0;
		return;
	}

	//if shift is down, macos adds vertical axis to horizontal axis and sets vertical to 0 for 'legacy scroll events' (indicated by NSEventPhaseNone)
	if ((event.modifierFlags & NSEventModifierFlagShift) != 0 && event.phase == NSEventPhaseNone)
	{
		//note: this conversion is slightly incorrect for horizontal scrolling on legacy input devices when shift is down (doesn't affect the trackpad),
		//using (x, y) = (y, x) doesn't fix it, so we do the below instead since it would work if someone is running an app that converts horizontal scrolls to vertical always
		//(which is currently illegal for speedrunning, and no it's not a good solution to the original bug)
		y += x;
		x = 0.0;
	}

	//enable the following block if you want to ensure scrolling direction is the same regardless of user preferences:
	/*
	if (!event.directionInvertedFromDevice)
	{
		x = -x;
		y = -y;
	}
	*/

	//if it is a non-legacy scrolling event (meaning it has a beginning and an end), and it is on the trackpad or other high precision scroll,
	//then we don't want to interpret it as a million scroll events for a very small movement.
	if (event.phase != NSEventPhaseNone && event.hasPreciseScrollingDeltas)
	{
		const double sensitivity = 20.0;
		if (event.phase == NSEventPhaseBegan)
		{
			//reset scroll counter, and ensure that the first event generates a scroll
			scrollX = sgn(x) * std::max(std::abs(x) - 1, 0.0);
			scrollY = sgn(y) * std::max(std::abs(y) - 1, 0.0);
			x = sgn(x);
			y = sgn(y);
		}
		else
		{
			//group scrolls together up to a magnitude of the sensitivity
			scrollX += x;
			scrollY += y;
			x = 0.0;
			y = 0.0;
			if (std::abs(scrollX) >= sensitivity)
			{
				x = sgn(scrollX) * (int)(std::abs(scrollX) / sensitivity);
				scrollX = sgn(scrollX) * (std::fmod(std::abs(scrollX), sensitivity));
			}
			if (std::abs(scrollY) >= sensitivity)
			{
				y = sgn(scrollY) * (int)(std::abs(scrollY) / sensitivity);
				scrollY = sgn(scrollY) * (std::fmod(std::abs(scrollY), sensitivity));
			}
		}
	}

	//if there isn't precise deltas (ie. mouse), macos often causes smooth scrolling anyway, this effectively disables it
	if (!event.hasPreciseScrollingDeltas)
	{
		x = sgn(x);
		y = sgn(y);
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

		//modify x and y based on information available from the event
		processScroll(event, x, y);

		//send the event to java
		if (x != 0 || y != 0)
		{
			JNIEnv* jenv = get_jenv();
			jobject _x = jenv->NewObject(_Double, _DoubleCtor, (jdouble)x);
			jobject _y = jenv->NewObject(_Double, _DoubleCtor, (jdouble)y);
			jenv->CallVoidMethod(_scrollCallback, _BiConsumerAccept, _x, _y);
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
 * Class:     com_hamarb123_macos_input_fixes_MacOSInputFixesMod
 * Method:    registerCallbacks
 * Signature: (Ljava/util/function/BiConsumer;J)V
 */
JNIEXPORT void JNICALL Java_com_hamarb123_macos_1input_1fixes_MacOSInputFixesMod_registerCallbacks
  (JNIEnv* jenv, jclass, jobject scrollCallback, jlong window)
{
	//this is the function that is called from java
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
	_BiConsumerAccept = jenv->GetMethodID(jenv->FindClass("java/util/function/BiConsumer"), "accept", "(Ljava/lang/Object;Ljava/lang/Object;)V");
	UpdateGlobalRef(oldJenv, jenv, _Double, jenv->FindClass("java/lang/Double"));
	_DoubleCtor = jenv->GetMethodID(_Double, "<init>", "(D)V");
	UpdateGlobalRef(oldJenv, jenv, _Integer, jenv->FindClass("java/lang/Integer"));
	_IntegerCtor = jenv->GetMethodID(_Integer, "<init>", "(I)V");
	UpdateGlobalRef(oldJenv, jenv, _Boolean, jenv->FindClass("java/lang/Boolean"));
	_BooleanCtor = jenv->GetMethodID(_Boolean, "<init>", "(Z)V");

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
	}
}
