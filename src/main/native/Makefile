CC=clang++
JNIINCLUDEFOLDER="jdk8_include"
CFLAGS=-c -Wall -arch x86_64 -arch arm64 -std=c++17 -I${JNIINCLUDEFOLDER} -I${JNIINCLUDEFOLDER}/darwin -mmacosx-version-min=10.9
LDFLAGS=-arch x86_64 -arch arm64 -dynamiclib -framework Foundation -framework AppKit -mmacosx-version-min=10.9
SOURCES=com_hamarb123_macos_input_fixes_MacOSInputFixesMod.mm
OBJECTS=$(SOURCES:.mm=.o)
EXECUTABLE=macos_input_fixes.dylib

all: $(SOURCES) $(EXECUTABLE)

run:
	$(EXECUTABLE)

$(EXECUTABLE): $(OBJECTS)
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@
	strip -S $(EXECUTABLE)
	mkdir -p ../resources/natives
	cp $(EXECUTABLE) ../resources/natives/$(EXECUTABLE)

$(OBJECTS): %.o : %.mm
	$(CC) $(CFLAGS) -o $@ -c $<

clean:
	rm -Rf $(EXECUTABLE) *.o
