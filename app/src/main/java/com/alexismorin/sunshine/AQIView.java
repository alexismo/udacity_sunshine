package com.alexismorin.sunshine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Range;
import android.view.View;

/**
 * Created by alexis on 02/12/15.
 */
public class AQIView extends View {

    private int airQualityIndex = 0;

    private int[] colorThreshholds = new int[] {0,50, 100, 150, 200, 300, 500};
    private int[] aqiColors = new int[] {0xff009966, 0xffffde33, 0xffff9933, 0xffcc0033, 0xff660099, 0xff7e0023};

    private int theAQI = -1;//for testing and development purposes only

    private Paint mAQIBackgroundPaint;
    private Paint mAQITextPaint;
    private Paint mAQIBaublePaint;
    private float measureText;
    private float aqiRangeWidth = 0.0f;
    private float baubleX = 0.0f;

    private void init(){
        mAQIBackgroundPaint = new Paint();
        mAQITextPaint = new Paint();
        mAQIBaublePaint = new Paint();

        mAQIBackgroundPaint.setColor(aqiColors[3]);

        mAQITextPaint.setTextSize(60.0f);
        mAQITextPaint.setAntiAlias(true);
        mAQITextPaint.setColor(0xFF000000);
        mAQITextPaint.setTextAlign(Paint.Align.CENTER);

        mAQIBaublePaint.setAntiAlias(true);
        mAQIBaublePaint.setColor(0xFFFFFFFF);
    }

    public AQIView(Context context) {
        super(context);
        init();
    }

    public AQIView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    public AQIView(Context context, AttributeSet attrs,
                   int defaultStyle){
        super(context, attrs, defaultStyle);
        init();
    }

    public void setAQI(int newAQI){
        this.theAQI = newAQI;
        invalidate();
    }

    @Override
    protected void onMeasure(int wMeasureSpec, int hMeasureSpec) {

        int wSpecMode = MeasureSpec.getMode(wMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(wMeasureSpec);
        int myWidth = wSpecSize;

        int hSpecMode = MeasureSpec.getMode(hMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(hMeasureSpec);
        int myHeight = hSpecSize;

        if(wSpecMode == MeasureSpec.EXACTLY){
            myWidth = wSpecSize;
        }else if(wSpecMode == MeasureSpec.AT_MOST){
            myWidth = 300;
        }

        if(hSpecMode == MeasureSpec.EXACTLY){
            myHeight = hSpecSize;
        }else if(hSpecMode == MeasureSpec.UNSPECIFIED){
            myHeight = 200;
        }
        if(hSpecMode == MeasureSpec.AT_MOST){
            myHeight = 300;
        }

        measureText = mAQITextPaint.measureText(theAQI + "");

        setMeasuredDimension(myWidth, myHeight);
    }

    @Override
    public void invalidate(){
        aqiRangeWidth = 0.0f;
        baubleX = 0.0f;
        super.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas){
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        //add a margin
        int marginSide = height/2;
        width -= 2*marginSide;

        if (aqiRangeWidth == 0.0f)
            aqiRangeWidth = width / aqiColors.length;

        if (baubleX == 0.0f) {
            int aqiSection = aqiSection(theAQI);
            baubleX = (marginSide + (aqiSection * aqiRangeWidth));

            float extra = ((theAQI - (colorThreshholds[aqiSection])) / (colorThreshholds[aqiSection+1] - theAQI));
            int aqiRangeMin = colorThreshholds[aqiSection];

            float baubleXtra = ((float)(theAQI - aqiRangeMin)) / ((float)(colorThreshholds[aqiSection+1] - aqiRangeMin));

            baubleX += baubleXtra * aqiRangeWidth; //the percentage within an aqi range
        }

        drawBandsBackground(canvas, width, height);

        canvas.drawCircle(baubleX,
                height / 2,
                height / 3,
                mAQIBaublePaint);

        canvas.drawText(theAQI + "",
                baubleX,
                height / 2 + (2* (mAQITextPaint.getTextSize() / 5)),
                mAQITextPaint);
    }

    private int aqiSection(int aqi){
        for (int i=0; i<= colorThreshholds.length-2; i++) {
            Range aqiRange = Range.create(colorThreshholds[i], colorThreshholds[i+1]);

            if (aqiRange.contains(aqi)){
                return i;
            }
        }

        return -1;
    }

    private void drawBandsBackground(Canvas canvas, int width, int height) {

        width -= height;

        for( int i = 0; i < aqiColors.length; i++){
            mAQIBackgroundPaint.setColor(aqiColors[i]);

            canvas.drawRect(aqiRangeWidth *i + height/2, height/2, (aqiRangeWidth *i)+ aqiRangeWidth + height/2, height, mAQIBackgroundPaint);
        }
    }
}
