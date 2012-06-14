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
	
	public int m_mbBufBegin = 0;
	public int m_mbBufEnd = 0;
	public int m_mbBufLen = 0;
	
	private int mLineCount;
	
	private boolean m_isFirstPage;
	private boolean m_isLastPage;
	private List<String> m_lines = new ArrayList<String>();
	public static int m_downSpace = 0;
	
	private ByteBuffer m_mbBuf;
	private Bitmap m_book_bg;
	private String m_strCharsetName = "GBK";
	
	private int m_backColor = 0xffff9e85;
	
	private boolean isLastGetNext = true;
	
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
	
	public void nextPage()
	{
		if(m_mbBufEnd>=m_mbBufLen)
		{
			m_mbBufEnd = m_mbBufLen;
			m_isLastPage = true;
		}
		else
		{
			isLastGetNext = true;
			m_isLastPage = false;
			m_lines.clear();
			m_mbBufBegin = m_mbBufEnd;
			pageDown(m_lines);
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
			isLastGetNext = false;
			m_isFirstPage = false;
			m_lines.clear();
			m_mbBufEnd = m_mbBufBegin;
			pageUp(m_lines);
		}
	}
	
	public boolean isLastGetNext()
	{
		return isLastGetNext;
	}
	
	public void onDraw(Canvas canvas)
	{
		if(m_lines.size()==0)
			pageDown(m_lines);
		if(m_lines.size()>0)
		{
			if(m_book_bg==null)
				canvas.drawColor(m_backColor);
			else
				canvas.drawBitmap(m_book_bg, 0.0f, 0.0f, null);
			
			int dy = marginUp;
			mPaint.setTextSize(AppData.m_fontSize);
			for(String text: m_lines)
			{
				canvas.drawText(text, marginWidth, dy, mPaint);
				dy += AppData.m_fontSize + mRowSpace;
			}
			
			if(mWidth==240)
				mPaint.setTextSize(6.0f);
			else
				mPaint.setTextSize(AppData.m_textFontSize);
				
			int rateLen = 3+ (int)mPaint.measureText("999.9%");
			float ration = (float)(1.0d * m_mbBufBegin/m_mbBufLen);
			String strRation = new DecimalFormat("#0.00").format(100.0f*ration)+"%";
			Calendar calendar = Calendar.getInstance();
			String time = calendar.get(11)+":"+calendar.get(12);
			canvas.drawText(time, 10.0f, mHeight-m_downSpace-AppData.m_textFontSize, mPaint);
			canvas.drawText(strRation, mWidth-rateLen, mHeight-m_downSpace-AppData.m_textFontSize, mPaint);
			mPaint.setTextSize(AppData.m_fontSize);
		}
	}
	
	public void openBook(AssetManager assetManager, String fileName)
	{
		reset();
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
	
	protected void pageDown(List<String> tList)
	{
		if(m_mbBufEnd>=m_mbBufLen)
			m_mbBufEnd = m_mbBufLen;
		else
		{
			byte[] arrayOfByte = readParagraphForward(m_mbBufEnd);
			m_mbBufEnd += arrayOfByte.length;
			try {
				String source = new String(arrayOfByte, m_strCharsetName);
				int strPos = -1;
				int extraLen = 0;
				while(tList.size()<mLineCount&&source.length()>0)
				{
					strPos = source.indexOf(AppData.REPLACE_STR);
					String subSource = "";
					if(strPos!=-1)
					{
						subSource = source.substring(0, strPos);
						source = source.substring(strPos+AppData.REPLACE_STR_LEN);
					}
					else
					{
						subSource = source;
						source = "";
					}
					
					while(subSource.length()>0)
					{
						int len = mPaint.breakText(subSource, true, mVisibleWidth, null);
						String subString = subSource.substring(0, len);
						subSource = subSource.substring(len);
						tList.add(subString);
						if(tList.size()>=mLineCount)
						{
							extraLen += subSource.getBytes(m_strCharsetName).length;
							break;
						}
					}
				}
				m_mbBufEnd -= source.getBytes(m_strCharsetName).length;
				if(extraLen!=0)
					m_mbBufEnd -= (extraLen+(strPos!=-1?2:0));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void pageUp(List<String> tList)
	{
		if(m_mbBufBegin<0)
			m_mbBufBegin = 0;
		else
		{
			byte[] arrayOfByte = readParagraphBack(m_mbBufBegin);
			m_mbBufBegin -= arrayOfByte.length;
			try {
				String source = new String(arrayOfByte, m_strCharsetName);
				int extraLen = 0;
				int strPos = -1;
				while(tList.size()<mLineCount&&source.length()>0)
				{
					strPos = source.lastIndexOf(AppData.REPLACE_STR);
					String subSource = "";
					if(strPos!=-1)
					{
						subSource = source.substring(strPos+AppData.REPLACE_STR_LEN);
						source = source.substring(0, strPos);
					}
					else
					{
						subSource = source;
						source = "";
					}
					int pos = 0;
					while(subSource.length()>0)
					{
						int len = mPaint.breakText(subSource, true, mVisibleWidth, null);
						String subString = subSource.substring(0, len);
						subSource = subSource.substring(len);
						if(tList.size()>=mLineCount)
						{
							String tmp = tList.get(0);
							extraLen += tmp.getBytes(m_strCharsetName).length;
							tList.remove(0);
							pos--;
						}
						tList.add(pos, subString);
						pos++;
					}
				}
				m_mbBufBegin += source.getBytes(m_strCharsetName).length;
				if(extraLen!=0)
					m_mbBufBegin += (extraLen + (strPos!=-1?2:0));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected byte[] readParagraphBack(int end)
	{
		int bufLen = end>AppData.BUFFER_LEN?AppData.BUFFER_LEN:end;
		byte[] arrayOfByte = new byte[bufLen];
		for(int i=0, j=end-bufLen; i<bufLen; i++,j++)
		{
			arrayOfByte[i] = m_mbBuf.get(j);
		}
		return arrayOfByte;
	}
	
	protected byte[] readParagraphForward(int begin)
	{
		int bufLen = m_mbBufLen-begin>AppData.BUFFER_LEN?AppData.BUFFER_LEN:m_mbBufLen-begin;
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
		m_mbBufLen = 0;
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
