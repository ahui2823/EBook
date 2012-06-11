package com.hj.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

public class PageWidget extends View{
	private static final int DURATION = 1200;
	private PointF mBezierControl1 = new PointF();
	private PointF mBezierControl2 = new PointF();
	private PointF mBezierEnd1 = new PointF();
	private PointF mBezierEnd2 = new PointF();
	private PointF mBezierStart1 = new PointF();
	private PointF mBezierStart2 = new PointF();
	private PointF mBezierVertex1 = new PointF();
	private PointF mBezierVertex2 = new PointF();
	
	private ColorMatrixColorFilter mColorMatrixFilter;
	
	private int mCornerX = 0;
	private int mCornerY = 0;
	private Bitmap mCurPageBitmap = null;
	private float mDegrees;
	
	private int[] mBackShadowColors;
	private int[] mFrontShadowColors;
	private GradientDrawable mBackShadowDrawableLR;
	private GradientDrawable mBackShadowDrawableRL;
	private GradientDrawable mFolderShadowDrawableLR;
	private GradientDrawable mFolderShadowDrawableRL;
	private GradientDrawable mFrontShadowDrawableHBT;
	private GradientDrawable mFrontShadowDrawableHTB;
	private GradientDrawable mFrontShadowDrawableVLR;
	private GradientDrawable mFrontShadowDrawableVRL;
	
	private int mHeight = 800;
	private int mWidth = 480;
	private boolean mIsRTandLB;
	private Matrix mMatrix;
	private float[] mMatrixArray;
	private float mMaxLength;
	private float mMiddleX;
	private float mMiddleY;
	private Bitmap mNextPageBitmap = null;
	private Paint mPaint;
	
	private Path mPath0;
	private Path mPath1;
	
	private Scroller mScroller;
	private PointF mTouch = new PointF();
	private float mTouchToCornerDis;
	
	public PageWidget(Context context) {
		super(context);
		mMatrixArray = new float[]{
				0.0f, 0.0f, 0.0f,
				0.0f, 0.0f, 0.0f,
				0.0f, 0.0f, 1.0f
		};
		mPath0 = new Path();
		mPath1 = new Path();
		createDrawable();
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.set(new float[]{
				0.55f, 0.0f, 0.0f, 0.0f, 80.0f,
				0.0f, 0.55f, 0.0f, 0.0f, 80.0f,
				0.0f, 0.0f, 0.55f, 0.0f, 80.0f,
				0.0f, 0.0f, 0.0f, 0.2f, 0.0f
		});
		mColorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
		mMatrix = new Matrix();
		mScroller = new Scroller(getContext());
		mTouch.x = 0.01f;
		mTouch.y = 0.01f;
	}
	
	private void calcPoints()
	{
		mMiddleX = (mTouch.x+mCornerX)/2.0f;
		mMiddleY = (mTouch.y+mCornerY)/2.0f;
		mBezierControl1.x = (mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX));
	    mBezierControl1.y = mCornerY;
	    mBezierControl2.x = mCornerX;
	    mBezierControl2.y = (mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY));
	    mBezierStart1.x = (mBezierControl1.x - (mCornerX - mBezierControl1.x) / 2.0F);
	    mBezierStart1.y = mCornerY;
	    if ((mTouch.x > 0.0F) && (mTouch.x < mWidth) && ((mBezierStart1.x < 0.0F) || (mBezierStart1.x > mWidth)))
	    {
	    	if (mBezierStart1.x < 0.0F)
				mBezierStart1.x = (mWidth - mBezierStart1.x);
			float f1 = Math.abs(mCornerX - mTouch.x);
			float f2 = f1 * mWidth / mBezierStart1.x;
			mTouch.x = Math.abs(mCornerX - f2);
			float f3 = Math.abs(mCornerX - mTouch.x) * Math.abs(mCornerY - mTouch.y) / f1;
			mTouch.y = Math.abs(mCornerY - f3);
			mMiddleX = ((mTouch.x + mCornerX) / 2.0F);
			mMiddleY = ((mTouch.y + mCornerY) / 2.0F);
			mBezierControl1.x = (mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX));
			mBezierControl1.y = mCornerY;
			mBezierControl2.x = mCornerX;
			mBezierControl2.y = (mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY));
			mBezierStart1.x = (mBezierControl1.x - (mCornerX - mBezierControl1.x) / 2.0F);
	    }
	    mBezierStart2.x = mCornerX;
	    mBezierStart2.y = (mBezierControl2.y - (mCornerY - mBezierControl2.y) / 2.0F);
	    mTouchToCornerDis = (float)Math.hypot(mTouch.x - mCornerX, mTouch.y - mCornerY);
	    mBezierEnd1 = getCross(mTouch, mBezierControl1, mBezierStart1, mBezierStart2);
	    mBezierEnd2 = getCross(mTouch, mBezierControl2, mBezierStart1, mBezierStart2);
	    mBezierVertex1.x = ((mBezierStart1.x + 2.0F * mBezierControl1.x + mBezierEnd1.x) / 4.0F);
	    mBezierVertex1.y = ((2.0F * mBezierControl1.y + mBezierStart1.y + mBezierEnd1.y) / 4.0F);
	    mBezierVertex2.x = ((mBezierStart2.x + 2.0F * mBezierControl2.x + mBezierEnd2.x) / 4.0F);
	    mBezierVertex2.y = ((2.0F * mBezierControl2.y + mBezierStart2.y + mBezierEnd2.y) / 4.0F);
	}
	
	private void drawCurrentPageArea(Canvas canvas, Bitmap bitmap, Path path)
	{
		mPath0.reset();
		mPath0.moveTo(mBezierStart1.x, mBezierStart1.y);
		mPath0.quadTo(mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x, mBezierEnd1.y);
		mPath0.lineTo(mTouch.x, mTouch.y);
		mPath0.lineTo(mBezierEnd2.x, mBezierEnd2.y);
		mPath0.quadTo(mBezierControl2.x, mBezierControl2.y, mBezierStart2.x, mBezierStart2.y);
		mPath0.lineTo(mCornerX, mCornerY);
		mPath0.close();
		canvas.save();
		canvas.clipPath(path, Region.Op.XOR);
		canvas.drawBitmap(bitmap, 0.0f, 0.0f, null);
		canvas.restore();
	}
	
	private void drawNextPageAreaAndShadow(Canvas canvas, Bitmap bitmap)
	{
		mPath1.reset();
		mPath1.moveTo(mBezierStart1.x, mBezierStart1.y);
		mPath1.lineTo(mBezierVertex1.x, mBezierVertex1.y);
		mPath1.lineTo(mBezierVertex2.x, mBezierVertex2.y);
		mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
		mPath1.lineTo(mCornerX, mCornerY);
		mPath1.close();
		
		mDegrees = (float)Math.toDegrees(Math.atan2(mBezierControl1.x - mCornerX, mBezierControl2.y - mCornerY));
		
		canvas.save();
		canvas.clipPath(mPath0);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		canvas.drawBitmap(bitmap, 0.0f, 0.0f, null);
		canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
		mBackShadowDrawableLR.setBounds((int)mBezierStart1.x, (int)mBezierStart1.y, 
				(int)(mBezierStart1.x + mTouchToCornerDis/4.0f), (int)(mMaxLength + mBezierStart1.y));
		mBackShadowDrawableRL.draw(canvas);
		canvas.restore();
	}
	
	public void drawCurrentPageShadow(Canvas canvas)
	{
		double radian = Math.PI/4 - Math.atan2(mBezierControl1.y - mTouch.y, mTouch.x - mBezierControl1.x);
		double d2 = 35.35d * Math.cos(radian);
		double d3 = 35.35d * Math.signum(radian);
		float f1 = (float)(d2+mTouch.x);
		float f2 = (float)(d2+mTouch.y);
		float f3;
		mPath1.reset();
		mPath1.moveTo(f1, f2);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierControl1.x, mBezierControl1.y);
		mPath1.lineTo(mBezierStart1.x, mBezierStart1.y);
		mPath1.close();
		canvas.save();
		canvas.clipPath(mPath0, Region.Op.XOR);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		
		int left = (int)mBezierControl1.x;
		int right = 25 + (int)mBezierControl1.x;
		
		canvas.rotate((float)Math.toDegrees(Math.atan2(mTouch.x - mBezierControl1.x, mBezierControl1.y - mTouch.y)), 
				mBezierControl1.x, mBezierControl1.y);
		mFrontShadowDrawableVLR.setBounds(left, (int)(mBezierControl1.y - mMaxLength), 
				right, (int)mBezierControl1.y);
		mFrontShadowDrawableVLR.draw(canvas);
		canvas.restore();
		
		mPath1.reset();
		mPath1.moveTo(f1, f2);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierControl2.x, mBezierControl2.y);
		mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
		mPath1.close();
		canvas.save();
		canvas.clipPath(mPath0, Region.Op.XOR);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		
		int top = (int)mBezierControl2.y;
		int bottom = (int)(25.0f+mBezierControl2.x);
		canvas.rotate((float)Math.toDegrees(Math.atan2(mBezierControl2.y - mTouch.y, mBezierControl2.x - mTouch.x)), 
				mBezierControl2.x, mBezierControl2.y);
		if(mBezierControl2.y>=0.0f)
			f3 = mBezierControl2.y;
		else
			f3 = mBezierControl2.y - mHeight;
		int n = (int)Math.hypot(mBezierControl2.x, f3);
		if(n<=mMaxLength)
			mFrontShadowDrawableHTB.setBounds((int)(mBezierControl2.x - mMaxLength), top, 
					(int)mBezierControl2.x, bottom);
		else
			mFrontShadowDrawableHTB.setBounds((int)(mBezierControl2.x - 25.0F)-n, top, 
					(int)(mBezierControl2.x + mMaxLength)-n, bottom);
	}
	
	public void setBitmaps(Bitmap bmp1, Bitmap bmp2)
	{
		mCurPageBitmap = bmp1;
		mNextPageBitmap = bmp2;
	}
	
	public boolean abortAnimation()
	{
		if(mCornerX>0)
			return true;
		else
			return false;
	}
	
	public void calcCornerXY(float width, float height)
	{
		if(width<=mWidth/2)
		{
			mCornerX = 0;
			if(height>mHeight/2)
				mIsRTandLB = true;
			mCornerY = 0;
		}
	}
	
	public void computeScroll()
	{
		super.computeScroll();
		if(mScroller.computeScrollOffset())
		{
			mTouch.x = mScroller.getCurrX();
			mTouch.y = mScroller.getCurrY();
			postInvalidate();
		}
	}
	
	public void setScreen(int width, int height)
	{
		mWidth = width;
		mHeight = height;
		mMaxLength = (float)Math.hypot(mWidth, mHeight);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(0xffaaaaaa);
		calcPoints();
		drawCurrentPageArea(canvas, mCurPageBitmap, mPath0);
		drawNextPageAreaAndShadow(canvas, mNextPageBitmap);
		drawCurrentPageShadow(canvas);
		drawCurrentBackArea(canvas, mCurPageBitmap);
	}
	
	private void drawCurrentBackArea(Canvas canvas, Bitmap bitmap)
	{
		float f1 = Math.min(Math.abs((int)(mBezierStart1.x + mBezierControl1.x)/2-mBezierControl1.x), 
				Math.abs((int)(mBezierStart2.y + mBezierControl2.y)/2-mBezierControl2.y));
	    mPath1.reset();
	    mPath1.moveTo(mBezierVertex2.x, mBezierVertex2.y);
	    mPath1.lineTo(mBezierVertex1.x, mBezierVertex1.y);
	    mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y);
	    mPath1.lineTo(mTouch.x, mTouch.y);
	    mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y);
	    mPath1.close();
	    
	    int left = (int)(mBezierStart1.x - 1.0f);
	    int right = (int)(1.0F + (f1 + mBezierStart1.x));
	    
	    canvas.save();
	    canvas.clipPath(mPath1, Region.Op.INTERSECT);
	    mPaint.setColorFilter(mColorMatrixFilter);
	    float f2 = (float)Math.hypot(mCornerX - mBezierControl1.x, mBezierControl2.y - mCornerY);
		float f3 = (mCornerX - mBezierControl1.x) / f2;
		float f4 = (mBezierControl2.y - mCornerY) / f2;
		mMatrixArray[0] = (1.0F - f4 * (2.0F * f4));
		mMatrixArray[1] = (f4 * (2.0F * f3));
		mMatrixArray[3] = mMatrixArray[1];
		mMatrixArray[4] = (1.0F - f3 * (2.0F * f3));
		mMatrix.reset();
		mMatrix.setValues(mMatrixArray);
		mMatrix.preTranslate(-mBezierControl1.x, -mBezierControl1.y);
		mMatrix.postTranslate(mBezierControl1.x, mBezierControl1.y);
		canvas.drawBitmap(bitmap, mMatrix, mPaint);
		mPaint.setColorFilter(null);
		canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
		mFolderShadowDrawableLR.setBounds(left, (int)mBezierStart1.y, right, (int)(mBezierStart1.y + mMaxLength));
		mFolderShadowDrawableLR.draw(canvas);
		canvas.restore();
	}
	
	public PointF getCross(PointF pf1, PointF pf2, PointF pf3, PointF pf4)
	{
		PointF pf = new PointF();
		float f1 = (pf2.y - pf1.y) / (pf2.x - pf1.x);
	    float f2 = (pf1.x * pf2.y - pf2.x * pf1.y) / (pf1.x - pf2.x);
	    float f3 = (pf4.y - pf3.y) / (pf4.x - pf3.x);
	    float f4 = (pf3.x * pf4.y - pf4.x * pf3.y) / (pf3.x - pf4.x);
	    pf.x = ((f4 - f2) / (f1 - f3));
	    pf.y = (f2 + f1 * pf.x);
	    return pf;
	}
	
	private void createDrawable()
	{
		int[] colors = new int[]{0x00333333, 0xb0333333};
		mFolderShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors);
		mFolderShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		mFolderShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
		mFolderShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		
		mBackShadowColors = new int[]{0xff111111, 0x00111111};
		mBackShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mBackShadowColors);
		mBackShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		mBackShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
		mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		
		mFrontShadowColors = new int[]{0x80111111, 0x00111111};
		mFrontShadowDrawableVLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mFrontShadowColors);;
		mFrontShadowDrawableVLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		mFrontShadowDrawableVRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mFrontShadowColors);;
		mFrontShadowDrawableVRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		mFrontShadowDrawableHTB = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, mFrontShadowColors);;
		mFrontShadowDrawableHTB.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		mFrontShadowDrawableHBT = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, mFrontShadowColors);;
		mFrontShadowDrawableHBT.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		
	}

	public boolean doTouchEvent(MotionEvent event)
	{
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			mTouch.x = event.getX();
			mTouch.y = event.getY();
		}
		else if(event.getAction() == MotionEvent.ACTION_UP)
		{
			if(!canDragOver())
			{
				mTouch.x = mCornerX-0.09f;
				mTouch.y = mCornerY-0.09f;
				postInvalidate();
			}
			else
				startAnimation(1200);
		}
		else if(event.getAction() == MotionEvent.ACTION_MOVE)
		{
			mTouch.x = event.getX();
			mTouch.y = event.getY();
			postInvalidate();
		}
		return true;
	}
	
	private void startAnimation(int duration)
	{
		if(mCornerX>0)
		{
			int dx = -(int)(mWidth+mTouch.x);
			int dy = (int)(mHeight-mTouch.y);
			mScroller.startScroll((int)mTouch.x, (int)mTouch.y, dx, dy, duration);
		}
	}
	
	public boolean canDragOver()
	{
		if(mTouchToCornerDis>mWidth/10)
			return true;
		else
			return false;
	}
}