package com.hj.view;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class BookPageFactory {
	private int m_textColor = 0xff000000;
	
	private int mWidth;
	private int mHeight;
	private Paint mPaint;
	
	private float mVisibleHeight;
	private float mVisibleWidth;
	
	private int marginDown = 30;
	private int marginUp = 25;
	private int marginWidth = 15;
	private int mRowSpace = 3;
	
	private int mLineCount;
	
	private boolean m_isFirstPage;
	private boolean m_isLastPage;
	private List<String> m_lines = new ArrayList<String>();
	public static int m_downSpace = 0;
	
	public int m_mbBufBegin = 0;
	public int m_mbBufEnd = 0;
	public int m_mbBufLen = 0;
	
	private ByteBuffer m_mbBuf;
	
	private Bitmap m_book_bg;
	
	private String m_strCharsetName = "GBK";
	
	public BookPageFactory(int width, int height)
	{
		mWidth = width;
		mHeight = height;
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(AppData.fontColor);
		mPaint.setTextAlign(Paint.Align.LEFT);
		mPaint.setTextSize(AppData.fontSize);
		mPaint.setColor(m_textColor);
		calculateLineCount();
	}
	
	public void calculateLineCount()
	{
		mVisibleWidth = mWidth - 2*marginWidth;
		mVisibleHeight = mHeight - marginUp - marginDown - m_downSpace;
		mLineCount = (int)(mVisibleHeight/(AppData.fontSize+mRowSpace));
	}
	
	public boolean isFirstPage()
	{
		return m_isFirstPage;
	}
	public boolean isLastPage()
	{
		return m_isLastPage;
	}
	
	public void nextPage()
	{
		if(m_mbBufEnd>=m_mbBufLen)
			m_isLastPage = true;
		else
		{
			m_isLastPage = false;
			m_lines.clear();
			m_mbBufBegin = m_mbBufEnd;
			pageDown(m_lines);
		}
	}
	
	protected void pageDown(List<String> tList)
	{
		String source = "";
		byte[] arrayOfByte = readParagraphForward(m_mbBufEnd);
		m_mbBufEnd += arrayOfByte.length;
		try {
			source = new String(arrayOfByte, m_strCharsetName);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		while(source.indexOf("\r\n")!=-1)
		{
			source = source.replaceAll("\r\n", "");
		}
		while(tList.size()<mLineCount)
		{
			int len = mPaint.breakText(source, true, mVisibleWidth, null);
			tList.add(source.substring(0, len));
			source = source.substring(len);
		}
	}
	
	protected void pageUp()
	{
		
	}
	
	public void prePage()
	{
		if(m_mbBufBegin<=0)
		{
			m_mbBufBegin = 0;
			m_isFirstPage = true;
		}
		else
		{
			m_isFirstPage = false;
			m_lines.clear();
			pageUp();
			pageDown(m_lines);
		}
	}
	
	public void onDraw(Canvas canvas)
	{
		mPaint.setTextSize(AppData.fontSize);
		if(m_lines.size()==0)
			pageDown(m_lines);
		if(m_book_bg==null)
			canvas.drawColor(0xffff9e85);
		else
			canvas.drawBitmap(m_book_bg, 0.0f, 0.0f, null);
		
		mPaint.setTextSize(6.0f);
		int rateLen = 3+ (int)mPaint.measureText("999.9%");
		float ration = (float)(1.0d * m_mbBufBegin/m_mbBufLen);
		String strRation = new DecimalFormat("#0.00").format(100.0f*ration)+"%";
		Calendar calendar = Calendar.getInstance();
		canvas.drawText(calendar.get(11)+"Ê±"+calendar.get(12)+"·Ö", 10.0f, mHeight-m_downSpace-8, mPaint);
		canvas.drawText(strRation, mWidth-rateLen, mHeight-m_downSpace, mPaint);
		
		int dy = marginUp;
		
		mPaint.setTextSize(AppData.fontSize);
		for(String text: m_lines)
		{
			canvas.drawText(text, marginWidth, dy, mPaint);
			dy += AppData.fontSize + mRowSpace;
		}
	}
	
	public void openBook(AssetManager assetManager, String fileName)
	{
		reset();
		m_mbBufLen = 0;
		try {
			InputStream is = assetManager.open(fileName);
			m_mbBufLen = is.available();
			byte[] arrayOfByte = new byte[m_mbBufLen];
			is.read(arrayOfByte);
			m_mbBuf = ByteBuffer.allocate(m_mbBufLen);
			m_mbBuf.put(arrayOfByte);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	protected byte[] readParagraphBack(int begin)
	{
		byte[] arrayOfByte = new byte[100];
		return arrayOfByte;
		
	}
	
	protected byte[] readParagraphForward(int begin)
	{
		byte[] arrayOfByte = new byte[100];
		return arrayOfByte;
	}
	
	public void reset()
	{
		m_mbBufBegin = 0;
		m_mbBufEnd = 0;
		m_isLastPage = false;
		m_lines.clear();
	}
	
	public void setBgBitmap(Bitmap bitmap)
	{
		m_book_bg = bitmap;
	}
	
	public void setFontSize(int size)
	{
		AppData.fontSize = size;
		mLineCount = (int)(mVisibleHeight/(AppData.fontSize+mRowSpace));
	}
}
