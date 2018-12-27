package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.dennyy.osrscompanion.R;

import java.util.ArrayList;

public class NpcVersionsAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> versions;

    public NpcVersionsAdapter(Context context, ArrayList<String> versions) {
        this.context = context;
        this.versions = new ArrayList<>(versions);
    }

    @Override
    public int getCount() {
        return  versions.size();
    }

    @Override
    public String getItem(int position) {
        return versions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.adapter_row, null);
            viewHolder = new ViewHolder();
            viewHolder.version = convertView.findViewById(R.id.adapter_row_text);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String version = versions.get(i);
        viewHolder.version.setText(version);
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.adapter_row_dropdown, null);
            viewHolder = new ViewHolder();
            viewHolder.version = convertView.findViewById(R.id.adapter_row_text);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String version = versions.get(position);

        viewHolder.version.setText(version);

        return convertView;
    }

    public void updateList(ArrayList<String> drops) {
        this.versions.clear();
        this.versions.trimToSize();
        this.versions.addAll(drops);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        public TextView version;
    }
}
