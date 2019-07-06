package com.xue.viewpagerdemo.inidicator.callback;

import android.content.Context;

import com.xue.viewpagerdemo.inidicator.impl.IPagerIndicator;
import com.xue.viewpagerdemo.inidicator.impl.IPagerTitleView;


public class OnGetIndicatorViewAdapter {
    public IPagerTitleView getTitleView(Context context, int index){
        return null;
    }

    public IPagerIndicator getIndicator(Context context){
        return null;
    }

    public void getSelectedIndex(int  index){ }
}
