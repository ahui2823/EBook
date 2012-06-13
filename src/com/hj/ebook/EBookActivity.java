package com.hj.ebook;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.hj.view.AppData;
import com.hj.view.BookPageFactory;
import com.hj.view.PageWidget;

public class EBookActivity extends Activity {
	
	private int mWinWidth;
	private int mWinHeight;
	
	private PageWidget mPageWidget;
	private Canvas mCurPageCanvas;
	private Canvas mNextPageCanvas;
	
	private BookPageFactory mPageFactory;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mWinWidth = dm.widthPixels;
        mWinHeight = dm.heightPixels;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.ALPHA_CHANGED, WindowManager.LayoutParams.ALPHA_CHANGED);
        mPageWidget = new PageWidget(this);
        mPageWidget.setScreen(mWinWidth, mWinHeight);
        setContentView(mPageWidget);
        if(AppData.mCurPageBitmap==null)
        	AppData.mCurPageBitmap = Bitmap.createBitmap(mWinWidth, mWinHeight, Config.ARGB_8888);
        if(AppData.mNextPageBitmap==null)
        	AppData.mNextPageBitmap = Bitmap.createBitmap(mWinWidth, mWinHeight, Config.ARGB_8888);
        mCurPageCanvas = new Canvas(AppData.mCurPageBitmap);
        mNextPageCanvas = new Canvas(AppData.mNextPageBitmap);
        
        mPageFactory = new BookPageFactory(mWinWidth, mWinHeight);
        mPageFactory.setFontSize(AppData.m_fontSize);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        if(AppData.bg==null)
        	AppData.bg = Bitmap.createScaledBitmap(bitmap, mWinWidth, mWinHeight, true);
        mPageFactory.setBgBitmap(AppData.bg);
        mPageFactory.openBook(getAssets(), "08.src");
        mPageFactory.onDraw(mCurPageCanvas);
        mPageWidget.setBitmaps(AppData.mCurPageBitmap, AppData.mNextPageBitmap);
        mPageWidget.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					mPageWidget.abortAnimation();
					mPageWidget.calcCornerXY(event.getX(), event.getY());
					mPageFactory.onDraw(mCurPageCanvas);
					if(!mPageWidget.dragToRight())
					{
						mPageFactory.nextPage();
						if(mPageFactory.isLastPage())
							Toast.makeText(EBookActivity.this, "已经是最后一页了", Toast.LENGTH_SHORT).show();
					}
					else
					{
						mPageFactory.prePage();
						if(mPageFactory.isFirstPage())
						{
							Toast.makeText(EBookActivity.this, "已经是第一页了", Toast.LENGTH_SHORT).show();
							return false;
						}
					}
					mPageFactory.onDraw(mNextPageCanvas);
				}
				mPageWidget.setBitmaps(AppData.mCurPageBitmap, AppData.mNextPageBitmap);
				return mPageWidget.doTouchEvent(event);
			}});
    }
}