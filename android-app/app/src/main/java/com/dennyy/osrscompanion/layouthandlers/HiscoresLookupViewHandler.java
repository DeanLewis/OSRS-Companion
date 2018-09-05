package com.dennyy.osrscompanion.layouthandlers;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.dennyy.osrscompanion.AppController;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.customviews.ClearableEditText;
import com.dennyy.osrscompanion.customviews.LineIndicatorButton;
import com.dennyy.osrscompanion.enums.HiscoreType;
import com.dennyy.osrscompanion.enums.SkillType;
import com.dennyy.osrscompanion.helpers.AppDb;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.RsUtils;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.General.Combat;
import com.dennyy.osrscompanion.models.General.PlayerStats;
import com.dennyy.osrscompanion.models.General.Skill;
import com.dennyy.osrscompanion.models.Hiscores.UserStats;

import java.util.HashMap;
import java.util.Map;


public class HiscoresLookupViewHandler extends BaseViewHandler implements View.OnClickListener {

    public String hiscoresData;
    public HiscoreType selectedHiscore = HiscoreType.NORMAL;

    private static final String HISCORES_REQUEST_TAG = "hiscoresrequest";
    private EditText rsnEditText;
    private NestedScrollView scrollView;
    private TableLayout hiscoresTable;
    private TableLayout hiscoresMinigameTable;
    private SwipeRefreshLayout refreshLayout;
    private TableRow.LayoutParams rowParams;
    private HashMap<HiscoreType, Integer> indicators;

    private long lastRefreshTimeMs;
    private int refreshCount;

    public HiscoresLookupViewHandler(final Context context, View view) {
        super(context, view);

        rowParams = new TableRow.LayoutParams(0, (int) Utils.convertDpToPixel(35, context), 1f);

        scrollView = view.findViewById(R.id.hiscores_scrollview);
        rsnEditText = ((ClearableEditText) view.findViewById(R.id.hiscores_rsn_input)).getEditText();
        rsnEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    if (allowUpdateUser())
                        updateUser();
                    Utils.hideKeyboard(context, v);
                    return true;
                }
                return false;
            }
        });

        hiscoresTable = view.findViewById(R.id.hiscores_table);
        hiscoresMinigameTable = view.findViewById(R.id.hiscores_minigame_table);
        refreshLayout = view.findViewById(R.id.hiscores_refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (allowUpdateUser())
                    updateUser();
            }
        });
        view.findViewById(R.id.hiscores_lookup_button).setOnClickListener(this);
        indicators = new HashMap<>();
        indicators.put(HiscoreType.NORMAL, R.id.hiscores_normal);
        indicators.put(HiscoreType.IRONMAN, R.id.hiscores_ironman);
        indicators.put(HiscoreType.HCIM, R.id.hiscores_hardcore_ironman);
        indicators.put(HiscoreType.UIM, R.id.hiscores_ultimate_ironman);
        indicators.put(HiscoreType.DMM, R.id.hiscores_dmm);
        indicators.put(HiscoreType.SDMM, R.id.hiscores_sdmm);

        for (Map.Entry<HiscoreType, Integer> entry : indicators.entrySet()) {
            view.findViewById(entry.getValue()).setOnClickListener(this);
        }
        clearTables();
        if (!defaultRsn.isEmpty()) {
            rsnEditText.setText(defaultRsn);
            UserStats cachedData = AppDb.getInstance(context).getUserStats(defaultRsn, selectedHiscore);
            if (cachedData == null) {
                return;
            }
            showToast(resources.getString(R.string.last_updated_at, Utils.convertTime(cachedData.dateModified)), Toast.LENGTH_LONG);
            handleHiscoresData(cachedData.stats);
            view.findViewById(R.id.hiscores_data_layout).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean wasRequesting() {
        return wasRequesting;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.hiscores_lookup_button:
                Utils.hideKeyboard(context, rsnEditText);
                if (allowUpdateUser())
                    updateUser();
                break;
            case R.id.hiscores_normal:
            case R.id.hiscores_ironman:
            case R.id.hiscores_hardcore_ironman:
            case R.id.hiscores_ultimate_ironman:
            case R.id.hiscores_sdmm:
            case R.id.hiscores_dmm:
                updateUserFromHiscoreType(id);
                break;
        }
    }

    private void updateUserFromHiscoreType(int selectedButtonResourceId) {
        if (!allowUpdateUser())
            return;
        HiscoreType mode = getHiscoresMode(selectedButtonResourceId);
        if (selectedHiscore == mode)
            return;
        selectedHiscore = mode;
        updateIndicators();
        updateUser();
    }

    public void updateIndicators() {
        for (Map.Entry<HiscoreType, Integer> entry : indicators.entrySet()) {
            ((LineIndicatorButton) view.findViewById(entry.getValue())).setActive(false);
        }
        ((LineIndicatorButton) view.findViewById(indicators.get(selectedHiscore))).setActive(true);
    }

    private String getHiscoresUrl(HiscoreType mode) {
        String url;
        switch (mode) {
            case UIM:
                url = Constants.RS_HISCORES_UIM_URL;
                break;
            case IRONMAN:
                url = Constants.RS_HISCORES_IRONMAN_URL;
                break;
            case HCIM:
                url = Constants.RS_HISCORES_HCIM_URL;
                break;
            case DMM:
                url = Constants.RS_HISCORES_DMM_URL;
                break;
            case SDMM:
                url = Constants.RS_HISCORES_SDMM_URL;
                break;
            default:
                url = Constants.RS_HISCORES_URL;
        }
        return url;
    }

    private HiscoreType getHiscoresMode(int buttonId) {
        HiscoreType mode;
        switch (buttonId) {
            case R.id.hiscores_ultimate_ironman:
                mode = HiscoreType.UIM;
                break;
            case R.id.hiscores_ironman:
                mode = HiscoreType.IRONMAN;
                break;
            case R.id.hiscores_hardcore_ironman:
                mode = HiscoreType.HCIM;
                break;
            case R.id.hiscores_dmm:
                mode = HiscoreType.DMM;
                break;
            case R.id.hiscores_sdmm:
                mode = HiscoreType.SDMM;
                break;
            default:
                mode = HiscoreType.NORMAL;
                break;
        }
        return mode;
    }

    public boolean allowUpdateUser() {
        long refreshPeriod = System.currentTimeMillis() - lastRefreshTimeMs;
        if (rsnEditText.getText().toString().isEmpty()) {
            showToast(resources.getString(R.string.empty_rsn_error), Toast.LENGTH_SHORT);
            refreshLayout.setRefreshing(false);
            return false;
        }
        if (refreshPeriod >= Constants.REFRESH_COOLDOWN_MS) {
            refreshCount = 0;
        }
        if (refreshPeriod < Constants.REFRESH_COOLDOWN_MS && refreshCount >= Constants.MAX_REFRESH_COUNT) {
            double timeLeft = (Constants.REFRESH_COOLDOWN_MS - refreshPeriod) / 1000;
            showToast(resources.getString(R.string.wait_before_refresh, (int) Math.ceil(timeLeft + 0.5)), Toast.LENGTH_SHORT);
            refreshLayout.setRefreshing(false);
            return false;
        }
        activateRefreshCooldown();
        return true;
    }

    public void updateUser() {
        final String rsn = rsnEditText.getText().toString();
        defaultRsn = rsn;
        refreshLayout.setRefreshing(true);
        wasRequesting = true;
        Utils.getString(getHiscoresUrl(selectedHiscore) + rsn, HISCORES_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                hiscoresData = result;
                refreshLayout.setRefreshing(false);
                clearTables();
                handleHiscoresData(result);
                AppDb.getInstance(context).insertOrUpdateUserStats(new UserStats(rsn, result, selectedHiscore));
                view.findViewById(R.id.hiscores_data_layout).setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(VolleyError error) {
                refreshLayout.setRefreshing(false);
                clearTables();
                if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                    showToast(resources.getString(R.string.player_not_found), Toast.LENGTH_LONG);
                    AppDb.getInstance(context).insertOrUpdateUserStats(new UserStats(rsn, "", selectedHiscore));
                }
                else if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    UserStats cachedData = AppDb.getInstance(context).getUserStats(rsn, selectedHiscore);
                    if (cachedData == null) {
                        showToast(resources.getString(R.string.failed_to_obtain_data, "hiscore data", resources.getString(R.string.network_error)), Toast.LENGTH_LONG);
                        return;
                    }

                    showToast(resources.getString(R.string.using_cached_data, Utils.convertTime(cachedData.dateModified)), Toast.LENGTH_LONG);
                    view.findViewById(R.id.hiscores_data_layout).setVisibility(View.VISIBLE);
                    handleHiscoresData(cachedData.stats);
                }
                else
                    showToast(resources.getString(R.string.failed_to_obtain_data, "stats", error.getMessage()), Toast.LENGTH_LONG);
            }

            @Override
            public void always() {
                wasRequesting = false;
            }
        });
    }

    public void handleHiscoresData(String result) {
        PlayerStats playerStats = new PlayerStats(result);
        if (playerStats.isUnranked()) {
            showToast(resources.getString(R.string.player_not_found), Toast.LENGTH_LONG);
            return;
        }
        Combat combat = playerStats.getCombat();
        hiscoresTable.addView(createRow(-1, (int) combat.getLevel(), -1, playerStats.getCombatExp(), false));

        for (SkillType skillType : playerStats.keySet()) {
            Skill skill = playerStats.getSkill(skillType);
            if (skill.isMinigame()) {
                hiscoresMinigameTable.addView(createRow(skill.getId(), -1, skill.getRank(), skill.getScore(), true));
            }
            else if (skill.isOverall()) {
                hiscoresTable.addView(createRow(skill.getId(), playerStats.getTotalLevel(), skill.getRank(), playerStats.getTotalExp(), skill.isMinigame()));
            }
            else {
                hiscoresTable.addView(createRow(skill.getId(), skill.getLevel(), skill.getRank(), skill.getExp(), skill.isMinigame()));
            }
        }
    }

    private TableRow createRow(int skillId, int lvl, int rank, long exp, boolean isMinigameRow) {
        TableRow.LayoutParams imageParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f);
        imageParams.gravity = Gravity.CENTER;

        TableRow row = new TableRow(context);

        ImageView skillImageView = new ImageView(context);
        skillImageView.setImageDrawable(resources.getDrawable(RsUtils.getSkillResourceId(skillId)));
        skillImageView.setLayoutParams(imageParams);
        row.addView(skillImageView);
        if (!isMinigameRow) {
            TextView lvlTextView = new TextView(context);
            lvlTextView.setText(lvl < 0 ? "-" : String.valueOf(lvl));
            lvlTextView.setGravity(Gravity.CENTER);
            lvlTextView.setLayoutParams(rowParams);
            lvlTextView.setTextColor(context.getResources().getColor(R.color.text));
            row.addView(lvlTextView);
        }

        TextView rankTextView = new TextView(context);
        rankTextView.setText(rank < 0 ? "-" : Utils.formatNumber(rank));
        rankTextView.setGravity(Gravity.CENTER);
        rankTextView.setLayoutParams(rowParams);
        rankTextView.setTextColor(context.getResources().getColor(R.color.text));
        row.addView(rankTextView);

        TextView expTextView = new TextView(context);
        expTextView.setText(exp < 0 ? "-" : Utils.formatNumber(exp));
        expTextView.setGravity(Gravity.CENTER);
        expTextView.setLayoutParams(rowParams);
        expTextView.setTextColor(context.getResources().getColor(R.color.text));
        row.addView(expTextView);

        return row;
    }

    private void activateRefreshCooldown() {
        if (refreshCount == 0)
            lastRefreshTimeMs = System.currentTimeMillis();
        refreshCount++;
    }

    private void clearTables() {
        hiscoresTable.removeAllViews();
        hiscoresMinigameTable.removeAllViews();
    }

    @Override
    public void cancelVolleyRequests() {
        AppController.getInstance().cancelPendingRequests(HISCORES_REQUEST_TAG);
    }
}
