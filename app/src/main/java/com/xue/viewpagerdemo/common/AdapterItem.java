package com.xue.viewpagerdemo.common;

/**
 * Created by 薛雷 on 2019/2/21.
 */
public interface AdapterItem<T> {

    T getDataModel();

    int getViewType();
}
