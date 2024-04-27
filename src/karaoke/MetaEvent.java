package karaoke;

import java.nio.charset.Charset;

public class MetaEvent extends MidiEvent {
  private final int type;
  private final byte[] data;
  private final String dataAsCp874String;

  public MetaEvent(int deltaTime, int absoluteTime, int type, byte[] data) {
    super(deltaTime, absoluteTime);
    this.type = type;
    this.data = data;
    this.dataAsCp874String = new String(data, Charset.forName("Cp874")).replaceAll("�", " ");
  }

  public int getType() {
    return type;
  }

  public byte[] getData() {
    return data;
  }

  public String getDataAsCp874String() {
    return dataAsCp874String;
  }

  @Override
  public String toString() {
    return "MetaEvent(" + getDeltaTime() + ", " + type + ", " + data.length + " bytes: "
        + getDataAsCp874String() + ")" +
        (data.length == 1 && data[0] == '/' ? "\n" : "");
  }
}
