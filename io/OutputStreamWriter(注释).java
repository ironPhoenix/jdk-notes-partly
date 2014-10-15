package java.io;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import sun.nio.cs.StreamEncoder;

/*
OutputStreamWriter 中与InputStreamReader极为类似 
						不同点
							InputStreamReader是解码StreamDecoder
							本楼是编码StreamEncoder
							多了两个flush方法
							分别为  
							void flush() throws IOException
							void flushBuffer() throws IOException//该方法为默认范围
*/
// 将“字节输出流”转换成“字符输出流”
public class OutputStreamWriter extends Writer {

    private final StreamEncoder se;

    // 根据out创建OutputStreamWriter，使用编码charsetName(编码名)
    public OutputStreamWriter(OutputStream out, String charsetName)
        throws UnsupportedEncodingException
    {
        super(out);
        if (charsetName == null)
            throw new NullPointerException("charsetName");
        se = StreamEncoder.forOutputStreamWriter(out, this, charsetName);
    }

    // 根据out创建OutputStreamWriter，使用默认的编码
    public OutputStreamWriter(OutputStream out) {
        super(out);
        try {
            se = StreamEncoder.forOutputStreamWriter(out, this, (String)null);
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    // 根据out创建OutputStreamWriter，使用编码cs
    public OutputStreamWriter(OutputStream out, Charset cs) {
        super(out);
        if (cs == null)
            throw new NullPointerException("charset");
        se = StreamEncoder.forOutputStreamWriter(out, this, cs);
    }

    // 根据out创建OutputStreamWriter，使用编码器enc
    public OutputStreamWriter(OutputStream out, CharsetEncoder enc) {
        super(out);
        if (enc == null)
            throw new NullPointerException("charset encoder");
        se = StreamEncoder.forOutputStreamWriter(out, this, enc);
    }java io系列01之 "目录"

    // 获取编码器enc
    public String getEncoding() {
        return se.getEncoding();
    }

    // 刷新缓冲区
    void flushBuffer() throws IOException {
        se.flushBuffer();
    }

    // 将单个字符写入到OutputStreamWriter中
    public void write(int c) throws IOException {
        se.write(c);
    }

    // 将字符数组cbuf从off开始的数据写入到OutputStreamWriter中，写入长度是len
    public void write(char cbuf[], int off, int len) throws IOException {
        se.write(cbuf, off, len);
    }

    // 将字符串str从off开始的数据写入到OutputStreamWriter中，写入长度是len
    public void write(String str, int off, int len) throws IOException {
        se.write(str, off, len);
    }java io系列01之 "目录"

    // 刷新“输出流”
    // 它与flushBuffer()的区别是：flushBuffer()只会刷新缓冲，而flush()是刷新流，flush()包括了flushBuffer。
    public void flush() throws IOException {
        se.flush();
    }

    // 关闭“输出流”
    public void close() throws IOException {
        se.close();
    }
}