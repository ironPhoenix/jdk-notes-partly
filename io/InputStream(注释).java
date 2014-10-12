package java.io;

public abstract class InputStream implements Closeable {

    // 每一次最大能skip的大小
    private static final int MAX_SKIP_BUFFER_SIZE = 2048;

    // 从输入流中读取数据的下一个字节。
    public abstract int read() throws IOException;

    // 将数据从输入流读入 byte 数组。
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    // 将最多 len 个数据字节从此输入流读入 byte 数组。
    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte)c;

        int i = 1;
        try {
            for (; i < len ; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                b[off + i] = (byte)c;
            }
        } catch (IOException ee) {
        }
        return i;
    }

    // 跳过输入流中的n个字节
	// 其实就是读出来 扔掉
    public long skip(long n) throws IOException {

        long remaining = n;
        int nr;

        if (n <= 0) {
            return 0;
        }
		
		//比最大能跳范围大 就分次扔
        int size = (int)Math.min(MAX_SKIP_BUFFER_SIZE, remaining);
        byte[] skipBuffer = new byte[size];
        while (remaining > 0) {
            nr = read(skipBuffer, 0, (int)Math.min(size, remaining));
            if (nr < 0) {
                break;
            }
            remaining -= nr;
        }

        return n - remaining;
    }

    public int available() throws IOException {
        return 0;
    }

    public void close() throws IOException {}

    public synchronized void mark(int readlimit) {}

    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    public boolean markSupported() {
        return false;
    }
}