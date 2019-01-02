package com.dennyy.osrscompanion.viewhandlers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.dennyy.osrscompanion.AppController;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.GeHistoryAdapter;
import com.dennyy.osrscompanion.adapters.GrandExchangeSearchAdapter;
import com.dennyy.osrscompanion.asynctasks.GeAsyncTasks;
import com.dennyy.osrscompanion.asynctasks.GetOSBuddyExchangeSummaryTask;
import com.dennyy.osrscompanion.asynctasks.LoadGeItemsTask;
import com.dennyy.osrscompanion.asynctasks.WriteOSBuddyExchangeSummaryTask;
import com.dennyy.osrscompanion.customviews.ClearableAutoCompleteTextView;
import com.dennyy.osrscompanion.customviews.DelayedAutoCompleteTextView;
import com.dennyy.osrscompanion.customviews.LineIndicatorButton;
import com.dennyy.osrscompanion.database.AppDb;
import com.dennyy.osrscompanion.enums.GeGraphDays;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Logger;
import com.dennyy.osrscompanion.helpers.RsUtils;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.interfaces.AdapterGeHistoryClickListener;
import com.dennyy.osrscompanion.interfaces.GeListeners;
import com.dennyy.osrscompanion.interfaces.JsonItemsLoadedListener;
import com.dennyy.osrscompanion.interfaces.OSBuddySummaryLoadedListener;
import com.dennyy.osrscompanion.models.GrandExchange.*;
import com.dennyy.osrscompanion.models.OSBuddy.OSBuddySummaryItem;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GrandExchangeViewHandler extends BaseViewHandler implements View.OnClickListener, JsonItemsLoadedListener, GeListeners.GeHistoryLoadedListener, AdapterGeHistoryClickListener {

    private JsonItem jsonItem;
    private GeGraphDays currentSelectedDays = GeGraphDays.WEEK;
    private boolean wasRequestingGe;
    private boolean wasRequestingGeupdate;
    private boolean wasRequestingGegraph;
    private boolean wasRequestingOsBuddy;

    private static final String GE_REQUEST_TAG = "grandexchangerequest";
    private static final String GEUPDATE_REQUEST_TAG = "grandexchangeupdaterequest";
    private static final String GEGRAPH_REQUEST_TAG = "grandexchangegraphrequest";
    private static final String OSBUDDY_EXCHANGE_REQUEST_TAG = "osbuddy_exchange_request_tag";

    private DelayedAutoCompleteTextView autoCompleteTextView;
    private SwipeRefreshLayout refreshLayout;
    private HashMap<String, JsonItem> allItems = new HashMap<>();
    private ListView geHistoryListView;
    private GeHistoryAdapter geHistoryAdapter;
    private LinearLayout geHistoryContainer;
    private GeHistory geHistory = new GeHistory();

    private ImageButton favoriteIcon;
    private HashMap<GeGraphDays, Integer> indicators = new HashMap<>();
    private long lastRefreshTimeMs;
    private JsonItemsLoadedListener jsonItemsLoadedListener;
    private HashMap<String, OSBuddySummaryItem> summaryItems = new HashMap<>();
    private long summaryItemsDateModified;
    private LineChart chart;

    public GrandExchangeViewHandler(Context context, View view, boolean isFloatingView, JsonItemsLoadedListener listener) {
        super(context, view, isFloatingView);

        initIndicators();
        initChartSettings();
        autoCompleteTextView = ((ClearableAutoCompleteTextView) view.findViewById(R.id.ge_search_input)).getAutoCompleteTextView();
        jsonItemsLoadedListener = listener;
        refreshLayout = view.findViewById(R.id.ge_refresh_layout);
        favoriteIcon = view.findViewById(R.id.ge_favorite_icon);
        favoriteIcon.setOnClickListener(this);
        geHistoryContainer = view.findViewById(R.id.ge_history_container);
        geHistoryListView = geHistoryContainer.findViewById(R.id.ge_history_listview);
        geHistoryContainer.findViewById(R.id.ge_history_clear).setOnClickListener(this);
        new LoadGeItemsTask(context, this).execute();
        new GetOSBuddyExchangeSummaryTask(context, loadSummaryDataCallback()).execute();
        if (isFloatingView) {
            ImageButton historyIcon = view.findViewById(R.id.ge_history_icon);
            historyIcon.setVisibility(View.VISIBLE);
            historyIcon.setOnClickListener(this);
        }
    }

    @Override
    public void onJsonItemsLoaded(HashMap<String, JsonItem> items) {
        allItems = new HashMap<>(items);
        GrandExchangeSearchAdapter searchAdapter = new GrandExchangeSearchAdapter(context, allItems.values());
        autoCompleteTextView.setAdapter(searchAdapter);
        new GeAsyncTasks.GetHistory(context, false, this).execute();
        initListeners();
    }

    private void initListeners() {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (allowUpdateItem()) {
                    updateItem(jsonItem);
                }
            }
        });

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId) {
                Utils.hideKeyboard(context, autoCompleteTextView);
                autoCompleteTextView.forceDismissDropdown();
                jsonItem = (JsonItem) adapterView.getItemAtPosition(position);
                boolean isFavorite = geHistory.isFavorite(jsonItem.id);
                new GeAsyncTasks.InsertOrUpdateGeHistory(context, jsonItem.id, jsonItem.name, isFavorite, GrandExchangeViewHandler.this).execute();
                if (allowUpdateItem()) {
                    updateItem(jsonItem);
                }
            }
        });

        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });
        if (jsonItemsLoadedListener != null) {
            jsonItemsLoadedListener.onJsonItemsLoaded(null);
        }
    }

    @Override
    public void onJsonItemsLoadError() {
        showToast(getResources().getString(R.string.exception_occurred, "exception", "loading items from file"), Toast.LENGTH_LONG);
    }

    @Override
    public void onGeHistoryLoaded(GeHistory geHistory) {
        this.geHistory = new GeHistory(geHistory);
        if (geHistoryAdapter == null) {
            geHistoryAdapter = new GeHistoryAdapter(context, geHistory, this);
            geHistoryListView.setAdapter(geHistoryAdapter);
            if (!geHistory.isEmpty()) {
                toggleGeData(false);
            }
        }
        else {
            geHistoryAdapter.updateList(geHistory);
        }
    }

    @Override
    public void onClickGeHistory(String itemId) {
        updateItem(itemId, false);
    }

    @Override
    public void onClickRemoveFavorite(String itemId, String itemName) {
        new GeAsyncTasks.InsertOrUpdateGeHistory(context, itemId, itemName, false, this).execute();
    }

    private void initChartSettings() {
        chart = view.findViewById(R.id.ge_item_graph);
        int white = getResources().getColor(R.color.text);
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setScaleYEnabled(false);
        chart.setMaxVisibleValueCount(10);
        chart.setDrawBorders(true);
        chart.setBorderColor(white);
        chart.getAxisLeft().setGranularity(1);
        chart.getLegend().setTextColor(white);
        chart.getAxisLeft().setTextColor(white);
        chart.getXAxis().setTextColor(white);
        chart.setNoDataText(getResources().getString(R.string.ge_graph_loading));
        Paint paint = chart.getPaint(Chart.PAINT_INFO);
        paint.setColor(getResources().getColor(R.color.text));
    }

    public void updateItem(final String id, boolean fromDb) {
        jsonItem = allItems.get(id);
        if (jsonItem == null) {
            showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_SHORT);
            Logger.log(id, new NullPointerException("jsonitem not found"));
            return;
        }
        autoCompleteTextView.setText(jsonItem.name);
        autoCompleteTextView.forceDismissDropdown();
        if (fromDb) {
            new GeAsyncTasks.GetItemData(context, Integer.parseInt(jsonItem.id), new GeListeners.ItemDataLoadedListener() {
                @Override
                public void onItemDataLoaded(ItemData itemData) {
                    if (itemData.geData != null) {
                        handleGeData(itemData.geData.data);
                        toggleGeData(true);
                    }
                    if (itemData.graphData != null) {
                        handleGeGraphData(itemData.graphData.data);
                    }
                    if (itemData.geUpdate != null) {
                        handleGeUpdateData(itemData.geUpdate.data);
                    }
                    summaryItems = itemData.osbSummary;
                    handleOSBuddyData();
                }

                @Override
                public void onItemDataLoadFailed() {
                    Logger.log(id, new RuntimeException("failed to restore item from savedinstancestate"));
                    showToast(getString(R.string.unexpected_error_try_reopen), Toast.LENGTH_SHORT);
                }
            }).execute();
        }
        else {
            updateItem(jsonItem);
        }
    }

    private void updateItem(final JsonItem jsonItem) {
        activateRefreshCooldown();
        refreshLayout.setRefreshing(true);
        wasRequestingGe = true;
        final LinearLayout cacheInfoLinearLayout = view.findViewById(R.id.ge_cache_info_wrapper);
        final TextView cacheInfoTextView = view.findViewById(R.id.ge_cache_info);
        Utils.getString(Constants.GE_ITEM_URL + jsonItem.id, GE_REQUEST_TAG, false, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                cacheInfoLinearLayout.setVisibility(View.GONE);
                toggleGeData(true);
                new GeAsyncTasks.InsertOrUpdateGeData(context, jsonItem.id, result).execute();
                loadGraph();
                loadGeUpdate();
                loadOSBuddyExchange();
                handleGeData(result);
            }

            @Override
            public void onError(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    GrandExchangeData cachedData = AppDb.getInstance(context).getGrandExchangeData(Integer.parseInt(jsonItem.id));
                    if (cachedData == null) {
                        showToast(getResources().getString(R.string.failed_to_obtain_data, "GE item data", getResources().getString(R.string.network_error)), Toast.LENGTH_LONG);
                        return;
                    }
                    String cacheText = getResources().getString(R.string.using_cached_data, Utils.convertTime(cachedData.dateModified));
                    cacheInfoTextView.setText(cacheText);
                    cacheInfoLinearLayout.setVisibility(View.VISIBLE);
                    toggleGeData(true);
                    loadGraph();
                    loadGeUpdate();
                    loadOSBuddyExchange();
                    handleGeData(cachedData.data);
                }
                else {
                    showToast(getResources().getString(R.string.failed_to_obtain_data, "ge item data", error.getMessage()), Toast.LENGTH_LONG);
                }
            }

            @Override
            public void always() {
                refreshLayout.setRefreshing(false);
                wasRequestingGe = false;
            }
        });
    }

    private void toggleFavoriteItem(String itemId) {
        boolean isFavorite = geHistory.isFavorite(itemId);
        favoriteIcon.setBackground(ContextCompat.getDrawable(context, isFavorite ? R.drawable.baseline_star_white_24 : R.drawable.baseline_star_border_white_24));
        favoriteIcon.setVisibility(View.VISIBLE);
    }

    private void handleGeData(String geItemData) {
        try {
            JSONObject obj = new JSONObject(geItemData);
            JSONObject jItem = obj.getJSONObject("item");
            GrandExchangeItem item = getItemFromJson(jsonItem.id, jsonItem.limit, jItem);
            int red = getResources().getColor(R.color.red);
            int green = getResources().getColor(R.color.green);
            toggleFavoriteItem(item.id);
            if (Utils.isValidContextForGlide(context)) {
                Glide.with(context).load(Constants.GE_IMG_LARGE_URL + item.id).into((ImageView) view.findViewById(R.id.ge_item_icon));
            }
            ((ImageView) view.findViewById(R.id.ge_item_members_indicator)).setImageDrawable(item.members ? getResources().getDrawable(R.drawable.members) : null);

            ((TextView) view.findViewById(R.id.ge_item_name)).setText(item.name);
            ((TextView) view.findViewById(R.id.ge_item_examine)).setText(item.description);
            ((TextView) view.findViewById(R.id.ge_item_price)).setText(RsUtils.kmbt(item.price, 2));
            ((TextView) view.findViewById(R.id.ge_buy_limit)).setText(item.limit < 0 ? getString(R.string.unknown) : RsUtils.kmbt(item.limit, 0));

            TextView itemChangeTextView = view.findViewById(R.id.ge_item_change);
            TextView itemChangePercentTextView = view.findViewById(R.id.ge_item_change_percent);
            itemChangeTextView.setText(String.format("%s%s", item.change < 0 ? "" : "+", RsUtils.kmbt(item.change)));
            itemChangePercentTextView.setText(String.format("%s%s%%", item.changePercent < 0 ? "" : "+", String.valueOf((int) Math.round(item.changePercent))));
            itemChangeTextView.setTextColor(item.change < 0 ? red : green);
            itemChangePercentTextView.setTextColor(item.change < 0 ? red : green);

            TextView item30daysTextView = view.findViewById(R.id.ge_item_30days);
            TextView item30daysPercentTextView = view.findViewById(R.id.ge_item_30days_percent);
            item30daysTextView.setText(String.format("%s%s", item.day30change < 0 ? "" : "+", RsUtils.kmbt(item.day30change)));
            item30daysPercentTextView.setText(String.format("%s%s%%", item.day30change < 0 ? "" : "+", String.valueOf((int) Math.round(item.day30changePercent))));
            item30daysTextView.setTextColor(item.day30change < 0 ? red : green);
            item30daysPercentTextView.setTextColor(item.day30change < 0 ? red : green);

            TextView item90daysTextView = view.findViewById(R.id.ge_item_90days);
            TextView item90daysPercentTextView = view.findViewById(R.id.ge_item_90days_percent);
            item90daysTextView.setText(String.format("%s%s", item.day90change < 0 ? "" : "+", RsUtils.kmbt(item.day90change)));
            item90daysPercentTextView.setText(String.format("%s%s%%", item.day90change < 0 ? "" : "+", String.valueOf((int) Math.round(item.day90changePercent))));
            item90daysTextView.setTextColor(item.day90change < 0 ? red : green);
            item90daysPercentTextView.setTextColor(item.day90change < 0 ? red : green);


            TextView item180daysTextView = view.findViewById(R.id.ge_item_180days);
            TextView item180daysPercentTextview = view.findViewById(R.id.ge_item_180days_percent);
            item180daysTextView.setText(String.format("%s%s", item.day180change < 0 ? "" : "+", RsUtils.kmbt(item.day180change)));
            item180daysPercentTextview.setText(String.format("%s%s%%", item.day180change < 0 ? "" : "+", String.valueOf((int) Math.round(item.day180changePercent))));
            item180daysTextView.setTextColor(item.day180change < 0 ? red : green);
            item180daysPercentTextview.setTextColor(item.day180change < 0 ? red : green);

            TextView highAlchTextView = view.findViewById(R.id.ge_item_high_alch);
            TextView lowAlchTextView = view.findViewById(R.id.ge_item_low_alch);
            double lowAlch = 0;
            double highAlch = 0;
            if (jsonItem.store != null) {
                lowAlch = Math.floor(Integer.valueOf(jsonItem.store) * 0.4);
                highAlch = Math.floor(Integer.valueOf(jsonItem.store) * 0.6);
            }
            lowAlchTextView.setText(RsUtils.kmbt(lowAlch < 1 ? 1 : lowAlch, 2));
            highAlchTextView.setText(RsUtils.kmbt(highAlch < 1 ? 1 : highAlch, 2));
        }
        catch (JSONException ex) {
            Logger.log(ex);
            showToast(getResources().getString(R.string.exception_occurred, ex.getClass().getCanonicalName(), "parsing ge item data"), Toast.LENGTH_LONG);
        }
    }

    private GrandExchangeItem getItemFromJson(String id, int limit, JSONObject jsonItem) {
        GrandExchangeItem geItem = new GrandExchangeItem();
        try {
            geItem.id = id;
            geItem.name = jsonItem.getString("name");
            geItem.description = jsonItem.getString("description");
            geItem.members = jsonItem.getBoolean("members");
            geItem.limit = limit;
            geItem.price = RsUtils.revkmbt(jsonItem.getJSONObject("current").getString("price").replace(",", ""));
            geItem.change = RsUtils.revkmbt(jsonItem.getJSONObject("today").getString("price").replace(",", ""));
            geItem.changePercent = RsUtils.getGEPercentChange(geItem.price, geItem.change);
            geItem.day30changePercent = RsUtils.revkmbt(jsonItem.getJSONObject("day30").getString("change").replace("%", ""));
            geItem.day30change = RsUtils.getGEPriceChange(geItem.price, geItem.day30changePercent);
            geItem.day90changePercent = RsUtils.revkmbt(jsonItem.getJSONObject("day90").getString("change").replace("%", ""));
            geItem.day90change = RsUtils.getGEPriceChange(geItem.price, geItem.day90changePercent);
            geItem.day180changePercent = RsUtils.revkmbt(jsonItem.getJSONObject("day180").getString("change").replace("%", ""));
            geItem.day180change = RsUtils.getGEPriceChange(geItem.price, geItem.day180changePercent);
        }
        catch (JSONException ex) {
            Logger.log(id, ex);
            showToast(getResources().getString(R.string.exception_occurred, ex.getClass().getSimpleName(), "parsing json to object"), Toast.LENGTH_LONG);
        }
        return geItem;
    }

    public boolean allowUpdateItem() {
        long refreshPeriod = System.currentTimeMillis() - lastRefreshTimeMs;
        if (jsonItem == null || autoCompleteTextView.getText().toString().isEmpty()) {
            showToast(getResources().getString(R.string.empty_item_error), Toast.LENGTH_SHORT);
            refreshLayout.setRefreshing(false);
            return false;
        }
        if (refreshPeriod < Constants.REFRESH_COOLDOWN_MS) {
            double timeLeft = (Constants.REFRESH_COOLDOWN_MS - refreshPeriod) / 1000;
            showToast(getResources().getString(R.string.wait_before_refresh, (int) Math.ceil(timeLeft + 0.5)), Toast.LENGTH_SHORT);
            refreshLayout.setRefreshing(false);
            return false;
        }
        if (jsonItem != null && jsonItem.id.equals("-1")) {
            refreshLayout.setRefreshing(false);
            return false;
        }
        return true;
    }

    private void loadGeUpdate() {
        wasRequestingGeupdate = true;
        new GeAsyncTasks.GetGeUpdate(context, new GeListeners.GeUpdateLoadedListener() {
            @Override
            public void onGeUpdateLoaded(boolean cacheExpired, GrandExchangeUpdateData grandExchangeUpdateData) {
                if (!cacheExpired) {
                    handleGeUpdateData(grandExchangeUpdateData.data);
                    return;
                }
                fetchGeUpdateData(grandExchangeUpdateData.data);
            }

            @Override
            public void onGeUpdateLoadFailed() {
                fetchGeUpdateData(null);
            }
        }).execute();
    }

    private void fetchGeUpdateData(final String cachedData) {
        Utils.getString(Constants.GE_UPDATE_URL, GEUPDATE_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                new GeAsyncTasks.InsertGeUpdate(context, result).execute();
                handleGeUpdateData(result);
            }

            @Override
            public void onError(VolleyError error) {
                if (Utils.isNullOrEmpty(cachedData)) {
                    TextView geupdateTextView = view.findViewById(R.id.geupdate);
                    geupdateTextView.setText(getString(R.string.failed_to_obtain_data, "ge update data", error.getClass().getSimpleName()));
                    return;
                }
                handleGeUpdateData(cachedData);
            }

            @Override
            public void always() {
                wasRequestingGeupdate = false;
            }
        });
    }

    private void handleGeUpdateData(String geupdateData) {
        TextView geupdateTextView = view.findViewById(R.id.geupdate);
        try {
            JSONObject obj = new JSONObject(geupdateData);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = format.parse(obj.getString("datetime"));

            SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.getDefault());
            displayFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            geupdateTextView.setText(String.format(getResources().getString(R.string.time_utc), displayFormat.format(date)));

            TextView geupdateTimeAgoTextView = view.findViewById(R.id.geupdate_time_ago);
            long millisAgo = new Date().getTime() - date.getTime();
            String timeAgo = getResources().getString(R.string.geupdate_time_ago, TimeUnit.MILLISECONDS.toHours(millisAgo), TimeUnit.MILLISECONDS.toMinutes(millisAgo) % TimeUnit.HOURS.toMinutes(1));
            geupdateTimeAgoTextView.setText(timeAgo);
        }
        catch (JSONException | ParseException ex) {
            Logger.log(geupdateData, ex);
            geupdateTextView.setText(getResources().getString(R.string.exception_occurred, ex.getClass().getCanonicalName(), "getting geupdate data"));
        }
    }

    private void loadGraph() {
        final String id = jsonItem.id;
        wasRequestingGegraph = true;
        chart.setNoDataText(getString(R.string.ge_graph_loading));
        chart.clear();
        Utils.getString(Constants.GE_GRAPH_URL(id), GEGRAPH_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                AppDb.getInstance(context).insertOrUpdateGrandExchangeGraphData(id, result);
                handleGeGraphData(result);
            }

            @Override
            public void onError(VolleyError error) {
                GrandExchangeGraphData cachedData = AppDb.getInstance(context).getGrandExchangeGraphData(Integer.parseInt(id));
                if ((error instanceof TimeoutError || error instanceof NoConnectionError) && cachedData != null) {
                    handleGeGraphData(cachedData.data);
                }
                else {
                    chart.setNoDataText(getString(R.string.failed_to_obtain_data, "ge graph data", error.getClass().getSimpleName()));
                    chart.invalidate();
                }
            }

            @Override
            public void always() {
                wasRequestingGegraph = false;
            }
        });
    }

    private void handleGeGraphData(String geGraphData) {
        try {
            JSONObject dailyGraphData = new JSONObject(geGraphData).getJSONObject("daily");
            List<Entry> data = new ArrayList<>();
            for (Iterator<String> iter = dailyGraphData.keys(); iter.hasNext(); ) {
                String key = iter.next();
                float time = Float.parseFloat(key);
                data.add(new Entry(time, dailyGraphData.getInt(key)));
            }
            CustomLineDataSet dataSet = new CustomLineDataSet(data, getResources().getString(R.string.price), getResources().getColor(R.color.ge_graph_price));

            JSONObject trendGraphData = new JSONObject(geGraphData).getJSONObject("average");
            List<Entry> trendData = new ArrayList<>();
            for (Iterator<String> iter = trendGraphData.keys(); iter.hasNext(); ) {
                String key = iter.next();
                float time = Float.parseFloat(key);
                trendData.add(new Entry(time, trendGraphData.getInt(key)));
            }
            CustomLineDataSet dataSet2 = new CustomLineDataSet(trendData, getResources().getString(R.string.trend), getResources().getColor(R.color.ge_graph_trend));

            LineData lineData = new LineData(dataSet, dataSet2);
            XAxis xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAvoidFirstLastClipping(true);
            xAxis.setValueFormatter(new IAxisValueFormatter() {

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    long unixSeconds = (long) value;
                    Date date = new Date(unixSeconds);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                    return sdf.format(date);
                }
            });
            chart.setData(lineData);
            zoomGraphToDays(currentSelectedDays);
        }
        catch (JSONException ex) {
            Logger.log(geGraphData, ex);
            chart.setNoDataText(getString(R.string.exception_occurred, ex.getClass().getCanonicalName(), "parsing ge graph data"));
            chart.invalidate();
        }
    }

    private void zoomGraphToDays(GeGraphDays days) {
        chart.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
        chart.fitScreen();
        chart.setVisibleXRangeMaximum((long) days.getDays() * 86400000);
        chart.moveViewToX(chart.getXAxis().getAxisMaximum());
        chart.invalidate();

        // update indicators
        for (Map.Entry<GeGraphDays, Integer> entry : indicators.entrySet()) {
            ((LineIndicatorButton) view.findViewById(entry.getValue())).setActive(false);
        }
        ((LineIndicatorButton) view.findViewById(indicators.get(days))).setActive(true);
    }

    private void loadOSBuddyExchange() {
        setOSBuddyText("...", false);
        boolean cacheExpired = Math.abs(System.currentTimeMillis() - summaryItemsDateModified) > Constants.OSBUDDY_SUMMARY_CACHE_DURATION;
        if (summaryItems.isEmpty() || cacheExpired) {
            wasRequestingOsBuddy = true;
            new GetOSBuddyExchangeSummaryTask(context, loadSummaryDataCallback()).execute();
        }
        else {
            handleOSBuddyData();
        }
    }

    private void handleOSBuddyData() {
        String error = getResources().getString(R.string.osb_error);
        if (jsonItem == null) {
            setOSBuddyText(error, true);
            return;
        }
        OSBuddySummaryItem summaryItem = summaryItems.get(jsonItem.id);
        if (summaryItem == null) {
            setOSBuddyText(error, true);
            return;
        }

        int buyPrice = summaryItem.buyPrice;
        int sellPrice = summaryItem.sellPrice;
        String buyText = buyPrice < 1 ? getResources().getString(R.string.inactive) : RsUtils.kmbt(buyPrice, 2);
        String sellText = sellPrice < 1 ? getResources().getString(R.string.inactive) : RsUtils.kmbt(sellPrice, 2);
        setOSBuddyText(buyText, sellText, false);
    }

    private void setOSBuddyText(String buyText, String sellText, boolean isError) {
        TextView osbBuyPriceTextView = view.findViewById(R.id.osb_buy_price);
        TextView osbSellPriceTextView = view.findViewById(R.id.osb_sell_price);
        osbBuyPriceTextView.setTextColor(getResources().getColor(isError ? R.color.red : R.color.text));
        osbSellPriceTextView.setTextColor(getResources().getColor(isError ? R.color.red : R.color.text));
        osbBuyPriceTextView.setText(buyText);
        osbSellPriceTextView.setText(sellText);
    }

    private void setOSBuddyText(String text, boolean isError) {
        setOSBuddyText(text, text, isError);
    }

    private OSBuddySummaryLoadedListener loadSummaryDataCallback() {
        return new OSBuddySummaryLoadedListener() {
            @Override
            public void onContentLoaded(final HashMap<String, OSBuddySummaryItem> content, long dateModified, boolean cacheExpired) {
                summaryItemsDateModified = dateModified;
                if (content.isEmpty() || cacheExpired) {
                    Utils.getString(Constants.OSBUDDY_EXCHANGE_SUMMARY_URL, OSBUDDY_EXCHANGE_REQUEST_TAG, new Utils.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            new WriteOSBuddyExchangeSummaryTask(context, result).execute();
                            try {
                                summaryItems = parseOSBuddySummary(result);
                                handleOSBuddyData();
                            }
                            catch (JSONException ex) {
                                Logger.log(ex);
                                setOSBuddyText(getResources().getString(R.string.osb_parse_error), true);
                            }
                        }

                        @Override
                        public void onError(VolleyError error) {
                            if (!content.isEmpty()) {
                                summaryItems = content;
                                handleOSBuddyData();
                            }
                            else {
                                setOSBuddyText(getResources().getString(R.string.osb_error), true);
                            }
                        }

                        @Override
                        public void always() {
                            wasRequestingOsBuddy = false;
                        }
                    });
                }
                else {
                    summaryItems = content;
                    handleOSBuddyData();
                    wasRequestingOsBuddy = false;
                }
            }

            @Override
            public void onContentLoadError() {
                showToast(getResources().getString(R.string.exception_occurred, "error", "loading osbuddy prices"), Toast.LENGTH_LONG);
            }
        };
    }

    public static HashMap<String, OSBuddySummaryItem> parseOSBuddySummary(String osBuddyItemData) throws JSONException {
        HashMap<String, OSBuddySummaryItem> summaryItems = new HashMap<>();
        JSONObject jsonObject = new JSONObject(osBuddyItemData);
        Iterator itemLimitsIterator = jsonObject.keys();
        while (itemLimitsIterator.hasNext()) {
            String itemId = (String) itemLimitsIterator.next();
            JSONObject summaryJson = jsonObject.getJSONObject(itemId);
            OSBuddySummaryItem summaryItem = new OSBuddySummaryItem();
            summaryItem.id = itemId;
            summaryItem.buyPrice = summaryJson.getInt("buy_average");
            summaryItem.sellPrice = summaryJson.getInt("sell_average");
            summaryItems.put(itemId, summaryItem);
        }
        return summaryItems;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ge_favorite_icon && jsonItem != null) {
            boolean isFavorite = geHistory.isFavorite(jsonItem.id);
            geHistory.toggleFavorite(jsonItem.id, !isFavorite);
            new GeAsyncTasks.InsertOrUpdateGeHistory(context, jsonItem.id, jsonItem.name, !isFavorite, this).execute();
            toggleFavoriteItem(jsonItem.id);
        }
        else if (id == R.id.ge_history_clear) {
            new GeAsyncTasks.GetHistory(context, true, this).execute();
        }
        else if (id == R.id.ge_history_icon) {
            toggleGeData();
        }
        else {
            GeGraphDays days;
            switch (id) {
                case R.id.ge_graph_show_quarter:
                    days = GeGraphDays.QUARTER;
                    break;
                case R.id.ge_graph_show_month:
                    days = GeGraphDays.MONTH;
                    break;
                case R.id.ge_graph_show_week:
                    days = GeGraphDays.WEEK;
                    break;
                default:
                    days = GeGraphDays.ALL;
            }
            if (currentSelectedDays != days) {
                currentSelectedDays = days;
                zoomGraphToDays(days);
            }
        }
    }

    private void activateRefreshCooldown() {
        lastRefreshTimeMs = System.currentTimeMillis();
    }

    private Resources getResources() {
        return this.resources;
    }

    @Override
    public void cancelRunningTasks() {
        AppController.getInstance().cancelPendingRequests(GE_REQUEST_TAG);
        AppController.getInstance().cancelPendingRequests(GEUPDATE_REQUEST_TAG);
        AppController.getInstance().cancelPendingRequests(GEGRAPH_REQUEST_TAG);
        AppController.getInstance().cancelPendingRequests(OSBUDDY_EXCHANGE_REQUEST_TAG);
    }

    @Override
    public boolean wasRequesting() {
        return wasRequestingGe || wasRequestingGegraph || wasRequestingGeupdate || wasRequestingOsBuddy;
    }

    @Override
    public void onGeHistoryLoadFailed() {

    }

    private void initIndicators() {
        indicators.put(GeGraphDays.ALL, R.id.ge_graph_show_all);
        indicators.put(GeGraphDays.QUARTER, R.id.ge_graph_show_quarter);
        indicators.put(GeGraphDays.MONTH, R.id.ge_graph_show_month);
        indicators.put(GeGraphDays.WEEK, R.id.ge_graph_show_week);
        for (Map.Entry<GeGraphDays, Integer> entry : indicators.entrySet()) {
            view.findViewById(entry.getValue()).setOnClickListener(this);
        }
    }

    public String getItemId() {
        return jsonItem != null ? jsonItem.id : null;
    }

    public void toggleGeData() {
        if (jsonItem == null) {
            toggleGeData(false);
        }
        else {
            toggleGeData(!isGeDataVisible());
        }
    }

    public void toggleGeData(boolean visible) {
        view.findViewById(R.id.ge_data).setVisibility(visible ? View.VISIBLE : View.GONE);
        favoriteIcon.setVisibility(visible ? View.VISIBLE : View.GONE);
        geHistoryContainer.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    public boolean isGeDataVisible() {
        return view.findViewById(R.id.ge_data).getVisibility() == View.VISIBLE;
    }
}
