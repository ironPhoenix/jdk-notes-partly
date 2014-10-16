package java.io;

//该类中的receive是准备被PipedInputStream调用的
public class PipedInputStream extends InputStream {
	/*
	静态常量 默认管道大小 1024
		  管道型号 = 默认管道大小
    boolean  closedByWriter = false;
         closedByReader = false;
		 connected = false
    Thread  readSide
        writeSide
    byte buffer[]		
    in = -1
    out = 0//int==out 代表写入的数据都被读了
	*/
    // “管道输出流”是否关闭的标记
    boolean closedByWriter = false;
    // “管道输入流”是否关闭的标记
    volatile boolean closedByReader = false;
    // “管道输入流”与“管道输出流”是否连接的标记
    // 它在PipedOutputStream的connect()连接函数中被设置为true
    boolean connected = false;

    Thread readSide;    // 读取“管道”数据的线程
    Thread writeSide;    // 向“管道”写入数据的线程

    // “管道”的默认大小
    private static final int DEFAULT_PIPE_SIZE = 1024;

    protected static final int PIPE_SIZE = DEFAULT_PIPE_SIZE;

    // 缓冲区
    protected byte buffer[];

    //下一个写入字节的位置。in==out代表满，说明“写入的数据”全部被读取了。
    protected int in = -1;
    //下一个读取字节的位置。in==out代表满，说明“写入的数据”全部被读取了。
    protected int out = 0;

	/*private void initPipe(int pipeSize)
    pipeSize>0 就初始化缓冲区
	public void connect(PipedOutputStream src) 
		throws IOException
		调用流出管道的方法与之连接
	四个构造函数
	  PipedInputStream
	()
	初始buffer大小为 管道型号 
	不设置与之连接的输出流

	(int pipeSize)
	初始buffer大小为 pipeSize 
	不设置与之连接的输出流

	(PipedOutputStream src)
	初始buffer大小为 管道型号 
	设置与之连接的输出流为src

	(PipedOutputStream src, int pipeSize)
	初始buffer大小为 pipeSize 
	设置与之连接的输出流为src
    */
    // 构造函数：指定与“管道输入流”关联的“管道输出流”
    public PipedInputStream(PipedOutputStream src) throws IOException {
        this(src, DEFAULT_PIPE_SIZE);
    }

    // 构造函数：指定与“管道输入流”关联的“管道输出流”，以及“缓冲区大小”
    public PipedInputStream(PipedOutputStream src, int pipeSize)
            throws IOException {
         initPipe(pipeSize);
         connect(src);
    }

    // 构造函数：默认缓冲区大小是1024字节
    public PipedInputStream() {
        initPipe(DEFAULT_PIPE_SIZE);
    }

    // 构造函数：指定缓冲区大小是pipeSize
    public PipedInputStream(int pipeSize) {
        initPipe(pipeSize);
    }

    // 初始化“管道”：新建缓冲区大小
    private void initPipe(int pipeSize) {
         if (pipeSize <= 0) {
            throw new IllegalArgumentException("Pipe Size <= 0");
         }
         buffer = new byte[pipeSize];
    }

    // 将“管道输入流”和“管道输出流”绑定。
    // 实际上，这里调用的是PipedOutputStream的connect()函数
    public void connect(PipedOutputStream src) throws IOException {
        src.connect(this);
    }

    // 接收int类型的数据b。
    // 它只会在PipedOutputStream的write(int b)中会被调用
    protected synchronized void receive(int b) throws IOException {
        // 检查管道状态
        checkStateForReceive();
        // 获取“写入管道”的线程
        writeSide = Thread.currentThread();
        // 若“写入管道”的数据正好全部被读取完，则等待。
        if (in == out)
            awaitSpace();
        if (in < 0) {
            in = 0;
            out = 0;
        }
        // 将b保存到缓冲区
        buffer[in++] = (byte)(b & 0xFF);
        if (in >= buffer.length) {
            in = 0;
        }
    }
	/*主功能方法
		一个检查管道方法 一个等待方法
		//让读取线程读取缓冲区中的内容
				每秒个愣醒所有线程
		两个receive方法  一个结束接收方法
		两个read 方法 一个特殊读方法 
		一个关闭方法

		receive(int b)
			检查管道
				将当前线程赋值给写线程
			in == out
				启动等待方法
			in<0  将 in out 都设为 0
			每在缓冲区中写一个 in都加1
			如果in>buffer.length
					in=0;
		receivedLast()
			将写入关闭表示设置为true
			唤醒所有线程
		int read()
			检查管道 
				将当前线程复制给读线程
			当in<0时 报错抛异常
			从缓冲区中一个个读
			*/
    // 接收字节数组b。
    synchronized void receive(byte b[], int off, int len)  throws IOException {
        // 检查管道状态
        checkStateForReceive();
        // 获取“写入管道”的线程
        writeSide = Thread.currentThread();
        int bytesToTransfer = len;
        while (bytesToTransfer > 0) {
            // 若“写入管道”的数据正好全部被读取完，则等待。
            if (in == out)
                awaitSpace();
            int nextTransferAmount = 0;
            // 如果“管道中被读取的数据，少于写入管道的数据”；
            // 则设置nextTransferAmount=“buffer.length - in”
            if (out < in) {
                nextTransferAmount = buffer.length - in;
            } else if (in < out) { // 如果“管道中被读取的数据，大于/等于写入管道的数据”，则执行后面的操作
                // 若in==-1(即管道的写入数据等于被读取数据)，此时nextTransferAmount = buffer.length - in;
                // 否则，nextTransferAmount = out - in;
                if (in == -1) {
                    in = out = 0;
                    nextTransferAmount = buffer.length - in;
                } else {
                    nextTransferAmount = out - in;
                }
            }
            if (nextTransferAmount > bytesToTransfer)
                nextTransferAmount = bytesToTransfer;
            // assert断言的作用是，若nextTransferAmount <= 0，则终止程序。
            assert(nextTransferAmount > 0);
            // 将数据写入到缓冲中
            System.arraycopy(b, off, buffer, in, nextTransferAmount);
            bytesToTransfer -= nextTransferAmount;
            off += nextTransferAmount;
            in += nextTransferAmount;
            if (in >= buffer.length) {
                in = 0;
            }
        }
    }

    // 检查管道状态
    private void checkStateForReceive() throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        } else if (closedByWriter || closedByReader) {
            throw new IOException("Pipe closed");
        } else if (readSide != null && !readSide.isAlive()) {
            throw new IOException("Read end dead");
        }
    }

    // 等待。
    // 若“写入管道”的数据正好全部被读取完(例如，管道缓冲满)，则执行awaitSpace()操作；
    // 它的目的是让“读取管道的线程”管道产生读取数据请求，从而才能继续的向“管道”中写入数据。
    private void awaitSpace() throws IOException {
        
        // 如果“管道中被读取的数据，等于写入管道的数据”时，
        // 则每隔1000ms检查“管道状态”，并唤醒管道操作：若有“读取管道数据线程被阻塞”，则唤醒该线程。
        while (in == out) {
            checkStateForReceive();

            /* full: kick any waiting readers */
            notifyAll();
            try {
                wait(1000);
            } catch (InterruptedException ex) {
                throw new java.io.InterruptedIOException();
            }
        }
    }

    // 当PipedOutputStream被关闭时，被调用
    synchronized void receivedLast() {
        closedByWriter = true;
        notifyAll();
    }

    // 从管道(的缓冲)中读取一个字节，并将其转换成int类型
    public synchronized int read()  throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        } else if (closedByReader) {
            throw new IOException("Pipe closed");
        } else if (writeSide != null && !writeSide.isAlive()
                   && !closedByWriter && (in < 0)) {
            throw new IOException("Write end dead");
        }

        readSide = Thread.currentThread();
        int trials = 2;
        while (in < 0) {
            if (closedByWriter) {
                /* closed by writer, return EOF */
                return -1;
            }
            if ((writeSide != null) && (!writeSide.isAlive()) && (--trials < 0)) {
                throw new IOException("Pipe broken");
            }
            /* might be a writer waiting */
            notifyAll();
            try {
                wait(1000);
            } catch (InterruptedException ex) {
                throw new java.io.InterruptedIOException();
            }
        }
        int ret = buffer[out++] & 0xFF;
        if (out >= buffer.length) {
            out = 0;
        }
        if (in == out) {
            /* now empty */
            in = -1;
        }

        return ret;
    }

    // 从管道(的缓冲)中读取数据，并将其存入到数组b中
    public synchronized int read(byte b[], int off, int len)  throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        /* possibly wait on the first character */
        int c = read();
        if (c < 0) {
            return -1;
        }
        b[off] = (byte) c;
        int rlen = 1;
        while ((in >= 0) && (len > 1)) {

            int available;

            if (in > out) {
                available = Math.min((buffer.length - out), (in - out));
            } else {
                available = buffer.length - out;
            }

            // A byte is read beforehand outside the loop
            if (available > (len - 1)) {
                available = len - 1;
            }
            System.arraycopy(buffer, out, b, off + rlen, available);
            out += available;
            rlen += available;
            len -= available;

            if (out >= buffer.length) {
                out = 0;
            }
            if (in == out) {
                /* now empty */
                in = -1;
            }
        }
        return rlen;
    }

    // 返回不受阻塞地从此输入流中读取的字节数。
    public synchronized int available() throws IOException {
        if(in < 0)
            return 0;
        else if(in == out)
            return buffer.length;
        else if (in > out)
            return in - out;
        else
            return in + buffer.length - out;
    }

    // 关闭管道输入流
    public void close()  throws IOException {
        closedByReader = true;
        synchronized (this) {
            in = -1;
        }
    }
}