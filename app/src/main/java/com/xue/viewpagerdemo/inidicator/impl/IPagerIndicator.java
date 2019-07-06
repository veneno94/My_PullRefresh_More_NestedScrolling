package com.xue.viewpagerdemo.inidicator.impl;

import java.util.List;

/**
 * @author: hezhiqiang
 * @date: 2017/7/4
 * @version:
 * @description:    抽象的viewpager指示器，适用于CommonNavigator
 */

public interface IPagerIndicator {

    void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

    void onPageSelected(int position);

    void onPageScrollStateChanged(int state);

    void onPositionDataProvide(List<PositionData> dataList);
}
