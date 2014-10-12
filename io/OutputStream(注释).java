package java.io;

public abstract class OutputStream implements Closeable, Flushable {
    // ���ֽ�bд�뵽����������С�
    // ����������ʵ��,��Ϊnative ����
    public abstract void write(int b) throws IOException;

    // д���ֽ�����b�����ֽ�������������С�
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    // д���ֽ�����b�����ֽ�������������У�����off�ǡ�����b����ʼλ�á���len��д��ĳ���
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