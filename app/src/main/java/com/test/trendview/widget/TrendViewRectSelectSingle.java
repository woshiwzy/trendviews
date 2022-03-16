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

public class TrendViewRectSelectSingle extends View {

	private Context mContext;
	private Paint mPaint = null;

	private int w = 0;
	private int h = 0;

	private ArrayList<Holder> mHolderLocal = null;
	private PointF mTouchPoint = new PointF();

	private int paddingLeftRight = 30;// 坐标间视图的水平间距，可以由此调节坐标轴的宽度
	private int paddingTop = 20;// 视图上边距，TreendView顶部区域，可以由此调节坐标轴的
	private int padingBottom = 54;

	private float highest = 0;// 最大值
	private float lowerest = 0;// 最小值
	private LineMoveListener moveListener;
	private boolean isSetting = true;// 是否是手动设值
	private boolean haveCicle = false;

	private int label_text_size = 25;
	private int radiusBig = 10;
	private int paintSolideSmall = 4;
	private int radiusSmall = 6;
	private String titlle = "";
	private int month = 12;
	private ArrayList<MyPoint> mPointsLocal = null;
	private boolean isShowAllStamp = false;
	private int selectIndex = -1;
	private int color_local = 0xffD51203;
	final static int[] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };

	public TrendViewRectSelectSingle(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		this.mPaint = new Paint();
		mPaint.setAntiAlias(true);
		this.mHolderLocal = new ArrayList<Holder>();
		this.mPointsLocal = new ArrayList<MyPoint>();

		// TypedArray array = context.obtainStyledAttributes(attrs,
		// R.styleable.TreendView);
		// label_text_size = (int)
		// array.getDimension(R.styleable.TreendView_textSize,
		// label_text_size);// 标题文字大小
		// paintSolideBig = (int)
		// array.getDimension(R.styleable.TreendView_paintSolideBig,paintSolideBig);//
		// 画笔粗细
		// radiusBig = (int)
		// array.getDimension(R.styleable.TreendView_radiusBig, radiusBig);//
		// 大圆圈半径
		// paintSolideSmall = (int)
		// array.getDimension(R.styleable.TreendView_paintSolideSmall,
		// paintSolideSmall);
		// radiusSmall = (int)
		// array.getDimension(R.styleable.TreendView_radiusSmall, radiusSmall);
		// array.recycle();
		setInitTrendViewFade();
	}

	@Deprecated
	public void setInitTrendViewFade() {
		Object fade[] = createFadeData();
		setInitTrendView(false, (ArrayList<Holder>) fade[0]);
	}

	public void setInitTrendView(boolean isShowAllStamp, ArrayList<Holder> hoderlistLocal) {
		this.setShowAllStamp(isShowAllStamp);

		if (null != hoderlistLocal && !hoderlistLocal.isEmpty()) {

			this.mHolderLocal = hoderlistLocal;
			month = this.mHolderLocal.size();

			ArrayList<Holder> totalHolderTemp = new ArrayList<Holder>();
			totalHolderTemp.addAll(mHolderLocal);
			float[] exvalue = findHightestAndLowerst(totalHolderTemp);
			// this.highest = exvalue[0];
			// this.lowerest = exvalue[1];
			this.highest = (exvalue[0]);
			this.lowerest = (exvalue[1]);

			// 清除旧得数据
			this.mPointsLocal.clear();
			int size = hoderlistLocal.size();
			this.isSetting = true;
			if (size == 2) {
				this.selectIndex = 0;
			} else {
				this.selectIndex = size / 2;
			}

		} else {
			return;
		}

		invalidate();
	}

	/**
	 * 创建假数据
	 */
	public Object[] createFadeData() {

		ArrayList<Holder> holderLocal = new ArrayList<Holder>();
		ArrayList<Holder> holderAll = new ArrayList<Holder>();
		Random random = new Random();
		for (int i = 0; i < 12; i++) {
			// float r = random.nextFloat();
			float r = Math.abs(random.nextFloat() * 10000 - 2000);
			holderLocal.add(new Holder(String.valueOf(i + ""), r));
			float r2 = random.nextFloat();
			holderAll.add(new Holder(String.valueOf(i + ""), r2));
		}
		Object ret[] = { holderLocal, holderAll };
		return ret;
	}

	/**
	 * 获得对象的副本
	 * 
	 * @param cloneSouce
	 * @return
	 */
	public ArrayList<Holder> getCloneList(ArrayList<Holder> cloneSouce) {
		ArrayList<Holder> hders = new ArrayList<Holder>();
		for (Holder h : cloneSouce) {
			hders.add(h.cloneSelf());
		}
		return hders;
	}

	/**
	 * 找出列表中的最大值和最小值
	 * 
	 * @param holders
	 * @return
	 */
	public float[] findHightestAndLowerst(ArrayList<Holder> holders) {
		float hightest = Integer.MIN_VALUE, lowerst = Integer.MAX_VALUE;
		for (int i = 0, isize = holders.size(); i < isize; i++) {
			Holder holder = holders.get(i);
			float value = holder.getValue();
			if (value > hightest) {
				hightest = value;
			}
			if (value < lowerst) {
				lowerst = value;
			}
		}
		float[] heightAndLow = { hightest, lowerst };
		return heightAndLow;
	}

	// private float floorHighest(float highestValue) {
	// float newHighest = highestValue;
	// // "负数" -> 不变
	// // ">1" -> 1
	// // “1-10” -> 近5原则
	// // “10-100” -> 近5原则
	// // ">100" -> floor第二位
	// if (highestValue < 0) {
	// newHighest = highestValue;
	// } else if (highestValue < 1) {
	// newHighest = 1;
	// } else if (highestValue < 10) {
	// if (highestValue < 5) {
	// newHighest = 5;
	// } else {
	// newHighest = 10;
	// }
	// } else if (highestValue < 100) {
	// float t1 = highestValue / 10;
	// int t2 = Math.round(t1);
	// if (t2 < t1) {
	// newHighest = (float) ((t2 + 0.5) * 10);
	// } else {
	// newHighest = t2 * 10;
	// }
	// } else {
	// int numSize = NumSize((int) highestValue);
	// int numHlp = (int) Math.pow(10, (numSize - 1));
	// float t1 = highestValue / numHlp;
	// int t2 = Math.round(t1);
	// if (t2 < t1) {
	// newHighest = (float) ((t2 + 0.5) * numHlp);
	// } else {
	// newHighest = t2 * numHlp;
	// }
	// }
	//
	// return newHighest;
	// }

	public void setHaseCirle(boolean haveCircle) {
		this.haveCicle = haveCircle;
	}

	// Requires positive x
	static int NumSize(int x) {
		for (int i = 0;; i++) {
			if (x <= sizeTable[i])
				return i + 1;
		}
	}

	/**
	 * 绘制趋势图
	 */
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		w = getWidth();
		h = getHeight();

		canvas.drawColor(Color.WHITE);
		mPaint.setStrokeWidth(paintSolideSmall - 1);
		mPaint.setTextSize(label_text_size);
		int title_width = (int) mPaint.measureText(this.titlle);
		mPaint.setStyle(Style.FILL);
		mPaint.setColor(Color.BLACK);
		canvas.drawText(this.titlle, w / 2 - title_width / 2, 40, mPaint);
		if ((null == this.mPointsLocal)) {
			String notify = "loading...";
			canvas.drawText(notify, w / 2 - mPaint.measureText(notify) / 2, h / 2, mPaint);
		} else {
			// 绘制背景和数据
			drawGrid(canvas);
		}

	}

	public void drawGrid(Canvas canvas) {
		mPaint.setColor(0x33dbdbdb);
		// 画垂直网格
		int cell_width = w / 12;
		try {
			cell_width = (w - paddingLeftRight * 2) / (month - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.mPointsLocal.clear();
		float pix_value_unit = (h - paddingTop - padingBottom) / (this.highest - this.lowerest);

		Path pathLocal = new Path();
		pathLocal.reset();

		for (int i = 0, isize = month; i < isize; i++) {
			int startX = paddingLeftRight + i * cell_width;
			int startY = 0;
			int stopX = paddingLeftRight + i * cell_width;
			int stopY = h;

			canvas.drawLine(startX, startY, stopX, stopY, mPaint);// 竖直网格
			canvas.drawLine(startX + cell_width / 2, startY, startX + cell_width / 2, stopY, mPaint);// 竖直网格
			if (i == 0) {
				canvas.drawLine(startX - cell_width / 2, startY, startX - cell_width / 2, stopY, mPaint);// 竖直网格
			}

			if (i < mHolderLocal.size()) {
				Holder hlocal = mHolderLocal.get(i);
				int x = startX;
				float y = (h - pix_value_unit * (hlocal.getValue() - this.lowerest)) - padingBottom;
				MyPoint pointLocal = new MyPoint(x, (int) y, hlocal);
				this.mPointsLocal.add(pointLocal);
				if (0 == i) {
					pathLocal.moveTo(pointLocal.x, pointLocal.y);
				} else {
					pathLocal.lineTo(pointLocal.x, pointLocal.y);
				}
			}
		}
		// 画水平网格
		int cell_height = h / (month * 2);
		for (int i = 0, isize = month * 2; i <= isize; i++) {
			canvas.drawLine(0, cell_height * i, w, cell_height * i, mPaint);
		}
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(paintSolideSmall);
		mPaint.setColor(color_local);
		canvas.drawPath(pathLocal, mPaint);
		// 添加小圆点
		if (haveCicle) {
			mPaint.setStyle(Style.FILL);
			for (MyPoint mPointLocal : mPointsLocal) {
				canvas.drawCircle(mPointLocal.x, mPointLocal.y, radiusBig, mPaint);
			}
		}
		mPaint.setColor(0xffdbe4e6);
		mPaint.setStyle(Style.FILL);

		// 画x轴刻度
		canvas.drawRect(0, h - padingBottom / 2, w, h, mPaint);// 标注x刻度
		mPaint.setColor(Color.BLACK);
		mPaint.setTextSize((label_text_size / 3) * 2);
		for (int i = 0, isize = month; i < isize; i++) {// X轴显示刻度间隔，
			if (i < mPointsLocal.size()) {
				int seg = month < 5 ? 1 : 2;
				if (i % seg == 0) {
					MyPoint p = this.mPointsLocal.get(i);
					String stamp = p.getHolder().getStamp();
					int stamp_width = (int) mPaint.measureText(stamp);
					canvas.drawText(stamp, p.x - stamp_width / 2, h - padingBottom / 2 + 20, mPaint);
				}
			}
		}

		// 画Y轴刻度
		pinY(canvas, pix_value_unit);
		// 底部分割线
		mPaint.setStrokeWidth(paintSolideSmall);
		mPaint.setColor(0xffb0b9c2);// 底部分割线
		canvas.drawLine(0, h, w, h, mPaint);
		// 选中
		MyPoint mp = null;
		int index = 0;
		if (-1 != selectIndex && isSetting) {
			// isSetting = false;
			mp = mPointsLocal.get(selectIndex);
			index = selectIndex;
		} else {
			Object rets[] = findNearestPoint(mPointsLocal);
			mp = (MyPoint) rets[0];
			index = (Integer) rets[1];
		}

		mPaint.setColor(0xff236ed8);

		canvas.drawLine(mp.x, 0, mp.x, h - padingBottom / 2, mPaint);// 移动竖线
		mPaint.setColor(Color.WHITE);

		if (null != moveListener) {
			moveListener.onMove(mHolderLocal.get(index), index);
		}
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

	private void pinY(Canvas canvas, float pix_value_unit) {
		// 画Y轴刻度
		mPaint.setColor(0xff808080);
		int h = getHeight();
		int startYAxisY = paddingLeftRight;

		float maxPos = paddingTop;
		canvas.drawText(String.valueOf(this.highest), startYAxisY, maxPos, mPaint);// 最大值

		float minPos = h - padingBottom / 2;
		canvas.drawText(String.valueOf(lowerest), startYAxisY, minPos, mPaint);// 最小值

		float mid = (this.highest + this.lowerest) / 2;
		float midPos = (maxPos + minPos) / 2;
		canvas.drawText(String.valueOf(mid), startYAxisY, midPos, mPaint);// 中间值

		//
		// float ystep = Math.abs(highest) / 5;
		// float yAxisStep = (h - h * coordinate_pading_v_percent -
		// coordinate_pading_b / 2) / 5;
		// int startYAxisY = h - coordinate_pading_b / 2 + 6;
		// int startYAxisX = mPointsLocal.get(0).x;
		// int startValue = 0;
		// Rect rectStr = new Rect();
		// for (int i = 0; i < 6; i++) {
		// float YAxisValue = startValue + i * ystep;
		// String YAxisStr = null;
		// if (highest <= 1) {
		// YAxisStr = Float.toString(YAxisValue);
		// } else {
		// YAxisStr = Integer.toString((int) YAxisValue);
		// }
		// if (YAxisValue == 0) {
		// YAxisStr = "0";
		// }
		// mPaint.getTextBounds(YAxisStr, 0, YAxisStr.length(), rectStr);
		// int strW = rectStr.width();
		// int strH = rectStr.height();
		// // int tw=(int)mPaint.measureText(YAxisStr);
		// canvas.drawText(YAxisStr, startYAxisX - strW / 2, startYAxisY -
		// yAxisStep * i - strH, mPaint);
		// }

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		isSetting = false;
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

	public int getSelectIndex() {
		return selectIndex;
	}

	public void setSelectIndex(int selectIndex) {
		this.isSetting = true;
		this.selectIndex = selectIndex;
	}

	public static interface LineMoveListener {
		public void onMove(Holder holderLocal, int index);
	}

	public static interface DrawCallBack {
		public void onDrawFinished();
	}

	public boolean isShowAllStamp() {
		return isShowAllStamp;
	}

	public void setShowAllStamp(boolean isShowAllStamp) {
		this.isShowAllStamp = isShowAllStamp;
	}

	public String getTitlle() {
		return titlle;
	}

	public void setTitlle(String titlle) {
		this.titlle = titlle;
	}

}
