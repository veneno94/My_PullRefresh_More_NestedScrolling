package com.xue.viewpagerdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xue.viewpagerdemo.callback.OnReadyPullListener;
import com.xue.viewpagerdemo.callback.OnRefreshListener;
import com.xue.viewpagerdemo.common.AdapterItem;
import com.xue.viewpagerdemo.common.BaseAdapter;
import com.xue.viewpagerdemo.common.BaseViewHolder;
import com.xue.viewpagerdemo.event.RefreshEvent;
import com.xue.viewpagerdemo.items.PageItem;
import com.xue.viewpagerdemo.items.ParentItem;
import com.xue.viewpagerdemo.model.NestedViewModel;
import com.xue.viewpagerdemo.model.PageVO;
import com.xue.viewpagerdemo.recyclerview.HeadFootRecyclerView;
import com.xue.viewpagerdemo.util.StatusBarUtil;
import com.xue.viewpagerdemo.viewholder.ImageViewHolder;
import com.xue.viewpagerdemo.viewholder.PagerViewHolder;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import static com.xue.viewpagerdemo.ViewType.TYPE_PAGER;
import static com.xue.viewpagerdemo.ViewType.TYPE_PARENT;

/**
 * Created by 薛雷 on 2019/2/21.
 */
public class MainActivity extends AppCompatActivity {

    private HeadFootRecyclerView recyclerView;

    private RecyclerView.Adapter adapter;

    private NestedViewModel viewModel;

    private NestedScrollLayout container;
    private LinearLayoutManager mLayoutManager;
    private View headView;
    private ImageView headIv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_generation);
        container = findViewById(R.id.rootview);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setNestedScrollingEnabled(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        container.setRootList(recyclerView);
        container.setTarget(this);
        initAdapter();
        viewModel = ViewModelProviders.of(this).get(NestedViewModel.class);
        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = container.getMeasuredHeight();
                viewModel.getPagerHeight().setValue(height);
            }
        });

        container.setReadyListener(new OnReadyPullListener() {
            @Override
            public boolean isReady() {
                return mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0;
            }
        })
                .setRefreshable(true)
                .setHeader(headIv)
                .setDefaultRefreshView(new OnRefreshListener() {
                    @Override
                    public void onRefreshing() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                //刷新完成后需要调用onRefreshComplete()通知FlexibleLayout
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        EventBus.getDefault().post(new RefreshEvent());
                                        container.onRefreshComplete();
                                    }
                                });
                            }
                        }).start();
                    }
                });

        StatusBarUtil.darkMode(this);
        viewModel.getTabLayout().observe(this, new Observer<View>() {
            @Override
            public void onChanged(View view) {
                if (view != null) {
                    StatusBarUtil.setPaddingSmart(MainActivity.this,view);
                }
            }
        });
    }

    private void initAdapter() {
        headView = LayoutInflater.from(this).inflate(R.layout.head_view_item, recyclerView, false);
        headIv = headView.findViewById(R.id.imageview);
        headIv.setImageResource(R.mipmap.pic2);
        SparseArray<Class<? extends BaseViewHolder>> viewHolders = new SparseArray<>();
        viewHolders.put(TYPE_PARENT, ImageViewHolder.class);
        viewHolders.put(TYPE_PAGER, PagerViewHolder.class);
        int[] ids = new int[]{R.mipmap.pic1, R.mipmap.pic2, R.mipmap.pic3, R.mipmap.pic4, R.mipmap.pic5};
        List<AdapterItem> itemList = new ArrayList<>();
        for (int id : ids) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
            itemList.add(new ParentItem(bitmap));
        }
        List<PageVO> pageList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            pageList.add(new PageVO(Color.WHITE, "tab" + i));
        }
        itemList.add(new PageItem(pageList));
        adapter = new BaseAdapter(itemList, this, viewHolders);
        recyclerView.setRecylcerViewAdapter(adapter);
        recyclerView.addHeadView(headView);

    }


}
