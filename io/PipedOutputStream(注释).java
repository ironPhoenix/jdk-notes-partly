package java.io;

import java.io.*;
/*
有一个PipedInputStream 私有域
	两个构造函数 其中有一个是用来设置连接的
	方法
			连接
			两个write
				调用的PipedInputStream的receive方法
			一个flush
			还有一个close
*/
public class PipedOutputStream extends OutputStream {

    // 与PipedOutputStream通信的PipedInputStream对象
    private PipedInputStream sink;

    // 构造函数，指定配对的PipedInputStream
    public PipedOutputStream(PipedInputStream snk)  throws IOException {
        connect(snk);
    }

    // 构造函数
    public PipedOutputStream() {
    }

    // 将“管道输出流” 和 “管道输入流”连接。
    public synchronized void connect(PipedInputStream snk) throws IOException {
        if (snk == null) {
            throw new NullPointerException();
        } else if (sink != null || snk.connected) {
            throw new IOException("Already connected");
        }
        // 设置“管道输入流”
        sink = snk;
        // 初始化“管道输入流”的读写位置
        // int是PipedInputStream中定义的，代表“管道输入流”的读写位置
        snk.in = -1;
        // 初始化“管道输出流”的读写位置。
        // out是PipedInputStream中定义的，代表“管道输出流”的读写位置
        snk.out = 0;
        // 设置“管道输入流”和“管道输出流”为已连接状态
        // connected是PipedInputStream中定义的，用于表示“管道输入流与管道输出流”是否已经连接
        snk.connected = true;
    }

    // 将int类型b写入“管道输出流”中。
    // 将b写入“管道输出流”之后，它会将b传输给“管道输入流”
    public void write(int b)  throws IOException {
        if (sink == null) {
            throw new IOException("Pipe not connected");
        }
        sink.receive(b);
    }

    // 将字节数组b写入“管道输出流”中。
    // 将数组b写入“管道输出流”之后，它会将其传输给“管道输入流”
    public void write(byte b[], int off, int len) throws IOException {
        if (sink == null) {
            throw new IOException("Pipe not connected");
        } else if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                   ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        // “管道输入流”接收数据
        sink.receive(b, off, len);
    }

    // 清空“管道输出流”。
    // 这里会调用“管道输入流”的notifyAll()；
    // 目的是让“管道输入流”放弃对当前资源的占有，让其它的等待线程(等待读取管道输出流的线程)读取“管道输出流”的值。
    public synchronized void flush() throws IOException {
        if (sink != null) {
            synchronized (sink) {
                sink.notifyAll();
            }
        }
    }

    // 关闭“管道输出流”。
    // 关闭之后，会调用receivedLast()通知“管道输入流”它已经关闭。
    public void close()  throws IOException {
        if (sink != null) {
            sink.receivedLast();
        }
    }
}