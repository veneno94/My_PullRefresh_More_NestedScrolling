package com.xue.viewpagerdemo.inidicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.xue.viewpagerdemo.R;
import com.xue.viewpagerdemo.inidicator.callback.OnGetIndicatorViewAdapter;
import com.xue.viewpagerdemo.inidicator.helper.ViewPagerHelper;
import com.xue.viewpagerdemo.inidicator.impl.CommonNavigator;
import com.xue.viewpagerdemo.inidicator.impl.CommonNavigatorAdapter;
import com.xue.viewpagerdemo.inidicator.impl.IPagerIndicator;
import com.xue.viewpagerdemo.inidicator.impl.IPagerTitleView;
import com.xue.viewpagerdemo.inidicator.impl.indicators.LinePagerIndicator;
import com.xue.viewpagerdemo.inidicator.impl.titles.ColorFlipPagerTitleView;
import com.xue.viewpagerdemo.inidicator.impl.titles.SimplePagerTitleView;


public class TabIndicator extends FrameLayout {

    private IPagerNavigator navigator;
    //注意：以下属性只有在使用默认TitleView和Indicator时才起作用，
    private int mTextColor;         //tab title未选中颜色
    private int mSelectTextColor;   //tab title选中颜色
    private int mTextSize;          //tab title字体大小
    private int mIndicatorHeight;   //tab indicator 高度
    private int mIndicatorColor;    //tab indicator 颜色
    private boolean adjustMode;     // 是否均分显示
    private ViewPager viewPager;
    private DataSetObserver dataSetObserver;
    private PagerAdapter pagerAdapter;

    private OnGetIndicatorViewAdapter getIndicatorViewAdapter; //通过这个适配器获取TitleView与IndicatorView

    public ViewPager getViewPager() {
        return viewPager;
    }

    public int getmTextColor() {
        return mTextColor;
    }

    public int getmSelectTextColor() {
        return mSelectTextColor;
    }

    public int getmTextSize() {
        return mTextSize;
    }

    public int getmIndicatorHeight() {
        return mIndicatorHeight;
    }

    public int getmIndicatorColor() {
        return mIndicatorColor;
    }

    public boolean isAdjustMode() {
        return adjustMode;
    }

    public PagerAdapter getPagerAdapter() {
        return pagerAdapter;
    }

    public TabIndicator(@NonNull Context context) {
        this(context,null);
    }

    public TabIndicator(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TabIndicator(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabIndicator,defStyleAttr,0);
        mTextColor = a.getColor(R.styleable.TabIndicator_tab_textColor, Color.parseColor("#515151"));
        mSelectTextColor = a.getColor(R.styleable.TabIndicator_tab_selectTextColor, Color.parseColor("#000000"));
        mIndicatorColor = a.getColor(R.styleable.TabIndicator_tab_indicatorColor,Color.parseColor("#ff6600"));
        mTextSize = a.getDimensionPixelSize(R.styleable.TabIndicator_tab_textSize,16);
        mIndicatorHeight = a.getDimensionPixelSize(R.styleable.TabIndicator_tab_indicatorHeight,IndicatorUtils.dp2px(context, 3));
        adjustMode = a.getBoolean(R.styleable.TabIndicator_tab_adjustMode,true);
        a.recycle();
    }

    /**适配ViewPager 指示器**/
    public void onPageScrolled(int position,float positionOffset,int positionOffsetPixels){
        if(navigator != null){
            navigator.onPageScrolled(position,positionOffset,positionOffsetPixels);
        }
    }

    public void onPageSelected(int position){
        if(navigator != null){
            navigator.onPageSelected(position);
            if(getIndicatorViewAdapter!=null){
                getIndicatorViewAdapter.getSelectedIndex(position);
            }
        }
    }

    public void onPageScrollStateChanged(int state){
        if(navigator != null){
            navigator.onPageScrollStateChanged(state);
        }
    }

    public IPagerNavigator getNavigator() {
        return navigator;
    }

    public void setNavigator(IPagerNavigator navigator) {
        if(this.navigator == navigator){
            return;
        }
        if(this.navigator != null)
            this.navigator.onDetachFromIndicator();

        this.navigator = navigator;

        removeAllViews();
        if(this.navigator instanceof View){
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addView((View) navigator,lp);
            navigator.onAttachToIndicator();
        }
    }

    /**
     * 与ViewPager绑定
     * @param viewPager
     */
    public void setupWithViewPager(ViewPager viewPager){
        pagerAdapter = viewPager.getAdapter();
        if(pagerAdapter == null)
            throw new NullPointerException("PagerAdapter is null");
        final IPagerNavigator navigator = getDefaultNavigator(viewPager);
        setNavigator(navigator);
        ViewPagerHelper.bind(this,viewPager);
        //PagerAdapter数据更新时通知TabIndicator刷新数据
        if(dataSetObserver == null){
            dataSetObserver = new DataSetObserver() {
                @Override
                public void onChanged() {
                    navigator.notifyDataSetChanged();
                }
            };
            pagerAdapter.registerDataSetObserver(dataSetObserver);
        }
    }


    //内置一个默认的指示器
    private IPagerNavigator getDefaultNavigator(final ViewPager viewPager){
        this.viewPager = viewPager;
        CommonNavigator commonNavigator = new CommonNavigator(getContext());
        commonNavigator.setSkimOver(true); //夸多页面切换，中间也是否显示 "掠过"效果
        if (adjustMode && pagerAdapter.getCount()<6){
            commonNavigator.setAdjustMode(true);
        } else {
            commonNavigator.setAdjustMode(false);
        }
        commonNavigator.setScrollPivotX(0.65f);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return pagerAdapter.getCount();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                if(getIndicatorViewAdapter != null){
                    IPagerTitleView titleView = getIndicatorViewAdapter.getTitleView(context,index);
                    if(titleView != null) {
                        return titleView;
                    }
                }

                SimplePagerTitleView simplePagerTitleView = new ColorFlipPagerTitleView(context);
                simplePagerTitleView.setText(pagerAdapter.getPageTitle(index).toString());
                simplePagerTitleView.setTextSize(mTextSize);
                simplePagerTitleView.setMinWidth(IndicatorUtils.dp2px(context,70));
                simplePagerTitleView.setNormalColor(mTextColor);
                simplePagerTitleView.setSelectedColor(mSelectTextColor);
                simplePagerTitleView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewPager.setCurrentItem(index);
                    }
                });
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                if(getIndicatorViewAdapter != null){
                    IPagerIndicator iPagerIndicator = getIndicatorViewAdapter.getIndicator(context);
                    if(iPagerIndicator != null)
                        return iPagerIndicator;
                }
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setColors(mIndicatorColor);
                indicator.setLineHeight(mIndicatorHeight);
                indicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                indicator.setYOffset(IndicatorUtils.dp2px(context,5));
                indicator.setLineWidth(IndicatorUtils.dp2px(context, 25));
                indicator.setRoundRadius(IndicatorUtils.dp2px(context, 3));
                indicator.setStartInterpolator(new AccelerateInterpolator());
                indicator.setEndInterpolator(new DecelerateInterpolator(2.0f));
                return indicator;
            }
        });
        return commonNavigator;
    }

    public void setGetIndicatorViewAdapter(OnGetIndicatorViewAdapter getIndicatorViewAdapter) {
        this.getIndicatorViewAdapter = getIndicatorViewAdapter;
    }
}
