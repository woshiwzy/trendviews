package com.test.trendview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class TrendViewRectSelectMarket extends View {

    private Context mContext;
    private Paint mPaint = null;

    private int w = 0;
    private int h = 0;

    private PointF mTouchPoint = new PointF();

    private int coordinate_pading_h = 30;// 坐标间视图的水平间距，可以由此调节坐标轴的宽度
    private int coordinate_pading_v = 60;// 视图上边距，TreendView顶部区域，可以由此调节坐标轴的
    private int coordinate_pading_b = 60;

    private float coordinate_pading_v_percent = 0.3f;
    private float highest = 0;// 最大值
    private float lowerest = 0;// 最小值
    private LineMoveListener moveListener;
    private DrawCallBack drawCallBack;
    private boolean isSetting = true;// 是否是手动设值

    private int label_text_size = 25;
    private int paintSolideBig = 3;// 画笔粗细
    private int paintSolideSmall = 2;
    private int radiusBig = 10;
    private int radiusSmall = 2;

    private String titlle = "销量图";
    private ArrayList<MyPoint> mPointsLocal = null;
    private ArrayList<MyPoint> mPointsLast = null;

    private ArrayList<MyPoint> mPointsAll = null;
    private ArrayList<MyPoint> mPointsAllLast = null;

//	private HashMap<String, ArrayList<Holder>> hashMapHolders = null;
//	private HashMap<String,ArrayList<MyPoint>> hashMapPoints=null;

    private ArrayList<Holder> mArrayListHolderLocalThisYears = null;
    private ArrayList<Holder> mArrayListHolderLocalLastYears = null;
    private ArrayList<Holder> mArrayListHolderAllThisYear = null;
    private ArrayList<Holder> mArrayListHolderAllLastYear = null;

    private int startIndex = 0;
    private int endIndex = 1;
    private final int months = 12;

    public TrendViewRectSelectMarket(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        this.mPaint = new Paint();
        mPaint.setAntiAlias(true);

        this.mPointsLocal = new ArrayList<MyPoint>();
        this.mPointsLast = new ArrayList<MyPoint>();
        this.mPointsAll = new ArrayList<MyPoint>();
        this.mPointsAllLast = new ArrayList<MyPoint>();

//		TypedArray array = context.obtainStyledAttributes(attrs,
//				R.styleable.TreendView);
//		label_text_size = (int) array.getDimension(
//				R.styleable.TreendView_textSize, label_text_size);// 标题文字大小
//		paintSolideBig = (int) array.getDimension(
//				R.styleable.TreendView_paintSolideBig, paintSolideBig);// 画笔粗细
//		paintSolideSmall = (int) array.getDimension(
//				R.styleable.TreendView_paintSolideSmall, paintSolideSmall);
//		radiusBig = (int) array.getDimension(R.styleable.TreendView_radiusBig,
//				radiusBig);// 大圆圈半径
//		radiusSmall = (int) array.getDimension(
//				R.styleable.TreendView_radiusSmall, radiusSmall);
//		array.recycle();

        setmArrayListHolderAllLastYear(createFadeData());
        setmArrayListHolderAllThisYear(createFadeData());
        setmArrayListHolderLocalThisYears(createFadeData());
        setmArrayListHolderLocalLastYears(createFadeData());
    }

    /**
     * 计算绘图的参数
     */
    public void computeParam() {
        ArrayList<Holder> holerlists = new ArrayList<Holder>();
        if (null != mArrayListHolderLocalThisYears && !mArrayListHolderLocalThisYears.isEmpty()) {
            holerlists.addAll(mArrayListHolderLocalThisYears);
        }
        if (null != mArrayListHolderLocalLastYears && !mArrayListHolderLocalLastYears.isEmpty()) {
            holerlists.addAll(mArrayListHolderLocalLastYears);
        }
        if (null != mArrayListHolderAllThisYear && !mArrayListHolderAllThisYear.isEmpty()) {
            holerlists.addAll(mArrayListHolderAllThisYear);
        }
        if (null != mArrayListHolderAllLastYear && !mArrayListHolderAllLastYear.isEmpty()) {
            holerlists.addAll(mArrayListHolderAllLastYear);
        }

        //计算最大值和最小值
        float[] exvalue = findHightestAndLowerst(holerlists);

        this.highest = exvalue[0];
        this.lowerest = exvalue[1];

        float distance = this.highest - this.lowerest;
        if (distance < this.highest * 0.2) {// 差距很小
            this.coordinate_pading_v_percent = 0.5f;
        } else {
            this.coordinate_pading_v_percent = 0.3f;
        }

    }


    /**
     * 创建假数据
     */
    public static ArrayList<Holder> createFadeData() {

        ArrayList<Holder> holderLocal = new ArrayList<Holder>();
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            float r = random.nextFloat();
            holderLocal.add(new Holder(String.valueOf(i + ""), r));
        }
        return holderLocal;
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
        float[] heightAndLow = {hightest, lowerst};
        return heightAndLow;
    }

    /**
     * 绘制趋势图
     */
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        w = getWidth();
        h = getHeight();
        this.coordinate_pading_v = (int) (h * this.coordinate_pading_v_percent);
        canvas.drawColor(Color.WHITE);
        mPaint.setStrokeWidth(paintSolideSmall);
        mPaint.setTextSize(label_text_size);
        int title_width = (int) mPaint.measureText(this.titlle);
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(Color.BLACK);
        canvas.drawText(this.titlle, w / 2 - title_width / 2, 50, mPaint);
        // 画背景网格
        if (0 == (countListSize(mArrayListHolderAllLastYear) + countListSize(mArrayListHolderAllThisYear) +
                countListSize(mArrayListHolderLocalLastYears) + countListSize(mArrayListHolderLocalThisYears))) {
        } else {
            drawGrid(canvas);
        }
    }

    public void drawGrid(Canvas canvas) {
        mPaint.setColor(0x33dbdbdb);
        // 画垂直网格
        // coordinate_pading_h
        int cell_width = (w - coordinate_pading_h * 2) / (months - 1);

        this.mPointsAll.clear();
        this.mPointsLocal.clear();
        float pix_value_unit = (h - h * coordinate_pading_v_percent - coordinate_pading_b) / (this.highest - this.lowerest);

        Path pathLocalThis = new Path();//本公司今年
        pathLocalThis.reset();

        Path pathLocalLast = new Path();//本公司去年
        pathLocalLast.reset();


        Path pathAllThis = new Path();//全市场今年
        pathAllThis.reset();

        Path pathAllLast = new Path();//全市场去年
        pathAllLast.reset();


        for (int i = 0, isize = months; i < isize; i++) {
            int startX = coordinate_pading_h + i * cell_width;
            int startY = 0;
            int stopX = coordinate_pading_h + i * cell_width;
            int stopY = h;
            canvas.drawLine(startX, startY, stopX, stopY, mPaint);// 竖直网格
            canvas.drawLine(startX + cell_width / 2, startY, startX + cell_width / 2, stopY, mPaint);// 竖直网格
            if (i == 0) {
                canvas.drawLine(startX - cell_width / 2, startY, startX - cell_width / 2, stopY, mPaint);// 竖直网格
            }

            if (null != mArrayListHolderLocalThisYears && !mArrayListHolderLocalThisYears.isEmpty()) {
                //本公司今年
                if (i < mArrayListHolderLocalThisYears.size()) {
                    Holder holderLocalThisYear = mArrayListHolderLocalThisYears.get(i);
                    if (null == this.mPointsLocal) {
                        this.mPointsLocal = new ArrayList<MyPoint>();
                    }
                    int x = startX;
                    float y = h - pix_value_unit * (holderLocalThisYear.getValue() - this.lowerest) - coordinate_pading_b;
                    MyPoint mypointlocalthisyear = new MyPoint(x, (int) y, holderLocalThisYear);
                    if (i == 0) {
                        pathLocalThis.moveTo(x, y);
                    } else {
                        pathLocalThis.lineTo(x, y);
                    }
                    this.mPointsLocal.add(mypointlocalthisyear);
                } else {
                    this.mPointsLocal = null;
                }
            }
            if (null != mArrayListHolderLocalLastYears && !mArrayListHolderLocalLastYears.isEmpty()) {
                //本公司去年
                if (i < mArrayListHolderLocalLastYears.size()) {
                    Holder holderLocalLastYear = mArrayListHolderLocalLastYears.get(i);
                    if (null == this.mPointsLast) {
                        this.mPointsLast = null;
                    }
                    int x = startX;
                    float y = h - pix_value_unit * (holderLocalLastYear.getValue() - this.lowerest) - coordinate_pading_b;
                    MyPoint mypointlocallastyear = new MyPoint(x, (int) y, holderLocalLastYear);
                    if (i == 0) {
                        pathLocalLast.moveTo(x, y);
                    } else {
                        pathLocalLast.lineTo(x, y);
                    }
                    this.mPointsLast.add(mypointlocallastyear);
                } else {
                    this.mPointsLast = null;
                }
            }

            if (null != mArrayListHolderAllThisYear && !mArrayListHolderAllThisYear.isEmpty()) {
                //全市场今年
                if (i < mArrayListHolderAllThisYear.size()) {
                    Holder holderAllHolderThisYear = mArrayListHolderAllThisYear.get(i);
                    if (null == this.mPointsAll) {
                        this.mPointsAll = new ArrayList<MyPoint>();
                    }
                    int x = startX;
                    float y = h - pix_value_unit * (holderAllHolderThisYear.getValue() - this.lowerest) - coordinate_pading_b;
                    MyPoint mypointallthisyear = new MyPoint(x, (int) y, holderAllHolderThisYear);
                    if (i == 0) {
                        pathAllThis.moveTo(x, y);
                    } else {
                        pathAllThis.lineTo(x, y);
                    }
                    this.mPointsAll.add(mypointallthisyear);
                } else {
                    this.mPointsAll = null;
                }
            }

            if (null != mArrayListHolderAllLastYear && !mArrayListHolderAllLastYear.isEmpty()) {
                //全市场去年
                if (i < mArrayListHolderAllLastYear.size()) {
                    Holder holderAllHolderLastYear = mArrayListHolderAllLastYear.get(i);
                    if (null == this.mPointsAllLast) {
                        this.mPointsAllLast = new ArrayList<MyPoint>();
                    }
                    int x = startX;
                    float y = h - pix_value_unit * (holderAllHolderLastYear.getValue() - this.lowerest) - coordinate_pading_b;
                    MyPoint mypointalllastyear = new MyPoint(x, (int) y, holderAllHolderLastYear);
                    if (i == 0) {
                        pathAllLast.moveTo(x, y);
                    } else {
                        pathAllLast.lineTo(x, y);
                    }
                    this.mPointsAllLast.add(mypointalllastyear);
                } else {
                    this.mPointsAllLast = null;
                }
            }
        }

        //组装path结束
        // 画水平网格
        int cell_height = h / (months * 2);
        for (int i = 0, isize = months * 2; i <= isize; i++) {
            canvas.drawLine(0, cell_height * i, w, cell_height * i, mPaint);
        }
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(radiusBig);

        mPaint.setColor(0xff667788);//
        canvas.drawPath(pathLocalThis, mPaint);

        mPaint.setColor(0xff908765);//
        canvas.drawPath(pathLocalLast, mPaint);

        mPaint.setColor(0xff114477);//
        canvas.drawPath(pathAllThis, mPaint);

        mPaint.setColor(0xff765432);//
        canvas.drawPath(pathAllLast, mPaint);

        mPaint.setColor(0xffdbe4e6);
        mPaint.setStyle(Style.FILL);

        // canvas.drawLine(0, h-coordinate_pading_b, w, h-coordinate_pading_b, mPaint);
        canvas.drawRect(0, h - coordinate_pading_b / 2, w, h, mPaint);// 标注x刻度
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize((label_text_size / 3) * 2);

        ArrayList<MyPoint> locationPoints = getNotNullPointList();
        if (locationPoints != null) {
            for (int i = 0, isize = months; i < isize; i++) {
                MyPoint p = locationPoints.get(i);
                canvas.drawText(p.getHolder().getStamp(), p.x - 5, h - coordinate_pading_b / 2 + 25, mPaint);
            }
        }

        mPaint.setStrokeWidth(paintSolideBig / 2);
        mPaint.setColor(0xffb0b9c2);// 底部分割线
        canvas.drawLine(0, h, w, h, mPaint);
        mPaint.setStrokeWidth(paintSolideBig);

        if (isSetting) {
            ArrayList<MyPoint> templist = getNotNullPointList();
            if (null != templist) {
                // 找到第一个点
                if (this.startIndex < templist.size()) {
                    MyPoint startPoint = templist.get(this.startIndex);
                    drawMoveLine(canvas, startPoint, cell_width);
                    //画第二根线
                    if (endIndex < startIndex) {
                        endIndex = startIndex + 1;
                    }
                    if (endIndex < templist.size() && templist.size() > 1) {
                        MyPoint endPoint = templist.get(endIndex);
                        drawMoveLine(canvas, endPoint, cell_width);
                        drawRectransparent(canvas, startPoint, endPoint);
                    }

                }
            }
        } else {
            ArrayList<MyPoint> notNullPoints = getNotNullPointList();
            Object rets[] = findNearestPoint(notNullPoints);
            if (rets.length > 1) {
                MyPoint nearestPoint = (MyPoint) rets[0];//最近的触摸点
                //第一个点距离触摸点的
                if (1 == notNullPoints.size()) {
                    mPaint.setColor(0xff34DE71);
                    drawMoveLine(canvas, nearestPoint, cell_width);
                } else {
                    if (startIndex < 0) {
                        startIndex = 0;
                    }
                    if (endIndex < startIndex) {
                        endIndex = startIndex + 1;
                    }
                    if (endIndex >= notNullPoints.size()) {
                        endIndex = notNullPoints.size() - 1;
                    }
                    if (startIndex >= 0 && startIndex < endIndex) {
                        //找到离触摸点最近的点
                        float dstart = cputeDistanceX(mTouchPoint, notNullPoints.get(this.startIndex));
                        float dend = cputeDistanceX(mTouchPoint, notNullPoints.get(this.endIndex));
                        int index = (Integer) rets[1];
                        if (dstart < dend) {
                            //将起始点移动到触摸点	，备注:效果图要求不移动
                            this.startIndex = index;
                        }
                        if (dstart > dend) {
                            //将结束点移动到触摸点
                            this.endIndex = index;
                        }
                        MyPoint startPoint = notNullPoints.get(this.startIndex);
                        MyPoint endPoint = notNullPoints.get(this.endIndex);
                        drawMoveLine(canvas, startPoint, cell_width);//画第一根线
                        drawMoveLine(canvas, endPoint, cell_width);//画第二根线
                        drawRectransparent(canvas, startPoint, endPoint);//画半透明区域
                    }
                }
                if (null != moveListener) {
                    moveListener.onMove(this.endIndex);
                }
            }
        }


    }

    /**
     * 画移动竖线
     *
     * @param nearestPoint
     * @param cell_width
     * @param canvas
     */
    public void drawMoveLine(Canvas canvas, MyPoint nearestPoint, int cell_width) {
        mPaint.setColor(0xff34DE71);
        canvas.drawLine(nearestPoint.x, 0, nearestPoint.x, h - coordinate_pading_b / 2, mPaint);// 移动竖线
        // 底部的方块
        Rect rect = new Rect(nearestPoint.x - cell_width / 2 - paintSolideBig / 2, h - coordinate_pading_b / 2, nearestPoint.x + cell_width / 2 + paintSolideBig / 2, h);
        canvas.drawRect(rect, mPaint);
        // 底部白色文字
        mPaint.setColor(Color.WHITE);
        canvas.drawText(nearestPoint.getHolder().getStamp(), nearestPoint.x - 5, h - coordinate_pading_b / 2 + 25, mPaint);
    }

    /**
     * 画半透明区域
     *
     * @param canvas
     * @param startPoint
     * @param endPoint
     */
    public void drawRectransparent(Canvas canvas, MyPoint startPoint, MyPoint endPoint) {
        Rect rtect = new Rect(startPoint.x, 0, endPoint.x, h - coordinate_pading_b / 2);
        mPaint.setColor(0x6634DE71);
        canvas.drawRect(rtect, mPaint);
    }

    /**
     * 计算list大小
     *
     * @param list
     * @return
     */
    public int countListSize(ArrayList list) {
        if (list == null || list.isEmpty()) {
            return 0;
        } else {
            return list.size();
        }
    }


    public float cputeDistanceX(PointF touchPoint, MyPoint otherPoint) {
        return Math.abs(touchPoint.x - otherPoint.x);
    }

    /**
     * 返回不为空的pointlist
     *
     * @return
     */
    private ArrayList<MyPoint> getNotNullPointList() {

        if (null != mPointsLocal && !mPointsLocal.isEmpty()) {
            return mPointsLocal;
        }
        if (null != mPointsLast && !mPointsLast.isEmpty()) {
            return mPointsLast;
        }
        if (null != mPointsAll && !mPointsAll.isEmpty()) {
            return mPointsAll;
        }
        if (null != mPointsAllLast && !mPointsAllLast.isEmpty()) {
            return mPointsAllLast;
        }
        return null;
    }

    /**
     * 找到最近的点
     *
     * @param pointlist
     * @return
     */
    public Object[] findNearestPoint(ArrayList<MyPoint> pointlist) {
        if (null == pointlist) {
            Object[] a = {null};
            return a;
        }
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
        Object[] data = {p, index};
        return data;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        isSetting = false;
        mTouchPoint.x = event.getX();
        mTouchPoint.y = event.getY();
        invalidate();
        return true;
    }



    public void setmArrayListHolderLocalThisYears(ArrayList<Holder> mArrayListHolderLocalThisYears) {
        this.mArrayListHolderLocalThisYears = mArrayListHolderLocalThisYears;
        computeParam();
        invalidate();

    }

    public ArrayList<Holder> getmArrayListHolderLocalLastYears() {
        return mArrayListHolderLocalLastYears;
    }

    public void setmArrayListHolderLocalLastYears(ArrayList<Holder> mArrayListHolderLocalLastYears) {
        this.mArrayListHolderLocalLastYears = mArrayListHolderLocalLastYears;
        computeParam();
        invalidate();
    }

    public ArrayList<Holder> getmArrayListHolderAllThisYear() {
        return mArrayListHolderAllThisYear;
    }

    public void setmArrayListHolderAllThisYear(
            ArrayList<Holder> mArrayListHolderAllThisYear) {
        this.mArrayListHolderAllThisYear = mArrayListHolderAllThisYear;
        computeParam();
        invalidate();
    }

    public ArrayList<Holder> getmArrayListHolderAllLastYear() {
        return mArrayListHolderAllLastYear;
    }

    public void setmArrayListHolderAllLastYear(
            ArrayList<Holder> mArrayListHolderAllLastYear) {
        this.mArrayListHolderAllLastYear = mArrayListHolderAllLastYear;
        computeParam();
        invalidate();
    }

    public void setMoveListener(LineMoveListener moveListener) {
        this.moveListener = moveListener;
    }

    public static interface LineMoveListener {
        public void onMove(int index);
    }

    public static interface DrawCallBack {
        public void onDrawFinished();
    }

    public String getTitlle() {
        return titlle;
    }

    public void setTitlle(String titlle) {
        this.titlle = titlle;
    }


	public void setStartIndex(int start) {
		isSetting = true;
		this.startIndex = start;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setSecondIndex(int end) {
		isSetting = true;
		this.endIndex = end;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public LineMoveListener getMoveListener() {
		return moveListener;
	}


	public ArrayList<Holder> getmArrayListHolderLocalThisYears() {
		return mArrayListHolderLocalThisYears;
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
}
