package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.dbcapp.club.R;

import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetCustomerListResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.adapter.CustomerAdapter;
import cn.rongcloud.im.ui.widget.AutoLoadListView;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.CSCustomServiceInfo;


public class PublicServiceActivity extends BaseActivity {

    private static final int REQUEST_CUSTOMER = 35;

    private static final int PAGE_SIZE = 20;

    private AutoLoadListView mLvCustomer;
    private CustomerAdapter mAdapter;

    private boolean mIsComplete;
    private long mRequestTime;
    private Paint mPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pub_list);
        setTitle(R.string.baojia_comtomer_title);

        initView();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.baojia_circle_decoration));
        mPaint.setStyle(Paint.Style.FILL);

        Button rightButton = getHeadRightButton();
        rightButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.de_ic_add));
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PublicServiceActivity.this, PublicServiceSearchActivity.class);
                startActivity(intent);
            }
        });

        LoadDialog.show(this);
        request(REQUEST_CUSTOMER);
    }

    private void initView() {
        mLvCustomer = findViewById(R.id.lv_customer_list);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        mLvCustomer.setLayoutManager(manager);
        mAdapter = new CustomerAdapter();
        mLvCustomer.setAdapter(mAdapter);

        mLvCustomer.addItemDecoration(new RecyclerView.ItemDecoration() {
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

        mLvCustomer.setOnloadMore(new AutoLoadListView.OnLoadMoreCallback() {
            @Override
            public void onLoadMore() {
                if (mIsComplete){
                    return;
                }

                request(REQUEST_CUSTOMER);
            }
        });
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode){
            case REQUEST_CUSTOMER:
                return mAction.getCustomerList(PAGE_SIZE, mRequestTime);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode){
            case REQUEST_CUSTOMER:
                LoadDialog.dismiss(this);
                mLvCustomer.completeLoad();
                GetCustomerListResponse response = (GetCustomerListResponse) result;
                if (response.getCode() == 100000){
                    if (response.getData() == null || response.getData().size() < PAGE_SIZE){
                        mIsComplete = true;
                    }
                    mAdapter.addData(response.getData(), mRequestTime == 0);
                    mLvCustomer.completeLoad();
                    mRequestTime = response.getData().get(response.getData().size() - 1).getCreateTime1();
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
