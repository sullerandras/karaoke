package karaoke;

public class Buffer {
  private byte[] buffer;
  private int offset;
  private int length;

  public Buffer(byte[] buffer) {
    this.buffer = buffer;
    this.offset = 0;
    this.length = buffer.length;
  }

  public byte[] readLine() {
    if (offset >= length) {
      return null;
    }

    boolean newLineFound = false;
    int start = offset;
    while (offset < length) {
      if (offset < length - 1 && buffer[offset] == '\r' && buffer[offset + 1] == '\n') {
        newLineFound = true;
        break;
      }
      offset++;
    }

    if (!newLineFound) {
      return null;
    }

    byte[] line = new byte[offset - start];
    for (int i = 0; i < line.length; i++) {
      line[i] = buffer[start + i];
    }

    offset += 2; // skip over '\r\n'
    return line;
  }

  // Returns the next byte in the buffer as a number between 0 and 255,
  // or -1 if the end of the buffer has been reached.
  public int next() {
    if (offset >= length) {
      return -1;
    }
    return buffer[offset++] & 0xFF;
  }
}
