package com.xue.viewpagerdemo.inidicator.callback;


public interface OnNavigatorScrollListener {
    void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight);

    void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight);

    void onSelected(int index, int totalCount);

    void onDeselected(int index, int totalCount);
}
