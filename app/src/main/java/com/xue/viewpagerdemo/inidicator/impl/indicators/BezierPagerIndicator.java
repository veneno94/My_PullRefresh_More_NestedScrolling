package com.xue.viewpagerdemo.inidicator.impl.indicators;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;


import com.xue.viewpagerdemo.inidicator.IndicatorUtils;
import com.xue.viewpagerdemo.inidicator.impl.ArgbEvaluatorHolder;
import com.xue.viewpagerdemo.inidicator.impl.IPagerIndicator;
import com.xue.viewpagerdemo.inidicator.impl.PositionData;

import java.util.Arrays;
import java.util.List;

/**
 * @author: hezhiqiang
 * @date: 2017/7/4
 * @version:
 * @description: 贝塞尔曲线指示器
 */

public class BezierPagerIndicator extends View implements IPagerIndicator {

    private List<PositionData> mPositionDataList = null;

    private float mLeftCircleRadius;
    private float mRightCircleRadius;
    private float mLeftCircleX;
    private float mRightCircleX;

    private Paint mPaint;
    private Path mPath;

    private float mYOffset;
    private float mMaxCircleRadius;
    private float mMinCircleRadius;

    private List<Integer> mColors;
    private AccelerateInterpolator mStartInterpolator = new AccelerateInterpolator();
    private DecelerateInterpolator mEndInterpolator = new DecelerateInterpolator();

    public BezierPagerIndicator(Context context) {
        super(context);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mMaxCircleRadius = IndicatorUtils.dp2px(context,3.5);
        mMinCircleRadius = IndicatorUtils.dp2px(context,2);
        mYOffset = IndicatorUtils.dp2px(context,1.5);

        mPath = new Path();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawCircle(mLeftCircleX,getHeight() - mYOffset - mMaxCircleRadius,mLeftCircleRadius,mPaint);
        canvas.drawCircle(mRightCircleX,getHeight() - mYOffset - mMaxCircleRadius,mRightCircleRadius,mPaint);
        drawBezierCurve(canvas);

    }

    private void drawBezierCurve(Canvas canvas) {
        mPath.reset();
        float y = getHeight() - mYOffset - mMaxCircleRadius;
        mPath.moveTo(mRightCircleX,y);
        mPath.lineTo(mRightCircleX,y - mRightCircleRadius);
        mPath.quadTo(mRightCircleX + (mLeftCircleX - mRightCircleX) / 2.0f,y,mLeftCircleX,y - mLeftCircleRadius);
        mPath.lineTo(mLeftCircleX,y + mLeftCircleRadius);
        mPath.quadTo(mRightCircleX + (mLeftCircleX - mRightCircleX) / 2.0f,y,mRightCircleX,y + mRightCircleRadius);
        mPath.close(); //闭合
        canvas.drawPath(mPath,mPaint);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if(mPositionDataList == null || mPositionDataList.isEmpty()){
            return;
        }
        //计算颜色
        if(mColors != null && mColors.size() > 0){
            int curColor = mColors.get(Math.abs(position) % mColors.size());
            int nextColor = mColors.get(Math.abs(position + 1) % mColors.size());
            int color = ArgbEvaluatorHolder.eval(positionOffset,curColor,nextColor);
            mPaint.setColor(color);
        }

        //计算锚点位置
        PositionData curData = mPositionDataList.get(Math.min(mPositionDataList.size() - 1,position));
        PositionData nextData = mPositionDataList.get(Math.min(mPositionDataList.size() - 1,position + 1));
        float leftX = curData.mLeft + (curData.mRight - curData.mLeft) / 2;
        float rightX = nextData.mLeft + (nextData.mRight - nextData.mLeft) / 2;

        mLeftCircleX = leftX + (rightX - leftX) * mStartInterpolator.getInterpolation(positionOffset);
        mRightCircleX = leftX + (rightX - leftX) * mEndInterpolator.getInterpolation(positionOffset);
        mLeftCircleRadius = mMaxCircleRadius + (mMinCircleRadius - mMaxCircleRadius) * mEndInterpolator.getInterpolation(positionOffset);
        mRightCircleRadius = mMinCircleRadius + (mMaxCircleRadius - mMinCircleRadius) * mStartInterpolator.getInterpolation(positionOffset);

        invalidate();
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void setColors(Integer... colors){
        mColors = Arrays.asList(colors);
    }

    @Override
    public void onPositionDataProvide(List<PositionData> dataList) {
        mPositionDataList = dataList;
    }
}
