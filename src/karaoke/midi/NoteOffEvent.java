package karaoke.midi;

public class NoteOffEvent extends MidiEvent {
  private final int note;
  private final int velocity;

  public NoteOffEvent(int deltaTime, int absoluteTime, int note, int velocity) {
    super(deltaTime, absoluteTime);
    this.note = note;
    this.velocity = velocity;
  }

  public int getNote() {
    return note;
  }

  public int getVelocity() {
    return velocity;
  }
}
