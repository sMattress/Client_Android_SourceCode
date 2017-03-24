package com.wtf.ui.select_time;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import com.wtf.R;

/**
 * @author jayce
 * @date 2015/3/9
 */
public class RangeSeekBar extends ViewGroup {
    private Drawable mThumbDrawable;
    private Drawable mThumbPlaceDrawable;

    private ThumbView mThumbLeft;   //左游标
    private ThumbView mThumbRight;  //右游标
    private int mProgressBarHeight;     //进度条的高度
    private int mThumbPlaceHeight;      //游标的高度
    private int center;
    private int mMaxValue = 180;   //分成120份，每一小格占2份

    public void setmLeftValue(int mLeftValue) {
        this.mLeftValue = mLeftValue;
    }

    private int mLeftValue;     //左游标  数值    (100分之多少)   例如：1就是 1/100

    public void setmRightValue(int mRightValue) {
        this.mRightValue = mRightValue;
    }

    private int mRightValue = mMaxValue;  //右游标  数值    (100分之多少)

    private int mLeftLimit;     //游标左边的限制坐标
    private int mRightLimit;        //游标右边的限制坐标
    private int proPaddingLeftAndRight;     //进度条左右的padding 等于游标图标宽度的一半
    private int mProBaseline;       //进度条top  坐标

    private float mLeft=6;
    private float mRight=6;

    private static final int PART_ITEM = 5;//半小 占的分数
    private float mPartWidth;   //每一小份的宽度

    public static final int SHORTLINE_HEIGHT = 5; //短线的高度 （画刻度时会有长短线）
    public static final int LONGLINE_HEIGHT = 10; //长线的高度

    public static final int RULE_HEIGHT_DP = 20;  //尺子的高度  dp
    public static int RULE_HEIGHT_PX;

    private boolean isMoving;

    private int degs[] = {6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24};      //尺子上标记刻度值
    // private String unitStr="点";     //尺子标记单位

    private OnRangeChangeListener mOnRangeChangeListener;       //当左右任意一个游标改变时，回调接口

    public interface OnRangeChangeListener {
        public void onRangeChange(int leftValue, int rightValue);
    }

    public RangeSeekBar(Context context) {
        this(context, null);
    }

    public RangeSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RangeSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundDrawable(new BitmapDrawable());
        //换算px
        RULE_HEIGHT_PX = DensityUtil.dip2px(context, RULE_HEIGHT_DP);
        mProgressBarHeight = DensityUtil.dip2px(context, 4);

        mThumbDrawable = getResources().getDrawable(R.drawable.rod_handshank_butten);
        //mThumbDrawable.setBounds(new Rect(10, 10, 14, 13));
        // mThumbDrawable.setBounds(10,10,10,10);

        mThumbPlaceDrawable = getResources().getDrawable(R.drawable.rod_place_icon);

        mThumbPlaceHeight = mThumbPlaceDrawable.getIntrinsicHeight();
        mProBaseline = RULE_HEIGHT_PX + mThumbPlaceHeight;

        mThumbLeft = new ThumbView(getContext());
        mThumbLeft.setRangeSeekBar(this);
        mThumbLeft.setImageDrawable(mThumbDrawable);

        mThumbRight = new ThumbView(getContext());
        mThumbRight.setRangeSeekBar(this);
        mThumbRight.setImageDrawable(mThumbDrawable);


        addView(mThumbLeft);
        addView(mThumbRight);
        mThumbLeft.setOnThumbListener(new ThumbView.OnThumbListener() {
            @Override
            public void onThumbChange(int i) {
                mLeftValue = i;
                if (mOnRangeChangeListener != null) {
                    isMoving = true;
                    mOnRangeChangeListener.onRangeChange(mLeftValue, mRightValue);
                }
            }
        });
        mThumbRight.setOnThumbListener(new ThumbView.OnThumbListener() {
            @Override
            public void onThumbChange(int i) {
                mLeftValue = i;
                if (mOnRangeChangeListener != null) {
                    isMoving = true;
                    mOnRangeChangeListener.onRangeChange(mLeftValue, mRightValue);
                }
            }
        });
    }

    public void setOnRangeChangeListener(OnRangeChangeListener mOnRangeChangeListener) {
        this.mOnRangeChangeListener = mOnRangeChangeListener;
    }

//    private void measureView(View view){
//        ViewGroup.LayoutParams params=view.getLayoutParams();
//
//        if(params==null){
//            params=new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        }
//
//        int widthSpec=ViewGroup.getChildMeasureSpec(0,0,params.width);
//
//        int heightSpec;
//        if(params.height>0){
//            heightSpec=MeasureSpec.makeMeasureSpec(params.height,MeasureSpec.EXACTLY);
//        }else{
//            heightSpec=MeasureSpec.makeMeasureSpec(params.height,MeasureSpec.UNSPECIFIED);
//        }
//
//        view.measure(widthSpec,heightSpec);
//    }

    /**
     * 画尺子
     *
     * @param canvas
     */
    protected void drawProgressBar(Canvas canvas) {
        //画背景
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(R.color.grey));
        Rect rect = new Rect(mLeftLimit, mProBaseline, mRightLimit, mProBaseline + mProgressBarHeight);
        canvas.drawRect(rect, paint);

        //画进度
        paint.setColor(getResources().getColor(R.color.blue));
        rect = new Rect((int)mThumbLeft.getCenterX(), mProBaseline, (int)mThumbRight.getCenterX(), mProBaseline + mProgressBarHeight);
        canvas.drawRect(rect, paint);
    }

    /**
     * 画刻度尺
     *
     * @param canvas
     */
    protected void drawRule(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setColor(getResources().getColor(R.color.white));
        paint.setTextSize(45);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);

        //一次遍历两份,绘制的位置都是在奇数位置
        for (int i = 5; i <= mMaxValue + 10; i += 2) {
            if (i < PART_ITEM || i > mMaxValue + 10 - PART_ITEM) {
                continue;
            }

            float degX = mLeftLimit + (i - 5) * mPartWidth;
            int degY;

            if ((i - PART_ITEM) % (PART_ITEM * 2) == 0) {
                degY = mProBaseline - DensityUtil.dip2px(getContext(), LONGLINE_HEIGHT);
                if ((i - PART_ITEM) % (PART_ITEM * 12) == 0) {
                    canvas.drawText(String.valueOf(degs[(i) / 10]) + ":00", degX, degY, paint);
                }
            } else {
                degY = mProBaseline - DensityUtil.dip2px(getContext(), SHORTLINE_HEIGHT);
            }
            canvas.drawLine(degX, mProBaseline, degX, degY, paint);
        }
    }

    /**
     * 画 Thumb 位置的数值
     */
    protected void drawRodPlaceValue(Canvas canvas, ThumbView thumbView, int s) {
        int centerX1 = (int)(center + s*proPaddingLeftAndRight*1.4);
        int centerX2 = (int)thumbView.getCenterX();
        int centerX = centerX2;
        if(s < 0) {//left
            if (centerX > centerX1)
                centerX = centerX1;
        } else {//right
            if (centerX < centerX1)
                centerX = centerX1;
        }
        Paint paint = new Paint();
        BitmapDrawable bd = (BitmapDrawable) mThumbPlaceDrawable;
        canvas.drawBitmap(bd.getBitmap(), centerX - mThumbPlaceDrawable.getIntrinsicWidth() / 2, 0, paint);

        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(50);
        if (minValue(thumbView) < 10) {
            canvas.drawText(hourValue(thumbView) + ":0" + minValue(thumbView), centerX, mThumbDrawable.getIntrinsicHeight() / 2, paint);
        } else {
            canvas.drawText(hourValue(thumbView) + ":" + minValue(thumbView), centerX, mThumbDrawable.getIntrinsicHeight() / 2, paint);
        }
    }

    //onLayout调用后执行的函数
    public void onLayoutPrepared() {
        System.out.println("mLeft:" + mLeft);
        System.out.println("mRight:" + mRight);
        mThumbLeft.setCenterX((mLeftLimit + (mLeft - 6) * (mRightLimit - mLeftLimit) / 18));
        mThumbRight.setCenterX((mLeftLimit + (mRight - 6) * (mRightLimit - mLeftLimit) / 18));

    }

    public int hourValue(ThumbView view) {
        //todo 这里只是计算了100之多少的值，需要自行转换成刻度上的值
        int hourValue = (int)(1. * mMaxValue * (view.getCenterX() - mLeftLimit) / (mRightLimit - mLeftLimit) / 10 + 6);
        return hourValue;
    }

    public int minValue(ThumbView view) {
        //todo 这里只是计算了100之多少的值，需要自行转换成刻度上的值
        double pro = 1. * mMaxValue * (view.getCenterX() - mLeftLimit) / (mRightLimit - mLeftLimit) / 10.0;
        int h = (int) (pro);
        int m = (int) ((pro - h) * 60 + 0.5);
        Log.i("min", m + "");
        return m;
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);    //测量子控件
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int mWidth = MeasureSpec.getSize(widthMeasureSpec);
        center = mWidth/2;
        proPaddingLeftAndRight = mThumbLeft.getMeasuredWidth() / 2;
        mLeftLimit = proPaddingLeftAndRight;
        mRightLimit = mWidth - proPaddingLeftAndRight;

        //位置标记的高度+尺子的刻度高度+尺子的高度+游标的高度
        setMeasuredDimension(mWidth, mThumbPlaceHeight + RULE_HEIGHT_PX + mProgressBarHeight + mThumbLeft.getMeasuredHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        drawProgressBar(canvas);
        drawRule(canvas);

        //if (mThumbLeft.isMoving()) {
        drawRodPlaceValue(canvas, mThumbLeft, -1);
        mThumbLeft.setLimit(mLeftLimit, (int)mThumbRight.getCenterX() - 80);
        //} else if (mThumbRight.isMoving()) {
        drawRodPlaceValue(canvas, mThumbRight, 1);
        mThumbRight.setLimit((int)mThumbLeft.getCenterX(), mRightLimit - 1);
        // }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);



    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int heightSum = 0;

        heightSum += mThumbPlaceHeight;

        heightSum += RULE_HEIGHT_PX;

        heightSum += mProgressBarHeight;

        mPartWidth = (mRightLimit - mLeftLimit) / (float) mMaxValue;   //计算一份所占的宽度  一定要用float

        mThumbLeft.setLimit(mLeftLimit, mRightLimit);    //设置可以移动的范围
        mThumbLeft.layout(0, heightSum, mThumbLeft.getMeasuredWidth(), b - 10);      //设置在父布局的位置

        mThumbRight.setLimit(mLeftLimit, mRightLimit);
        mThumbRight.layout(0, heightSum, mThumbLeft.getMeasuredWidth(), b - 10);
        //mLeft = mLeftLimit;
        //mRight = mRightLimit;
        onLayoutPrepared();     //layout调用后调用的方法，比如设置thumb limit
    }

    public ThumbView getThumbLeft() {
        return mThumbLeft;
    }

    public void setThumbLeft(ThumbView mThumbLeft) {
        this.mThumbLeft = mThumbLeft;
    }

    public ThumbView getThumbRight() {
        return mThumbRight;
    }

    public void setThumbRight(ThumbView mThumbRight) {
        this.mThumbRight = mThumbRight;
    }

    public int getLeftValue() {
        return mLeftValue;
    }

    public void setLeftValue(int mLeftValue) {
        this.mLeftValue = mLeftValue;
    }

    public int getRightValue() {
        return mRightValue;
    }

    public void setRightValue(int mRightValue) {
        this.mRightValue = mRightValue;
    }

    public float getMLeft() {
        return mLeft;
    }

    public void setMLeft(float mLeft) {
        this.mLeft = mLeft;
    }

    public float getMRight() {
        return mRight;
    }

    public void setMRight(float mRight) {
        this.mRight = mRight;
    }

    public boolean getIsMoving() {
        return isMoving;
    }
}
