package cn.rongcloud.im.ui.activity;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import com.dbcapp.club.R;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetCircleResponse;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.adapter.CircleAdapter;
import cn.rongcloud.im.ui.widget.AutoLoadListView;
import cn.rongcloud.im.ui.widget.touchgallery.GalleryWidget.GalleryViewPager;
import cn.rongcloud.im.ui.widget.touchgallery.GalleryWidget.UrlPagerAdapter;

/**
*
* Author: 邓言诚  Create at : 2018/6/20  16:18
* Email: yanchengdeng@gmail.com
* Describle: 收藏列表
*/

public class CollectionActivity extends BaseActivity {

    private static final int GET_COLLECT = 60;
    private static final int LIKE_CIRCLE = 34;
    private static final int COMPLAIN_CIRCLE = 35;
    private SwipeRefreshLayout mRefreshLayout;
    private AutoLoadListView mLvCircle;
    private CircleAdapter mCirCleAdapter;

    private boolean mIsRefreshing;
    private boolean mIsComplete;
    private String mSyncName;
    private long mRequestTime;

    private GalleryViewPager mGalleryViewPager;
    private FrameLayout mLayoutBg;
    private List<String> mUrls = new ArrayList<>();
    private UrlPagerAdapter mUrlPagerAdapter;

    private Paint mPaint;
    private static final int PAGE_SIZE = 20;

    private int currentActionPosion = -1;//当前操作的行


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collections);

        setTitle(R.string.baojia_collections_title, false);

        mSyncName = getSharedPreferences("config", MODE_PRIVATE).
                getString(SealConst.BAOJIA_USER_SYNCNAME, "");

        initView();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.baojia_circle_decoration));
        mPaint.setStyle(Paint.Style.FILL);


        mGalleryViewPager.setOnItemClickListener(new GalleryViewPager.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                if (mLayoutBg.getVisibility() == View.VISIBLE) {
                    mLayoutBg.setVisibility(View.GONE);
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mLvCircle.setLayoutManager(layoutManager);
        mCirCleAdapter = new CircleAdapter(CollectionActivity.this,mSyncName);
        mLvCircle.setAdapter(mCirCleAdapter);
        mLvCircle.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                int right = parent.getMeasuredWidth();

                int count = parent.getChildCount();
                for (int i = 0; i < count; i++) {
                    View childView = parent.getChildAt(i);

                    RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) childView.getLayoutParams();
                    int left = childView.getPaddingLeft();
                    int top = layoutParams.bottomMargin + childView.getBottom();
                    int bottom = top + 2;
                    c.drawRect(left, top, right, bottom, mPaint);
                }
            }

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
            }
        });

        //下拉
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mIsRefreshing) {
                    mIsRefreshing = true;
                    mRequestTime = 0;
                    mIsComplete = false;
                    request(GET_COLLECT);
                }
            }
        });

        //上拉
        mLvCircle.setOnloadMore(new AutoLoadListView.OnLoadMoreCallback() {
            @Override
            public void onLoadMore() {
                if (mIsComplete) {
                    return;
                }

                request(GET_COLLECT);
            }
        });

        mCirCleAdapter.setOnImageClickListener(new CircleAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(List<String> urls, int position) {
                if (urls != null) {
                    UrlPagerAdapter adapter = new UrlPagerAdapter(CollectionActivity.this, urls);
                    mGalleryViewPager.setAdapter(adapter);
                    mGalleryViewPager.setCurrentItem(position);
                    mLayoutBg.setVisibility(View.VISIBLE);
                }
            }
        });

        //点赞
        mCirCleAdapter.setLikeClickListener(new CircleAdapter.OnLikeClickListerner() {
            @Override
            public void onLike(int id) {
                currentActionPosion = id;
                LoadDialog.show(CollectionActivity.this);
                request(LIKE_CIRCLE);
            }
        });

        //投诉
        mCirCleAdapter.setmOnComplainClickListener(new CircleAdapter.OnComplainClickListerner() {
            @Override
            public void onComplain(int id) {
                currentActionPosion = id;
                LoadDialog.show(CollectionActivity.this);
                request(COMPLAIN_CIRCLE);
            }
        });

        request(GET_COLLECT);

    }

    private void initView() {
        mLvCircle = findViewById(R.id.lv_circle);
        mRefreshLayout = findViewById(R.id.swipe_layout);
        mGalleryViewPager = findViewById(R.id.gallery_image);
        mLayoutBg = findViewById(R.id.fl_image_bg);
        mUrlPagerAdapter = new UrlPagerAdapter(this, mUrls);
        mGalleryViewPager.setOffscreenPageLimit(3);
        mGalleryViewPager.setAdapter(mUrlPagerAdapter);
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case GET_COLLECT:
                return mAction.getCollected(mSyncName, mRequestTime, PAGE_SIZE);
            case LIKE_CIRCLE:
                if (mCirCleAdapter.getDatas()==null){
                    return null;
                }
                return mAction.likeCircle(mSyncName,mCirCleAdapter.getDatas().get(currentActionPosion).getId() );

            case COMPLAIN_CIRCLE:
                if (mCirCleAdapter.getDatas()==null){
                    return null;
                }
                return mAction.complainCircle(mSyncName, mCirCleAdapter.getDatas().get(currentActionPosion).getId());
        }
        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode) {
            case GET_COLLECT:
                LoadDialog.dismiss(this);
                GetCircleResponse response = (GetCircleResponse) result;
                if (response.getCode() == 100000) {
                    List<GetCircleResponse.ResultEntity> datas = response.getData();
                    //如果是刷新状态下
                    if (mRequestTime == 0) {
                        mRefreshLayout.setRefreshing(false);
                    }
                    mLvCircle.completeLoad();
                    mIsRefreshing = false;

                    mCirCleAdapter.addData(datas, mRequestTime == 0);
                    if (datas != null && datas.size() > 0) {
                        mRequestTime = datas.get(datas.size() - 1).getPublishTime();
                    }
                    if (datas.size() < PAGE_SIZE) {
                        mIsComplete = true;
                    }
                }
                break;
            case LIKE_CIRCLE:
                LoadDialog.dismiss(this);
//                NToast.shortToast(this, R.string.baojia_delete_circle_success);
                if (mCirCleAdapter.getDatas()==null) {
                    return;
                }

                mCirCleAdapter.getDatas().get((int) currentActionPosion).setLikeCount(mCirCleAdapter.getDatas().get((int) currentActionPosion).getLikeCount()+1);
                mCirCleAdapter.notifyItemChanged((int) currentActionPosion);
                mCirCleAdapter.getDatas().get((int) currentActionPosion).setLike(true);
                break;
            case COMPLAIN_CIRCLE:
//                NToast.shortToast(this, R.string.baojia_delete_circle_success);
                LoadDialog.dismiss(this);
                if (mCirCleAdapter.getDatas()==null) {
                    return;
                }

                mCirCleAdapter.getDatas().get((int) currentActionPosion).setComplaintCount(mCirCleAdapter.getDatas().get((int) currentActionPosion).getComplaintCount()+1);
                mCirCleAdapter.notifyItemChanged((int) currentActionPosion);
                mCirCleAdapter.getDatas().get((int) currentActionPosion).setComplaint(true);
                break;


        }

    }


    @Override
    public void onBackPressed() {
        if (mLayoutBg.getVisibility() == View.VISIBLE) {
            mLayoutBg.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        super.onFailure(requestCode, state, result);
        LoadDialog.dismiss(this);
    }
}
