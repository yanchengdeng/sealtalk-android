package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.dbcapp.club.R;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.response.GroupNumbersBaoResponse;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

/**
 * Created by AMing on 16/7/1.
 * Company RongCloud
 */
public class TotalGroupMemberActivity extends BaseActivity {
    private static final int CLICK_CONVERSATION_USER_PORTRAIT = 1;
    private static final int ALL_GROUP_NUMBERS = 22;

    private List<GroupNumbersBaoResponse.ResultEntity> mGroupMember;

    private ListView mTotalListView;
    private TotalGroupMember adapter;
    private EditText mSearch;
    private String mGroupID;
    private String mSyncName;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toatl_member);
        setTitle(R.string.total_member);

        mSyncName = getSharedPreferences("config", MODE_PRIVATE).
                getString(SealConst.BAOJIA_USER_SYNCNAME, "");
        initViews();
        mGroupID = getIntent().getStringExtra("targetId");
        /*SealUserInfoManager.getInstance().getGroupMembers(mGroupID, new SealUserInfoManager.ResultCallback<List<GroupNumbersBaoResponse.ResultEntity>>() {
            @Override
            public void onSuccess(List<GroupNumbersBaoResponse.ResultEntity> groupMembers) {
                mGroupMember = groupMembers;
                if (mGroupMember != null && mGroupMember.size() > 0) {
                    setTitle(getString(R.string.total_member) + "(" + mGroupMember.size() + ")");
                    adapter = new TotalGroupMember(mGroupMember, mContext);
                    mTotalListView.setAdapter(adapter);
                    mTotalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            GroupNumbersBaoResponse.ResultEntity bean = (GroupNumbersBaoResponse.ResultEntity) adapter.getItem(position);
                            UserInfo userInfo = new UserInfo(bean.getUserId(), bean.getName(),
                                                             TextUtils.isEmpty(bean.getPortraitUri().toString())
                                                                     ? Uri.parse(RongGenerate.generateDefaultAvatar(bean.getName(), bean.getUserId())) : bean.getPortraitUri());
                            Intent intent = new Intent(mContext, UserDetailActivity.class);
                            Friend friend = CharacterParser.getInstance().generateFriendFromUserInfo(userInfo);
                            intent.putExtra("friend", friend);
                            intent.putExtra("type", CLICK_CONVERSATION_USER_PORTRAIT);
                            intent.putExtra("conversationType", Conversation.ConversationType.GROUP.getValue());
                            startActivity(intent);
                        }
                    });
                    mSearch.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            filterData(s.toString());
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });
                }
            }

            @Override
            public void onError(String errString) {

            }
        });
*/

        LoadDialog.show(this);
        request(ALL_GROUP_NUMBERS);
    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode){
            case ALL_GROUP_NUMBERS:
                return  mAction.getGroupNumbers(mGroupID,mSyncName);
        }
        return super.doInBackground(requestCode, id);
    }


    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode){
            case ALL_GROUP_NUMBERS:
                LoadDialog.dismiss(this);
                GroupNumbersBaoResponse response = (GroupNumbersBaoResponse) result;
                if (response.getCode() == 100000) {
                    if (response.getData()!=null && response.getData().size()>0) {
                        initGroupNumbers(response.getData());
                    }
                }
                break;
                
        }

    }

    private void initGroupNumbers(List<GroupNumbersBaoResponse.ResultEntity> groupMembers) {
        mGroupMember = groupMembers;
        if (mGroupMember != null && mGroupMember.size() > 0) {
            setTitle(getString(R.string.total_member) + "(" + mGroupMember.size() + ")");
            adapter = new TotalGroupMember(mGroupMember, mContext);
            mTotalListView.setAdapter(adapter);
            mTotalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    GroupNumbersBaoResponse.ResultEntity bean = (GroupNumbersBaoResponse.ResultEntity) adapter.getItem(position);
                    UserInfo userInfo = new UserInfo(bean.getId(), bean.getSyncName(),
                            TextUtils.isEmpty(bean.getPortrait().toString())
                                    ? Uri.parse(RongGenerate.generateDefaultAvatar(bean.getUserName(), bean.getId())) : Uri.parse(bean.getPortrait()));
                    Intent intent = new Intent(mContext, UserDetailActivity.class);
                    Friend friend = CharacterParser.getInstance().generateFriendFromUserInfo(userInfo);
                    intent.putExtra("friend", friend);
                    intent.putExtra("type", CLICK_CONVERSATION_USER_PORTRAIT);
                    intent.putExtra("conversationType", Conversation.ConversationType.GROUP.getValue());
                    startActivity(intent);
                }
            });
            mSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterData(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        super.onFailure(requestCode, state, result);
        LoadDialog.dismiss(this);
    }

    private void filterData(String s) {
        List<GroupNumbersBaoResponse.ResultEntity> filterDateList = new ArrayList<>();
        if (TextUtils.isEmpty(s)) {
            filterDateList = mGroupMember;
        } else {
            for (GroupNumbersBaoResponse.ResultEntity groupMember : mGroupMember) {
                if (groupMember.getUserName().contains(s)) {
                    filterDateList.add(groupMember);
                }
            }
        }
        adapter.updateListView(filterDateList);
    }

    private void initViews() {
        mTotalListView = (ListView) findViewById(R.id.total_listview);
        mSearch = (EditText) findViewById(R.id.group_member_search);
    }


    class TotalGroupMember extends BaseAdapter {

        private List<GroupNumbersBaoResponse.ResultEntity> list;

        private Context context;

        private ViewHolder holder;


        public TotalGroupMember(List<GroupNumbersBaoResponse.ResultEntity> list, Context mContext) {
            this.list = list;
            this.context = mContext;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.share_item, parent, false);
                holder.mImageView = (SelectableRoundedImageView) convertView.findViewById(R.id.share_icon);
                holder.title = (TextView) convertView.findViewById(R.id.share_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            GroupNumbersBaoResponse.ResultEntity bean = list.get(position);
            Friend friend = SealUserInfoManager.getInstance().getFriendByID(bean.getId());
            if (friend != null && !TextUtils.isEmpty(friend.getDisplayName())) {
                holder.title.setText(friend.getDisplayName());
            } else {
                holder.title.setText(bean.getUserName());
            }
            ImageLoader.getInstance().displayImage(bean.getPortrait(), holder.mImageView, App.getOptions());
            return convertView;
        }


        public void updateListView(List<GroupNumbersBaoResponse.ResultEntity> list) {
            this.list = list;
            notifyDataSetChanged();
        }
    }


    final static class ViewHolder {
        /**
         * 头像
         */
        SelectableRoundedImageView mImageView;

        TextView title;
    }
}
