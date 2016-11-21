package cn.rongcloud.im.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.pinyin.PinyinComparator;
import cn.rongcloud.im.server.pinyin.SideBar;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.activity.GroupListActivity;
import cn.rongcloud.im.ui.activity.NewFriendListActivity;
import cn.rongcloud.im.ui.activity.PublicServiceActivity;
import cn.rongcloud.im.ui.activity.UserDetailActivity;
import cn.rongcloud.im.ui.adapter.FriendListAdapter;
import io.rong.imkit.RongIM;

/**
 * tab 2 通讯录的 Fragment
 * Created by Bob on 2015/1/25.
 */
public class ContactsFragment extends Fragment implements View.OnClickListener {

    private SelectableRoundedImageView mSelectableRoundedImageView;
    private TextView mNameTextView;
    private TextView mNoFriends;
    private TextView mUnreadTextView;
    private View mHeadView;
    private EditText mSearchEditText;
    private ListView mListView;
    private PinyinComparator mPinyinComparator;
    private SideBar mSidBar;
    /**
     * 中部展示的字母提示
     */
    private TextView mDialogTextView;

    private List<Friend> mSourceFriendList;
    private List<Friend> mFriendList;
    private List<Friend> mFilteredFriendList;
    /**
     * 好友列表的 mFriendListAdapter
     */
    private FriendListAdapter mFriendListAdapter;
    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser mCharacterParser;
    /**
     * 根据拼音来排列ListView里面的数据类
     */

    private String mId;
    private String mCacheName;

    private static final int CLICK_CONTACT_FRAGMENT_FRIEND = 2;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_address, container, false);
        initView(view);
        initData();
        updateUI();
        refreshUIListener();
        return view;
    }

    private void startFriendDetailsPage(Friend friend) {
        Intent intent = new Intent(getActivity(), UserDetailActivity.class);
        intent.putExtra("type", CLICK_CONTACT_FRAGMENT_FRIEND);
        intent.putExtra("friend", friend);
        startActivity(intent);
    }

    private void initView(View view) {
        mSearchEditText = (EditText) view.findViewById(R.id.search);
        mListView = (ListView) view.findViewById(R.id.listview);
        mNoFriends = (TextView) view.findViewById(R.id.show_no_friend);
        mSidBar = (SideBar) view.findViewById(R.id.sidrbar);
        mDialogTextView = (TextView) view.findViewById(R.id.group_dialog);
        mSidBar.setTextView(mDialogTextView);
        LayoutInflater mLayoutInflater = LayoutInflater.from(getActivity());
        mHeadView = mLayoutInflater.inflate(R.layout.item_contact_list_header,
                                            null);
        mUnreadTextView = (TextView) mHeadView.findViewById(R.id.tv_unread);
        RelativeLayout newFriendsLayout = (RelativeLayout) mHeadView.findViewById(R.id.re_newfriends);
        RelativeLayout groupLayout = (RelativeLayout) mHeadView.findViewById(R.id.re_chatroom);
        RelativeLayout publicServiceLayout = (RelativeLayout) mHeadView.findViewById(R.id.publicservice);
        RelativeLayout selfLayout = (RelativeLayout) mHeadView.findViewById(R.id.contact_me_item);
        mSelectableRoundedImageView = (SelectableRoundedImageView) mHeadView.findViewById(R.id.contact_me_img);
        mNameTextView = (TextView) mHeadView.findViewById(R.id.contact_me_name);
        SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        mId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "");
        mCacheName = sp.getString(SealConst.SEALTALK_LOGIN_NAME, "");
        final String header = sp.getString(SealConst.SEALTALK_LOGING_PORTRAIT, "");
        mNameTextView.setText(mCacheName);
        ImageLoader.getInstance().displayImage(TextUtils.isEmpty(header) ? RongGenerate.generateDefaultAvatar(mCacheName, mId) : header, mSelectableRoundedImageView, App.getOptions());
        mListView.addHeaderView(mHeadView);
        mNoFriends.setVisibility(View.VISIBLE);

        selfLayout.setOnClickListener(this);
        groupLayout.setOnClickListener(this);
        newFriendsLayout.setOnClickListener(this);
        publicServiceLayout.setOnClickListener(this);
        //设置右侧触摸监听
        mSidBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = mFriendListAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }

            }
        });
    }

    private void initData() {
        mSourceFriendList = new ArrayList<>();
        mFriendList = new ArrayList<>();
        FriendListAdapter adapter = new FriendListAdapter(getActivity(), mFriendList);
        mListView.setAdapter(adapter);
        mFilteredFriendList = new ArrayList<>();
        //实例化汉字转拼音类
        mCharacterParser = CharacterParser.getInstance();
        mPinyinComparator = PinyinComparator.getInstance();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mDialogTextView != null) {
            mDialogTextView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr 需要过滤的 String
     */
    private void filterData(String filterStr) {
        List<Friend> filterDateList = new ArrayList<>();

        try {
            if (TextUtils.isEmpty(filterStr)) {
                filterDateList = mFriendList;
            } else {
                filterDateList.clear();
                for (Friend friendModel : mFriendList) {
                    String name = friendModel.getName();
                    String displayName = friendModel.getDisplayName();
                    if (!TextUtils.isEmpty(displayName)) {
                        if (name.contains(filterStr) || mCharacterParser.getSpelling(name).startsWith(filterStr) || displayName.contains(filterStr) || mCharacterParser.getSpelling(displayName).startsWith(filterStr)) {
                            filterDateList.add(friendModel);
                        }
                    } else {
                        if (name.contains(filterStr) || mCharacterParser.getSpelling(name).startsWith(filterStr)) {
                            filterDateList.add(friendModel);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, mPinyinComparator);
        mFilteredFriendList = filterDateList;
        mFriendListAdapter.updateListView(filterDateList);
    }


    /**
     * 为ListView填充数据
     */
    private List<Friend> labelSourceFriendList(List<Friend> list) {
        List<Friend> mFriendList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Friend friendModel = new Friend();
            friendModel.setName(list.get(i).getName());
            //汉字转换成拼音
            String pinyin;
            if (!TextUtils.isEmpty(list.get(i).getDisplayName())) {
                pinyin = mCharacterParser.getSpelling(list.get(i).getDisplayName());
            } else {
                pinyin = mCharacterParser.getSpelling(list.get(i).getName());
            }
            if (pinyin != null && pinyin.length() > 0) {
                String sortString = pinyin.substring(0, 1).toUpperCase();
                // 正则表达式，判断首字母是否是英文字母
                if (sortString.matches("[A-Z]")) {
                    friendModel.setLetters(sortString.toUpperCase());
                } else {
                    friendModel.setLetters("#");
                }
            } else {
                friendModel.setLetters("#");
            }

            mFriendList.add(friendModel);
        }
        return mFriendList;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.re_newfriends:
                mUnreadTextView.setVisibility(View.GONE);
                Intent intent = new Intent(getActivity(), NewFriendListActivity.class);
                startActivityForResult(intent, 20);
                break;
            case R.id.re_chatroom:
                startActivity(new Intent(getActivity(), GroupListActivity.class));
                break;
            case R.id.publicservice:
                Intent intentPublic = new Intent(getActivity(), PublicServiceActivity.class);
                startActivity(intentPublic);
                break;
            case R.id.contact_me_item:
                RongIM.getInstance().startPrivateChat(getActivity(), mId, mCacheName);
                break;
        }
    }


    private void refreshUIListener() {
        BroadcastManager.getInstance(getActivity()).addAction(SealAppContext.UPDATE_FRIEND, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getAction();
                if (!TextUtils.isEmpty(command)) {
                    updateUI();
                }
            }
        });

        BroadcastManager.getInstance(getActivity()).addAction(SealAppContext.UPDATE_RED_DOT, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getAction();
                if (!TextUtils.isEmpty(command)) {
                    mUnreadTextView.setVisibility(View.INVISIBLE);
                }
            }
        });
        BroadcastManager.getInstance(getActivity()).addAction(SealConst.CHANGEINFO, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
                mId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "");
                mCacheName = sp.getString(SealConst.SEALTALK_LOGIN_NAME, "");
                String header = sp.getString(SealConst.SEALTALK_LOGING_PORTRAIT, "");
                mNameTextView.setText(mCacheName);
                ImageLoader.getInstance().displayImage(TextUtils.isEmpty(header) ? RongGenerate.generateDefaultAvatar(mCacheName, mId) : header, mSelectableRoundedImageView, App.getOptions());
            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            BroadcastManager.getInstance(getActivity()).destroy(SealAppContext.UPDATE_FRIEND);
            BroadcastManager.getInstance(getActivity()).destroy(SealAppContext.UPDATE_RED_DOT);
            BroadcastManager.getInstance(getActivity()).destroy(SealConst.CHANGEINFO);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void updateUI() {
        SealUserInfoManager.getInstance().getFriends(new SealUserInfoManager.ResultCallback<List<Friend>>() {
            @Override
            public void onSuccess(List<Friend> friendsList) {
                updateFriendsList(friendsList);
            }

            @Override
            public void onError(String errString) {
                updateFriendsList(null);
            }
        });
    }

    private void updateFriendsList(List<Friend> friendsList) {
        //updateUI fragment初始化和好友信息更新时都会调用,isReloadList表示是否是好友更新时调用
        // 这种实现方式可能不是最佳,以后可以重构 //// TODO: 16/9/19
        boolean isReloadList = false;
        if (mSourceFriendList != null && mSourceFriendList.size() > 0) {
            mSourceFriendList.clear();
            isReloadList = true;
        }
        if (mFriendList != null && mFriendList.size() > 0) {
            mFriendList.clear();
        }
        if (friendsList != null && friendsList.size() > 0) {
            for (Friend friend : friendsList) {
                mSourceFriendList.add(new Friend(friend.getUserId(), friend.getName(), friend.getPortraitUri(), friend.getPhoneNumber(), friend.getDisplayName()));
            }
        }
        if (mSourceFriendList != null && mSourceFriendList.size() > 0) {
            mFriendList = labelSourceFriendList(mSourceFriendList); //过滤数据为有字母的字段  现在有字母 别的数据没有
            mNoFriends.setVisibility(View.GONE);
        } else {
            mNoFriends.setVisibility(View.VISIBLE);
        }

        //还原除了带字母字段的其他数据
        if (mSourceFriendList != null) {
            for (int i = 0; i < mSourceFriendList.size(); i++) {
                mFriendList.get(i).setName(mSourceFriendList.get(i).getName());
                mFriendList.get(i).setUserId(mSourceFriendList.get(i).getUserId());
                mFriendList.get(i).setPortraitUri(mSourceFriendList.get(i).getPortraitUri());
                mFriendList.get(i).setPhoneNumber(mSourceFriendList.get(i).getPhoneNumber());
                mFriendList.get(i).setDisplayName(mSourceFriendList.get(i).getDisplayName());
            }
        }

        // 根据a-z进行排序源数据
        Collections.sort(mFriendList, mPinyinComparator);
        if (isReloadList) {
            mSidBar.setVisibility(View.VISIBLE);
            mFriendListAdapter.updateListView(mFriendList);
        } else {
            mSidBar.setVisibility(View.VISIBLE);
            mFriendListAdapter = new FriendListAdapter(getActivity(), mFriendList);

            mListView.setAdapter(mFriendListAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mListView.getHeaderViewsCount() > 0) {
                        startFriendDetailsPage(mFriendList.get(position - 1));
                    } else {
                        startFriendDetailsPage(mFilteredFriendList.get(position));
                    }
                }
            });


            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    Friend bean = mFriendList.get(position - 1);
                    startFriendDetailsPage(bean);
                    return true;
                }
            });
            mSearchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                    filterData(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() != 0) {
                        if (mListView.getHeaderViewsCount() > 0) {
                            mListView.removeHeaderView(mHeadView);
                        }
                    } else {
                        if (mListView.getHeaderViewsCount() == 0) {
                            mListView.addHeaderView(mHeadView);
                        }
                    }
                }
            });
        }
    }
}
