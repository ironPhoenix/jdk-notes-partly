package java.io;

public class BufferedOutputStream extends FilterOutputStream {
    // 保存“缓冲输出流”数据的字节数组
    protected byte buf[];

    // 缓冲中数据的大小
    protected int count;

    // 构造函数：新建字节数组大小为8192的“缓冲输出流”
    public BufferedOutputStream(OutputStream out) {
        this(out, 8192);
    }

    // 构造函数：新建字节数组大小为size的“缓冲输出流”
    public BufferedOutputStream(OutputStream out, int size) {
        super(out);
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        buf = new byte[size];
    }

    // 将缓冲数据都写入到输出流中
    private void flushBuffer() throws IOException {
        if (count > 0) {
            out.write(buf, 0, count);
            count = 0;
        }
    }

    // 将“数据b(转换成字节类型)”写入到输出流中
    public synchronized void write(int b) throws IOException {
        // 若缓冲已满，则先将缓冲数据写入到输出流中。
        if (count >= buf.length) {
            flushBuffer();
        }
        // 将“数据b”写入到缓冲中
        buf[count++] = (byte)b;
    }

    public synchronized void write(byte b[], int off, int len) throws IOException {
        // 若“写入长度”大于“缓冲区大小”，则先将缓冲中的数据写入到输出流，然后直接将数组b写入到输出流中
        if (len >= buf.length) {
            flushBuffer();
            out.write(b, off, len);
            return;
        }
        // 若“剩余的缓冲空间 不足以 存储即将写入的数据”，则先将缓冲中的数据写入到输出流中
        if (len > buf.length - count) {
            flushBuffer();
        }
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    // 将“缓冲数据”写入到输出流中
    public synchronized void flush() throws IOException {
        flushBuffer();
        out.flush();
    }
}