package com.test.trendview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class TrendView extends View {

	private Context mContext;
	private Paint mPaint = null;
	private int w = 0;
	private int h = 0;

	private ArrayList<Holder> dataHolder = null;
	private PointF mTouchPoint = new PointF();

	private int coordinate_pading_h = 20;// 坐标间视图的水平间距，可以由此调节坐标轴的宽度
	private int coordinate_pading_v = 80;// 视图上边距，TreendView顶部区域，可以由此调节坐标轴的
	private float coordinate_pading_v_percent = 0.3f;
	private LineMoveListener moveListener;
	private int intDeltaBase = 0;// 价格相差更大的话，减去一个基数
	private int empty_value = -1;// 空值

	private int label_text_size = 25;
	private int paintSolideBig = 5;// 画笔粗细
	private int paintSolideSmall = 2;
	private int radiusBig = 10;
	private int radiusSmall = 4;
	private int max=1000;

	public TrendView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		this.mPaint = new Paint();
		mPaint.setAntiAlias(true);
		this.dataHolder = new ArrayList<Holder>();
		// TypedArray array =
		// context.obtainStyledAttributes(attrs,R.styleable.TreendView);
		// label_text_size=(int)array.getDimension(R.styleable.TreendView_textSize,
		// label_text_size);//标题文字大小
		// paintSolideBig=(int)array.getDimension(R.styleable.TreendView_paintSolideBig,
		// paintSolideBig);//画笔粗细
		// paintSolideSmall=(int)array.getDimension(R.styleable.TreendView_paintSolideSmall,
		// paintSolideSmall);
		// radiusBig=(int)array.getDimension(R.styleable.TreendView_radiusBig,
		// radiusBig);//大圆圈半径
		// radiusSmall=(int)array.getDimension(R.styleable.TreendView_radiusSmall,
		// radiusSmall);
		// array.recycle();
		setInitTrendView(createTestData());
	}

	private ArrayList<Holder> createTestData() {
		ArrayList<Holder> holders = new ArrayList<Holder>();
		for (int i = 0; i < 30; i++) {
			Holder holer = new Holder("xx" + i, 100+rand()*i);
			holders.add(holer);
		}
		return holders;
	}

	private int rand() {
		Random rdn = new Random();
		return rdn.nextInt(30);
	}

	/**
	 * 设置显示值
	 * 
	 * @param hoderlist
	 */
	public void setInitTrendView(ArrayList<Holder> hoderlist) {
		if (null == hoderlist || hoderlist.isEmpty()) {
			return;
		}
		// ============找出
		if (null != hoderlist && !hoderlist.isEmpty()) {
			this.dataHolder=hoderlist;
			invalidate();
		} else {

		}
	}

	/**
	 * 画网格背景
	 * 
	 * @param canvas
	 */
	public void drawGrid(Canvas canvas) {
		mPaint.setColor(0x33dbdbdb);
		// 画垂直网格
		for (int i = 0, isize = dataHolder.size(); i <= isize; i++) {
			canvas.drawLine(w / isize * i, 0, w / isize * i, h, mPaint);
		}
		// 画水平网格
		for (int i = 0, isize = dataHolder.size(); i <= isize; i++) {
			canvas.drawLine(0, h / dataHolder.size() * i, w, h / dataHolder.size() * i, mPaint);
		}
	}

	/**
	 * 绘制趋势图
	 */
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		w = getWidth();
		h = getHeight();
		this.coordinate_pading_v = (int) (h * this.coordinate_pading_v_percent);
		if (null == this.dataHolder || this.dataHolder.isEmpty()) {
			return;
		}
		canvas.drawColor(Color.WHITE);
		mPaint.setStrokeWidth(paintSolideSmall);
		mPaint.setTextSize(label_text_size);
		// 画背景网格
		drawGrid(canvas);
		// ========================================
		mPaint.setColor(Color.RED);
		int cell_width = (w - coordinate_pading_h * 2) / (dataHolder.size() - 1);// 单元格宽度
		int mic_pading = 5;// 微调，使整个坐标轴在视图的中间
		
		int highestPrice=max;
		mPaint.setColor(Color.LTGRAY);
		Path p = new Path();
		p.reset();

		ArrayList<MyPoint> pricePointlist = new ArrayList<MyPoint>();
		// p.moveTo(coordinate_pading_h + mic_pading, h);// 从坐标轴0,0点开始画
		float price_gradient_pix = (float) (h - coordinate_pading_v) / (highestPrice/*-intDeltaBase*/);// 每个价格单位代表多少像素
		for (int i = 0, isize = dataHolder.size(); i < isize; i++) {
			Holder holder = dataHolder.get(i);
			if (empty_value != holder.getValue()) {// 不是空值
				// 计算x所在点
				int x = coordinate_pading_h + i * cell_width + mic_pading;// 注意加边距
				int y = h - (int) (holder.getValue() * price_gradient_pix);
				mPaint.setColor(0xff388ec7);
				pricePointlist.add(new MyPoint(x, y, holder));
				if (pricePointlist.size() == 0) {
					p.moveTo(x, y);
				} else {
					p.lineTo(x, y);
				}
			}
		}

		p.lineTo(pricePointlist.get(pricePointlist.size() - 1).x, h);// 右下角的点
		p.lineTo(pricePointlist.get(0).x, h);
		p.lineTo(pricePointlist.get(0).x, pricePointlist.get(0).y);
		// p.lineTo(coordinate_pading_h + mic_pading, h);// 回到(0,0)

		p.close();
		mPaint.setStyle(Style.FILL);
		mPaint.setColor(0x88b3e2f0);
		canvas.drawPath(p, mPaint);
		// mPaint.setStyle(Style.STROKE);
		// ====设置价格点间隔线
		// 画顶部粗体线段
		for (int i = 0, isize = pricePointlist.size(); i < isize - 1; i++) {
			mPaint.setStrokeWidth(paintSolideBig);
			mPaint.setColor(0xff388ec7);
			Point tempPoint = pricePointlist.get(i);
			Point tempPoint1 = pricePointlist.get(i + 1);
			canvas.drawLine(tempPoint.x, tempPoint.y, tempPoint1.x, tempPoint1.y, mPaint);

			canvas.drawCircle(tempPoint.x, tempPoint.y, radiusBig, mPaint);
			mPaint.setStrokeWidth(paintSolideSmall);
			mPaint.setColor(Color.WHITE);
			canvas.drawCircle(tempPoint.x, tempPoint.y, radiusSmall, mPaint);
		}
		// ==画最后一个点
		Point lastPoint = pricePointlist.get(pricePointlist.size() - 1);
		mPaint.setStrokeWidth(paintSolideBig);
		mPaint.setColor(0xff388ec7);
		canvas.drawCircle(lastPoint.x, lastPoint.y, radiusBig, mPaint);
		mPaint.setStrokeWidth(paintSolideSmall);
		mPaint.setColor(Color.WHITE);
		canvas.drawCircle(lastPoint.x, lastPoint.y, radiusSmall, mPaint);
		// =============画最后一个点end=============================

		// ==============画黄色的移动线段==========
		MyPoint nearPoint = (MyPoint) findNearestPoint(pricePointlist)[0];
		mPaint.setColor(0xffff6600);
		mPaint.setStrokeWidth(paintSolideBig);
		canvas.drawLine(nearPoint.x, 0, nearPoint.x, nearPoint.y, mPaint);
		canvas.drawLine(0, 0, w, 0, mPaint);
		mPaint.setStyle(Style.FILL);
		canvas.drawCircle(nearPoint.x, nearPoint.y, radiusBig, mPaint);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(paintSolideSmall);
		if (null != moveListener) {
			Holder h = nearPoint.getHolder();
			Holder temHolder = h.cloneSelf();
			temHolder.setValue(temHolder.getValue() + intDeltaBase);
			moveListener.onMove(temHolder);
		}
		// ==============画黄色的移动线段 end==========
	}

	/**
	 * 找到最近的点
	 * 
	 * @param pointlist
	 * @return
	 */
	public Object[] findNearestPoint(ArrayList<MyPoint> pointlist) {
		Point p = null;
		Integer index = 0;
		float deltax = Integer.MAX_VALUE;
		for (int i = 0, isize = pointlist.size(); i < isize; i++) {
			Point tempPoint = pointlist.get(i);
			float distance = Math.abs(mTouchPoint.x - tempPoint.x);
			if (distance < deltax) {
				deltax = distance;
				p = tempPoint;
				index = i;
			}
		}
		Object[] data = { p, index };
		return data;
	}

	/**
	 * 查找最高点和最低点
	 * 
	 * @param pointlist
	 * @return
	 */
	public MyPoint[] findMaxAndLowersPoint(ArrayList<MyPoint> pointlist) {
		MyPoint ph = null;
		MyPoint pl = null;
		for (int i = 0, isize = pointlist.size(); i < isize; i++) {
			MyPoint tempP = pointlist.get(i);
			// 查找最高点
			if (null == ph) {
				ph = tempP;
			} else {
				if (tempP.y < ph.y) {
					ph = tempP;
				}
			}
			// ===查找最低点
			if (null == pl) {
				pl = tempP;
			} else {
				if (tempP.y > pl.y) {
					pl = tempP;
				}
			}
		}
		MyPoint[] estPoint = { ph, pl };
		return estPoint;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mTouchPoint.x = event.getX();
		mTouchPoint.y = event.getY();
		invalidate();
		return true;
	}

	public LineMoveListener getMoveListener() {
		return moveListener;
	}

	public void setMoveListener(LineMoveListener moveListener) {
		this.moveListener = moveListener;
	}

	public static interface LineMoveListener {
		public void onMove(Holder holder);
	}

	public static interface DrawCallBack {
		public void onDrawFinished();
	}

}
