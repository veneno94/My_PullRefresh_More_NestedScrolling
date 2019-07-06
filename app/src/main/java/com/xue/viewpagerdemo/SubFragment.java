package com.xue.viewpagerdemo;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.xue.viewpagerdemo.common.AdapterItem;
import com.xue.viewpagerdemo.common.BaseAdapter;
import com.xue.viewpagerdemo.common.BaseViewHolder;
import com.xue.viewpagerdemo.event.RefreshEvent;
import com.xue.viewpagerdemo.items.TextItem;
import com.xue.viewpagerdemo.model.NestedViewModel;
import com.xue.viewpagerdemo.recyclerview.EndlessGridRecyclerOnScrollListener;
import com.xue.viewpagerdemo.recyclerview.HeadFootRecyclerView;
import com.xue.viewpagerdemo.viewholder.TextViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 薛贤俊 on 2019/2/13.
 */
public class SubFragment extends Fragment {

    private HeadFootRecyclerView recyclerView;

    private BaseAdapter adapter;

    private NestedViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        View view = inflater.inflate(R.layout.goods_list, container, false);
        init(view);
        initEvent();

        return view;
    }

    private void init(View view) {
        int color = getArguments().getInt("color");
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLinearLayout();
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setBackgroundColor(color);

        SparseArray<Class<? extends BaseViewHolder>> viewHolders = new SparseArray<>();
        viewHolders.put(ViewType.TYPE_TEXT, TextViewHolder.class);
        List<AdapterItem> itemList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            itemList.add(new TextItem("text" + i));
        }
        adapter = new BaseAdapter(itemList, view.getContext(), viewHolders);
        recyclerView.setRecylcerViewAdapter(adapter);
    }

    private void initEvent() {
        recyclerView.setOnPullLoadMoreListener(new HeadFootRecyclerView.PullLoadMoreListener() {
            @Override
            public void onLoadMore() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //模拟网络请求 加载数据
                            Thread.sleep(500);
                            final List<AdapterItem> itemList = new ArrayList<>();
                            for (int i = adapter.getItemList().size(); i < adapter.getItemList().size()+20; i++) {
                                itemList.add(new TextItem("text" + i));
                            }


                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.setItemList(itemList);
                                    recyclerView.setPullLoadMoreCompleted();
                                    adapter.notifyDataSetChanged();
                                }
                            });


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSelectedChange(RefreshEvent event) {
        if (adapter != null && recyclerView!=null) {
            recyclerView.onRefresh();
            adapter.clearList();
            final List<AdapterItem> itemList = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                itemList.add(new TextItem("text" + i));
            }
            adapter.setItemList(itemList);
            recyclerView.setPullLoadMoreCompleted();
            adapter.notifyDataSetChanged();

        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        initViewModel();
        if (isVisibleToUser && trackFragment() && viewModel != null) {
            viewModel.getChildList().setValue(recyclerView);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initViewModel();
        if (trackFragment() && viewModel != null) {
            viewModel.getChildList().setValue(recyclerView);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        initViewModel();
        if (!hidden && trackFragment() && viewModel != null) {
            viewModel.getChildList().setValue(recyclerView);
        }
    }

    private void initViewModel() {
        if (viewModel == null && getActivity() != null) {
            viewModel = ViewModelProviders.of(getActivity()).get(NestedViewModel.class);
        }
    }

    private boolean trackFragment() {
        if (getView() == null || !(getView().getParent() instanceof View)) {
            return false;
        }
        View parent = (View) getView().getParent();
        if (parent instanceof ViewPager) {
            ViewPager viewPager = (ViewPager) parent;
            int currentItem = viewPager.getCurrentItem();
            //这里需要注意，SubPagerAdapter中，需要把每个Fragment的position传入Arguments
            int position = getArguments() != null ? getArguments().getInt("position", -1) : -1;
            return currentItem == position;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
