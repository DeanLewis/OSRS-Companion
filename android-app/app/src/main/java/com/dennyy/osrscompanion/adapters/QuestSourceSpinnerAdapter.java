package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.enums.QuestSource;

import java.util.ArrayList;

public class QuestSourceSpinnerAdapter extends BaseAdapter {
    private ArrayList<QuestSource> questSources;
    private Context context;

    public QuestSourceSpinnerAdapter(Context context, ArrayList<QuestSource> questSources) {
        this.context = context;
        this.questSources = questSources;
    }

    @Override
    public int getCount() {
        return questSources.size();
    }

    @Override
    public QuestSource getItem(int i) {
        return questSources.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.adapter_row, null);
            viewHolder = new ViewHolder();
            viewHolder.questName = convertView.findViewById(R.id.adapter_row_text);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        QuestSource questSource = questSources.get(i);
        viewHolder.questName.setText(questSource.getName());
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.adapter_row_dropdown, null);
            viewHolder = new ViewHolder();
            viewHolder.questName = convertView.findViewById(R.id.adapter_row_text);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        QuestSource questSource = questSources.get(position);
        viewHolder.questName.setText(questSource.getName());
        return convertView;
    }

    private static class ViewHolder {
        public TextView questName;
    }
}
