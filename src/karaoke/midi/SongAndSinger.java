package karaoke.midi;

public class SongAndSinger {
  private byte[] song;
  private byte[] singer;

  public SongAndSinger(byte[] song, byte[] singer) {
    this.song = song;
    this.singer = singer;
  }

  public byte[] getSong() {
    return song;
  }

  public byte[] getSinger() {
    return singer;
  }
}
