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
	private int mWidth;
	private int mHeight;
	private Paint mPaint;
	
	private float mVisibleHeight;
	private float mVisibleWidth;
	
	private int marginDown = 30;
	private int marginUp = 40;
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
		mPaint.setColor(AppData.m_fontColor);
		mPaint.setTextAlign(Paint.Align.LEFT);
		mPaint.setTextSize(AppData.m_fontSize);
		calculateLineCount();
	}
	
	public void calculateLineCount()
	{
		mVisibleWidth = mWidth - 2*marginWidth;
		mVisibleHeight = mHeight - marginUp - marginDown - m_downSpace;
		mLineCount = (int)(mVisibleHeight/(AppData.m_fontSize+mRowSpace));
	}
	
	public boolean isFirstPage()
	{
		return m_isFirstPage;
	}
	public boolean isLastPage()
	{
		return m_isLastPage;
	}
	
	protected void pageDown(List<String> tList)
	{
		byte[] arrayOfByte = readParagraphForward(m_mbBufEnd);
		m_mbBufEnd += arrayOfByte.length;
		try {
			String source = new String(arrayOfByte, m_strCharsetName);
			while(tList.size()<mLineCount)
			{
				int len = mPaint.breakText(source, true, mVisibleWidth, null);
				String subString = source.substring(0, len);
				int pos = subString.indexOf(AppData.REPLACE_STR);
				if(pos!=-1)
				{
					subString = subString.substring(0, pos);
					source = source.substring(pos+AppData.REPLACE_STR_LEN);
				}
				else
				{
					source = source.substring(len);
				}
				tList.add(subString);
				if(source.length()<=0)
					break;
			}
			m_mbBufEnd -= source.getBytes(m_strCharsetName).length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	protected void pageUp(List<String> tList)
	{
		byte[] arrayOfByte = readParagraphBack(m_mbBufBegin);
		m_mbBufBegin -= arrayOfByte.length;
		try {
			String source = new String(arrayOfByte, m_strCharsetName);
			while(tList.size()<mLineCount)
			{
				int len = mPaint.breakText(source, false, mVisibleWidth, null);
				int strLen = source.length();
				String subString = source.substring(strLen-len);
				int pos = subString.lastIndexOf(AppData.REPLACE_STR);
				if(pos!=-1)
				{
					subString = subString.substring(pos+AppData.REPLACE_STR_LEN);
					source = source.substring(0, pos);
				}
				else
					source = source.substring(0, strLen-len);
				tList.add(0, subString);
				if(source.length()<=0)
					break;
			}
			m_mbBufBegin += source.getBytes(m_strCharsetName).length;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			m_mbBufEnd = m_mbBufBegin;
			pageUp(m_lines);
		}
	}
	
	public void nextPage()
	{
		if(m_mbBufEnd>=m_mbBufLen)
		{
			m_mbBufEnd = m_mbBufLen;
			m_isLastPage = true;
		}
		else
		{
			m_isLastPage = false;
			m_lines.clear();
			m_mbBufBegin = m_mbBufEnd;
			pageDown(m_lines);
		}
	}
	
	public void onDraw(Canvas canvas)
	{
		if(m_lines.size()==0)
			pageDown(m_lines);
		if(m_book_bg==null)
			canvas.drawColor(0xffff9e85);
		else
			canvas.drawBitmap(m_book_bg, 0.0f, 0.0f, null);
		
		int dy = marginUp;
		mPaint.setTextSize(AppData.m_fontSize);
		for(String text: m_lines)
		{
			canvas.drawText(text, marginWidth, dy, mPaint);
			dy += AppData.m_fontSize + mRowSpace;
		}
		
		mPaint.setTextSize(AppData.m_textFontSize);
		int rateLen = 3+ (int)mPaint.measureText("999.9%");
		float ration = (float)(1.0d * m_mbBufBegin/m_mbBufLen);
		String strRation = new DecimalFormat("#0.00").format(100.0f*ration)+"%";
		Calendar calendar = Calendar.getInstance();
		String time = calendar.get(11)+"Ê±"+calendar.get(12)+"·Ö";
		canvas.drawText(time, 10.0f, mHeight-m_downSpace-AppData.m_textFontSize, mPaint);
		canvas.drawText(strRation, mWidth-rateLen, mHeight-m_downSpace-AppData.m_textFontSize, mPaint);
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
	
	protected byte[] readParagraphBack(int end)
	{
		int pageLen = 2048;
		int bufLen = end>pageLen?pageLen:end;
		byte[] arrayOfByte = new byte[bufLen];
		for(int i=0, j=end-bufLen; i<bufLen; i++,j++)
		{
			arrayOfByte[i] = m_mbBuf.get(j);
		}
		return arrayOfByte;
	}
	
	protected byte[] readParagraphForward(int begin)
	{
		int pageLen = 2048;
		int bufLen = m_mbBufLen-begin>pageLen?pageLen:m_mbBufLen-begin;
		byte[] arrayOfByte = new byte[bufLen];
		for(int i=0,j=begin; i<bufLen; i++,j++)
		{
			arrayOfByte[i] = m_mbBuf.get(j);
		}
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
		AppData.m_fontSize = size;
		mLineCount = (int)(mVisibleHeight/(AppData.m_fontSize+mRowSpace));
	}
}
