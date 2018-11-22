package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.RsUtils;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.SkillCalculator.SkillDataAction;
import com.dennyy.osrscompanion.models.SkillCalculator.SkillDataBonus;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ActionsAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<SkillDataAction> skillDataActions;
    private int expDifference;
    private int currentLvl;
    private int targetLvl;
    private SkillDataBonus skillDataBonus;
    private DecimalFormat decimalFormat;

    public ActionsAdapter(Context context, ArrayList<SkillDataAction> skillDataActions) {
        this.context = context;
        this.skillDataActions = skillDataActions;
        this.expDifference = -1;
        this.currentLvl = -1;
        this.targetLvl = -1;
        this.decimalFormat = new DecimalFormat("#.#");
        this.decimalFormat.setRoundingMode(RoundingMode.FLOOR);
    }

    @Override
    public int getCount() {
        return skillDataActions.size();
    }

    @Override
    public SkillDataAction getItem(int i) {
        return skillDataActions.get(i);
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
            convertView = layoutInflater.inflate(R.layout.action_list_row, null);
            viewHolder = new ViewHolder();
            viewHolder.lvl = convertView.findViewById(R.id.action_list_lvl);
            viewHolder.action = convertView.findViewById(R.id.action_list_action);
            viewHolder.exp = convertView.findViewById(R.id.action_list_exp);
            viewHolder.amount = convertView.findViewById(R.id.action_list_amount);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        SkillDataAction skillDataAction = skillDataActions.get(i);
        viewHolder.lvl.setText(String.valueOf(skillDataAction.level));
        if (targetLvl > 0 || currentLvl > 0) {
            if (skillDataAction.level <= currentLvl) {
                viewHolder.lvl.setTextColor(context.getResources().getColor(R.color.green));
            }
            else if (skillDataAction.level <= targetLvl) {
                viewHolder.lvl.setTextColor(context.getResources().getColor(R.color.orange));
            }
            else if (skillDataAction.level > currentLvl) {
                viewHolder.lvl.setTextColor(context.getResources().getColor(R.color.red));
            }
        }

        double exp = skillDataAction.exp + (!skillDataAction.ignoreBonus && skillDataBonus != null ? skillDataBonus.value * skillDataAction.exp : 0);
        viewHolder.action.setText(skillDataAction.name);
        viewHolder.exp.setText(decimalFormat.format(exp));
        viewHolder.amount.setText(expDifference == -1 ? "N/A" : String.valueOf(Utils.formatNumber((int) Math.ceil(expDifference / exp))));
        if (skillDataBonus != null && skillDataAction.ignoreBonus) {
            convertView.setAlpha(0.3f);
        }
        else {
            convertView.setAlpha(1.0f);
        }
        return convertView;
    }

    public void updateList(ArrayList<SkillDataAction> skillDataActions) {
        this.skillDataActions.clear();
        this.skillDataActions.addAll(skillDataActions);
        notifyDataSetChanged();
    }

    public void updateListFromExp(int currentExp, int targetExp) {
        expDifference = targetExp - currentExp;
        this.currentLvl = RsUtils.lvl(currentExp, true);
        this.targetLvl = RsUtils.lvl(targetExp, true);
        notifyDataSetChanged();
    }

    public void updateBonus(SkillDataBonus bonus) {
        skillDataBonus = bonus;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        public TextView lvl;
        public TextView action;
        public TextView exp;
        public TextView amount;
    }
}
