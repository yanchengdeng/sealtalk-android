package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dbcapp.club.R;

import cn.rongcloud.im.App;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.server.response.GetRelationFriendResponse;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imlib.model.UserInfo;

@SuppressWarnings("deprecation")
public class NewFriendListAdapter extends BaseAdapters {

    public NewFriendListAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.rs_ada_user_ship, parent, false);
            holder.mName = (TextView) convertView.findViewById(R.id.ship_name);
            holder.mMessage = (TextView) convertView.findViewById(R.id.ship_message);
            holder.mHead = (SelectableRoundedImageView) convertView.findViewById(R.id.new_header);
            holder.mState = (TextView) convertView.findViewById(R.id.ship_state);
            holder.mRefuseState = convertView.findViewById(R.id.ship_state_refuse);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final GetRelationFriendResponse.ResultEntity bean = (GetRelationFriendResponse.ResultEntity) dataSet.get(position);
        holder.mName.setText(bean.getUserName());
        String portraitUri = null;
        if (bean != null) {
            portraitUri = SealUserInfoManager.getInstance().getPortraitUri(new UserInfo(
                              String.valueOf(bean.getId()), bean.getUserName(), Uri.parse("")));
        }
        ImageLoader.getInstance().displayImage(portraitUri, holder.mHead, App.getOptions());
//        holder.mMessage.setText(bean.getMessage());
        holder.mState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mOnItemButtonClick != null) {
                    mOnItemButtonClick.onButtonClick(position, v, bean.getStatus());
                }
            }
        });

        holder.mRefuseState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemButtonRefuseClick != null) {
                    onItemButtonRefuseClick.onButtonRefuseClick(position, v, bean.getStatus());
                }
            }
        });

        return convertView;
    }

    /**
     * displayName :
     * message : 手机号:18622222222昵称:的用户请求添加你为好友
     * status : 11
     * updatedAt : 2016-01-07T06:22:55.000Z
     * user : {"id":"i3gRfA1ml","nickname":"nihaoa","portraitUri":""}
     */

    class ViewHolder {
        SelectableRoundedImageView mHead;
        TextView mName;
        TextView mState,mRefuseState;
        //        TextView mtime;
        TextView mMessage;
    }

    OnItemButtonClick mOnItemButtonClick;

    OnItemButtonRefuseClick onItemButtonRefuseClick;


    public void setOnItemButtonClick(OnItemButtonClick onItemButtonClick) {
        this.mOnItemButtonClick = onItemButtonClick;
    }


    public void setOnItemButtonRefuseClick(OnItemButtonRefuseClick onItemButtonClick) {
        this.onItemButtonRefuseClick = onItemButtonClick;
    }

    public interface OnItemButtonClick {
        boolean onButtonClick(int position, View view, int status);

    }


    public interface OnItemButtonRefuseClick {
        boolean onButtonRefuseClick(int position, View view, int status);

    }
}
