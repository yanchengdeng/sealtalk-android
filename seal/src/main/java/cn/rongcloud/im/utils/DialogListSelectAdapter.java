package cn.rongcloud.im.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dbcapp.club.R;

import java.util.List;

public class DialogListSelectAdapter extends BaseAdapter {
    private Context context;

    private LayoutInflater inflater;
    private List<String> list;

    public DialogListSelectAdapter(Context context, List<String> list) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list == null ? 0 : list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.dialog_listview_item, parent, false);
            holder = new ViewHolder();
            holder.bottomLine = convertView.findViewById(R.id.bottomLine);
            holder.typeName = (TextView) convertView.findViewById(R.id.typeName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == getCount() - 1) {
            holder.bottomLine.setVisibility(View.GONE);
        } else {
            holder.bottomLine.setVisibility(View.VISIBLE);
        }

        String model = list.get(position);
        holder.typeName.setText(model);
        return convertView;
    }

    private static class ViewHolder {
        View bottomLine;
        TextView typeName;

    }
}
