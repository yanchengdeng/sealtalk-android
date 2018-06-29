package cn.rongcloud.im.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.GroupListBaoResponse;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;

/**
 * Created by AMing on 16/3/8.
 * Company RongCloud
 */
public class GroupListActivity extends BaseActivity {

    private ListView mGroupListView;
    private GroupAdapter adapter;
    private TextView mNoGroups;
    private EditText mSearch;
    private List<GroupListBaoResponse.ResultEntity> mList;
    private TextView mTextView;
    private String mSyncName;
    public static final int GET_GROUP_LIST = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fr_group_list);
        setTitle(R.string.my_groups);
        mGroupListView = (ListView) findViewById(R.id.group_listview);
        mNoGroups = (TextView) findViewById(R.id.show_no_group);
        mSearch = (EditText) findViewById(R.id.group_search);
        mTextView = (TextView)findViewById(R.id.foot_group_size);
        mSyncName = getSharedPreferences("config", MODE_PRIVATE).
                getString(SealConst.BAOJIA_USER_SYNCNAME, "");
        initData();
        BroadcastManager.getInstance(mContext).addAction(SealConst.GROUP_LIST_UPDATE, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                initData();
            }
        });
    }

    private void initData() {
        /*SealUserInfoManager.getInstance().getGroups(new SealUserInfoManager.ResultCallback<List<Groups>>() {
            @Override
            public void onSuccess(List<Groups> groupsList) {
                mList = groupsList;
                if (mList != null && mList.size() > 0) {
                    adapter = new GroupAdapter(mContext, mList);
                    mGroupListView.setAdapter(adapter);
                    mNoGroups.setVisibility(View.GONE);
                    mTextView.setVisibility(View.VISIBLE);
                    mTextView.setText(getString(R.string.ac_group_list_group_number, mList.size()));
                    mGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Groups bean = (Groups) adapter.getItem(position);
                            RongIM.getInstance().startGroupChat(GroupListActivity.this, bean.getGroupsId(), bean.getName());
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
                } else {
                    mNoGroups.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String errString) {

            }
        });
*/
        LoadDialog.show(this);
        request(GET_GROUP_LIST, true);
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case GET_GROUP_LIST:
                return mAction.getGroupList(mSyncName);

        }
        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        switch (requestCode){
            case GET_GROUP_LIST:
                LoadDialog.dismiss(this);
                GroupListBaoResponse response = (GroupListBaoResponse) result;
                if (response.getCode() == 100000) {
                    List<GroupListBaoResponse.ResultEntity> datas = response.getData();
                        mList = datas;
                        if (mList != null && mList.size() > 0) {
                            adapter = new GroupAdapter(mContext, mList);
                            mGroupListView.setAdapter(adapter);
                            mNoGroups.setVisibility(View.GONE);
                            mTextView.setVisibility(View.VISIBLE);
                            mTextView.setText(getString(R.string.ac_group_list_group_number, mList.size()));
                            mGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    GroupListBaoResponse.ResultEntity bean = (GroupListBaoResponse.ResultEntity) adapter.getItem(position);
                                    RongIM.getInstance().startGroupChat(GroupListActivity.this, bean.getGroupToken(), bean.getGroupName());
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
                        } else {
                            mNoGroups.setVisibility(View.VISIBLE);
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

    private void filterData(String s) {
        List<GroupListBaoResponse.ResultEntity> filterDataList = new ArrayList<>();
        if (TextUtils.isEmpty(s)) {
            filterDataList = mList;
        } else {
            for (GroupListBaoResponse.ResultEntity groups : mList) {
                if (groups.getGroupName().contains(s)) {
                    filterDataList.add(groups);
                }
            }
        }
        adapter.updateListView(filterDataList);
        mTextView.setText(getString(R.string.ac_group_list_group_number, filterDataList.size()));
    }


    class GroupAdapter extends BaseAdapter {

        private Context context;

        private List<GroupListBaoResponse.ResultEntity> list;

        public GroupAdapter(Context context, List<GroupListBaoResponse.ResultEntity> list) {
            this.context = context;
            this.list = list;
        }

        /**
         * 传入新的数据 刷新UI的方法
         */
        public void updateListView(List<GroupListBaoResponse.ResultEntity> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (list != null) return list.size();
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (list == null)
                return null;

            if (position >= list.size())
                return null;

            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            final GroupListBaoResponse.ResultEntity mContent = list.get(position);
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.group_item_new, parent, false);
                viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.groupname);
                viewHolder.mImageView = (SelectableRoundedImageView) convertView.findViewById(R.id.groupuri);
                viewHolder.groupId = (TextView) convertView.findViewById(R.id.group_id);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.tvTitle.setText(mContent.getGroupName());
//            String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(mContent.getGroupIcon());
            ImageLoader.getInstance().displayImage(mContent.getGroupIcon(), viewHolder.mImageView, App.getOptions());
            if (context.getSharedPreferences("config", MODE_PRIVATE).getBoolean("isDebug", false)) {
                viewHolder.groupId.setVisibility(View.VISIBLE);
//                viewHolder.groupId.setText(mContent.getGroupsId());
            }
            return convertView;
        }


        class ViewHolder {
            /**
             * 昵称
             */
            TextView tvTitle;
            /**
             * 头像
             */
            SelectableRoundedImageView mImageView;
            /**
             * userId
             */
            TextView groupId;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BroadcastManager.getInstance(mContext).destroy(SealConst.GROUP_LIST_UPDATE);
    }


}
