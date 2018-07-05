package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dbcapp.club.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.response.AgreeFriendResponse;
import cn.rongcloud.im.server.response.GetRelationFriendResponse;
import cn.rongcloud.im.server.response.UserRelationshipResponse;
import cn.rongcloud.im.server.utils.CommonUtils;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.adapter.NewFriendListAdapter;


public class NewFriendListActivity extends BaseActivity implements NewFriendListAdapter.OnItemButtonClick,NewFriendListAdapter.OnItemButtonRefuseClick, View.OnClickListener {

    private static final int GET_ALL = 11;
    private static final int AGREE_FRIENDS = 12;
    public static final int FRIEND_LIST_REQUEST_CODE = 1001;
    private static final int REUFSE_FRIENDS = 13;

    private ListView shipListView;
    private NewFriendListAdapter adapter;
    private String mFriendSync;
    private TextView isData;
    private GetRelationFriendResponse userRelationshipResponse;
    private String mSyncName;
    private long mRequestTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friendlist);
        mSyncName = getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.BAOJIA_USER_SYNCNAME, "");
        initView();
        if (!CommonUtils.isNetworkConnected(mContext)) {
            NToast.shortToast(mContext, R.string.check_network);
            return;
        }
        LoadDialog.show(mContext);
        request(GET_ALL);
        adapter = new NewFriendListAdapter(mContext);
        shipListView.setAdapter(adapter);
    }

    protected void initView() {
        setTitle(R.string.new_friends);
        shipListView = (ListView) findViewById(R.id.shiplistview);
        isData = (TextView) findViewById(R.id.isData);
        Button rightButton = getHeadRightButton();
        rightButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.de_address_new_friend));
        rightButton.setOnClickListener(this);
    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case GET_ALL:
                return mAction.getRaletionFriend(mSyncName, mRequestTime);
            case AGREE_FRIENDS:
                return mAction.agreeFriend(mSyncName, mFriendSync);
            case REUFSE_FRIENDS:
                return mAction.refuseFriend(mSyncName,mFriendSync);
        }
        return super.doInBackground(requestCode, id);
    }


    @Override
    @SuppressWarnings("unchecked")
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case GET_ALL:
                    userRelationshipResponse = (GetRelationFriendResponse) result;

                    if (userRelationshipResponse.getData().size() == 0) {
                        isData.setVisibility(View.VISIBLE);
                        LoadDialog.dismiss(mContext);
                        return;
                    }

//                    Collections.sort(userRelationshipResponse.getResult(), new Comparator<UserRelationshipResponse.ResultEntity>() {
//
//                        @Override
//                        public int compare(UserRelationshipResponse.ResultEntity lhs, UserRelationshipResponse.ResultEntity rhs) {
//                            Date date1 = stringToDate(lhs);
//                            Date date2 = stringToDate(rhs);
//                            if (date1.before(date2)) {
//                                return 1;
//                            }
//                            return -1;
//                        }
//                    });

                    adapter.removeAll();
                    adapter.addData(userRelationshipResponse.getData());

                    adapter.notifyDataSetChanged();
                    adapter.setOnItemButtonClick(this);
                    adapter.setOnItemButtonRefuseClick(this);
                    LoadDialog.dismiss(mContext);
                    break;
                case AGREE_FRIENDS:
                    AgreeFriendResponse response = (AgreeFriendResponse) result;
                    GetRelationFriendResponse.ResultEntity bean = userRelationshipResponse.getData().get(index);
                    SealUserInfoManager.getInstance().addFriend(new Friend(bean.getSyncName(),
                            bean.getUserName(),
                            Uri.parse(bean.getPortrait() + ""),
                            bean.getUserName(),
                            String.valueOf(bean.getStatus()),
                            null,
                            null,
                            null,
                            CharacterParser.getInstance().getSpelling(bean.getUserName()),
                            CharacterParser.getInstance().getSpelling(bean.getUserName())));
                    // 通知好友列表刷新数据
                    NToast.shortToast(mContext, R.string.agreed_friend);
                    LoadDialog.dismiss(mContext);
                    BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATE_FRIEND);
                    userRelationshipResponse.getData().remove(index);
                    adapter.setDataSet(userRelationshipResponse.getData());
                    adapter.notifyDataSetChanged();
                    request(GET_ALL); //刷新 UI 按钮
                    break;
                case REUFSE_FRIENDS:
                    GetRelationFriendResponse.ResultEntity bean1 = userRelationshipResponse.getData().get(index);
                    SealUserInfoManager.getInstance().deleteFriend(new Friend(bean1.getSyncName(),
                            bean1.getUserName(),
                            Uri.parse(bean1.getPortrait() + ""),
                            bean1.getUserName(),
                            String.valueOf(bean1.getStatus()),
                            null,
                            null,
                            null,
                            CharacterParser.getInstance().getSpelling(bean1.getUserName()),
                            CharacterParser.getInstance().getSpelling(bean1.getUserName())));

                    // 通知好友列表刷新数据
                    NToast.shortToast(mContext, R.string.reject_friend);
                    LoadDialog.dismiss(mContext);
                    BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATE_FRIEND);
                    userRelationshipResponse.getData().remove(index);
                    adapter.setDataSet(userRelationshipResponse.getData());
                    adapter.notifyDataSetChanged();
                    request(GET_ALL); //刷新 UI 按钮
                    break;
            }
        }
    }


    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case GET_ALL:
                LoadDialog.dismiss(mContext);
                break;

        }
    }


    @Override
    protected void onDestroy() {
        if (adapter != null) {
            adapter = null;
        }
        super.onDestroy();
    }

    private int index;

    @Override
    public boolean onButtonClick(int position, View view, int status) {
        index = position;
        if (!CommonUtils.isNetworkConnected(mContext)) {
            NToast.shortToast(mContext, R.string.check_network);
            return false;
        }

        if (position<userRelationshipResponse.getData().size()) {
            LoadDialog.show(mContext);
            mFriendSync = userRelationshipResponse.getData().get(position).getSyncName();
            request(AGREE_FRIENDS);
        }
        return false;
    }

    private Date stringToDate(UserRelationshipResponse.ResultEntity resultEntity) {
        String updatedAt = resultEntity.getUpdatedAt();
        String updatedAtDateStr = updatedAt.substring(0, 10) + " " + updatedAt.substring(11, 16);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date updateAtDate = null;
        try {
            updateAtDate = simpleDateFormat.parse(updatedAtDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return updateAtDate;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(NewFriendListActivity.this, SearchFriendActivity.class);
        startActivityForResult(intent, FRIEND_LIST_REQUEST_CODE);
    }

    @Override
    public boolean onButtonRefuseClick(int position, View view, int status) {
        index = position;
        if (!CommonUtils.isNetworkConnected(mContext)) {
            NToast.shortToast(mContext, R.string.check_network);
            return false;
        }

        if (position<userRelationshipResponse.getData().size()) {
            LoadDialog.show(mContext);
            mFriendSync = userRelationshipResponse.getData().get(position).getSyncName();
            request(REUFSE_FRIENDS);
        }
        return false;
    }
}
