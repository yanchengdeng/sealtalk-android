package cn.rongcloud.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dbcapp.club.R;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetMineAmountResponse;
import cn.rongcloud.im.server.response.GetPlatformAmmountResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.utils.CommonUtils;
import cn.rongcloud.im.utils.PerfectClickListener;

/**
 * Created by star1209 on 2018/5/16.
 */

public class MineWalletActivity extends BaseActivity {

    private static final int REQUEST_AMMOUNT = 22;
    private static final int REQUEST_PLATFORM = 25;

    private static final int REQUEST_RECHARGE = 111;

    private Button mBtnSubmit;
    private EditText mEtInput;
    private TextView mTvMyAmmout;
    private TextView mTvPlatformAmmout;

    private String mSyncName;
    private SharedPreferences mSp;
    private double mMineAmmount;
    private double mPlatformAmmount;
    private double mRechargeAmmount;
    private String mLoginName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine_wallet);

        mSp = getSharedPreferences("config", MODE_PRIVATE);
        mSyncName = mSp.getString(SealConst.BAOJIA_USER_SYNCNAME, "");

        initView();
        LoadDialog.show(this);
        request(REQUEST_AMMOUNT);

        findViewById(R.id.btn_recharge_history).setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                startActivity(new Intent(MineWalletActivity.this,AcountHistoryActivity.class));
            }
        });
    }

    private void initView() {
        setTitle(R.string.baojia_mine_ammount_title, false);

        mBtnSubmit = findViewById(R.id.btn_recharge_mine_ammount);
        mEtInput = findViewById(R.id.et_input_recharge);
        mTvMyAmmout = findViewById(R.id.tv_ammount_remained);
        mTvPlatformAmmout = findViewById(R.id.tv_platform_remained);

        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRechargeAmmount = CommonUtils.string2Double(mEtInput.getText().toString(), 0);
                //最大判断
                if (mRechargeAmmount > mPlatformAmmount){
                    NToast.shortToast(MineWalletActivity.this, R.string.baojia_recharge_max_limit);
                    return;
                }

                //最小判断
                if (TextUtils.isEmpty(String.valueOf(mRechargeAmmount)) || mRechargeAmmount <= 0){
                    NToast.shortToast(MineWalletActivity.this, R.string.baojia_recharge_min_limit);
                    return;
                }

                gotoRecharge();
            }
        });
    }

    private void gotoRecharge() {
        Intent intent = new Intent(this, RechargeWebActivity.class);
        intent.putExtra("recharge_ammount", mRechargeAmmount);
        intent.putExtra("login_name", mLoginName);
        startActivityForResult(intent, REQUEST_RECHARGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_RECHARGE:
                if (resultCode == Activity.RESULT_OK){
                    mTvMyAmmout.setText(String.format(getString(R.string.baojia_mine_wallet_ammount),
                            CommonUtils.twoDecimalFormat(data.getDoubleExtra("wallet_remain", 0))));
                    request(REQUEST_PLATFORM);
                    NToast.shortToast(this, R.string.baojia_recharge_suceess);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode){
            case REQUEST_AMMOUNT:
                return mAction.getAmount(mSyncName);
            case REQUEST_PLATFORM:
                return mAction.getPlatformAmmount(mSyncName);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode){
            case REQUEST_AMMOUNT:
                GetMineAmountResponse amountResponse = (GetMineAmountResponse) result;
                request(REQUEST_PLATFORM);
                if (amountResponse.getCode() == 100000){
                    mMineAmmount = amountResponse.getData();
                    mTvMyAmmout.setText(String.format(getString(R.string.baojia_mine_wallet_ammount),
                            CommonUtils.twoDecimalFormat(amountResponse.getData())));
                }else {
                    NToast.shortToast(this, amountResponse.getMessage());
                }
                break;
            case REQUEST_PLATFORM:
                LoadDialog.dismiss(this);
                GetPlatformAmmountResponse platformResponse = (GetPlatformAmmountResponse) result;
                if (platformResponse.getCode() == 100000){
                    mLoginName = platformResponse.getData().getLoginName();
                    mPlatformAmmount = platformResponse.getData().getBalance();
                    mTvPlatformAmmout.setText(String.format(getString(R.string.baojia_mine_wallet_ammount),
                            CommonUtils.twoDecimalFormat(platformResponse.getData().getBalance())));
                }else {
                    NToast.shortToast(this, platformResponse.getMessage());
                }
                break;
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        super.onFailure(requestCode, state, result);
        LoadDialog.dismiss(this);
        NToast.shortToast(this, "获取数据失败！");
    }
}
