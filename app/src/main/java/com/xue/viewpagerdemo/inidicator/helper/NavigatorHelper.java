package com.xue.viewpagerdemo.inidicator.helper;

import android.util.SparseArray;
import android.util.SparseBooleanArray;

import com.xue.viewpagerdemo.inidicator.ScrollState;
import com.xue.viewpagerdemo.inidicator.callback.OnNavigatorScrollListener;


/**
 * @author: hezhiqiang
 * @date: 2017/7/3
 * @version:
 * @description:
 */

public class NavigatorHelper {
    private SparseBooleanArray mDeselectItems = new SparseBooleanArray();
    private SparseArray<Float> mLeavedPercents = new SparseArray<>();

    private int mTotalCount;
    private int mCurrentIndex;
    private int mLastIndex;
    private float mLastPositionOffsetSum;
    private int mScrollState;

    private boolean mSkimOver;

    private OnNavigatorScrollListener mOnNavigatorScrollListener;

    public void onPageScrolled(int position,float positionOffset,int positionOffsetPixels){
        float currentPositionOffsetSum = position + positionOffset;
        boolean leftToRight = false;
        if(mLastPositionOffsetSum <= currentPositionOffsetSum){
            leftToRight = true;
        }

        if(mScrollState != ScrollState.SCROLL_STATE_IDLE){
            if(currentPositionOffsetSum == mLastPositionOffsetSum){
                return;
            }
            int nextPosition = position + 1;
            boolean normalDispatch = true;
            if(positionOffset == 0.0f){
                if(leftToRight) {
                    nextPosition = position -1;
                    normalDispatch = false;
                }
            }
            for (int i = 0; i < mTotalCount; i++) {
                if(i == position || i == positionOffset){
                    continue;
                }
                Float leavedPercent = mLeavedPercents.get(i,0.0f);
                if(leavedPercent != 1.0f){
                    dispatchOnLeave(i,1.0f,leftToRight,true);
                }
            }
            if(normalDispatch) {
                if(leftToRight) {
                    dispatchOnLeave(position,positionOffset,true,false);
                    dispatchOnEnter(nextPosition,positionOffset,true,false);
                } else {
                    dispatchOnLeave(nextPosition,1.0f - positionOffset,false,false);
                    dispatchOnEnter(position,1.0f - positionOffset,false,false);
                }
            } else {
                dispatchOnLeave(nextPosition,1.0f - positionOffset,true,false);
                dispatchOnEnter(position,1.0f - positionOffset,true,false);
            }
        } else {
            for (int i = 0; i < mTotalCount; i++) {
                if(i == mCurrentIndex) {
                    continue;
                }
                boolean deselected = mDeselectItems.get(i);
                if(!deselected){
                    dispatchOnDeselected(i);
                }
                Float leavedPercent = mLeavedPercents.get(i,0.0f);
                if(leavedPercent != 1.0f){
                    dispatchOnLeave(i,1.0f,false,true);
                }
            }
            dispatchOnEnter(mCurrentIndex,1.0f,false,true);
            dispatchOnSelected(mCurrentIndex);
        }
        mLastPositionOffsetSum = currentPositionOffsetSum;
    }

    private void dispatchOnEnter(int index,float enterPercent,boolean leftToRight,boolean force) {
        if(mSkimOver || index == mCurrentIndex || mScrollState == ScrollState.SCROLL_STATE_DRAGGING || force) {
            if(mOnNavigatorScrollListener != null){
                mOnNavigatorScrollListener.onEnter(index,mTotalCount,enterPercent,leftToRight);
            }
            mLeavedPercents.put(index,1.0f - enterPercent);
        }
    }

    private void dispatchOnLeave(int index,float leavePercent,boolean leftToRight,boolean force) {
        if(mSkimOver || index == mLastIndex || mScrollState == ScrollState.SCROLL_STATE_DRAGGING
                || ((index == mCurrentIndex - 1 || index == mCurrentIndex + 1) && mLeavedPercents.get(index,0.0f) != -1) || force){
            if(mOnNavigatorScrollListener != null) {
                mOnNavigatorScrollListener.onLeave(index,mTotalCount,leavePercent,leftToRight);
            }
            mLeavedPercents.put(index,leavePercent);
        }
    }

    private void dispatchOnSelected(int index){
        if(mOnNavigatorScrollListener != null){
            mOnNavigatorScrollListener.onSelected(index,mTotalCount);
        }
        mDeselectItems.put(index,false);
    }

    private void dispatchOnDeselected(int index){
        if(mOnNavigatorScrollListener != null){
            mOnNavigatorScrollListener.onDeselected(index,mTotalCount);
        }
        mDeselectItems.put(index,true);
    }

    public void setOnNavigatorScrollListener(OnNavigatorScrollListener mOnNavigatorScrollListener) {
        this.mOnNavigatorScrollListener = mOnNavigatorScrollListener;
    }

    /********************************************/
    public void onPageSelected(int position){
        mLastIndex = mCurrentIndex;
        mCurrentIndex = position;
        dispatchOnSelected(mCurrentIndex);
        for (int i = 0; i < mTotalCount; i++) {
            if(i == mCurrentIndex) {
                continue;
            }
            boolean deselected = mDeselectItems.get(i);
            if(!deselected){
                dispatchOnDeselected(i);
            }
        }
    }

    public void onPageScrollStateChanged(int state){
        mScrollState = state;
    }

    public void setSkimOver(boolean mSkimOver) {
        this.mSkimOver = mSkimOver;
    }

    public void setTotalCount(int mTotalCount) {
        this.mTotalCount = mTotalCount;
        mDeselectItems.clear();
        mLeavedPercents.clear();
    }

    public int getTotalCount() {
        return mTotalCount;
    }

    public int getScrollState() {
        return mScrollState;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }
}
