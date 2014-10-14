package java.io;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class BufferedInputStream extends FilterInputStream {

    // 默认的缓冲大小是8192字节
    // BufferedInputStream 会根据“缓冲区大小”来逐次的填充缓冲区；
    // 即，BufferedInputStream填充缓冲区，用户读取缓冲区，读完之后，BufferedInputStream会再次填充缓冲区。如此循环，直到读完数据...
    private static int defaultBufferSize = 8192;

    // 缓冲数组
    protected volatile byte buf[];

    // 缓存数组的原子更新器。
    // 该成员变量与buf数组的volatile关键字共同组成了buf数组的原子更新功能实现，
    // 即，在多线程中操作BufferedInputStream对象时，buf和bufUpdater都具有原子性(不同的线程访问到的数据都是相同的)
    private static final
        AtomicReferenceFieldUpdater<BufferedInputStream, byte[]> bufUpdater =
        AtomicReferenceFieldUpdater.newUpdater
        (BufferedInputStream.class,  byte[].class, "buf");

    // 当前缓冲区的有效字节数。
    // 注意，这里是指缓冲区的有效字节数，而不是输入流中的有效字节数。
    protected int count;

    // 当前缓冲区的位置索引
    // 注意，这里是指缓冲区的位置索引，而不是输入流中的位置索引。
    protected int pos;

    // 当前缓冲区的标记位置
    // markpos和reset()配合使用才有意义。操作步骤：
    // (01) 通过mark() 函数，保存pos的值到markpos中。
    // (02) 通过reset() 函数，会将pos的值重置为markpos。接着通过read()读取数据时，就会从mark()保存的位置开始读取。
    protected int markpos = -1;

    // marklimit是标记的最大值。
    // 关于marklimit的原理，我们在后面的fill()函数分析中会详细说明。这对理解BufferedInputStream相当重要。
    protected int marklimit;

    // 获取输入流  有些时候没用他的返回值，是检验一下输入流是否关闭
    private InputStream getInIfOpen() throws IOException {
        InputStream input = in;//是他父类的InputStream
        if (input == null)
            throw new IOException("Stream closed");
        return input;
    }

    // 获取缓冲  有些时候没用他的返回值，是检验一下是否关闭
    private byte[] getBufIfOpen() throws IOException {
        byte[] buffer = buf;
        if (buffer == null)
            throw new IOException("Stream closed");
        return buffer;
    }

    // 构造函数：新建一个缓冲区大小为8192的BufferedInputStream
    public BufferedInputStream(InputStream in) {
        this(in, defaultBufferSize);
    }

    // 构造函数：新建指定缓冲区大小的BufferedInputStream
    public BufferedInputStream(InputStream in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        buf = new byte[size];
    }

    // 从“输入流”中读取数据，并填充到缓冲区中。
    // 后面会对该函数进行详细说明！
    private void fill() throws IOException {
        byte[] buffer = getBufIfOpen();
        if (markpos < 0)
            pos = 0;            /* no mark: throw away the buffer */
        else if (pos >= buffer.length)  /* no room left in buffer */
            if (markpos > 0) {  /* can throw away early part of the buffer */
                int sz = pos - markpos;
                System.arraycopy(buffer, markpos, buffer, 0, sz);
                pos = sz;
                markpos = 0;
            } else if (buffer.length >= marklimit) {
                markpos = -1;   /* buffer got too big, invalidate mark */
                pos = 0;        /* drop buffer contents */
            } else {            /* grow buffer */
                int nsz = pos * 2;
                if (nsz > marklimit)
                    nsz = marklimit;
                byte nbuf[] = new byte[nsz];
                System.arraycopy(buffer, 0, nbuf, 0, pos);
                if (!bufUpdater.compareAndSet(this, buffer, nbuf)) {
                    throw new IOException("Stream closed");
                }
                buffer = nbuf;
            }
        count = pos;
        int n = getInIfOpen().read(buffer, pos, buffer.length - pos);
        if (n > 0)
            count = n + pos;
    }

    // 读取下一个字节
    public synchronized int read() throws IOException {
        // 若已经读完缓冲区中的数据，则调用fill()从输入流读取下一部分数据来填充缓冲区
        if (pos >= count) {
            fill();
            if (pos >= count)
                return -1;
        }
        // 从缓冲区中读取指定的字节
        return getBufIfOpen()[pos++] & 0xff;
    }

    // 将缓冲区中的数据写入到字节数组b中。off是字节数组b的起始位置，len是写入长度
    private int read1(byte[] b, int off, int len) throws IOException {
        int avail = count - pos;
        if (avail <= 0) {
            // 加速机制。
            // 如果读取的长度大于缓冲区的长度 并且没有markpos，
            // 则直接从原始输入流中进行读取，从而避免无谓的COPY（从原始输入流至缓冲区，读取缓冲区全部数据，清空缓冲区， 
            //  重新填入原始输入流数据）
            if (len >= getBufIfOpen().length && markpos < 0) {
                return getInIfOpen().read(b, off, len);
            }
            // 若已经读完缓冲区中的数据，则调用fill()从输入流读取下一部分数据来填充缓冲区
            fill();
            avail = count - pos;
            if (avail <= 0) return -1;
        }
        int cnt = (avail < len) ? avail : len;
        System.arraycopy(getBufIfOpen(), pos, b, off, cnt);
        pos += cnt;
        return cnt;
    }

    // 将缓冲区中的数据写入到字节数组b中。off是字节数组b的起始位置，len是写入长度
    public synchronized int read(byte b[], int off, int len)
        throws IOException
    {
        getBufIfOpen(); // Check for closed stream
        if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        // 读取到指定长度的数据才返回
        int n = 0;
        for (;;) {
            int nread = read1(b, off + n, len - n);
            if (nread <= 0)
                return (n == 0) ? nread : n;
            n += nread;
            if (n >= len)
                return n;
            // if not closed but no bytes available, return
            InputStream input = in;
            if (input != null && input.available() <= 0)
                return n;
        }
    }

    // 忽略n个字节
    public synchronized long skip(long n) throws IOException {
        getBufIfOpen(); // Check for closed stream
        if (n <= 0) {
            return 0;
        }
        long avail = count - pos;

        if (avail <= 0) {
            // If no mark position set then don't keep in buffer
            if (markpos <0)
                return getInIfOpen().skip(n);

            // Fill in buffer to save bytes for reset
            fill();
            avail = count - pos;
            if (avail <= 0)
                return 0;
        }

        long skipped = (avail < n) ? avail : n;
        pos += skipped;
        return skipped;
    }

    // 下一个字节是否存可读
    public synchronized int available() throws IOException {
        int n = count - pos;
        int avail = getInIfOpen().available();
        return n > (Integer.MAX_VALUE - avail)
                    ? Integer.MAX_VALUE
                    : n + avail;
    }

    // 标记“缓冲区”中当前位置。
    // readlimit是marklimit，关于marklimit的作用，参考后面的说明。
    public synchronized void mark(int readlimit) {
        marklimit = readlimit;
        markpos = pos;
    }

    // 将“缓冲区”中当前位置重置到mark()所标记的位置
    public synchronized void reset() throws IOException {
        getBufIfOpen(); // Cause exception if closed
        if (markpos < 0)
            throw new IOException("Resetting to invalid mark");
        pos = markpos;
    }

    public boolean markSupported() {
        return true;
    }

    // 关闭输入流
    public void close() throws IOException {
        byte[] buffer;
        while ( (buffer = buf) != null) {
            if (bufUpdater.compareAndSet(this, buffer, null)) {
                InputStream input = in;
                in = null;
                if (input != null)
                    input.close();
                return;
            }
            // Else retry in case a new buf was CASed in fill()
        }
    }
}