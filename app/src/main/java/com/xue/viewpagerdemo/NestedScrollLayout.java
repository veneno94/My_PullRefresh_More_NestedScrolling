package com.xue.viewpagerdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.xue.viewpagerdemo.callback.OnPullListener;
import com.xue.viewpagerdemo.callback.OnReadyPullListener;
import com.xue.viewpagerdemo.callback.OnRefreshListener;
import com.xue.viewpagerdemo.model.NestedViewModel;
import com.xue.viewpagerdemo.util.PullAnimatorUtil;

/**
 * Created by 薛雷 on 2019/2/15.用这个不会上拉加载更多时一甩出很多页
 */
public class NestedScrollLayout extends FrameLayout implements IFlexible {

    /**
     * 是否允许下拉放大
     */
    private boolean isEnable = true;

    /**
     * 是否允许下拉刷新
     */
    private boolean isRefreshable = false;

    /**
     * 头部高度
     */
    private int mHeaderHeight = 0;

    /**
     * 头部宽度
     */
    private int mHeaderWidth = 0;

    /**
     * 头部size ready
     */
    private boolean mHeaderSizeReady;

    /**
     * 头部
     */
    private View mHeaderView;

    /**
     * 刷新
     */
    private View mRefreshView;

    /**
     * 刷新View的宽高
     */
    private int mRefreshSize = getScreenWidth() / 15;

    /**
     * 最大头部下拉高度
     */
    private int mMaxPullHeight = getScreenWidth() / 3;

    /**
     * 最大 刷新 下拉高度
     */
    private int mMaxRefreshPullHeight = getScreenWidth() / 3;

    /**
     * true 开始下拽
     */
    private boolean mIsBeingDragged;

    /**
     * 标志：正在刷新
     */
    private boolean mIsRefreshing;

    /**
     * 准备下拉监听
     */
    private OnReadyPullListener mListener;

    /**
     * 刷新监听
     */
    private OnRefreshListener mRefreshListener;

    /**
     * 初始坐标
     */
    private float mInitialY, mInitialX;

    /**
     * 下拉监听
     */
    private OnPullListener mOnPullListener;

    /**
     * 刷新动画消失监听
     */
    private RefreshAnimatorListener mRefreshAnimatorListener = new RefreshAnimatorListener();


    private View mChildView;
    /**
     * 最外层的RecyclerView
     */
    private RecyclerView mRootList;
    /**
     * 子RecyclerView
     */
    private RecyclerView mChildList;
    /**
     * 用来处理Fling
     */
    private OverScroller mScroller;

    private NestedViewModel mScrollViewModel;

    private int mLastY;

    public NestedScrollLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public NestedScrollLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setTarget(LifecycleOwner target) {
        if (target instanceof FragmentActivity) {
            mScrollViewModel = ViewModelProviders.of((FragmentActivity) target).get(NestedViewModel.class);
        } else if (target instanceof Fragment) {
            mScrollViewModel = ViewModelProviders.of((Fragment) target).get(NestedViewModel.class);
        } else {
            throw new IllegalArgumentException("target must be FragmentActivity or Fragment");
        }
        mScrollViewModel.getChildView().observe(target, new Observer<View>() {
            @Override
            public void onChanged(@Nullable View view) {
                mChildView = view;
            }
        });
        mScrollViewModel.getChildList().observe(target, new Observer<View>() {
            @Override
            public void onChanged(@Nullable View view) {
                Log.d("xuetest", "onChanged");
                mChildList = (RecyclerView) view;
            }
        });
    }

    public void setRootList(RecyclerView recyclerView) {
        mRootList = recyclerView;
    }


    private void init() {
        mIsRefreshing = false;
        mHeaderSizeReady = false;

        mScroller = new OverScroller(getContext());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        log("onInterceptTouchEvent");
        if (isEnable && isHeaderReady() && isReady()) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    log("onInterceptTouchEvent DOWN");
                    mInitialX = ev.getX();
                    mInitialY = ev.getY();
                    mIsBeingDragged = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    log("onInterceptTouchEvent MOVE");
                    float diffY = ev.getY() - mInitialY;
                    float diffX = ev.getX() - mInitialX;
                    if (diffY > 0 && diffY / Math.abs(diffX) > 2) {
                        mIsBeingDragged = true;
                        log("onInterceptTouchEvent return true");
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        log("onTouchEvent");
        if (isEnable && isHeaderReady() && isReady()) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (mIsBeingDragged) {
                        float diffY = ev.getY() - mInitialY;
                        changeHeader((int) diffY);
                        changeRefreshView((int) diffY);
                        if (mOnPullListener != null) {
                            mOnPullListener.onPull((int) diffY);
                        }
                        log("onTouchEvent return true");
                        //return true;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (mIsBeingDragged) {
                        resetHeader();
                        if (mOnPullListener != null) {
                            mOnPullListener.onRelease();
                        }
                        //刷新操作
                        float diffY = ev.getY() - mInitialY;
                        changeRefreshViewOnActionUp((int) diffY);
                        return true;
                    }
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean isReady() {
        return mListener != null && mListener.isReady();
    }

    @Override
    public boolean isHeaderReady() {
        return mHeaderView != null && mHeaderSizeReady;
    }

    @Override
    public void changeHeader(int offsetY) {
        PullAnimatorUtil.pullAnimator(mHeaderView, mHeaderHeight, mHeaderWidth, offsetY, mMaxPullHeight);
    }

    @Override
    public void resetHeader() {
        PullAnimatorUtil.resetAnimator(mHeaderView, mHeaderHeight, mHeaderWidth);
    }

    @Override
    public void changeRefreshView(int offsetY) {
        if (!isRefreshable || mRefreshView == null || isRefreshing()) {
            return;
        }
        PullAnimatorUtil.pullRefreshAnimator(mRefreshView, offsetY, mRefreshSize, mMaxRefreshPullHeight);
    }

    @Override
    public void changeRefreshViewOnActionUp(int offsetY) {
        if (!isRefreshable || mRefreshView == null || isRefreshing()) {
            return;
        }
        mIsRefreshing = true;
        if (offsetY > mMaxRefreshPullHeight) {
            PullAnimatorUtil.onRefreshing(mRefreshView);
            if (mRefreshListener != null) {
                mRefreshListener.onRefreshing();
            }
        } else {
            PullAnimatorUtil.resetRefreshView(mRefreshView, mRefreshSize, mRefreshAnimatorListener);
        }
    }

    @Override
    public void onRefreshComplete() {
        if (!isRefreshable || mRefreshView == null) {
            return;
        }
        PullAnimatorUtil.resetRefreshView(mRefreshView, mRefreshSize, mRefreshAnimatorListener);
    }


    @Override
    public boolean isRefreshing() {
        return mIsRefreshing;
    }

    /**
     * 是否允许下拉放大
     *
     * @param isEnable
     * @return
     */
    public NestedScrollLayout setEnable(boolean isEnable) {
        this.isEnable = isEnable;
        return this;
    }

    /**
     * 是否允许下拉刷新
     *
     * @param isEnable
     * @return
     */
    public NestedScrollLayout setRefreshable(boolean isEnable) {
        this.isRefreshable = isEnable;
        return this;
    }

    /**
     * 设置头部
     *
     * @param header
     * @return
     */
    public NestedScrollLayout setHeader(View header) {
        mHeaderView = header;
        mHeaderView.post(new Runnable() {
            @Override
            public void run() {
                mHeaderHeight = mHeaderView.getHeight();
                mHeaderWidth = mHeaderView.getWidth();
                mHeaderSizeReady = true;
            }
        });
        return this;
    }

    /**
     * Header最大下拉高度
     *
     * @param height
     * @return
     */
    public NestedScrollLayout setMaxPullHeight(int height) {
        mMaxPullHeight = height;
        return this;
    }

    /**
     * 刷新控件 最大下拉高度
     *
     * @param height
     * @return
     */
    public NestedScrollLayout setMaxRefreshPullHeight(int height) {
        mMaxRefreshPullHeight = height;
        return this;
    }

    /**
     * 设置刷新View的尺寸（正方形）
     *
     * @param size
     * @return
     */
    public NestedScrollLayout setRefreshSize(int size) {
        mRefreshSize = size;
        return this;
    }

    /**
     * 设置刷新View
     *
     * @param refreshView
     * @param listener
     * @return
     */
    public NestedScrollLayout setRefreshView(View refreshView, OnRefreshListener listener) {
        if (mRefreshView != null) {
            removeView(mRefreshView);
        }
        mRefreshView = refreshView;
        mRefreshListener = listener;
        FrameLayout.LayoutParams layoutParams = new LayoutParams(mRefreshSize, mRefreshSize);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        mRefreshView.setLayoutParams(layoutParams);
        mRefreshView.setTranslationY(-mRefreshSize);
        addView(mRefreshView);
        return this;
    }

    /**
     * 设置默认的刷新头
     *
     * @param listener
     * @return
     */
    public NestedScrollLayout setDefaultRefreshView(OnRefreshListener listener) {
        ImageView refreshView = new ImageView(getContext());
        refreshView.setImageResource(R.drawable.flexible_loading);
        return setRefreshView(refreshView, listener);
    }

    /**
     * 监听 是否可以下拉放大
     *
     * @param listener
     * @return
     */
    public NestedScrollLayout setReadyListener(OnReadyPullListener listener) {
        mListener = listener;
        return this;
    }

    /**
     * 设置下拉监听
     *
     * @param onPullListener
     * @return
     */
    public NestedScrollLayout setOnPullListener(OnPullListener onPullListener) {
        mOnPullListener = onPullListener;
        return this;
    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    private int getScreenWidth() {
        WindowManager mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        if (mWindowManager != null) {
            mWindowManager.getDefaultDisplay().getMetrics(metrics);
            return metrics.widthPixels;
        } else {
            return 300;
        }
    }

    private void log(String str) {
        //Log.i("FlexibleView", str);
    }

    class RefreshAnimatorListener extends AnimatorListenerAdapter {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            mIsRefreshing = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            super.onAnimationCancel(animation);
            mIsRefreshing = false;
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        stopScroller();
        if (mChildView == null) {
            return;
        }
        if (target == mRootList) {
            onParentScrolling(mChildView.getTop(), dy, consumed);
        } else {
            onChildScrolling(mChildView.getTop(), dy, consumed);
        }
    }

    /**
     * 父列表在滑动
     *
     * @param childTop
     * @param dy
     * @param consumed
     */
    private void onParentScrolling(int childTop, int dy, int[] consumed) {
        //列表已经置顶
        if (childTop == 0) {
            if (dy > 0 && mChildList != null) {
                //还在向下滑动，此时滑动子列表
                mChildList.scrollBy(0, dy);
                consumed[1] = dy;
            } else {
                if (mChildList != null && mChildList.canScrollVertically(dy)) {
                    consumed[1] = dy;
                    mChildList.scrollBy(0, dy);
                }
            }
        } else {
            if (childTop < dy) {
                consumed[1] = dy - childTop;
            }
        }
    }

    private void onChildScrolling(int childTop, int dy, int[] consumed) {
        if (childTop == 0) {
            if (dy < 0) {
                //向上滑动
                if (!mChildList.canScrollVertically(dy)) {
                    consumed[1] = dy;
                    mRootList.scrollBy(0, dy);
                }
            }
        } else {
            if (dy < 0 || childTop > dy) {
                consumed[1] = dy;
                mRootList.scrollBy(0, dy);
            } else {
                //dy大于0
                consumed[1] = dy;
                mRootList.scrollBy(0, childTop);
            }
        }
    }

    private void stopScroller() {
        mScroller.forceFinished(true);
    }

    private void onFling(int dy) {
        if (mChildView != null) {
            //子列表有显示
            int top = mChildView.getTop();
            if (top == 0) {
                if (dy > 0) {
                    if (mChildList != null && mChildList.canScrollVertically(dy)) {
                        mChildList.scrollBy(0, dy);
                    } else {
                        stopScroller();
                    }
                } else {
                    if (mChildList != null && mChildList.canScrollVertically(dy)) {
                        mChildList.scrollBy(0, dy);
                    } else {
                        mRootList.scrollBy(0, dy);
                    }
                }
            } else {
                if (dy > 0) {
                    if (top > dy) {
                        mRootList.scrollBy(0, dy);
                    } else {
                        mRootList.scrollBy(0, top);
                    }
                } else {
                    if (mRootList.canScrollVertically(dy)) {
                        mRootList.scrollBy(0, dy);
                    } else {
                        stopScroller();
                    }
                }
            }
        } else {
            if (!mRootList.canScrollVertically(dy)) {
                stopScroller();
            } else {
                mRootList.scrollBy(0, dy);
            }
        }
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        mLastY = 0;
        this.mScroller.fling(0, 0, (int) velocityX, (int) velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        invalidate();
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int currY = mScroller.getCurrY();
            int dy = currY - mLastY;
            mLastY = currY;
            if (dy != 0) {
                onFling(dy);
            }
            invalidate();
        }
        super.computeScroll();
    }
}
