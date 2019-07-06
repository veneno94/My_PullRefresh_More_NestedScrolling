package com.xue.viewpagerdemo.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.xue.viewpagerdemo.R;


/**
 *
 */

public class HeadFootRecyclerView extends RecyclerView {

    private Context mContext;
    private HeaderViewGridRecyclerAdapter mAdapter;
    private boolean isLoading = true;

    private boolean isLoadMore = false;
    private boolean isNoMore = false;//没有更多了 false为有更多
    private FooterView footerView;


    public HeadFootRecyclerView(Context context) {
        this(context, null);
    }

    public HeadFootRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeadFootRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);

    }

    private void initView(Context context) {
        mContext = context;
        setHasFixedSize(true);
        addOnScrollListener(new EndlessGridRecyclerOnScrollListener(this) {
            @Override
            public void onLoadMore() {
                if (mPullLoadMoreListener != null && footerView != null) {
                    footerView.setState(FooterView.Loading);
                    mPullLoadMoreListener.onLoadMore();
                }
            }
        });

    }

    /**
     * 刷新时调用
     */
    public void onRefresh() {
        isNoMore = false;
        setIsLoading(true);
        if (footerView != null) {
            footerView.setState(FooterView.Normal);
        }
    }

    public void setLinearLayout() {
        CrashLinearLayoutManager linearLayoutManager = new CrashLinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        setLayoutManager(linearLayoutManager);
    }

    /**
     * GridLayoutManager
     */

    public void setGridLayout(int spanCount) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, spanCount);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        setLayoutManager(gridLayoutManager);
    }

    /**
     * StaggeredGridLayoutManager
     */
    public void setStaggeredGridLayout(int spanCount) {
        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(spanCount, LinearLayoutManager.VERTICAL);
        setLayoutManager(staggeredGridLayoutManager);
    }

    public void setRecylcerViewAdapter(RecyclerView.Adapter adapter) {
        mAdapter = new HeaderViewGridRecyclerAdapter(adapter);
        setAdapter(mAdapter);
        footerView = new FooterView(mContext);
        mAdapter.addFooterView(footerView);
        footerView.setState(FooterView.Normal);

    }


    public void addHeadView(View headView) {
        if (mAdapter != null) {
            mAdapter.addHeaderView(headView);
        }
    }

    public boolean isLoadMore() {
        return isLoadMore;
    }

    public void setLoadMore(boolean loadMore) {
        isLoadMore = loadMore;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setIsLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public boolean isNoMore() {
        return isNoMore;
    }

    //设置没有更多数据了
    public void setNoMore(String text) {
        if (footerView != null) {
            isNoMore = true;
            setLoadMore(false);
            footerView.setState(FooterView.TheEnd);

        }
    }

    public void setPullLoadMoreCompleted() {
        if (footerView != null) {
            setLoadMore(false);
            footerView.setState(FooterView.Normal);
        }
    }

    public interface PullLoadMoreListener {
        void onLoadMore();
    }

    public void setOnPullLoadMoreListener(PullLoadMoreListener listener) {
        mPullLoadMoreListener = listener;
    }

    private PullLoadMoreListener mPullLoadMoreListener;
}
