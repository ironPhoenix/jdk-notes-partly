package java.io;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import sun.nio.cs.StreamDecoder;

/*
InputStreamReader 和 OutputStreamWriter ：字节流 转 字符流
                                             （可指定编码集）
分别继承字Reader and Writer

InputStreamReader 四种构造方法
	InputStream ,null//使用默认的编码名
	InputStream ,String charsetName//指出使用的编码名
	InputStream ,Charset//指出使用的编码方式
	InputStream ,CharsetDecoder//指出要使用的解码器
	共同调用了
	SteamDecoder.forInputStreamReader(in,this,charsetName)
	
	下面共有五个方法
			String getEncoding()
			int read() throws IOException
			read(char cbuf[], int offset, int length) throws IOException
			boolean ready() throws IOException
			void close() throws IOException
	都是调用StreamDecoder中相应的方法
*/
	

// 将“字节输入流”转换成“字符输入流”
public class InputStreamReader extends Reader {

    private final StreamDecoder sd;

    // 根据in创建InputStreamReader，使用默认的编码
    public InputStreamReader(InputStream in) {
        super(in);
        try {
            sd = StreamDecoder.forInputStreamReader(in, this, (String)null); // ## check lock object
        } catch (UnsupportedEncodingException e) {
            // The default encoding should always be available
            throw new Error(e);
        }
    }

    // 根据in创建InputStreamReader，使用编码charsetName(编码名)
    public InputStreamReader(InputStream in, String charsetName)
        throws UnsupportedEncodingException
    {
        super(in);
        if (charsetName == null)
            throw new NullPointerException("charsetName");
        sd = StreamDecoder.forInputStreamReader(in, this, charsetName);
    }

    // 根据in创建InputStreamReader，使用编码cs
    public InputStreamReader(InputStream in, Charset cs) {
        super(in);
        if (cs == null)
            throw new NullPointerException("charset");
        sd = StreamDecoder.forInputStreamReader(in, this, cs);
    }

    // 根据in创建InputStreamReader，使用解码器dec
    public InputStreamReader(InputStream in, CharsetDecoder dec) {
        super(in);
        if (dec == null)
            throw new NullPointerException("charset decoder");
        sd = StreamDecoder.forInputStreamReader(in, this, dec);
    }

    // 获取解码器
    public String getEncoding() {
        return sd.getEncoding();
    }

    // 读取并返回一个字符
    public int read() throws IOException {
        return sd.read();
    }

    // 将InputStreamReader中的数据写入cbuf中，从cbuf的offset位置开始写入，写入长度是length
    public int read(char cbuf[], int offset, int length) throws IOException {
        return sd.read(cbuf, offset, length);
    }

    // 能否从InputStreamReader中读取数据
    public boolean ready() throws IOException {
        return sd.ready();
    }

    // 关闭InputStreamReader
    public void close() throws IOException {
        sd.close();
    }
}