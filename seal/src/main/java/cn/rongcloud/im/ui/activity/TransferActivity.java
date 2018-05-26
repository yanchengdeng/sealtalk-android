package cn.rongcloud.im.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dbcapp.club.R;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.message.plugins.TransferMessage;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GetMineAmountResponse;
import cn.rongcloud.im.server.response.TransferResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.utils.CommonUtils;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

/**
 * Created by star1209 on 2018/5/15.
 */

public class TransferActivity extends BaseActivity {

    private static final int GET_MINE_AMOUNT = 39;
    private static final int TRANSFER_MONEY = 38;

    private String mSyncName;
    private String mTargetId;
    private SharedPreferences mSp;
    private String mUserName;
    private String mPortrait;
    private String mLeaveWords;
    private double mMineAmount;

    private TextView mTvRemained;
    private Button mBtnSure;
    private EditText mEtMoney;
    private EditText mEtLeave;

    private double mTransferMoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        mSp = getSharedPreferences("config", MODE_PRIVATE);
        mSyncName = mSp.getString(SealConst.BAOJIA_USER_SYNCNAME, "");
        mUserName = mSp.getString(SealConst.SEALTALK_LOGIN_NAME, "");
        mPortrait = mSp.getString(SealConst.SEALTALK_LOGING_PORTRAIT, "");
        mTargetId = getIntent().getStringExtra("targetId");
        initView();
    }

    private void initView() {
        setTitle(R.string.baojia_transfer_title);
        mTvRemained = findViewById(R.id.tv_money_remainder);
        mEtLeave = findViewById(R.id.et_leaving_transfer);
        mEtMoney = findViewById(R.id.et_money_transfer);
        mBtnSure = findViewById(R.id.btn_sure_transfer);

        LoadDialog.show(this);
        request(GET_MINE_AMOUNT);

        mBtnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String moneyContent = mEtMoney.getText().toString().trim();
                mTransferMoney = CommonUtils.string2Double(moneyContent, 0);
                if (TextUtils.isEmpty(moneyContent) || mTransferMoney <= 0){
                    NToast.shortToast(TransferActivity.this, R.string.baojia_transfer_zero_money);
                    return;
                }

                if (mMineAmount < mTransferMoney){
                    NToast.shortToast(TransferActivity.this, R.string.baojia_transfer_max_limit);
                    return;
                }


                if (TextUtils.isEmpty(mEtLeave.getText().toString().trim())){
                    mLeaveWords = getString(R.string.baojia_transfer_leave_default);
                }else {
                    mLeaveWords = mEtLeave.getText().toString().trim();
                }

                request(TRANSFER_MONEY);
            }
        });

        mEtLeave.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String string = s.toString().trim();
                if (string.length() > 16){
                    mEtLeave.setText(string.substring(0, 16));
                    mEtLeave.setSelection(mEtLeave.getText().toString().trim().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode){
            case GET_MINE_AMOUNT:
                return mAction.getAmount(mSyncName);
            case TRANSFER_MONEY:
                return mAction.transfer(mTargetId, mTransferMoney, mSyncName);
            default:
                return null;
        }

    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode){
            case GET_MINE_AMOUNT: //我的金额
                LoadDialog.dismiss(this);
                GetMineAmountResponse response = (GetMineAmountResponse) result;
                if (response.getCode() == 100000){
                    mMineAmount = response.getData();
                    mTvRemained.setText(String.format(getString(R.string.baojia_transfer_amount),
                            CommonUtils.twoDecimalFormat(response.getData())));
                }else {
                    NToast.shortToast(this, response.getMessage());
                }
                break;
            case TRANSFER_MONEY:
                LoadDialog.dismiss(this);
                TransferResponse transferRespons = (TransferResponse) result;
                if (transferRespons.getCode() == 100000){
                    TransferMessage transferMessage = new TransferMessage(mTransferMoney, mUserName,
                            mSyncName, mLeaveWords, mPortrait);
                    RongIM.getInstance().sendMessage(Message.obtain(mTargetId, Conversation.ConversationType.PRIVATE, transferMessage),
                            "转账", null, new RongIMClient.SendImageMessageCallback() {
                                @Override
                                public void onAttached(Message message) {

                                }

                                @Override
                                public void onError(Message message, RongIMClient.ErrorCode errorCode) {

                                }

                                @Override
                                public void onSuccess(Message message) {

                                }

                                @Override
                                public void onProgress(Message message, int i) {

                                }
                            });
                    finish();
                }else {
                    NToast.shortToast(this, transferRespons.getMessage());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        super.onFailure(requestCode, state, result);
        LoadDialog.dismiss(this);
        NToast.shortToast(this, "获取金额失败！");
    }
}
