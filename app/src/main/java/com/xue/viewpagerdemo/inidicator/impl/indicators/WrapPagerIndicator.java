package com.xue.viewpagerdemo.inidicator.impl.indicators;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.LinearInterpolator;


import com.xue.viewpagerdemo.inidicator.IndicatorUtils;
import com.xue.viewpagerdemo.inidicator.impl.IPagerIndicator;
import com.xue.viewpagerdemo.inidicator.impl.PositionData;

import java.util.List;


/**
 * @author: hezhiqiang
 * @date: 2017/7/4
 * @version:
 * @description: 包裹住内容区域的指示器，需要和IMeasurablePagerTitleView配合使用
 */

public class WrapPagerIndicator extends View implements IPagerIndicator {

    private RectF mRect = new RectF();
    private Paint mPaint;
    private List<PositionData> mPositionDataList;

    private int mVerticalPadding;       //纵向padding
    private int mHorizontalPadding;     //横向padding
    private int mFillColor;             //填充色
    private float mRoundRadius;         //半径
    private boolean mRoundRadiusSet;    //是否手动设置圆角大小

    private LinearInterpolator startInterpolator = new LinearInterpolator();
    private LinearInterpolator endInterpolator = new LinearInterpolator();

    public WrapPagerIndicator(Context context) {
        super(context);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mVerticalPadding = IndicatorUtils.dp2px(context,6);
        mHorizontalPadding = IndicatorUtils.dp2px(context,10);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(mFillColor);
        canvas.drawRoundRect(mRect,mRoundRadius,mRoundRadius,mPaint);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if(mPositionDataList == null || mPositionDataList.isEmpty()) return;
        //计算锚点位置
        int currPos = Math.min(mPositionDataList.size() - 1,position);
        int nextPos = Math.min(mPositionDataList.size() - 1,position + 1);
        PositionData currData = mPositionDataList.get(currPos);
        PositionData nextData = mPositionDataList.get(nextPos);

        mRect.left = currData.mContentLeft - mHorizontalPadding + (nextData.mContentLeft - currData.mContentLeft) * endInterpolator.getInterpolation(positionOffset);
        mRect.right = currData.mContentRight + mHorizontalPadding + (nextData.mContentRight - currData.mContentRight) * startInterpolator.getInterpolation(positionOffset);
        mRect.top = currData.mContentTop - mVerticalPadding;
        mRect.bottom = currData.mContentBottom + mVerticalPadding;

        if(!mRoundRadiusSet){
            mRoundRadius = mRect.height() / 2;
        }

        invalidate();
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPositionDataProvide(List<PositionData> dataList) {
        mPositionDataList = dataList;
    }

    public void setFillColor(int mFillColor) {
        this.mFillColor = mFillColor;
    }

    public void setVerticalPadding(int mVerticalPadding) {
        this.mVerticalPadding = IndicatorUtils.dp2px(getContext(),mVerticalPadding);
    }

    public void setHorizontalPadding(int mHorizontalPadding) {
        this.mHorizontalPadding = IndicatorUtils.dp2px(getContext(),mHorizontalPadding);
    }

    public void setRoundRadius(float mRoundRadius) {
        this.mRoundRadius = mRoundRadius;
        this.mRoundRadiusSet = true;
    }
}
