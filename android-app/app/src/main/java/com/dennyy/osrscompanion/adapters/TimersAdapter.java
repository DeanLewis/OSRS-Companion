package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.interfaces.AdapterTimerClickListener;
import com.dennyy.osrscompanion.models.Timers.Timer;

import java.util.ArrayList;

public class TimersAdapter extends BaseAdapter {
    private ArrayList<Timer> timers;
    private Context context;
    private LayoutInflater inflater;
    private AdapterTimerClickListener listener;

    public TimersAdapter(Context context, ArrayList<Timer> timers, AdapterTimerClickListener listener) {
        this.context = context;
        this.timers = timers;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public void updateList(ArrayList<Timer> newtimers) {
        timers.clear();
        timers.trimToSize();
        timers.addAll(newtimers);
        this.notifyDataSetChanged();
    }

    public Timer getById(int id) {
        for (Timer timer : timers) {
            if (timer.id == id) {
                return timer;
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return timers.size();
    }

    @Override
    public Timer getItem(int i) {
        return timers.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.timer_row, null);
            viewHolder = new ViewHolder();
            viewHolder.title = convertView.findViewById(R.id.timer_listview_title);
            viewHolder.description = convertView.findViewById(R.id.timer_listview_description);
            viewHolder.duration = convertView.findViewById(R.id.timer_listview_duration);
            viewHolder.deleteButton = convertView.findViewById(R.id.timer_delete);
            viewHolder.editButton = convertView.findViewById(R.id.timer_edit);
            viewHolder.startButton = convertView.findViewById(R.id.timer_startstop);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final Timer timer = timers.get(i);
        viewHolder.title.setText(timer.title);
        viewHolder.description.setText(timer.description);
        viewHolder.duration.setText(context.getString((timer.repeat ? R.string.timer_repeated : R.string.timer_once), formatSeconds(timer.interval)));
        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onDeleteClick(timer.id);
            }
        });
        viewHolder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onEditClick(timer.id);
            }
        });
        viewHolder.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onStartClick(timer.id);
            }
        });
        return convertView;
    }

    private String formatSeconds(int timeInSeconds) {
        int hours = timeInSeconds / 3600;
        int secondsLeft = timeInSeconds - hours * 3600;
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;

        String formattedTime = "";
        if (hours < 10)
            formattedTime += "0";
        formattedTime += hours + ":";

        if (minutes < 10)
            formattedTime += "0";
        formattedTime += minutes + ":";

        if (seconds < 10)
            formattedTime += "0";
        formattedTime += seconds;

        return formattedTime;
    }

    private static class ViewHolder {
        public TextView title;
        public TextView description;
        public TextView duration;
        public Button deleteButton;
        public Button editButton;
        public Button startButton;
    }
}
