package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import cn.rongcloud.im.ui.adapter.SubConversationListAdapterDYC;
import io.rong.imkit.R.id;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imlib.model.Conversation.ConversationType;

public class SubConversationListFragmentNew extends ConversationListFragment {
    private static final String TAG = "SubConversationListFragmentNew";
    private ListView mList;
    private SubConversationListAdapterDYC mAdapter;

    public SubConversationListFragmentNew() {
    }

    public void setAdapter(SubConversationListAdapterDYC adapter) {
        this.mAdapter = adapter;
        if (mAdapter!=null) {
            mAdapter.setOnPortraitItemClick(null);
        }
    }

    public ConversationListAdapter onResolveAdapter(Context context) {
        if (this.mAdapter == null) {
            this.mAdapter = new SubConversationListAdapterDYC(context);

        }

        mAdapter.setOnPortraitItemClick(null);
        return mAdapter;
    }

    public boolean getGatherState(ConversationType conversationType) {
        return false;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        this.mList = (ListView)this.findViewById(view, id.rc_list);
        this.mList.setAdapter(mAdapter);
        return view;
    }
}