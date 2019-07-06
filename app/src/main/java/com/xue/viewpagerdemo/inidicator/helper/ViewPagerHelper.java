package com.xue.viewpagerdemo.inidicator.helper;


import androidx.viewpager.widget.ViewPager;

import com.xue.viewpagerdemo.inidicator.TabIndicator;

/**
 * @author: hezhiqiang
 * @date: 2017/7/3
 * @version:
 * @description: 简化和ViewPager的绑定
 */

public class ViewPagerHelper {
    public static void bind(final TabIndicator tabIndicator, final ViewPager pager){
        if(tabIndicator == null || pager == null) return;
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tabIndicator.onPageScrolled(position,positionOffset,positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                tabIndicator.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                tabIndicator.onPageScrollStateChanged(state);
            }
        });
    }
}
