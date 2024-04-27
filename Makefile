run: compile
	java -cp classes karaoke.Main

compile: clean
	javac -d classes src/karaoke/*.java src/karaoke/gui/*.java

clean:
	rm -rf classes
	mkdir -p classes

play1: compile
	java -cp classes karaoke.MidiPlayer A018696.kar ${SF}

play2: compile
	java -cp classes karaoke.MidiPlayer A013620.kar ${SF}

play3: compile
	java -cp classes karaoke.MidiPlayer 35074.kar ${SF}

convert: compile
	java -cp classes karaoke.ConvertAllToKar
