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
 */

public class HeadFootRecyclerView extends RecyclerView {

    private Context mContext;
    private HeaderViewGridRecyclerAdapter mAdapter;
    private View loadMoreView;
    private boolean isLoading = true;

    private boolean isLoadMore = false;
    private boolean isNoMore = false;//没有更多了
    private ProgressBar mProgressBar;
    private TextView mLoadTv;
    private LinearLayout loadMoreLl;


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
                if (mPullLoadMoreListener != null) {
                    loadMoreLl.setVisibility(View.VISIBLE);
                    mPullLoadMoreListener.onLoadMore();
                }
            }
        });

    }

    /**
     * 刷新时调用
     */
    public void onRefresh() {
        setNoMore(false);
        setIsLoading(true);
        if (mProgressBar != null && mLoadTv != null) {
            loadMoreLl.setVisibility(GONE);
            mProgressBar.setVisibility(VISIBLE);
            mLoadTv.setText("正在加载中...");
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
        loadMoreView = LayoutInflater.from(mContext).inflate(R.layout.layout_load_more, this, false);
        loadMoreLl =  loadMoreView.findViewById(R.id.load_more_ll);
        mProgressBar =  loadMoreView.findViewById(R.id.load_pro);
        mLoadTv =  loadMoreView.findViewById(R.id.load_tv);
        mAdapter.addFooterView(loadMoreView);
        loadMoreLl.setVisibility(View.GONE);

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

    public void setNoMore(boolean noMore) {
        isNoMore = noMore;
        if (!isNoMore) { //当是false的时候改变文字和显示圆圈
            if (mProgressBar != null && mLoadTv != null) {
                mProgressBar.setVisibility(VISIBLE);
                mLoadTv.setText("正在加载中...");
            }
        }
    }

    //设置没有更多数据了
    public void setNoMore(String text) {
        setNoMore(true);
        setLoadMore(false);
        loadMoreLl.setVisibility(VISIBLE);
        mProgressBar.setVisibility(GONE);
        mLoadTv.setText(text);
    }


    public void setPullLoadMoreCompleted() {
        setLoadMore(false);
        loadMoreLl.setVisibility(View.GONE);
    }

    public interface PullLoadMoreListener {
        void onLoadMore();
    }

    public void setOnPullLoadMoreListener(PullLoadMoreListener listener) {
        mPullLoadMoreListener = listener;
    }

    private PullLoadMoreListener mPullLoadMoreListener;
}
