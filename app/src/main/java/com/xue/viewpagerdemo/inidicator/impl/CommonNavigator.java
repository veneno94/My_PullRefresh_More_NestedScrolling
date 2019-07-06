package com.xue.viewpagerdemo.inidicator.impl;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;


import androidx.annotation.NonNull;

import com.xue.viewpagerdemo.R;
import com.xue.viewpagerdemo.inidicator.IPagerNavigator;
import com.xue.viewpagerdemo.inidicator.ScrollState;
import com.xue.viewpagerdemo.inidicator.callback.OnNavigatorScrollListener;
import com.xue.viewpagerdemo.inidicator.helper.NavigatorHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: hezhiqiang
 * @date: 2017/7/3
 * @version:
 * @description: 通用ViewPager指示器，包含PagerTitle和PagerIndicator
 */

public class CommonNavigator extends FrameLayout implements IPagerNavigator, OnNavigatorScrollListener {

    private HorizontalScrollView mScrollView;
    private LinearLayout mTitleContainer;
    private LinearLayout mIndicatorContainer;
    private IPagerIndicator mIndicator;

    private boolean mAdjustMode;            //自适应模式，适用于数目固定的，少量的title
    private boolean mEnablePrivotScroll;    //启动中心点滚动
    private float mScrollPivotX = 0.5f;     //滚动中心点
    private boolean mSmoothScroll = true;   //是否平滑滚动，
    private int mLeftPadding;
    private int mRightPadding;
    private boolean mFollowTouch = true;    //是否手指跟随滚动
    private boolean mSkimOver;              //夸多页面切换，中间也是否显示 "掠过"效果
    private boolean mIndicatorOnTop;        //指示器是否在title上层，默认为下层
    private boolean mReselectWhenLayout = true; //positionData准备好时是否重新选中当前页，为true可保证早极端情况下指示器状态正常

    private NavigatorHelper mNavigatorHelper;
    private CommonNavigatorAdapter adapter;

    //保存每个title的位置信息，为扩展indicator提供保障
    private List<PositionData> mPositionDataList = new ArrayList<>();

    private DataSetObserver mObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            mNavigatorHelper.setTotalCount(adapter.getCount());
            init();
        }
    };

    public CommonNavigator(@NonNull Context context) {
        super(context);
        mNavigatorHelper = new NavigatorHelper();
        mNavigatorHelper.setOnNavigatorScrollListener(this);
    }

    public boolean isAdjustMode() {
        return mAdjustMode;
    }

    public void setAdjustMode(boolean mAdjustMode) {
        this.mAdjustMode = mAdjustMode;
    }

    public CommonNavigatorAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(CommonNavigatorAdapter adapter) {
        if(this.adapter == adapter) return;
        if(this.adapter != null)
            this.adapter.unregisterDataSetObserver(mObserver);

        this.adapter = adapter;

        if(this.adapter != null){
            this.adapter.registerDataSetObserver(mObserver);
            mNavigatorHelper.setTotalCount(this.adapter.getCount());
            if(mTitleContainer != null) {   //adapter改变时，应该重新init，但是第一次设置adapter不用
                this.adapter.notifyDataSetChanged();
            }
        }else{
            mNavigatorHelper.setTotalCount(0);
            init();
        }
    }

    private void init(){
        removeAllViews();

        View root;
        if(mAdjustMode){
            root = LayoutInflater.from(getContext()).inflate(R.layout.lcs_indicator_pager_navigator_layout_no_scroll,this);
        } else {
            root = LayoutInflater.from(getContext()).inflate(R.layout.lcs_indicator_pager_navigator_layout,this);
        }

        mScrollView = (HorizontalScrollView) root.findViewById(R.id.scroll_view);
        mTitleContainer = (LinearLayout) root.findViewById(R.id.title_container);
        mTitleContainer.setPadding(mLeftPadding,0,mRightPadding,0);

        mIndicatorContainer = (LinearLayout) root.findViewById(R.id.indicator_container);
        if(mIndicatorOnTop){
            mIndicatorContainer.getParent().bringChildToFront(mIndicatorContainer);
        }
        initTitlesAndIndicator();
    }

    /**
     * 初始化title和indicator
     */
    private void initTitlesAndIndicator(){
        for (int i = 0,j = mNavigatorHelper.getTotalCount(); i < j; i++) {
            IPagerTitleView v = adapter.getTitleView(getContext(),i);
            if(v instanceof View){
                View view = (View) v;
                LinearLayout.LayoutParams lp;
                if(mAdjustMode){
                    lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                    lp.weight = adapter.getTitleWeight(getContext(),i);
                }else{
                    lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                }
                mTitleContainer.addView(view,lp);
            }
        }

        if(adapter != null){
            mIndicator = adapter.getIndicator(getContext());
            if(mIndicator instanceof View){
                LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                mIndicatorContainer.addView((View)mIndicator,lp);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(adapter != null){
            preparePositionData();
            if(mIndicator != null){
                mIndicator.onPositionDataProvide(mPositionDataList);
            }
            if(mReselectWhenLayout && mNavigatorHelper.getScrollState() == ScrollState.SCROLL_STATE_IDLE){
                onPageSelected(mNavigatorHelper.getCurrentIndex());
                onPageScrolled(mNavigatorHelper.getCurrentIndex(),0.0f,0);
            }
        }
    }

    /**
     * 获取title的位置信息，为打造不同的指示器，
     */
    private void preparePositionData(){
        mPositionDataList.clear();
        for (int i = 0; i < mNavigatorHelper.getTotalCount(); i++) {
            PositionData data = new PositionData();
            View view = mTitleContainer.getChildAt(i);
            if(view != null){
                data.mLeft = view.getLeft();
                data.mTop = view.getTop();
                data.mRight = view.getRight();
                data.mBottom = view.getBottom();
                if(view instanceof IMeasureablePagerTitleView){
                    IMeasureablePagerTitleView v1 = (IMeasureablePagerTitleView) view;
                    data.mContentLeft = v1.getContentLeft();
                    data.mContentTop = v1.getContentTop();
                    data.mContentRight = v1.getContentRight();
                    data.mContentBottom = v1.getContentBottom();
                }else{
                    data.mContentLeft = data.mLeft;
                    data.mContentTop = data.mTop;
                    data.mContentRight = data.mRight;
                    data.mContentBottom = data.mBottom;
                }
            }
            mPositionDataList.add(data);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if(adapter != null){
            mNavigatorHelper.onPageScrolled(position,positionOffset,positionOffsetPixels);
            if(mIndicator != null){
                mIndicator.onPageScrolled(position,positionOffset,positionOffsetPixels);
            }

            //手指跟随滚动
            if(mScrollView != null && mPositionDataList.size() > 0 && position >= 0 && position < mPositionDataList.size()){
                if(mFollowTouch){
                    int currentPosition = Math.min(mPositionDataList.size() - 1,position);
                    int nextPosition = Math.min(mPositionDataList.size() - 1,position + 1);
                    PositionData current = mPositionDataList.get(currentPosition);
                    PositionData next = mPositionDataList.get(nextPosition);
                    float scrollTo = current.horizontalCenter() - mScrollView.getWidth() * mScrollPivotX;
                    float nextScrollTo = next.horizontalCenter() - mScrollView.getWidth() * mScrollPivotX;
                    mScrollView.scrollTo((int)(scrollTo + (nextScrollTo - scrollTo) * positionOffset),0);
                }
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        if(adapter != null){
            mNavigatorHelper.onPageSelected(position);
            if(mIndicator != null)
                mIndicator.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if(adapter != null){
            mNavigatorHelper.onPageScrollStateChanged(state);
            if(mIndicator != null)
                mIndicator.onPageScrollStateChanged(state);
        }
    }

    @Override
    public void onAttachToIndicator() {
        init(); //将初始化延迟到这里
    }

    @Override
    public void onDetachFromIndicator() {

    }

    @Override
    public void notifyDataSetChanged() {
        if(adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
        if(mTitleContainer == null)
            return;
        View childAt = mTitleContainer.getChildAt(index);
        if(childAt instanceof IPagerTitleView){
            ((IPagerTitleView)childAt).onEnter(index,totalCount,enterPercent,leftToRight);
        }
    }

    @Override
    public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {
        if(mTitleContainer == null) return;
        View childAt = mTitleContainer.getChildAt(index);
        if(childAt instanceof IPagerTitleView){
            ((IPagerTitleView)childAt).inLeave(index,totalCount,leavePercent,leftToRight);
        }
    }

    @Override
    public void onSelected(int index, int totalCount) {
        if(mTitleContainer == null) return;
        View childAt = mTitleContainer.getChildAt(index);
        if(childAt instanceof IPagerTitleView){
            ((IPagerTitleView)childAt).onSelected(index,totalCount);
        }
        if(!mAdjustMode && !mFollowTouch && mScrollView != null && mPositionDataList.size() > 0){
            int currentIndex = Math.min(mPositionDataList.size() - 1,index);
            PositionData currData = mPositionDataList.get(currentIndex);
            if(mEnablePrivotScroll){
                float scrollTo = currData.horizontalCenter() - mScrollView.getWidth() * mScrollPivotX;
                if(mSmoothScroll){
                    mScrollView.smoothScrollTo((int) scrollTo,0);
                }else{
                    mScrollView.scrollTo((int) scrollTo,0);
                }
            }else{
                //如果当前项被部分遮挡，则滚动显示完全
                if(mScrollView.getScaleX() > currData.mLeft){
                    if(mSmoothScroll){
                        mScrollView.smoothScrollTo(currData.mLeft,0);
                    }else{
                        mScrollView.scrollTo(currData.mLeft,0);
                    }
                }else if(mScrollView.getScaleX() + getWidth() < currData.mRight){
                    if(mSmoothScroll){
                        mScrollView.smoothScrollTo(currData.mRight - getWidth(),0);
                    }else{
                        mScrollView.scrollTo(currData.mRight - getWidth(),0);
                    }
                }
            }
        }
    }

    @Override
    public void onDeselected(int index, int totalCount) {
        if(mTitleContainer == null) return;
        View childAt = mTitleContainer.getChildAt(index);
        if(childAt instanceof IPagerTitleView){
            ((IPagerTitleView)childAt).onDeselect(index,totalCount);
        }
    }

    public void setScrollPivotX(float mScrollPivotX) {
        this.mScrollPivotX = mScrollPivotX;
    }

    public float getScrollPivotX() {
        return mScrollPivotX;
    }

    public boolean isEnablePrivotScroll() {
        return mEnablePrivotScroll;
    }

    public void setEnablePrivotScroll(boolean mEnablePrivotScroll) {
        this.mEnablePrivotScroll = mEnablePrivotScroll;
    }

    public boolean isSkimOver() {
        return mSkimOver;
    }

    public void setSkimOver(boolean mSkimOver) {
        this.mSkimOver = mSkimOver;
        mNavigatorHelper.setSkimOver(mSkimOver);
    }

    public IPagerIndicator getPagerIndicator() {
        return mIndicator;
    }

    public IPagerTitleView getPagerTitleView(int index){
        if(mTitleContainer == null) return null;
        return (IPagerTitleView) mTitleContainer.getChildAt(index);
    }

    public LinearLayout getTitleContainer() {
        return mTitleContainer;
    }
}
