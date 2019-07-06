package com.xue.viewpagerdemo.inidicator;

import android.content.Context;


public class IndicatorUtils {

    public static int dp2px(Context context,double dpValue){
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5);
    }

    public static int getScreenWidth(Context context){
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}
