package com.xue.viewpagerdemo.inidicator.impl;

/**
 * @author: hezhiqiang
 * @date: 2017/7/4
 * @version:
 * @description: 实现颜色渐变
 */

public class ArgbEvaluatorHolder {
    public static int eval(float fraction,int startValue,int endValue){
        int startA = (startValue >> 24) & 0xff;
        int startR = (startValue >> 16) & 0xff;
        int startG = (startValue >> 8) & 0xff;
        int startB = startValue & 0xff;

        int endA = (endValue >> 24) & 0xff;
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;

        int curA = (startA + (int)(fraction * (endA - startA))) << 24;
        int curR = (startR + (int)(fraction * (endR - startR))) << 16;
        int curG = (startG + (int)(fraction * (endG - startG))) << 8;
        int curB = (startB + (int)(fraction * (endB - startB)));

        return curA | curR | curG | curB;
    }
}
