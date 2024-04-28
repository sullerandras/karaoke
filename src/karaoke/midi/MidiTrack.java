package karaoke.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MidiTrack {
  private final Buffer buffer;

  public MidiTrack(byte[] data, int offset, int length) {
    this.buffer = new Buffer(data, offset + 8, length - 8);
  }

  public List<MidiEvent> getEvents() {
    List<MidiEvent> events = new ArrayList<>();

    int absoluteTime = 0;

    while (true) {
      int deltaTime = buffer.readVariableLength();
      if (deltaTime == -1) {
        break;
      }
      int status = buffer.next();
      if (status == -1) {
        break;
      }
      if (status == 0xFF) {
        int type = buffer.next();
        int length = buffer.readVariableLength();
        byte[] data = buffer.read(length);
        events.add(new MetaEvent(deltaTime, absoluteTime, type, data));
      } else if ((status & 0xF0) == 0x90) {
        int note = buffer.next();
        int velocity = buffer.next();
        events.add(new NoteOnEvent(deltaTime, absoluteTime, note, velocity));
      } else if ((status & 0xF0) == 0x80) {
        int note = buffer.next();
        int velocity = buffer.next();
        events.add(new NoteOffEvent(deltaTime, absoluteTime, note, velocity));
      } else {
        throw new RuntimeException("Unsupported Midi event: " + status);
      }

      absoluteTime += deltaTime;
    }

    return events;
  }

  public List<MetaEvent> getType1Events() {
    return getEvents().stream()
        .filter(event -> (event instanceof MetaEvent) && ((MetaEvent) event).getType() == 1)
        .map(event -> (MetaEvent) event)
        .collect(Collectors.toList());
  }

  /**
   * Returns the value from the last FF 51 03 tt tt tt event in this track.
   * @return
   */
  public int getTempo() {
    int tempo = 500000; // Default tempo
    for (int i = 0; i < buffer.getLength() - 5; i++) {
      if (buffer.at(i) == 0xFF && buffer.at(i + 1) == 0x51 && buffer.at(i + 2) == 0x03) {
        tempo = buffer.at(i + 3) << 16 | buffer.at(i + 4) << 8 | buffer.at(i + 5);
      }
    }
    return tempo;
  }
}
