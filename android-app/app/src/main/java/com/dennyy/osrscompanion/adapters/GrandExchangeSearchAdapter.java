package com.dennyy.osrscompanion.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.GrandExchange.JsonItem;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GrandExchangeSearchAdapter extends ArrayAdapter<JsonItem> implements Filterable, View.OnTouchListener {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<JsonItem> grandExchangeItems;
    private ArrayList<JsonItem> originalGrandExchangeItems;
    private ItemFilter mFilter = new ItemFilter();

    public GrandExchangeSearchAdapter(Context context, Collection<JsonItem> grandExchangeItems) {
        super(context, 0, new ArrayList<>(grandExchangeItems));
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.grandExchangeItems = new ArrayList<>(grandExchangeItems);
        this.originalGrandExchangeItems = new ArrayList<>(grandExchangeItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.ge_search_row, null);

            viewHolder = new ViewHolder();
            viewHolder.icon = convertView.findViewById(R.id.ge_search_item_img);
            viewHolder.name = convertView.findViewById(R.id.ge_search_item_name);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        convertView.setOnTouchListener(this);
        JsonItem search = grandExchangeItems.get(position);
        viewHolder.name.setText(search.name);
        Glide.with(context).load(Constants.GE_IMG_SMALL_URL + search.id).into(viewHolder.icon);
        return convertView;
    }

    @Override
    public JsonItem getItem(int position) {
        return grandExchangeItems.get(position);
    }

    @Override
    public int getCount() {
        return grandExchangeItems != null ? grandExchangeItems.size() : 0;
    }

    @Override
    public void notifyDataSetChanged() {
        if (grandExchangeItems.size() < 1) {
            JsonItem item = new JsonItem();
            item.id = "-1";
            item.name = context.getString(R.string.ge_item_not_found);
            grandExchangeItems.add(item);
        }
        super.notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            Utils.hideKeyboard(context, view);
        }
        return false;
    }

    private static class ViewHolder {
        public TextView name;
        public ImageView icon;
    }

    // https://gist.github.com/fjfish/3024308
    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = grandExchangeItems;
                results.count = grandExchangeItems.size();
            }
            else {
                final List<JsonItem> nlist = new ArrayList<>();
                final List<JsonItem> fuzzyList = new ArrayList<>();
                for (JsonItem grandExchangeItem : originalGrandExchangeItems) {
                    if (grandExchangeItem.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        nlist.add(grandExchangeItem);
                    }
                    else if (FuzzySearch.partialRatio(grandExchangeItem.name.toLowerCase(), constraint.toString().toLowerCase()) >= Constants.FUZZY_RATIO) {
                        fuzzyList.add(grandExchangeItem);
                    }
                }
                nlist.addAll(fuzzyList);

                results.values = nlist;
                results.count = nlist.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            grandExchangeItems = (ArrayList<JsonItem>) results.values;
            notifyDataSetChanged();
        }
    }
}
