package com.dennyy.osrscompanion.viewhandlers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.NothingSelectedSpinnerAdapter;
import com.dennyy.osrscompanion.adapters.QuestSelectorSpinnerAdapter;
import com.dennyy.osrscompanion.adapters.QuestSourceSpinnerAdapter;
import com.dennyy.osrscompanion.asynctasks.QuestLoadTask;
import com.dennyy.osrscompanion.customviews.ObservableWebView;
import com.dennyy.osrscompanion.enums.QuestSource;
import com.dennyy.osrscompanion.helpers.AdBlocker;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.interfaces.QuestsLoadedListener;
import com.dennyy.osrscompanion.interfaces.WebViewScrollListener;
import com.dennyy.osrscompanion.models.General.Quest;

import java.util.ArrayList;
import java.util.Arrays;

import im.delight.android.webview.AdvancedWebView;

public class QuestViewHandler extends BaseViewHandler implements AdvancedWebView.Listener, AdapterView.OnItemSelectedListener, View.OnClickListener, QuestsLoadedListener, WebViewScrollListener {

    public ObservableWebView webView;
    public int selectedQuestIndex = -1;
    public Spinner questSelectorSpinner;

    private QuestSource selectedQuestSource;
    private ProgressBar progressBar;
    private ArrayList<Quest> quests;
    private ArrayList<QuestSource> questSources;
    private boolean clearHistory;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private QuestsLoadedListener questsLoadedListener;
    private RelativeLayout questSelectorContainer;

    public QuestViewHandler(final Context context, View view, boolean isFloatingView, QuestsLoadedListener questsLoadedListener) {
        super(context, view);

        selectedQuestSource = QuestSource.fromName(PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREF_QUEST_SOURCE, QuestSource.RSWIKI.getName()));
        webView = view.findViewById(R.id.webview);
        webView.setWebViewScrollListener(this);
        questSelectorContainer = view.findViewById(R.id.quest_selector_container);
        progressBar = view.findViewById(R.id.progressBar);
        questSelectorSpinner = view.findViewById(R.id.quest_selector_spinner);
        questSelectorSpinner.setOnItemSelectedListener(this);
        if (isFloatingView) {
            Button backButton = view.findViewById(R.id.navigate_back_button);
            backButton.setVisibility(View.VISIBLE);
            backButton.setOnClickListener(this);
        }
        this.questsLoadedListener = questsLoadedListener;
        new QuestLoadTask(context, this).execute();
        initWebView();
        initQuestSourceSpinner();
    }

    private void initQuestSourceSpinner() {
        Spinner questSourceSpinner = view.findViewById(R.id.quest_source_spinner);
        questSourceSpinner.setOnItemSelectedListener(this);
        questSources = new ArrayList<>(Arrays.asList(QuestSource.values()));
        questSourceSpinner.setAdapter(new QuestSourceSpinnerAdapter(context, questSources));
        questSourceSpinner.setSelection(questSources.indexOf(selectedQuestSource));
    }

    private void initWebView() {
        webView.addPermittedHostname("oldschool.runescape.wiki");
        webView.addPermittedHostname("runehq.com");
        webView.setThirdPartyCookiesEnabled(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setMixedContentAllowed(false);
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        }
        webView.setListener(activity, this);
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }

        });
        webView.setWebViewClient(AdBlocker.getWebViewClient());
    }

    @Override
    public void onQuestsLoaded(ArrayList<Quest> loadedQuests) {
        quests = new ArrayList<>(loadedQuests);
        QuestSelectorSpinnerAdapter questSelectorSpinnerAdapter = new QuestSelectorSpinnerAdapter(context, quests);
        questSelectorSpinner.setAdapter(new NothingSelectedSpinnerAdapter(questSelectorSpinnerAdapter, getString(R.string.select_a_quest), context));
        if (questsLoadedListener != null) {
            questsLoadedListener.onQuestsLoaded(null);
        }
    }

    @Override
    public void onQuestsLoadError() {
        showToast(getString(R.string.failed_to_load_quests), Toast.LENGTH_SHORT);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.navigate_back_button) {
            if (webView.canGoBack()) {
                webView.goBack();
            }
        }
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        progressBar.setVisibility(View.VISIBLE);
        pushWebViewDown();
    }

    @Override
    public void onPageFinished(String url) {
        handler.removeCallbacks(runnable);
        runnable = new Runnable() {
            @Override
            public void run() {
                handlePageTimerFinished();
            }
        };
        handler.postDelayed(runnable, 1500);
    }

    private void handlePageTimerFinished() {
        wasRequesting = false;
        progressBar.setProgress(progressBar.getMax());
        webView.setVisibility(View.VISIBLE);
        if (clearHistory) {
            clearHistory = false;
            webView.clearHistory();
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        }, 250);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        showToast(getString(R.string.unexpected_error, description), Toast.LENGTH_SHORT);
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

    }

    @Override
    public void onExternalPageRequest(String url) {
        showToast(getString(R.string.external_navigation_prohibited, "the guides"), Toast.LENGTH_SHORT);
    }

    public void restoreWebView(Bundle webViewState) {
        if (webView != null) {
            webView.restoreState(webViewState);
        }
    }

    @Override
    public boolean wasRequesting() {
        return wasRequesting;
    }

    @Override
    public void cancelRunningTasks() {
        handler.removeCallbacks(runnable);
        Utils.clearWebView(webView);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        if (adapterView.getId() == R.id.quest_selector_spinner) {
            pos = pos - 1;
            if (pos < 0) {
                return;
            }
            selectedQuestIndex = pos;
            selectQuest();
        }
        else if (adapterView.getId() == R.id.quest_source_spinner) {
            QuestSource questSource = questSources.get(pos);
            if (selectedQuestSource == questSource || quests == null) {
                return;
            }
            selectedQuestSource = questSource;
            if (selectedQuestIndex > -1) {
                selectQuest();
            }
        }
    }

    private void selectQuest() {
        Quest quest = quests.get(selectedQuestIndex);
        clearHistory();
        switch (selectedQuestSource) {
            case RSWIKI:
                loadQuestUrl(quest.url);
                break;
            case RUNEHQ:
                if (quest.hasRuneHqUrl()) {
                    loadQuestUrl(quest.runeHqUrl);
                }
                else {
                    showToast(getString(R.string.no_runehq_guide, quest.name), Toast.LENGTH_LONG);
                }
                break;
        }
        wasRequesting = true;
    }

    private void loadQuestUrl(String url) {
        webView.loadUrl(url);
        webView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void clearHistory() {
        webView.clearHistory();
        clearHistory = true;
    }

    @Override
    public void onWebViewScrollDown(ObservableWebView observableWebView, int y, int oldY) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) questSelectorContainer.getLayoutParams();
        int height = questSelectorContainer.getHeight() + params.bottomMargin + params.topMargin;
        questSelectorContainer.animate().translationY(-height).setInterpolator(new AccelerateInterpolator(2));
        view.findViewById(R.id.webview_container).animate().translationY(0).setInterpolator(new AccelerateInterpolator(2));
    }

    @Override
    public void onWebViewScrollUp(ObservableWebView observableWebView, int y, int oldY) {
        questSelectorContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        pushWebViewDown();
    }

    private void pushWebViewDown() {
        int height = questSelectorContainer.getHeight();
        view.findViewById(R.id.webview_container).animate().translationY(height).setInterpolator(new AccelerateInterpolator(2));
    }
}
