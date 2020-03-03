package encoding;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class URLDecoderTest {
	public static void main(String[] args) throws UnsupportedEncodingException {
		String _t = URLDecoder.decode("2018-11-28 11:50:56", "utf-8");
		System.out.println(_t);
	}
}
