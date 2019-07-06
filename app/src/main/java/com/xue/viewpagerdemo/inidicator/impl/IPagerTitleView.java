package com.xue.viewpagerdemo.inidicator.impl;

/**
 * @author: hezhiqiang
 * @date: 2017/7/4
 * @version:
 * @description: 抽象的指示器标题，适用于CommonNavigator
 */

public interface IPagerTitleView {

    /**
     * 被选中
     * @param index
     * @param totalCount
     */
    void onSelected(int index, int totalCount);

    /**
     * 未被选中
     * @param index
     * @param totalCount
     */
    void onDeselect(int index, int totalCount);

    /**
     * 离开
     * @param index
     * @param totalCount
     * @param leavePercent 离开的百分比 0.0f - 1.0f
     * @param leftToRight  从左至右离开
     */
    void inLeave(int index, int totalCount, float leavePercent, boolean leftToRight);

    /**
     * 进入
     * @param index
     * @param totalCount
     * @param enterPercent  进入的百分比 0.0f - 1.0f
     * @param leftToRight   从左至右离开
     */
    void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight);

    /**
     * 设置tab title
     * @param text
     */
    void setText(String text);
}
