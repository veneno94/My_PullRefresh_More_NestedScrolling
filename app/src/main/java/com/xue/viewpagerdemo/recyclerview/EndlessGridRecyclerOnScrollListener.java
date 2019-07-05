package com.xue.viewpagerdemo.recyclerview;

import androidx.recyclerview.widget.RecyclerView;

/**
 * <p/>
 * 自定义RecylcerView上拉加载处理
 */
public abstract class EndlessGridRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if(dy<=0)return;

        //新的监听滑动到底部
        if (isSlideToBottom(recyclerView)) {
            onLoadMore();
        }
    }


    //监听是否到底部
    private boolean isSlideToBottom(RecyclerView recyclerView) {
        if (recyclerView == null) return false;
        if (recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange())
            return true;
        return false;
    }

    public abstract void onLoadMore();

}