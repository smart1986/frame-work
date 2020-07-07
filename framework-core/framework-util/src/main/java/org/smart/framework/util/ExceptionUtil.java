package org.smart.framework.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: smart
 * Date: 2018-07-21
 * Time: 11:13
 */
public class ExceptionUtil {
	/**
	 * 追踪堆栈信息输出字符串
	 * @param throwable
	 * @return
	 */
	public static String getStackTrace(Throwable throwable){
		StringWriter sw;
		PrintWriter pw;

		sw = new StringWriter();
		pw =  new PrintWriter(sw);
		//将出错的栈信息输出到printWriter中
		throwable.printStackTrace(pw);
		pw.flush();
		sw.flush();

		try {
			sw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		pw.close();


		return sw.toString();
	}
}
