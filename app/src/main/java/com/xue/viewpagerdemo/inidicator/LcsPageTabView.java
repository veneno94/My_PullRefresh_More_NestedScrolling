package com.xue.viewpagerdemo.inidicator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xue.viewpagerdemo.R;
import com.xue.viewpagerdemo.inidicator.impl.IMeasureablePagerTitleView;


/**
 * 理财师个人主页自定义TabView
 *
 * 自定义一组合布局，包括一个TextView和ImageView
 */
public class LcsPageTabView extends ViewGroup implements IMeasureablePagerTitleView {
    private ImageView mImageView;
    private TextView mTextView;
    private Context context;

    protected int mSelectedColor;
    protected int mNormalColor;

    public boolean isShowArrow() {
        return showArrow;
    }

    private boolean showArrow;//显示箭头吗?
    public LcsPageTabView(Context context) {
        this(context,null);
    }

    public LcsPageTabView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public LcsPageTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        View view= LayoutInflater.from(context).inflate(R.layout.tab_item, this);
        initView(view);
    }

    private void initView(View view) {
        mTextView = view.findViewById(R.id.tv_title);
        mImageView = view.findViewById(R.id.img_arrow);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }



    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        View view = getChildAt(0);
        view.layout(0,0,view.getMeasuredWidth(),view.getMeasuredHeight());
    }

    @Override
    public int getContentLeft() {
        return 0;
    }

    @Override
    public int getContentTop() {
        return 0;
    }

    @Override
    public int getContentRight() {
        return 0;
    }

    @Override
    public int getContentBottom() {
        return 0;
    }

    @Override
    public void onSelected(int i, int i1) {
        if(mImageView==null || mTextView==null) return;

        mTextView.setTextColor(mSelectedColor);

        if(showArrow){
            mImageView.setVisibility(VISIBLE);
        }else {
            mImageView.setVisibility(GONE);
        }
    }

    @Override
    public void onDeselect(int i, int i1) {
        if(mTextView!=null){
            mTextView.setTextColor(mNormalColor);
        }
        if(mImageView!=null){
            mImageView.setVisibility(GONE);
        }
    }

    @Override
    public void inLeave(int i, int i1, float v, boolean b) {
    }

    @Override
    public void onEnter(int i, int i1, float v, boolean b) {
    }

    @Override
    public void setText(String s) {
        if(mTextView!=null){
            mTextView.setText(s);
        }
    }

    public void  setArrowDirection(int direction){
        if(mImageView==null||context==null) return;

        if(direction==0){
            //todo  设置方向朝上
//            mImageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.arrow_up));
        }else {
            //todo  设置方向朝下
//            mImageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.arrow_down));
        }

    }

    public void  setTextSize(float f){
        if(mTextView!=null){
            mTextView.setTextSize(f);
        }
    }
    public void setmSelectedColor(int mSelectedColor) {
        this.mSelectedColor = mSelectedColor;
    }

    public void setmNormalColor(int mNormalColor) {
        this.mNormalColor = mNormalColor;
    }

    public void setShowArrow(boolean showArrow) {
        this.showArrow = showArrow;
    }
}
