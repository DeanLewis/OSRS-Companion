package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.RsUtils;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.Bestiary.NpcDrop;
import com.dennyy.osrscompanion.models.SkillCalculator.SkillDataAction;
import com.dennyy.osrscompanion.models.SkillCalculator.SkillDataBonus;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class NpcDropsAdapter extends ArrayAdapter<NpcDrop> {
    private Context context;
    private ArrayList<NpcDrop> drops;

    public NpcDropsAdapter(Context context, ArrayList<NpcDrop> drops) {
        super(context, 0, drops);
        this.context = context;
        this.drops = new ArrayList<>(drops);
    }

    @Override
    public int getCount() {
       return this.drops.size();
    }

    @Override
    public NpcDrop getItem(int position) {
        return this.drops.get(position);
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.npc_drop_row, null);
            viewHolder = new ViewHolder();
            viewHolder.drop = convertView.findViewById(R.id.npc_drop_name);
            viewHolder.quantity = convertView.findViewById(R.id.npc_drop_quantity);
            viewHolder.rarity = convertView.findViewById(R.id.npc_drop_rarity);
            viewHolder.notes = convertView.findViewById(R.id.npc_drop_note);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        NpcDrop drop = drops.get(i);

        viewHolder.drop.setText(drop.name);
        viewHolder.quantity.setText(drop.quantity);
        int color = R.color.npc_drop_very_rare;
        switch (drop.rarity){
            case ALWAYS:
                color = R.color.npc_drop_always;
                break;
            case COMMON:
                color = R.color.npc_drop_common;
                break;
            case UNCOMMON:
                color = R.color.npc_drop_uncommon;
                break;
            case RARE:
                color = R.color.npc_drop_rare;
                break;
            case VERY_RARE:
                color = R.color.npc_drop_very_rare;
                break;
        }
        viewHolder.rarity.setTextColor(context.getResources().getColor(color));
        viewHolder.rarity.setText(drop.rarity.getValue());
        viewHolder.notes.setText(drop.rarityNotes);
        if (i % 2 == 0)
            convertView.setBackgroundColor(context.getResources().getColor(R.color.alternate_row_color));
        else
            convertView.setBackgroundColor(context.getResources().getColor(R.color.input_background_color));

        return convertView;
    }


    public void updateList(ArrayList<NpcDrop> drops) {
        this.drops.clear();
        this.drops.trimToSize();
        this.drops.addAll(drops);
        notifyDataSetChanged();
    }


    private static class ViewHolder {
        public TextView drop;
        public TextView quantity;
        public TextView rarity;
        public TextView notes;
    }
}
