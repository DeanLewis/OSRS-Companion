package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.enums.SkillType;
import com.dennyy.osrscompanion.models.SkillCalculator.SkillDataBonus;

import java.util.ArrayList;

public class SkillBonusSpinnerAdapter extends BaseAdapter {
    private ArrayList<SkillDataBonus> bonuses;
    private Context context;

    public SkillBonusSpinnerAdapter(Context context, ArrayList<SkillDataBonus> bonuses) {
        this.context = context;
        this.bonuses = new ArrayList<>(bonuses);
        this.bonuses.add(0, new SkillDataBonus(context.getString(R.string.no_bonus), 0));
    }

    @Override
    public int getCount() {
        return bonuses.size();
    }

    @Override
    public SkillDataBonus getItem(int position) {
        return bonuses.get(position);
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
            convertView = layoutInflater.inflate(R.layout.skill_bonus_selector_row, null);
            viewHolder = new ViewHolder();
            viewHolder.bonusName = convertView.findViewById(R.id.bonus_name);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        SkillDataBonus bonus = bonuses.get(i);
        viewHolder.bonusName.setText(bonus.name);
        return convertView;
    }

    private static class ViewHolder {
        public TextView bonusName;
    }
}
