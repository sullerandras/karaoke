run: compile
	java -cp classes karaoke.Main

compile: clean
	javac -d classes src/karaoke/*.java src/karaoke/gui/*.java src/karaoke/midi/*.java

clean:
	rm -rf classes
	mkdir -p classes

play1: compile
	java -cp classes karaoke.midi.MidiPlayer A018696.kar ${SF}

play2: compile
	java -cp classes karaoke.midi.MidiPlayer A013620.kar ${SF}

play3: compile
	java -cp classes karaoke.midi.MidiPlayer 35074.kar ${SF}

convert: compile
	java -cp classes karaoke.midi.ConvertAllToKar

convertemks: compile
	mkdir -p emkkar
	java -cp classes karaoke.midi.ConvertAllEmkToKar

build: compile
	rm -rf build
	rm -rf karaoke.jar
	mkdir build
	cp -r classes/* build
	cp -r src/* build
	cp LICENSE build
	jar -cfe karaoke.jar karaoke.Main -C build .
