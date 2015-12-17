package com.alexismorin.sunshine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by alexis on 02/12/15.
 */
public class AQIView extends View {

    private int airQualityIndex = 0;

    private int[] colorThreshholds = new int[] {0,50, 100, 150, 200, 300, 500};
    private int[] aqiColors = new int[] {0xff009966, 0xffffde33, 0xffff9933, 0xffcc0033, 0xff660099, 0xff7e0023};

    private int theAQI = 34;//for testing and development purposes only

    private Paint mAQIBackgroundPaint;
    private Paint mAQITextPaint;
    private Paint mAQIBaublePaint;
    private float measureText;

    private void init(){
        mAQIBackgroundPaint = new Paint();
        mAQITextPaint = new Paint();
        mAQIBaublePaint = new Paint();

        mAQIBackgroundPaint.setColor(aqiColors[3]);

        mAQITextPaint.setTextSize(90.0f);
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

    @Override
    protected void onMeasure(int wMeasureSpec, int hMeasureSpec){

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

        measureText = mAQITextPaint.measureText(theAQI+"");

        setMeasuredDimension(myWidth, myHeight);
    }

    @Override
    protected void onDraw(Canvas canvas){
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        //canvas.drawRect(0,0,width,height,mAQIBackgroundPaint);
        drawBandsBackground(canvas, width, height);

        canvas.drawCircle(width / 500 * theAQI, height/2,height/4, mAQIBaublePaint);

        canvas.drawText(theAQI + "", width/500 * theAQI, height / 3 + (mAQITextPaint.getTextSize() / 2), mAQITextPaint);
    }

    private void drawBandsBackground(Canvas canvas, int width, int height) {

        //add a margin
        width -= height/2;

        float sectionWidth = width / aqiColors.length;

        for( int i = 0; i < aqiColors.length; i++){
            mAQIBackgroundPaint.setColor(aqiColors[i]);

            canvas.drawRect(sectionWidth*i + height/2, height/2, (sectionWidth*i)+sectionWidth + height/2, height, mAQIBackgroundPaint);
        }
    }

}
