package cn.rongcloud.im.ui.activity;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.dbcapp.club.R;

import java.util.List;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.AccountHistoryResponse;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.adapter.AccountHistoryAdapter;
import cn.rongcloud.im.ui.widget.AutoLoadListView;


/**
*
* Author: 邓言诚  Create at : 2018/7/13  02:52
* Email: yanchengdeng@gmail.com
* Describle:转账金额列表
*/
public class AcountHistoryActivity extends BaseActivity {

    private static final int GET_ACCOUNT_HISTORY = 30;
    private SwipeRefreshLayout mRefreshLayout;
    private AutoLoadListView mLvCircle;
    private String mSyncName;
    private AccountHistoryAdapter accountHistoryAdapter;
    private Paint mPaint;
    private boolean mIsRefreshing;
    private boolean mIsComplete;
    private long mRequestTime;
    private static final int PAGE_SIZE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acount_history);

        setTitle(R.string.baojia_account_history, false);

        mSyncName = getSharedPreferences("config", MODE_PRIVATE).
                getString(SealConst.BAOJIA_USER_SYNCNAME, "");

        mLvCircle = findViewById(R.id.lv_circle);
        mRefreshLayout = findViewById(R.id.swipe_layout);


        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.baojia_circle_decoration));
        mPaint.setStyle(Paint.Style.FILL);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mLvCircle.setLayoutManager(layoutManager);
        accountHistoryAdapter = new AccountHistoryAdapter(AcountHistoryActivity.this);
        mLvCircle.setAdapter(accountHistoryAdapter);
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

        request(GET_ACCOUNT_HISTORY);

        //下拉
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mIsRefreshing) {
                    mIsRefreshing = true;
                    mRequestTime = 0;
                    mIsComplete = false;
                    request(GET_ACCOUNT_HISTORY);
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

                request(GET_ACCOUNT_HISTORY);
            }
        });
    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case GET_ACCOUNT_HISTORY:
                return mAction.accountHistory( mRequestTime,mSyncName, PAGE_SIZE);
           }
        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode) {
            case GET_ACCOUNT_HISTORY:
                LoadDialog.dismiss(this);
                AccountHistoryResponse response = (AccountHistoryResponse) result;
                if (response.getCode() == 100000) {
                    List<AccountHistoryResponse.ResultEntity> datas = response.getData();
                    //如果是刷新状态下
                    if (mRequestTime == 0) {
                        mRefreshLayout.setRefreshing(false);
                    }
                    mLvCircle.completeLoad();
                    mIsRefreshing = false;

                    accountHistoryAdapter.addData(datas, mRequestTime == 0);
                    if (datas != null && datas.size() > 0) {
                        mRequestTime = datas.get(datas.size() - 1).getCreateTime1();
                    }
                    if (datas.size() < PAGE_SIZE) {
                        mIsComplete = true;
                    }
                }
                break;

        }

    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        super.onFailure(requestCode, state, result);
        LoadDialog.dismiss(this);
    }


}
