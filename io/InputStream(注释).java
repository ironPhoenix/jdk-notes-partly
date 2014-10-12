package java.io;

public abstract class InputStream implements Closeable {

    // ÿһ�������skip�Ĵ�С
    private static final int MAX_SKIP_BUFFER_SIZE = 2048;

    // ���������ж�ȡ���ݵ���һ���ֽڡ�
    public abstract int read() throws IOException;

    // �����ݴ����������� byte ���顣
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    // ����� len �������ֽڴӴ����������� byte ���顣
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

    // �����������е�n���ֽ�
	// ��ʵ���Ƕ����� �ӵ�
    public long skip(long n) throws IOException {

        long remaining = n;
        int nr;

        if (n <= 0) {
            return 0;
        }
		
		//�����������Χ�� �ͷִ���
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