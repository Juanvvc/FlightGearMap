
package com.juanvvc.flightgear;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.util.Log;

/** Use this class instead of android.util.Log: simplify the process of uploading to Google Play
 * @author juanvi
 */
public class myLog{
	private static final boolean debug=false;
	
	public static void i(String tag, String msg){
		if(debug) Log.i(tag, msg);
	}
	public static void d(String tag, String msg){
		if(debug) Log.d(tag, msg);
	}
	public static void v(String tag, String msg){
		if(debug) Log.v(tag, msg);
	}
	public static void e(String tag, String msg){
		if(debug) Log.e(tag, msg);
	}
	public static void w(String tag, String msg){
		if(debug) Log.e(tag, msg);
	}
	
	public static String stackToString(Exception e) {
		Writer result = new StringWriter();
		PrintWriter printWriter = new PrintWriter(result);
		e.printStackTrace(printWriter);
		return result.toString();
	}
}