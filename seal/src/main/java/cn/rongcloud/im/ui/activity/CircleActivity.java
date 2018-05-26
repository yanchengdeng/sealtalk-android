package cn.rongcloud.im.ui.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.DeleteSelfCircleResponse;
import cn.rongcloud.im.server.response.GetCircleResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.adapter.CircleAdapter;
import cn.rongcloud.im.ui.widget.AutoLoadListView;
import cn.rongcloud.im.ui.widget.touchgallery.GalleryWidget.GalleryViewPager;
import cn.rongcloud.im.ui.widget.touchgallery.GalleryWidget.UrlPagerAdapter;

/**
 * Created by star1209 on 2018/5/13.
 */

public class CircleActivity extends BaseActivity implements View.OnClickListener {

    private static final int GET_CIRCLE = 55;
    private static final int DELETE_CIRCLE = 33;

    private SwipeRefreshLayout mRefreshLayout;
    private AutoLoadListView mLvCircle;
    private CircleAdapter mCirCleAdapter;

    private boolean mIsRefreshing;
    private boolean mIsComplete;
    private String mSyncName;
    private long mRequestTime;
    private long mDeleteId = -1;
    private GalleryViewPager mGalleryViewPager;
    private FrameLayout mLayoutBg;
    private List<String> mUrls = new ArrayList<>();
    private UrlPagerAdapter mUrlPagerAdapter;

    private Paint mPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle);
        setTitle(R.string.baojia_circlr_title, false);

        mSyncName = getSharedPreferences("config", MODE_PRIVATE).
                getString(SealConst.BAOJIA_USER_SYNCNAME, "");

        initView();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.baojia_circle_decoration));
        mPaint.setStyle(Paint.Style.FILL);

        BroadcastManager.getInstance(this).addAction(SealConst.BAOJIA_PUBLISH_CIRCLE, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRequestTime = 0;
                mIsComplete = false;
                request(GET_CIRCLE);
            }
        });
        request(GET_CIRCLE);
    }

    private void initView() {
        mLvCircle = findViewById(R.id.lv_circle);
        mRefreshLayout = findViewById(R.id.swipe_layout);
        mGalleryViewPager = findViewById(R.id.gallery_image);
        mLayoutBg = findViewById(R.id.fl_image_bg);
        mUrlPagerAdapter = new UrlPagerAdapter(this, mUrls);
        mGalleryViewPager.setOffscreenPageLimit(3);
        mGalleryViewPager.setAdapter(mUrlPagerAdapter);

        mHeadRightText.setVisibility(View.VISIBLE);
        mHeadRightText.setText(R.string.baojia_circle_publish);
        mHeadRightText.setOnClickListener(this);
        mGalleryViewPager.setOnItemClickListener(new GalleryViewPager.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                if (mLayoutBg.getVisibility() == View.VISIBLE){
                    mLayoutBg.setVisibility(View.GONE);
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mLvCircle.setLayoutManager(layoutManager);
        mCirCleAdapter = new CircleAdapter(mSyncName);
        mLvCircle.setAdapter(mCirCleAdapter);
        mLvCircle.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                int right = parent.getMeasuredWidth();

                int count = parent.getChildCount();
                for (int i = 0; i < count; i ++){
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

        mCirCleAdapter.setOnDeleteListener(new CircleAdapter.OnDeleteListener() {
            @Override
            public void onDelete(long id) {
                mDeleteId = id;
                AlertDialog.Builder builder = new AlertDialog.Builder(CircleActivity.this);
                builder.setMessage("确定要删除吗？");
                builder.setPositiveButton(R.string.baojia_delete_contact_sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LoadDialog.show(CircleActivity.this);
                        request(DELETE_CIRCLE);
                    }
                });
                builder.setNegativeButton(R.string.baojia_delete_contact_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create().show();
            }
        });

        mCirCleAdapter.setOnImageClickListener(new CircleAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(List<String> urls, int position) {
                if (urls != null){
                    UrlPagerAdapter adapter = new UrlPagerAdapter(CircleActivity.this, urls);
                    mGalleryViewPager.setAdapter(adapter);
                    mGalleryViewPager.setCurrentItem(position);
                    mLayoutBg.setVisibility(View.VISIBLE);
                }
            }
        });

        //下拉
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mIsRefreshing){
                    mIsRefreshing = true;
                    mRequestTime = 0;
                    mIsComplete = false;
                    request(GET_CIRCLE);
                }
            }
        });

        //上拉
        mLvCircle.setOnloadMore(new AutoLoadListView.OnLoadMoreCallback() {
            @Override
            public void onLoadMore() {
                if (mIsComplete){
                    return;
                }

                request(GET_CIRCLE);
            }
        });
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode){
            case GET_CIRCLE:
                return mAction.getCircle(mSyncName, mRequestTime);
            case DELETE_CIRCLE:
                return mAction.deleteSelfCircle(mSyncName, mDeleteId);
        }
        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode){
            case GET_CIRCLE:
                LoadDialog.dismiss(this);
                GetCircleResponse response = (GetCircleResponse) result;
                if (response.getCode() == 100000){
                    List<GetCircleResponse.ResultEntity> datas = response.getData();
                    //如果是刷新状态下
                    if (mRequestTime == 0){
                        mRefreshLayout.setRefreshing(false);
                    }
                    mLvCircle.completeLoad();
                    mIsRefreshing = false;

                    mCirCleAdapter.addData(datas, mRequestTime == 0);
                    if (datas != null && datas.size() > 0){
                        mRequestTime = datas.get(datas.size() - 1).getPublishTime();
                    }
                    if (datas.size() < 20){
                        mIsComplete = true;
                    }
                }
                break;
            case DELETE_CIRCLE:
                mDeleteId = -1;
                DeleteSelfCircleResponse deleteResponse = (DeleteSelfCircleResponse) result;
                if (deleteResponse.getCode() == 100000){
                    mRequestTime = 0;
                    mIsComplete = false;
                    request(GET_CIRCLE);
                    NToast.shortToast(this, R.string.baojia_delete_circle_success);
                }else {
                    NToast.shortToast(this, deleteResponse.getMessage());
                }
                break;
        }

    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        super.onFailure(requestCode, state, result);
        LoadDialog.dismiss(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.text_right: //发布
                gotoPublish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mLayoutBg.getVisibility() == View.VISIBLE){
            mLayoutBg.setVisibility(View.GONE);
        }else {
            super.onBackPressed();
        }
    }

    private void gotoPublish() {
        Intent intent = new Intent(this, PublishActivity.class);
        startActivity(intent);
    }
}
