package cn.rongcloud.im.message.plugins;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;

import cn.rongcloud.im.ui.activity.PreGooglMapsLocationActivity;
import io.rong.common.RLog;
import io.rong.imkit.R.drawable;
import io.rong.imkit.R.id;
import io.rong.imkit.R.layout;
import io.rong.imkit.R.string;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.plugin.location.AMapPreviewActivity;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.model.Message.MessageDirection;
import io.rong.message.LocationMessage;

@ProviderTag(
    messageContent = LocationMessage.class,
    showReadState = true
)
public class LocationMessageItemProvider extends MessageProvider<LocationMessage> {
    private static final String TAG = "LocationMessageItemProvider";

    public LocationMessageItemProvider() {
    }

    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(layout.rc_item_location_message, (ViewGroup)null);
        LocationMessageItemProvider.ViewHolder holder = new LocationMessageItemProvider.ViewHolder();
        holder.img = (AsyncImageView)view.findViewById(id.rc_img);
        holder.title = (TextView)view.findViewById(id.rc_content);
        holder.mLayout = (FrameLayout)view.findViewById(id.rc_layout);
        view.setTag(holder);
        return view;
    }

    public void onItemClick(View view, int position, LocationMessage content, UIMessage message) {
        try {


            LocationMessage locationMessage = (LocationMessage)message.getContent();
            double lat = locationMessage.getLat();
            double lng = locationMessage.getLng();
            String poi = locationMessage.getPoi();
            if (lat==0.0D && lng ==0.0d){
                return;
            }
            if (isInArea(lat,lng)) {
                Intent  intent = new Intent(view.getContext(), AMapPreviewActivity.class);
                intent.putExtra("location", message.getContent());
                view.getContext().startActivity(intent);
            }else{
                Intent intent = new Intent(view.getContext(), PreGooglMapsLocationActivity.class);
                intent.putExtra("location", message.getContent());
                view.getContext().startActivity(intent);
            }
        } catch (Exception var8) {
            RLog.i("LocationMessageItemProvider", "Not default AMap Location");
            var8.printStackTrace();
        }

    }



    /**
     * 粗略判断当前屏幕显示的地图中心点是否在国内
     *
     * @param latitude   纬度
     * @param longtitude 经度
     * @return 屏幕中心点是否在国内
     */
    private boolean isInArea(double latitude, double longtitude) {
        if ((latitude > 3.837031) && (latitude < 53.563624)
                && (longtitude < 135.095670) && (longtitude > 73.502355)) {
            return true;
        }
        return false;
    }

    public void bindView(View v, int position, LocationMessage content, UIMessage uiMsg) {
        LocationMessageItemProvider.ViewHolder holder = (LocationMessageItemProvider.ViewHolder)v.getTag();
        Uri uri = content.getImgUri();
        RLog.d("LocationMessageItemProvider", "uri = " + uri);
        if (uri != null && uri.getScheme().equals("file")) {
            holder.img.setResource(uri);
        } else {
            holder.img.setDefaultDrawable();
        }

        LayoutParams params = new LayoutParams(holder.title.getLayoutParams().width, -2);
        params.gravity = 80;
        if (uiMsg.getMessageDirection() == MessageDirection.SEND) {
            holder.mLayout.setBackgroundResource(drawable.rc_ic_bubble_no_right);
            params.leftMargin = 0;
            params.rightMargin = (int)(3.5D * (double)v.getResources().getDisplayMetrics().density);
            holder.title.setLayoutParams(params);
        } else {
            params.leftMargin = (int)(4.5D * (double)v.getResources().getDisplayMetrics().density);
            params.rightMargin = 0;
            holder.title.setLayoutParams(params);
            holder.mLayout.setBackgroundResource(drawable.rc_ic_bubble_no_left);
        }

        holder.title.setText(content.getPoi());
    }

    public Spannable getContentSummary(LocationMessage data) {
        return null;
    }

    public Spannable getContentSummary(Context context, LocationMessage data) {
        String text = context.getResources().getString(string.rc_message_content_location);
        return new SpannableString(text);
    }

    private static class ViewHolder {
        AsyncImageView img;
        TextView title;
        FrameLayout mLayout;

        private ViewHolder() {
        }
    }
}