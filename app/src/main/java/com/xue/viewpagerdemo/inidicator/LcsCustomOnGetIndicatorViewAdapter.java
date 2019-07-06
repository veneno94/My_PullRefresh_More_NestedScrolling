package com.xue.viewpagerdemo.inidicator;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xue.viewpagerdemo.R;
import com.xue.viewpagerdemo.inidicator.callback.OnGetIndicatorViewAdapter;
import com.xue.viewpagerdemo.inidicator.impl.IPagerIndicator;
import com.xue.viewpagerdemo.inidicator.impl.IPagerTitleView;


/**
 * 理财师个人主页用于填充每个tabview
 */
public abstract class LcsCustomOnGetIndicatorViewAdapter extends OnGetIndicatorViewAdapter {
    //view
    private LinearLayout ll;
    private PopupWindow window;
    private Context context;

    //data
    public static int ARROW_UP = 0; //箭头朝上
    public static int ARROW_DOWN = 1;
    private int currentIndex;
    private boolean clickItem=false;
    private boolean hasDismiss=false;
    private TabIndicator tabIndicator;
    private int outFirstTime = 0;
    private int outSecondTime = 0;
    //方法
    public  abstract TabIndicator getTabIndicator();
    public  abstract void childTabSelected(int index,String type);

    @Override
    public void getSelectedIndex(int index) {
        currentIndex = index;
    }

    @Override
    public IPagerTitleView getTitleView(Context context, final int index) {

        if(getTabIndicator()==null||context==null) return null;

        this.context = context;
        tabIndicator = getTabIndicator();
        final LcsPageTabView simplePagerTitleView = new LcsPageTabView(context);
        simplePagerTitleView.setText(tabIndicator.getPagerAdapter().getPageTitle(index).toString());
        simplePagerTitleView.setShowArrow(index == 0);
        simplePagerTitleView.setTextSize((float) tabIndicator.getmTextSize());
        simplePagerTitleView.setmNormalColor(tabIndicator.getmTextColor());
        simplePagerTitleView.setmSelectedColor(tabIndicator.getmSelectTextColor());
        simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                outSecondTime = (int) System.currentTimeMillis();

                if(currentIndex!=index){//旧的item转到新的item

                    restoreTabState();
                    tabIndicator.getViewPager().setCurrentItem(index);
                    currentIndex = index;
                    if(simplePagerTitleView.isShowArrow()){
                        simplePagerTitleView.setArrowDirection(ARROW_DOWN);
                    }

                }else {//重复点击此item

                    if(!simplePagerTitleView.isShowArrow()) return;//如果此item不包括箭头就不用判断后面的操作了

                    if(hasDismiss&&outSecondTime-outFirstTime<=300&&outSecondTime-outFirstTime!=0){
                        restoreTabState();
                    }else {
                        if(window!=null&&window.isShowing()) {
                            dismissPopWindow();
                        }else {
                            showPopwindow(simplePagerTitleView,index);
                        }
                    }


                }
            }


        });
        return simplePagerTitleView;
    }

    /**
     * 判断的参数全部还原
     */
    private void restoreTabState() {
        hasDismiss = false;
        outFirstTime = 0;
        outSecondTime = 0;
    }

    @Override
    public IPagerIndicator getIndicator(Context context) {
        return super.getIndicator(context);
    }


    public void showPopwindow(final LcsPageTabView tabView, int index) {

        if(context==null) return;

        tabView.setArrowDirection(ARROW_UP);
        View contentView = LayoutInflater.from(context).inflate(R.layout.pop_item, null, false);
        initView(contentView,tabView,index);

        window = new PopupWindow(contentView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, false);//并不获取焦点，防止拦截其他点击事件
        window.setOutsideTouchable(true);
        window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                outFirstTime = (int) System.currentTimeMillis();

                if(clickItem){
                    clickItem = false;
                    hasDismiss = false;
                }else {
                    hasDismiss = true;
                }
                if(tabView.isShowArrow()){
                    tabView.setArrowDirection(ARROW_DOWN);
                }
            }
        });
        window.showAsDropDown(tabView, 0, 2);
    }

    public void dismissPopWindow() {
        if(window!=null&& window.isShowing()){
            window.dismiss();
        }
    }

    private void initView(View contentView, final LcsPageTabView titleView, int index) {
        if(contentView==null||titleView==null) return;

        ll = contentView.findViewById(R.id.ll);

        dynamicCreateView(index, "关羽");
        dynamicCreateView(index, "张飞");

    }


    /**
     * 根据tab的个数 动态生成相应个数的view
     * @param index
     * @param text
     */
    private void dynamicCreateView(final int index, final String text) {
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView tv = new TextView(context);
        tv.setText(text);
        param.setMargins(20, 20, 0, 0);
        ll.addView(tv, param);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickItem = true;
                if (window != null && window.isShowing()) {
                    window.dismiss();
                }
                childTabSelected(index, text);
            }
        });
    }

}
