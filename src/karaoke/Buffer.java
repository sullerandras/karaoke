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

  public Buffer(byte[] buffer, int offset, int length) {
    this.buffer = buffer;
    this.offset = offset;
    this.length = offset + length;
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

  /**
   * Reads a variable-length integer from the buffer. The integer is encoded in
   * a variable-length format where the most significant bit of each byte is a
   * continuation flag. The lower 7 bits of each byte are used to store the
   * integer value, with the least significant bits stored in the first byte.
   */
  public int readVariableLength() {
    int value = 0;
    while (true) {
      int b = next();
      if (b == -1) {
        return -1;
      }
      value = (value << 7) | (b & 0x7F);
      if ((b & 0x80) == 0) {
        return value;
      }
    }
  }

  public byte[] read(int length) {
    byte[] data = new byte[length];
    for (int i = 0; i < length; i++) {
      data[i] = buffer[offset++];
    }
    return data;
  }

  /** Returns length minus offset. */
  public int getLength() {
    return length - offset;
  }

  public int at(int index) {
    return buffer[offset + index] & 0xFF;
  }
}
