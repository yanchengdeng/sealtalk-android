package cn.rongcloud.im.ui.activity;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.dbcapp.club.R;

import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetTransferAggregationResponse;
import cn.rongcloud.im.server.response.GetTransferHistoryResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.CircleImageView;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.adapter.TransferHistoryAdapter;
import cn.rongcloud.im.ui.widget.AutoLoadListView;
import cn.rongcloud.im.utils.CommonUtils;
import io.rong.imageloader.core.ImageLoader;

/**
 * Created by star1209 on 2018/5/17.
 */

public class TransferHistoryActivity extends BaseActivity {

    private static final int REQUEST_AGGREGATION = 66;
    private static final int REQUEST_HISTORY = 63;

    private AutoLoadListView mLvReceive;
    private SwipeRefreshLayout mRefreshLayouot;
    private TransferHistoryAdapter mAdapter;
    private TextView mTvReceiveAmount;
    private TextView mTvReceiveCount;
    private TextView mTvMaxCount;
    private CircleImageView mIvPortrait;
    private Paint mPaint;

    private String mSyncName;
    private long mRequestTime;
    private String mPortrait;
    private boolean mIsComplete;
    private boolean mIsRefreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_history);

        mSyncName = getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.BAOJIA_USER_SYNCNAME, "");
        mPortrait = getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGING_PORTRAIT, "");
        initView();
    }

    private void initView() {
        setTitle(R.string.baojia_transfer_history_title);
        mLvReceive = findViewById(R.id.lv_receive_history);
        mRefreshLayouot = findViewById(R.id.swipe_layout);
        mTvMaxCount = findViewById(R.id.tv_max_count);
        mTvReceiveCount = findViewById(R.id.tv_receive_count);
        mTvReceiveAmount = findViewById(R.id.tv_amount_receive);
        mIvPortrait = findViewById(R.id.iv_portrait_circle);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        mAdapter = new TransferHistoryAdapter();
        mLvReceive.setLayoutManager(manager);
        mLvReceive.setAdapter(mAdapter);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.baojia_circle_decoration));
        mPaint.setStyle(Paint.Style.FILL);
        mLvReceive.addItemDecoration(new RecyclerView.ItemDecoration() {
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

        ImageLoader.getInstance().displayImage(mPortrait, mIvPortrait, App.getOptions());

        mLvReceive.setOnloadMore(new AutoLoadListView.OnLoadMoreCallback() {
            @Override
            public void onLoadMore() {
                if (mIsComplete){
                    return;
                }

                request(REQUEST_HISTORY);
            }
        });

        mRefreshLayouot.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mIsRefreshing){
                    mIsRefreshing = true;
                    mRequestTime = 0;
                    mIsComplete = false;
                    request(REQUEST_AGGREGATION);
                }
            }
        });

        LoadDialog.show(this);
        request(REQUEST_AGGREGATION);
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode){
            case REQUEST_AGGREGATION:
                return mAction.getTransferAggregation(mSyncName);
            case REQUEST_HISTORY:
                return mAction.getTransferHistory(mSyncName, mRequestTime);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode){
            case REQUEST_AGGREGATION:
                request(REQUEST_HISTORY);
                GetTransferAggregationResponse aggregationResponse = (GetTransferAggregationResponse) result;
                if (aggregationResponse.getCode() == 100000){
                    if (aggregationResponse.getData() != null){
                        mTvMaxCount.setText(String.valueOf(aggregationResponse.getData().getMax()));
                        mTvReceiveCount.setText(String.valueOf(aggregationResponse.getData().getRecordCount()));
                        mTvReceiveAmount.setText(CommonUtils.twoDecimalFormat(aggregationResponse.getData().getTotal()));
                    }
                }else {
                    NToast.shortToast(this, aggregationResponse.getMessage());
                }
                break;
            case REQUEST_HISTORY:
                LoadDialog.dismiss(this);
                mIsRefreshing = false;
                mLvReceive.completeLoad();
                if (mRequestTime == 0){
                    mRefreshLayouot.setRefreshing(false);
                }
                GetTransferHistoryResponse response = (GetTransferHistoryResponse) result;
                if (response.getCode() == 100000){

                    List<GetTransferHistoryResponse.ResultEntity> datas = response.getData();
                    mAdapter.addData(response.getData(), mRequestTime ==0);
                    if (datas != null && datas.size() > 0){
                        mRequestTime = datas.get(datas.size() - 1).getAcceptTime1();
                    }
                    if (datas.size() < 20){
                        mIsComplete = true;
                    }
                }else {
                    NToast.shortToast(this, response.getMessage());
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
