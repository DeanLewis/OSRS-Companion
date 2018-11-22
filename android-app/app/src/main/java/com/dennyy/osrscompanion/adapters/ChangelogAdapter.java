package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.enums.ChangelogType;
import com.dennyy.osrscompanion.models.Changelog.Changelog;
import com.dennyy.osrscompanion.models.Changelog.ChangelogEntry;
import com.dennyy.osrscompanion.models.Changelog.Changelogs;

public class ChangelogAdapter extends BaseAdapter {
    private Context context;
    private Changelogs changelogs;

    public ChangelogAdapter(Context context, Changelogs changelogs) {
        this.context = context;
        this.changelogs = changelogs;
    }

    @Override
    public int getCount() {
        return changelogs.size();
    }

    @Override
    public Changelog getItem(int i) {
        return changelogs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.changelog_row, null);
            viewHolder = new ViewHolder();
            viewHolder.header = convertView.findViewById(R.id.changelog_header);
            viewHolder.logs = convertView.findViewById(R.id.changelog_text);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Changelog changelog = changelogs.get(index);

        viewHolder.header.setText(context.getResources().getString(R.string.version_string, changelog.versionName));
        SpannableStringBuilder builder = new SpannableStringBuilder();

        for (int i = 0; i < changelog.entriesSize; i++) {
            ChangelogEntry entry = changelog.entries.get(i);
            String type = "";
            if (entry.changelogType == ChangelogType.BUG_FIX) {
                type = context.getResources().getString(R.string.changelog_bugfix) + ": ";
            }
            else if (entry.changelogType == ChangelogType.NEW) {
                type = context.getResources().getString(R.string.changelog_new) + ": ";
            }

            String text = type + entry.text;
            SpannableString spannableString = new SpannableString(text);
            spannableString.setSpan(new BulletSpan(30), 0, text.length(), 0);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, type.length(), 0);
            builder.append(spannableString);
            if (i < changelog.entriesSize - 1) {
                builder.append("\n\n");
            }
        }
        viewHolder.logs.setText(builder);
        return convertView;
    }

    private static class ViewHolder {
        public TextView header;
        public TextView logs;
    }
}
