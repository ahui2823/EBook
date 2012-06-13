package com.hj.view;

import android.graphics.Bitmap;

public class AppData {
	public static int m_textFontSize = 26;
	public static int m_fontSize = 36;
	public static int m_fontColor = 0xff594d3a;
	
	public static Bitmap mCurPageBitmap;
	public static Bitmap mNextPageBitmap;
	public static Bitmap bg;
	
	public final static int BUFFER_LEN = 2048;
	public final static String REPLACE_STR = "\r\n";
	public final static int REPLACE_STR_LEN = REPLACE_STR.length();
}
