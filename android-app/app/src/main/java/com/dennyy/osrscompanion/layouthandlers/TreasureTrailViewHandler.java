package com.dennyy.osrscompanion.layouthandlers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dennyy.osrscompanion.AppController;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.ImageSliderAdapter;
import com.dennyy.osrscompanion.adapters.TreasureTrailMapAdapter;
import com.dennyy.osrscompanion.adapters.TreasureTrailSearchAdapter;
import com.dennyy.osrscompanion.customviews.ClearableAutoCompleteTextView;
import com.dennyy.osrscompanion.enums.TreasureTrailType;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.interfaces.TreasureTrailsLoadedCallback;
import com.dennyy.osrscompanion.models.TreasureTrails.TreasureTrail;
import com.dennyy.osrscompanion.models.TreasureTrails.TreasureTrailMap;
import com.dennyy.osrscompanion.models.TreasureTrails.TreasureTrails;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class TreasureTrailViewHandler extends BaseViewHandler implements View.OnClickListener, TreasureTrailMapAdapter.AdapterImageClickListener, TreasureTrailsLoadedCallback {
    public TreasureTrail treasureTrail;

    private static final String TT_REQUEST_TAG = "TT_REQUEST_TAG";

    private TreasureTrailSearchAdapter adapter;
    private int selectedAdapterIndex = -1;
    private TreasureTrailMapAdapter treasureTrailMapAdapter;
    private AutoCompleteTextView autoCompleteTextView;
    private TreasureTrails allItems = new TreasureTrails();
    private ViewPager viewPager;
    private ListView mapsListView;
    private View dimView;
    private ImageView expandedImageView;
    private TreasureTrailsLoadedCallback treasureTrailsLoadedCallback;

    public TreasureTrailViewHandler(Context context, View view, TreasureTrailsLoadedCallback treasureTrailsLoadedCallback) {
        super(context, view);
        this.treasureTrailsLoadedCallback = treasureTrailsLoadedCallback;
        new LoadItems(context, this).execute();
    }

    @Override
    public void onTreasureTrailsLoaded(TreasureTrails treasureTrails) {
        allItems = treasureTrails;
        updateView(view);
        if (treasureTrailsLoadedCallback != null) {
            treasureTrailsLoadedCallback.onTreasureTrailsLoaded(null);
        }
    }

    @Override
    public void onTreasureTrailsLoadError() {
        showToast(resources.getString(R.string.exception_occurred, "exception", "loading items from file"), Toast.LENGTH_LONG);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void updateView(View view) {
        this.view = view;
        mapsListView = view.findViewById(R.id.tt_maps_listview);
        expandedImageView = view.findViewById(R.id.expanded_image);
        dimView = view.findViewById(R.id.dim_img_view);
        autoCompleteTextView = ((ClearableAutoCompleteTextView) view.findViewById(R.id.clue_search_input)).getAutoCompleteTextView();
        if (treasureTrail != null)
            autoCompleteTextView.setText(treasureTrail.text);

        if (adapter == null) {
            adapter = new TreasureTrailSearchAdapter(context, (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE), allItems.treasureTrails);
        }
        if (treasureTrailMapAdapter == null) {
            treasureTrailMapAdapter = new TreasureTrailMapAdapter(context, allItems.treasureTrailMaps, this);
        }
        mapsListView.setAdapter(treasureTrailMapAdapter);

        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Utils.hideKeyboard(context, autoCompleteTextView);
                selectedAdapterIndex = i;
                updateItem();
            }
        });

        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });
        autoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                autoCompleteTextView.setThreshold(3);
                return false;
            }
        });
        dimView.setOnClickListener(this);
        expandedImageView.setOnClickListener(this);
        view.findViewById(R.id.tt_fab).setOnClickListener(this);
        viewPager = view.findViewById(R.id.viewPager);
    }

    private void updateItem() {
        treasureTrail = adapter.getItem(selectedAdapterIndex);
        if (treasureTrail != null && treasureTrail.type == null)
            return;
        reloadData();
    }

    public void reloadData() {
        if (treasureTrail == null) {
            return;
        }
        ((TextView) view.findViewById(R.id.treasure_trail_clue)).setText(treasureTrail.text);
        ((TextView) view.findViewById(R.id.treasure_trail_type)).setText(treasureTrail.type.getValue());

        TextView answerTextView = view.findViewById(R.id.treasure_trail_answer);
        LinearLayout answerLinearLayout = view.findViewById(R.id.tt_answer_layout);
        if (treasureTrail.answer == null) {
            answerLinearLayout.setVisibility(View.GONE);
        }
        else {
            answerTextView.setText(treasureTrail.answer);
            answerLinearLayout.setVisibility(View.VISIBLE);
        }

        TextView npcTextView = view.findViewById(R.id.treasure_trail_npc);
        LinearLayout npcLinearLayout = view.findViewById(R.id.tt_npc_layout);
        if (treasureTrail.npc == null) {
            npcLinearLayout.setVisibility(View.GONE);
        }
        else {
            npcTextView.setText(treasureTrail.npc);
            npcLinearLayout.setVisibility(View.VISIBLE);

        }

        TextView locationTextView = view.findViewById(R.id.treasure_trail_location);
        LinearLayout locationLinearLayout = view.findViewById(R.id.tt_location_layout);
        if (treasureTrail.location == null) {
            locationLinearLayout.setVisibility(View.GONE);
        }
        else {
            locationTextView.setText(treasureTrail.location);
            locationLinearLayout.setVisibility(View.VISIBLE);
        }

        if (treasureTrail.type == TreasureTrailType.COORDINATES) {
            ((TextView) view.findViewById(R.id.treasure_trail_clue)).setText(treasureTrail.getCoordinatesFormatted());
            loadTreasureTrailImages();
            view.findViewById(R.id.tt_images_layout).setVisibility(View.VISIBLE);
        }
        else {
            view.findViewById(R.id.tt_images_layout).setVisibility(View.GONE);
        }

        view.findViewById(R.id.treasure_trail_data).setVisibility(View.VISIBLE);
    }

    private void loadTreasureTrailImages() {
        ArrayList<String> imageUrls = new ArrayList<>();
        imageUrls.add(Constants.CLUE_MAP_URL(treasureTrail.getCoordinatesFormattedForUrl()));
        imageUrls.add(Constants.CLUE_LOC_URL(treasureTrail.getCoordinatesFormattedForUrl()));
        viewPager.setAdapter(new ImageSliderAdapter(AppController.getInstance().getApplicationContext(), imageUrls));
        CirclePageIndicator indicator = view.findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
        indicator.setRadius(Utils.convertDpToPixel(5, AppController.getInstance().getApplicationContext()));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.tt_fab:
                LinearLayout dataLayout = this.view.findViewById(R.id.tt_data_layout);
                boolean visible = dataLayout.getVisibility() == View.VISIBLE;
                dataLayout.setVisibility(visible ? View.GONE : View.VISIBLE);
                mapsListView.setVisibility(visible ? View.VISIBLE : View.GONE);
                break;
            case R.id.expanded_image:
            case R.id.dim_img_view:
                expandedImageView.setVisibility(View.GONE);
                dimView.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onClickImage(int index, View view) {
        if (view instanceof ImageView) {
            dimView.setVisibility(View.VISIBLE);
            expandedImageView.setVisibility(View.VISIBLE);
            expandedImageView.setImageDrawable(((ImageView) view).getDrawable());
        }
    }

    private static class LoadItems extends AsyncTask<String, Void, TreasureTrails> {
        private WeakReference<Context> context;
        private TreasureTrailsLoadedCallback treasureTrailsLoadedCallback;

        private LoadItems(Context context, TreasureTrailsLoadedCallback treasureTrailsLoadedCallback) {
            this.context = new WeakReference<>(context);
            this.treasureTrailsLoadedCallback = treasureTrailsLoadedCallback;
        }

        @Override
        protected TreasureTrails doInBackground(String... params) {
            TreasureTrails treasureTrails = new TreasureTrails();
            try {
                String clues = Utils.readFromAssets(context.get(), "clues.json");
                String maps = Utils.readFromAssets(context.get(), "treasure_maps.json");
                JSONObject cluesJsonObject = new JSONObject(clues);
                Iterator cluesIterator = cluesJsonObject.keys();
                while (cluesIterator.hasNext()) {
                    String id = (String) cluesIterator.next();
                    JSONArray jsonArray = cluesJsonObject.getJSONArray(id);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        TreasureTrail treasureTrail = new TreasureTrail();

                        treasureTrail.text = jsonObject.getString("text");
                        treasureTrail.type = TreasureTrailType.fromString(jsonObject.getString("type"));
                        treasureTrail.answer = jsonObject.has("answer") ? jsonObject.getString("answer") : null;
                        treasureTrail.npc = jsonObject.has("npc") ? jsonObject.getString("npc") : null;
                        treasureTrail.location = jsonObject.has("location") ? jsonObject.getString("location") : null;
                        treasureTrails.treasureTrails.add(treasureTrail);
                    }
                }
                JSONArray mapsJsonArray = new JSONArray(maps);
                for (int i = 0; i < mapsJsonArray.length(); i++) {
                    JSONObject jsonObject = mapsJsonArray.getJSONObject(i);
                    TreasureTrailMap treasureTrailMap = new TreasureTrailMap();
                    treasureTrailMap.id = jsonObject.getString("id");
                    treasureTrailMap.location = jsonObject.getString("location");
                    treasureTrails.treasureTrailMaps.add(treasureTrailMap);
                }
            }
            catch (JSONException ignored) {

            }
            return treasureTrails;
        }

        @Override
        protected void onPostExecute(TreasureTrails items) {
            if (items.treasureTrails.size() > 0 && items.treasureTrailMaps.size() > 0) {
                treasureTrailsLoadedCallback.onTreasureTrailsLoaded(items);
            }
            else {
                treasureTrailsLoadedCallback.onTreasureTrailsLoadError();
            }
        }
    }

    @Override
    public void cancelRunningTasks() {
        AppController.getInstance().cancelPendingRequests(TT_REQUEST_TAG);
    }

    @Override
    public boolean wasRequesting() {
        return false;
    }

}
