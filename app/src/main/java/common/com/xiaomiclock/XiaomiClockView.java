package common.com.xiaomiclock;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.Calendar;

/**
 * Created by Austin on 2017/3/23.
 */

//Todo 通过 rect.set 方法减少 rect的初始化数量。

public class XiaomiClockView extends View {

    private static final float SECOND_HAND_CORNER_RADIUS = 10f;

    //the larger the longer
    private static final float MINUTE_HAND_LENGTH_RATIO = 0.4f;

    private float DEFAULT_PADDING_RATIO = 0.2f;
    private float INNER_CICLE_RATIA = 0.85f;
    private float INNER_SCALE_LENGTH =20f;

    //the position of second hand, outer while bigger;
    private float SECOND_HAND_RATIA = 0.55f;

    private String mColorHeavy = "#ffffffff";
    private String mColorLight = "#77ffffff";
    private int mBackgroundColor;


    //Paint Rect Point Gredient Matrix Path
    private Path mSecondHandPath;

    private Path mHourHandPath;

    private Path mMinuteHandPath;

    private Paint mPaint;

    private Paint mHourHandPaint;

    private Paint mMinuteHandPaint;

    private Paint mSecondHandPaint;

    private Paint mScaleGapPaint;

    private Paint mScaleCirclePaint;

    private Paint mTextPaint;

    private Rect mTwoLetterRect;

    private Rect mSingleLetterRect;

    private RectF mOuterRingRect;

    private RectF mInnerRingRect;

    private PointF centerPoint;

    private SweepGradient mScaleGredient;

    private Matrix mMatrix;


    //Position
    private PointF m12Point;
    private PointF m3Point;
    private PointF m6Point;
    private PointF m9Point;


    //m Variables
    private int mRadius;
    private float mSecondDegree;
    private float mMinuteDegree;
    private float mHourDegree;
    private float mSecondHandWidth = 20;
    private float mHourHandLength;

    //


    public XiaomiClockView(Context context) {
        this(context, null);
    }

    public XiaomiClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        //get the background color
        Drawable background = getBackground();
        if (background instanceof ColorDrawable) {
            mBackgroundColor = ((ColorDrawable) background).getColor();
        }

        // initial default light paint
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.parseColor(mColorLight));
        mPaint.setStrokeWidth(dp2px(1f));



        //initial scale gap paint
        mScaleGapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleGapPaint.setColor(mBackgroundColor);
        mScaleGapPaint.setStrokeWidth(dp2px(2));

        //initial  scale circle paint
        mScaleCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleCirclePaint.setStyle(Paint.Style.STROKE);

        //initial second hand paint
        mSecondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondHandPaint.setStyle(Paint.Style.FILL);
        mSecondHandPaint.setColor(Color.parseColor(mColorHeavy));
        mSecondHandPaint.setPathEffect(new CornerPathEffect(SECOND_HAND_CORNER_RADIUS));

        //initial hour hand paint;
        mHourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourHandPaint.setStyle(Paint.Style.FILL);
        mHourHandPaint.setColor(Color.parseColor(mColorLight));

        //initial minute hand paint;
        mMinuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinuteHandPaint.setStyle(Paint.Style.STROKE);
        mMinuteHandPaint.setStrokeWidth(dp2px(3.5f));
        mMinuteHandPaint.setColor(Color.parseColor(mColorHeavy));

        //initial text paint
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(sp2px(15));
        mTextPaint.setColor(Color.parseColor(mColorLight));

        //initial clock two letters' rect and single letter's rect
        mSingleLetterRect = new Rect();
        mTwoLetterRect = new Rect();

        //initial matrix
        mMatrix = new Matrix();

        //initial second hand's path
        mSecondHandPath = new Path();
        mSecondHandPath.moveTo(0, 0);
        mSecondHandPath.lineTo(dp2px(mSecondHandWidth), 0);
        mSecondHandPath.lineTo(dp2px(mSecondHandWidth/2), -((float)Math.sin(Math.PI/4)*dp2px(mSecondHandWidth)));
        mSecondHandPath.close();

        //initial hour and minute hand's path
        mHourHandPath = new Path();
        mMinuteHandPath = new Path();

        startRunning();
    }

    private void startRunning() {

        postDelayed(new Runnable() {
            @Override
            public void run() {
                getTimeDegree();
                invalidate();
                startRunning();
            }
        }, 100);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //draw the text
        canvas.drawText("12", m12Point.x, m12Point.y, mTextPaint);
        canvas.drawText("3", m3Point.x, m3Point.y, mTextPaint);
        canvas.drawText("6", m6Point.x, m6Point.y, mTextPaint);
        canvas.drawText("9", m9Point.x, m9Point.y, mTextPaint);

        //draw the fourth arc
        for (int i = 0; i < 4; i++) {
            canvas.drawArc(mOuterRingRect, 5 + i * 90, 80, false, mPaint);
        }

        //draw the inner scales
        canvas.save();
        canvas.scale(INNER_CICLE_RATIA, INNER_CICLE_RATIA, centerPoint.x, centerPoint.y);
        drawScales(canvas);
        canvas.restore();

        //draw the second hand
        canvas.save();
        canvas.rotate(mSecondDegree, centerPoint.x, centerPoint.y);
        canvas.translate(centerPoint.x - dp2px(mSecondHandWidth / 2), centerPoint.y - mRadius * SECOND_HAND_RATIA);
        canvas.drawPath(mSecondHandPath, mSecondHandPaint);
        canvas.restore();

        //draw the minute hand
        drawHourHand(canvas);

        //draw the hour hand
        drawMinuteHand(canvas);

    }

    private void drawMinuteHand(Canvas canvas) {
        canvas.save();
        mMinuteHandPath.reset();
        mMinuteHandPath.moveTo(0, -dp2px(6));
        mMinuteHandPath.lineTo(0f, -mHourHandLength * 1.3f);
        mMinuteHandPath.addCircle(0, 0, dp2px(6), Path.Direction.CW);
        canvas.translate(centerPoint.x, centerPoint.y);
        canvas.rotate(mMinuteDegree);
        mMinuteHandPaint.setColor(Color.parseColor(mColorHeavy));
        canvas.drawPath(mMinuteHandPath, mMinuteHandPaint);
        canvas.restore();

    }


    private void drawHourHand(Canvas canvas) {


        canvas.save();
        mHourHandPath.reset();
        mHourHandPath.moveTo(-10, -dp2px(6));
        mHourHandPath.lineTo(-5, -mHourHandLength);
        mHourHandPath.quadTo(0, -mHourHandLength - 10, 5, -mHourHandLength);
        mHourHandPath.lineTo(10, -dp2px(6));
//        mHourHandPath.addCircle(0, 0, 15, Path.Direction.CW);

        mHourHandPath.close();
        canvas.translate(centerPoint.x, centerPoint.y);
        canvas.rotate(mHourDegree);
        canvas.drawPath(mHourHandPath, mHourHandPaint);
        mHourHandPath.reset();
        mMinuteHandPaint.setColor(Color.parseColor(mColorLight));
        canvas.drawCircle(0, 0, dp2px(6), mMinuteHandPaint);

        canvas.restore();
    }


    private void drawScales(Canvas canvas) {
        //initial the scale gredient
        mMatrix.setRotate(mSecondDegree - 90, centerPoint.x, centerPoint.y);
        mScaleGredient.setLocalMatrix(mMatrix);
        mScaleCirclePaint.setShader(mScaleGredient);

        float length = dp2px(INNER_SCALE_LENGTH / INNER_CICLE_RATIA);//length before scale

        //draw the inner scale circle
        mScaleCirclePaint.setStrokeWidth(length);
        canvas.drawCircle(centerPoint.x, centerPoint.y, mRadius - length / 2, mScaleCirclePaint);

        canvas.save();
        for (int i = 0; i < 200; i++) {
            canvas.drawLine(centerPoint.x, mOuterRingRect.top, centerPoint.x, mOuterRingRect.top + length, mScaleGapPaint);
            canvas.rotate(1.8f, centerPoint.x, centerPoint.y);
        }
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //get the center point
        centerPoint = new PointF(w / 2, h / 2);

        //figure out the clock radius(have considered the default padding);
        mRadius = Math.min(w / 2, h / 2);
        mRadius *= (1 - DEFAULT_PADDING_RATIO);


        //get the outer ring rect
        float paddingLeft = centerPoint.x - mRadius;
        float paddingRight = paddingLeft;
        float paddingTop = centerPoint.y - mRadius;
        float paddingBottom = paddingTop;
        mOuterRingRect = new RectF(paddingLeft, paddingTop, w - paddingRight, h - paddingBottom);

        //12, 3, 6, 9 position
        mTextPaint.getTextBounds("12", 0, 2, mTwoLetterRect);
        mTextPaint.getTextBounds("3", 0, 1, mSingleLetterRect);
        m12Point = new PointF(centerPoint.x - mTwoLetterRect.width() / 2, mOuterRingRect.top + mTwoLetterRect.height() / 2);
        m3Point = new PointF(mOuterRingRect.right - mSingleLetterRect.width() / 2, centerPoint.y + mSingleLetterRect.height() / 2);
        m6Point = new PointF(centerPoint.x - mSingleLetterRect.width()/2, mOuterRingRect.bottom+mSingleLetterRect.height()/2);
        m9Point = new PointF(mOuterRingRect.left - mSingleLetterRect.width() / 2, centerPoint.y + mSingleLetterRect.height() / 2);

        //set scale circle gredient
        mScaleGredient = new SweepGradient(centerPoint.x, centerPoint.y, new int[]{Color.parseColor(mColorLight), Color.parseColor(mColorHeavy)}, new float[]{0.75f, 1});

        //get the minute hand length
        mHourHandLength = mRadius * MINUTE_HAND_LENGTH_RATIO;

    }


    private float dp2px(float dp) {
        return Resources.getSystem().getDisplayMetrics().density * dp;
    }


    private float sp2px(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().getDisplayMetrics());
    }


    private void getTimeDegree() {
        Calendar calendar = Calendar.getInstance();
        float milisecond = calendar.get(Calendar.MILLISECOND);
        float second = calendar.get(Calendar.SECOND) + milisecond/1000;
        float minute = calendar.get(Calendar.MINUTE) + second/60;
        float hour = calendar.get(Calendar.HOUR) + minute/60;

        mSecondDegree = (second / 60 ) * 360;
        mMinuteDegree = (minute / 60 ) * 360;
        mHourDegree = (hour / 12) * 360;
    }
}

















