package java.io;

public abstract class OutputStream implements Closeable, Flushable {
    // 将字节b写入到“输出流”中。
    // 它在子类中实现,多为native 方法
    public abstract void write(int b) throws IOException;

    // 写入字节数组b到“字节数组输出流”中。
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    // 写入字节数组b到“字节数组输出流”中，并且off是“数组b的起始位置”，len是写入的长度
    public void write(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                   ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        for (int i = 0 ; i < len ; i++) {
            write(b[off + i]);
        }
    }

    public void flush() throws IOException {
    }

    public void close() throws IOException {
    }
}