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


public class TrendViewRectSelect extends View {

	private Context mContext;
	private Paint mPaint = null;

	private int w = 0;
	private int h = 0;

	private ArrayList<Holder> mHolderLocal = null;
	private ArrayList<Holder> mHolderOther = null;
	private PointF mTouchPoint = new PointF();

	private int pading_left_right = 30;// 坐标间视图的水平间距，可以由此调节坐标轴的宽度
	private int pading_top = 60;// 视图上边距，TreendView顶部区域，可以由此调节坐标轴的
	private int pading_bottom = 20;

	private float coordinate_pading_v_percent = 0.3f;
	private float highest = 0;// 最大值
	private float lowerest = 0;// 最小值
	private LineMoveListener moveListener;
	private boolean isSetting = true;// 是否是手动设值
	private int label_text_size = 25;
	private int paintSolideBig = 5;// 画笔粗细
	private int paintSolideSmall = 2;
	private int radiusBig = 10;
	private int radiusSmall = 4;
	private String titlle = "";
	private int month = 12;
	private ArrayList<MyPoint> mPointsLocal = null;
	private ArrayList<MyPoint> mPointsOther = null;
	private boolean isShowAllStamp = false;

	private int selectIndex = -1;

	public static final int COLOR_LOCAL_THIS = 0xfffb667b;// 本机场今年
	public static final int COLOR_LOCAL_OTHER = 0xff678ce1;// 本机场去年

	public static final int COLOR_LOCAL_THIS_CIRCLE = 0xffffA8A1;
	public static final int COLOR_LOCAL_OTHER_CIRCLE = 0xfff1d3e2;

	public static final int COLOR_OTHER_THIS = 0xfff3b613;// 关注机场今年
	public static final int COLOR_OTHER_OTHER = 0xfff48221;// 关注机场去年

	public static final int COLOR_OTHER_THIS_CIRCLE = 0xffb4e200;
	public static final int COLOR_OTHER_OTHER_CIRCLE = 0xffeafbe7;

	private static final int COLOR_LOCAL = 0xffD51203;
	private static final int COLOR_ALL = 0xff00CC99;

	private int color_local = COLOR_LOCAL;
	private int color_all = COLOR_ALL;
	private String data4ype = "year";

	private String label1;
	private String label2;

	private boolean localBase;// 被比较对象标志

	private boolean haveCircle = true;

	public TrendViewRectSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		this.mPaint = new Paint();
		mPaint.setAntiAlias(true);
		this.mHolderLocal = new ArrayList<Holder>();
		this.mHolderOther = new ArrayList<Holder>();
		this.mPointsLocal = new ArrayList<MyPoint>();
		this.mPointsOther = new ArrayList<MyPoint>();
//
//		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TreendView);
//		label_text_size = (int) array.getDimension(R.styleable.TreendView_textSize, label_text_size);// 标题文字大小
//		paintSolideBig = (int) array.getDimension(R.styleable.TreendView_paintSolideBig, paintSolideBig);// 画笔粗细
//		paintSolideSmall = (int) array.getDimension(R.styleable.TreendView_paintSolideSmall, paintSolideSmall);
//		radiusBig = (int) array.getDimension(R.styleable.TreendView_radiusBig, radiusBig);// 大圆圈半径
//		radiusSmall = (int) array.getDimension(R.styleable.TreendView_radiusSmall, radiusSmall);
//		array.recycle();
		setInitTrendViewFade();
	}

	public String getData4ype() {
		return data4ype;
	}

	public void setData4ype(String data4ype) {
		this.data4ype = data4ype;
	}

	@Deprecated
	public void setInitTrendViewFade() {
		Object fade[] = createFadeData();
		setInitTrendView(false, (ArrayList<Holder>) fade[0], (ArrayList<Holder>) fade[1]);
	}

	public void setInitTrendView(boolean isShowAllStamp, ArrayList<Holder> hoderlistLocal, ArrayList<Holder> holderOther) {
		this.setShowAllStamp(isShowAllStamp);
		if (!ListUtiles.isAllEmpty(hoderlistLocal, holderOther)) {
			this.mHolderLocal = hoderlistLocal;
			this.mHolderOther = holderOther;
			int localSize = ListUtiles.getListSize(hoderlistLocal);
			int otherSize = ListUtiles.getListSize(holderOther);
			month = Math.max(localSize, otherSize);
			ArrayList<Holder> totalHolderTemp = new ArrayList<Holder>();
			if (!ListUtiles.isEmpty(mHolderLocal)) {
				totalHolderTemp.addAll(mHolderLocal);
			}
			if (!ListUtiles.isEmpty(mHolderOther)) {
				totalHolderTemp.addAll(mHolderOther);
			}
			float[] exvalue = findHightestAndLowerst(totalHolderTemp);
			this.highest = ((float) exvalue[0]);
			this.lowerest = ((float) exvalue[1]);

			float distance = this.highest - this.lowerest;
			if (distance < this.highest * 0.2) {// 差距很小
				this.coordinate_pading_v_percent = 0.4f;
			} else {
				this.coordinate_pading_v_percent = 0.2f;
			}

			// 清除旧得数据
			this.mPointsLocal.clear();
			this.mPointsOther.clear();
			int size = Math.max(hoderlistLocal.size(), holderOther.size());
			this.isSetting = true;
			if (size == 2) {
				this.selectIndex = 0;
			} else {
				this.selectIndex = size / 2;
			}
			if (null != moveListener) {
				Holder local = null;
				Holder other = null;

				try {
					local = mHolderLocal.get(selectIndex);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					other = mHolderOther.get(selectIndex);
				} catch (Exception e) {
					e.printStackTrace();
				}
				moveListener.onTouchUp(local, other, selectIndex);
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
			float r = random.nextFloat();
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

	@Deprecated
	private float floorHighest(float highestValue) {
		float newHighest = highestValue;
		// "负数" -> 不变
		// ">1" -> 1
		// “1-10” -> 近5原则
		// “10-100” -> 近5原则
		// ">100" -> floor第二位
		if (highestValue < 0) {
			newHighest = highestValue;
		} else if (highestValue < 1) {
			newHighest = 1;
		} else if (highestValue < 10) {
			if (highestValue < 5) {
				newHighest = 5;
			} else {
				newHighest = 10;
			}
		} else if (highestValue < 100) {
			float t1 = highestValue / 10;
			int t2 = Math.round(t1);
			if (t2 < t1) {
				newHighest = (float) ((t2 + 0.5) * 10);
			} else {
				newHighest = t2 * 10;
			}
		} else {
			int numSize = NumSize((int) highestValue);
			int numHlp = (int) Math.pow(10, (numSize - 1));
			float t1 = highestValue / numHlp;
			int t2 = Math.round(t1);
			if (t2 < t1) {
				newHighest = (float) ((t2 + 0.5) * numHlp);
			} else {
				newHighest = t2 * numHlp;
			}
		}

		return newHighest;
	}

	final static int[] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };

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
		mPaint.setStrokeWidth(paintSolideSmall);
		mPaint.setTextSize(label_text_size);
		int title_width = (int) mPaint.measureText(this.titlle);
		mPaint.setStyle(Style.FILL);
		mPaint.setColor(Color.BLACK);
		canvas.drawText(this.titlle, w / 2 - title_width / 2, 40, mPaint);
		try {
			drawGrid(canvas);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void drawGrid(Canvas canvas) {
		int cell_width = 0;
		if (month <= 1) {
			month = (w - pading_left_right * 2) / 5;
		} else {
			cell_width = (w - pading_left_right * 2) / (month - 1);
		}

		this.mPointsOther.clear();
		this.mPointsLocal.clear();
		float pix_value_unit = (h - pading_top - pading_bottom) / (this.highest - this.lowerest);
		mPaint.setColor(0xffb0b9c2);
		// ==============// 画垂直网格
		Path pathLocal = new Path();
		pathLocal.reset();
		Path pathOther = new Path();
		pathOther.reset();
		mPaint.setColor(0x33dbdbdb);
		for (int i = 0, isize = month; i < isize; i++) {
			int startX = pading_left_right + i * cell_width;
			int startY = 0;
			int stopX = pading_left_right + i * cell_width;
			int stopY = h;
			canvas.drawLine(startX, startY, stopX, stopY, mPaint);// 竖直网格
			canvas.drawLine(startX + cell_width / 2, startY, startX + cell_width / 2, stopY, mPaint);// 竖直网格
			if (i == 0) {
				canvas.drawLine(startX - cell_width / 2, startY, startX - cell_width / 2, stopY, mPaint);// 竖直网格
			}
			int x = startX;
			buildPath(pathLocal, pix_value_unit, i, x, mHolderLocal, mPointsLocal);
			buildPath(pathOther, pix_value_unit, i, x, mHolderOther, mPointsOther);
		}
		// 画水平网格
		int cell_height = h / (month * 2);
		for (int i = 0, isize = month * 2; i <= isize; i++) {
			canvas.drawLine(0, cell_height * i, w, cell_height * i, mPaint);
		}
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(paintSolideBig);
		mPaint.setColor(color_local);
		canvas.drawPath(pathLocal, mPaint);// 画本机场的数据

		if (!this.mHolderOther.isEmpty()) {
			mPaint.setColor(color_all);
			canvas.drawPath(pathOther, mPaint);// 画关注机场的数据
		}
		// 开始画小圆点
		if (haveCircle) {
			mPaint.setStyle(Style.FILL);
			mPaint.setColor(color_local);
			for (MyPoint myPointLocal : mPointsLocal) {
				canvas.drawCircle(myPointLocal.x, myPointLocal.y, radiusBig, mPaint);
			}
			mPaint.setColor(color_all);
			for (MyPoint myPointOther : mPointsOther) {
				canvas.drawCircle(myPointOther.x, myPointOther.y, radiusBig, mPaint);
			}
		}
		// =====
		mPaint.setColor(0xffdbe4e6);
		mPaint.setStyle(Style.FILL);
		canvas.drawRect(0, h - pading_bottom, w, h, mPaint);// 标注x刻度背景
		mPaint.setColor(Color.BLACK);
		mPaint.setTextSize((label_text_size / 3) * 2);

		mPaint.setStrokeWidth(paintSolideBig / 2);
		mPaint.setColor(0xffb0b9c2);// 底部分割线
		canvas.drawLine(0, h, w, h, mPaint);
		mPaint.setStrokeWidth(paintSolideBig);

		ArrayList<MyPoint> pointlist = mPointsLocal.size() > mPointsOther.size() ? mPointsLocal : mPointsOther;// 找到最大的point
		MyPoint mp = null;
		int index = 0;
		if (-1 != selectIndex && isSetting) {
			// isSetting = false;
			if (selectIndex <= (mPointsLocal.size() - 1)) {
				mp = mPointsLocal.get(selectIndex);
				index = selectIndex;
			} else {
				if (selectIndex <= (mPointsOther.size() - 1)) {
					mp = mPointsOther.get(selectIndex);
					index = selectIndex;
				}
			}
		} else {
			Object rets[] = findNearestPoint(pointlist);
			mp = (MyPoint) rets[0];
			index = (Integer) rets[1];
		}

		if (null == mp) {
			return;
		}

		mPaint.setColor(0xff236ed8);
		canvas.drawLine(mp.x, 0, mp.x, h - pading_bottom, mPaint);// 移动竖线
		mPaint.setColor(Color.BLACK);

		for (int i = 0, isize = month; i < isize; i++) {
			if (i < pointlist.size()) {
				MyPoint p = pointlist.get(i);
				// 标注x刻度
				String stamp = p.getHolder().getStamp();
				float stamp_width = mPaint.measureText(stamp);
				int step = month < 5 ? 1 : 2;
				if (i % step == 0) {
					canvas.drawText(stamp, p.x - stamp_width / 2, h - pading_bottom / 2 + 5, mPaint);
				}

			}
		}

		pinY(canvas, pix_value_unit);// 标注Y轴
		if (null != moveListener) {
			Holder local = index <= (mHolderLocal.size() - 1) ? mHolderLocal.get(index) : null;
			Holder other = index <= (mHolderOther.size() - 1) ? mHolderOther.get(index) : null;
			selectIndex = index;
			moveListener.onMove(local, other, index);
		}

	}

	private void buildPath(Path path, float pix_value_unit, int i, int x, ArrayList<Holder> holers, ArrayList<MyPoint> points) {
		if (null != holers && i < holers.size()) {
			Holder hall = holers.get(i);
			float yall = (h - pix_value_unit * (hall.getValue() - this.lowerest)) - pading_bottom * 2;
			MyPoint pointOther = new MyPoint(x, (int) yall, hall);
			points.add(pointOther);
			if (0 == i) {
				path.moveTo(pointOther.x, pointOther.y);
			} else {
				path.lineTo(pointOther.x, pointOther.y);
			}
		}
	}

	// 标注Y轴
	private void pinY(Canvas canvas, float unit) {
		mPaint.setColor(0xff808080);

		String hight = String.valueOf((int) this.highest);
		String low = String.valueOf((int) this.lowerest);
		float mid = (this.highest + this.lowerest) / 2;
		String midText = String.valueOf((int) mid);

		canvas.drawText(hight, 10, pading_top, mPaint);// 最大值

		float yall = (h - unit * (mid - this.lowerest)) - pading_bottom * 2;

		canvas.drawText(midText, 10, yall, mPaint);
		canvas.drawText(low, 10, h - pading_bottom, mPaint);// 最小值

		// float ystep = Math.abs(highest) / 5;
		// float yAxisStep = (h - h * coordinate_pading_v_percent -
		// coordinate_pading_b / 2) / 5;
		// int startYAxisY = h - coordinate_pading_b / 2 + 6;
		// int startYAxisX = mPointsLocal.size() > 0 ? mPointsLocal.get(0).x :
		// mPointsOther.get(0).x;
		// int startValue = 0;
		// Rect rectStr = new Rect();
		// for (int i = 0; i < 6; i++) {
		// float YAxisValue = startValue + i * ystep;
		// String YAxisStr = null;
		// if (highest <= 1) {
		// YAxisStr = Float.toString(YAxisValue);
		// } else {
		// YAxisStr = Integer.toString((int) YAxisValue);
		// // YAxisStr =
		// // NumberHelper.numberToCnMoney(String.valueOf(YAxisValue));
		// }
		// if (YAxisValue == 0) {
		// YAxisStr = "0";
		// continue;
		// }
		// mPaint.getTextBounds(YAxisStr, 0, YAxisStr.length(), rectStr);
		// int strW = rectStr.width();
		// int strH = rectStr.height();
		// canvas.drawText(YAxisStr, startYAxisX - strW / 2, startYAxisY -
		// yAxisStep * i - strH, mPaint);
		// }
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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		isSetting = false;
		mTouchPoint.x = event.getX();
		mTouchPoint.y = event.getY();
		invalidate();

		int action = event.getAction();
		if (action == MotionEvent.ACTION_UP) {
			if (null != moveListener) {
				try {
					moveListener.onTouchUp(mHolderLocal.get(selectIndex), mHolderOther.get(selectIndex), selectIndex);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	public LineMoveListener getMoveListener() {
		return moveListener;
	}

	public void setMoveListener(LineMoveListener moveListener) {
		this.moveListener = moveListener;
	}

	public static abstract class LineMoveListener {
		public abstract void onMove(Holder holderLocal, Holder holderAll, int index);

		public void onTouchUp(Holder holderLocal, Holder holderAll, int index) {
		}
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

	public String getLabel1() {
		return label1;
	}

	public void setLabel1(String label1) {
		this.label1 = label1;
	}

	public String getLabel2() {
		return label2;
	}

	public void setLabel2(String label2) {
		this.label2 = label2;
	}

	public void setColorLocal(int color_local) {
		this.color_local = color_local;
	}

	public void setColorAll(int color_all) {
		this.color_all = color_all;
	}

	public boolean isLocalBase() {
		return localBase;
	}

	public void setLocalBase(boolean localBase) {
		this.localBase = localBase;
	}

	public int getSelectIndex() {
		return selectIndex;
	}

	public void setSelectIndex(int selectIndex) {
		this.isSetting = true;
		this.selectIndex = selectIndex;
	}

}
