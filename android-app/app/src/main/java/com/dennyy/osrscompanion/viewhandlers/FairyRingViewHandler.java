package com.dennyy.osrscompanion.viewhandlers;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.FairyRingListAdapter;
import com.dennyy.osrscompanion.adapters.FairyRingSearchAdapter;
import com.dennyy.osrscompanion.asynctasks.GetFairyRingsTask;
import com.dennyy.osrscompanion.customviews.ClearableAutoCompleteTextView;
import com.dennyy.osrscompanion.customviews.DelayedAutoCompleteTextView;
import com.dennyy.osrscompanion.customviews.ObservableListView;
import com.dennyy.osrscompanion.enums.ScrollState;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.interfaces.FairyRingsLoadedListener;
import com.dennyy.osrscompanion.interfaces.ObservableScrollViewCallbacks;
import com.dennyy.osrscompanion.models.FairyRings.FairyRing;

import java.util.ArrayList;

public class FairyRingViewHandler extends BaseViewHandler implements TextWatcher, ObservableScrollViewCallbacks {
    public int selectedIndex;

    private ClearableAutoCompleteTextView clearableAutoCompleteTextView;
    private DelayedAutoCompleteTextView autoCompleteTextView;
    private ObservableListView listView;
    private FairyRingSearchAdapter searchAdapter;
    private FairyRingListAdapter listViewAdapter;
    private ArrayList<FairyRing> fairyRings;

    public FairyRingViewHandler(Context context, final View view) {
        super(context, view);
        new GetFairyRingsTask(context, new FairyRingsLoadedListener() {
            @Override
            public void onFairyRingsLoaded(ArrayList<FairyRing> items) {
                fairyRings = new ArrayList<>(items);
                updateView(view);
            }

            @Override
            public void onFairyRingsLoadError() {
                showToast(resources.getString(R.string.exception_occurred, "exception", "loading items from file"), Toast.LENGTH_LONG);
            }
        }).execute();
    }

    private void updateView(View view) {
        clearableAutoCompleteTextView = view.findViewById(R.id.fr_search_input);
        listView = view.findViewById(R.id.fairy_rings_listview);
        listView.addScrollViewCallbacks(this);
        autoCompleteTextView = clearableAutoCompleteTextView.getAutoCompleteTextView();

        if (searchAdapter == null) {
            searchAdapter = new FairyRingSearchAdapter(context, fairyRings);
        }
        if (listViewAdapter == null) {
            listViewAdapter = new FairyRingListAdapter(context, fairyRings);
        }
        listView.setAdapter(listViewAdapter);
        autoCompleteTextView.setAdapter(searchAdapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Utils.hideKeyboard(context, autoCompleteTextView);
                String selection = adapterView.getItemAtPosition(position).toString();
                selectedIndex = -1;
                for (int i = 0; i < fairyRings.size(); i++) {
                    if (fairyRings.get(i).code.toLowerCase().equals(selection.toLowerCase())) {
                        selectedIndex = i;
                        break;
                    }
                }
                listView.setSelection(selectedIndex);
            }
        });

        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        autoCompleteTextView.addTextChangedListener(this);
        listView.setSelection(selectedIndex);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().trim().length() == 0) {
            searchAdapter.resetItems();
        }
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP && clearableAutoCompleteTextView.isShown()) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) clearableAutoCompleteTextView.getLayoutParams();
            int height = clearableAutoCompleteTextView.getHeight() + params.bottomMargin + params.topMargin;
            clearableAutoCompleteTextView.animate().translationY(-height).setInterpolator(new AccelerateInterpolator(2));
            listView.animate().translationY(0).setInterpolator(new AccelerateInterpolator(2));
            clearableAutoCompleteTextView.setVisibility(View.GONE);
        }

        else if (scrollState == ScrollState.DOWN && !clearableAutoCompleteTextView.isShown()) {
            clearableAutoCompleteTextView.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
            clearableAutoCompleteTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void cancelRunningTasks() {
    }

    @Override
    public boolean wasRequesting() {
        return false;
    }

}
