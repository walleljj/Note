package com.mingrisoft.notes.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.mingrisoft.notes.R;

import java.util.Timer;
import java.util.TimerTask;

public class TouchView extends View {
	private Bitmap  mBitmap,myBitmap;
	private Canvas  mCanvas;
	private Path    mPath;
	private Paint   mBitmapPaint;
	private Paint mPaint;
	private Handler bitmapHandler;
	GetCutBitmapLocation getCutBitmapLocation;
	private Timer timer;
	DisplayMetrics dm;
	private int w,h;
	private int currentColor = Color.RED;
	private int currentSize = 15;
	//画笔颜色
	private int[] paintColor = {
			Color.RED,
			Color.BLUE,
			Color.BLACK,
			Color.GREEN,
			Color.YELLOW,
			Color.CYAN,
			Color.LTGRAY
	};

	public TouchView(Context context) {
		super(context);
		dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		w = dm.widthPixels;
		h = dm.heightPixels;
		initPaint();
	}

	public TouchView(Context context, AttributeSet attrs) {
		super(context,attrs);
		dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		w = dm.widthPixels;
		h = dm.heightPixels;
		initPaint();
	}
	//设置handler
	public void setHandler(Handler mBitmapHandler){
		bitmapHandler = mBitmapHandler;
	}

	//初始化画笔，画布
	private void initPaint(){
		//设置画笔样式
		setPaintStyle();
		getCutBitmapLocation = new GetCutBitmapLocation();
		//画布大小
		mBitmap = Bitmap.createBitmap(w, h,
				Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
		mCanvas.drawColor(Color.TRANSPARENT);
		mPath = new Path();
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		timer = new Timer(true);
	}

	//设置画笔样式
	public void setPaintStyle(){
		mPaint = new Paint();//创建画笔
		// 创建线条画笔
		mPaint.setAntiAlias(true);
		//防止画图边缘锯齿
		mPaint.setDither(true);
        //设置画笔为空心
		mPaint.setStyle(Paint.Style.STROKE);
		//画笔接洽点类型 如影响矩形但角的外轮廓
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		//画笔笔刷类型 如影响画笔但始末端
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		//设置画笔宽
		mPaint.setStrokeWidth(currentSize);
		//设置画笔颜色
		mPaint.setColor(currentColor);
	}


	//设置画笔的大小
	public void selectHandWritetSize(int which){
		//设置size值
		int size =Integer.parseInt(this.getResources().getStringArray(R.array.paintsize1)[which]);
		currentSize = size;//设置画笔大小
		//设置画笔样式
		setPaintStyle();
	}

	//设置画笔颜色
	public void selectHandWriteColor(int which){
		//设置画笔颜色
		currentColor = paintColor[which];
		//设置画笔样式
		setPaintStyle();
	}

	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					myBitmap = getCutBitmap(mBitmap);
					Message message = new Message();
					message.what=1;
					Bundle bundle = new Bundle();;
					bundle.putParcelable("bitmap",myBitmap);
					message.setData(bundle);
					bitmapHandler.sendMessage(message);
					RefershBitmap();
					break;
			}
			super.handleMessage(msg);
		}
	};


	TimerTask task = new TimerTask() {
		public void run() {
			Message message = new Message();
			message.what=1;
			Log.i("线程", "来了");
			handler.sendMessage(message);
		}
	};

	//切割画布中的字并返回切割后图片
	public Bitmap getCutBitmap(Bitmap mBitmap){
		//得到手写字的四周位置，并向外延伸10px
		float cutLeft = getCutBitmapLocation.getCutLeft() - 10;
		float cutTop = getCutBitmapLocation.getCutTop() - 10;
		float cutRight = getCutBitmapLocation.getCutRight() + 10;
		float cutBottom = getCutBitmapLocation.getCutBottom() + 10;
		cutLeft = (0 > cutLeft ? 0 : cutLeft);
		cutTop = (0 > cutTop ? 0 : cutTop);
		cutRight = (mBitmap.getWidth() < cutRight ? mBitmap.getWidth() : cutRight);
		cutBottom = (mBitmap.getHeight() < cutBottom ? mBitmap.getHeight() : cutBottom);
		//取得手写的的高度和宽度
		float cutWidth = cutRight - cutLeft;
		float cutHeight = cutBottom - cutTop;
		Bitmap cutBitmap = Bitmap.createBitmap(mBitmap, (int)cutLeft, (int)cutTop, (int)cutWidth, (int)cutHeight);
		if (myBitmap!=null ) {
			myBitmap.recycle();
			myBitmap= null;
		}
		return cutBitmap;
	}
	//刷新画布
	private void RefershBitmap(){
		initPaint();
		invalidate();
		if(task != null)
			task.cancel();
	}
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);     //显示旧的画布
		canvas.drawPath(mPath, mPaint);  //画最后的path
	}

	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;

	//手按下时
	private void touch_start(float x, float y) {
		mPath.reset();//清空path
		mPath.moveTo(x, y);
		mX = x;
		mY = y;
		if(task != null)
			task.cancel();//取消之前的任务
		task = new TimerTask() {

			@Override
			public void run() {
				Message message = new Message();
				message.what=1;
				Log.i("线程", "来了");
				handler.sendMessage(message);
			}
		};
		getCutBitmapLocation.setCutLeftAndRight(mX,mY);
	}
	//手移动时
	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mPath.quadTo(mX, mY, x, y);
			// mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
			mX = x;
			mY = y;
			if(task != null)
				task.cancel();//取消之前的任务
			task = new TimerTask() {
				@Override
				public void run() {
					Message message = new Message();
					message.what=1;
					Log.i("线程", "来了");
					handler.sendMessage(message);
				}
			};
			getCutBitmapLocation.setCutLeftAndRight(mX,mY);

		}
	}
	//手抬起时
	private void touch_up() {
		//mPath.lineTo(mX, mY);
		mCanvas.drawPath(mPath, mPaint);
		mPath.reset();
		if (timer!=null) {
			if (task!=null) {
				task.cancel();
				task = new TimerTask() {
					public void run() {
						Message message = new Message();
						message.what = 1;
						handler.sendMessage(message);
					}
				};
				timer.schedule(task, 1000, 1000);				//2200秒后发送消息给handler更新Activity
			}
		}else {
			timer = new Timer(true);
			timer.schedule(task, 1000, 1000);					//2200秒后发送消息给handler更新Activity
		}
	}
	//处理界面手势事件
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();//手指在屏幕上x坐标
		float y = event.getY();//手指在屏幕上y坐标
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN://手指在屏幕按下
				touch_start(x, y);//设置开始坐标
				invalidate(); //刷新
				break;
			case MotionEvent.ACTION_MOVE://手指在屏幕移动
				touch_move(x, y);//设置移动坐标
				invalidate();//刷新
				break;
			case MotionEvent.ACTION_UP://手指在屏幕离开
				touch_up();//手指抬起
				invalidate();//刷新
				break;
		}
		return true;
	}
}

