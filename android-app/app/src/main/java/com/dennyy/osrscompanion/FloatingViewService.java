package com.dennyy.osrscompanion;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dennyy.osrscompanion.enums.ReloadTimerSource;
import com.dennyy.osrscompanion.helpers.AdBlocker;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Logger;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.Notes.NoteChangeEvent;
import com.dennyy.osrscompanion.models.Timers.ReloadTimersEvent;
import com.dennyy.osrscompanion.models.TodoList.ReloadTodoListEvent;
import com.dennyy.osrscompanion.models.Worldmap.WorldmapDownloadedEvent;
import com.dennyy.osrscompanion.viewhandlers.BestiaryViewHandler;
import com.dennyy.osrscompanion.viewhandlers.CalculatorViewHandler;
import com.dennyy.osrscompanion.viewhandlers.CombatCalculatorViewHandler;
import com.dennyy.osrscompanion.viewhandlers.DiaryCalculatorViewHandler;
import com.dennyy.osrscompanion.viewhandlers.ExpCalculatorViewHandler;
import com.dennyy.osrscompanion.viewhandlers.FairyRingViewHandler;
import com.dennyy.osrscompanion.viewhandlers.GrandExchangeViewHandler;
import com.dennyy.osrscompanion.viewhandlers.HiscoresCompareViewHandler;
import com.dennyy.osrscompanion.viewhandlers.HiscoresLookupViewHandler;
import com.dennyy.osrscompanion.viewhandlers.NotesViewHandler;
import com.dennyy.osrscompanion.viewhandlers.OSRSNewsViewHandler;
import com.dennyy.osrscompanion.viewhandlers.QuestViewHandler;
import com.dennyy.osrscompanion.viewhandlers.RSWikiViewHandler;
import com.dennyy.osrscompanion.viewhandlers.SkillCalculatorViewHandler;
import com.dennyy.osrscompanion.viewhandlers.TimersViewHandler;
import com.dennyy.osrscompanion.viewhandlers.TodoViewHandler;
import com.dennyy.osrscompanion.viewhandlers.TrackerViewHandler;
import com.dennyy.osrscompanion.viewhandlers.TreasureTrailViewHandler;
import com.dennyy.osrscompanion.viewhandlers.WorldmapViewHandler;
import com.flipkart.chatheads.ChatHead;
import com.flipkart.chatheads.arrangement.ChatHeadArrangement;
import com.flipkart.chatheads.arrangement.MinimizedArrangement;
import com.flipkart.chatheads.config.FloatingViewPreferences;
import com.flipkart.chatheads.container.DefaultChatHeadManager;
import com.flipkart.chatheads.container.WindowManagerContainer;
import com.flipkart.chatheads.interfaces.ChatHeadViewAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class FloatingViewService extends Service implements WindowManagerContainer.ArrangementChangeListener {
    private final static int NOTIFICATION_ID = 1337;
    private final static String calcHeadName = CalculatorViewHandler.class.getSimpleName();
    private final static String geHeadName = GrandExchangeViewHandler.class.getSimpleName();
    private final static String trackerHeadName = TrackerViewHandler.class.getSimpleName();
    private final static String hiscoreLookupHeadName = HiscoresLookupViewHandler.class.getSimpleName();
    private final static String hiscoreCompareHeadName = HiscoresCompareViewHandler.class.getSimpleName();
    private final static String treasureTrailHeadName = TreasureTrailViewHandler.class.getSimpleName();
    private final static String notesHeadName = NotesViewHandler.class.getSimpleName();
    private final static String combatCalculatorHeadName = CombatCalculatorViewHandler.class.getSimpleName();
    private final static String expListHeadName = ExpCalculatorViewHandler.class.getSimpleName();
    private final static String skillCalcHeadName = SkillCalculatorViewHandler.class.getSimpleName();
    private final static String questHeadName = QuestViewHandler.class.getSimpleName();
    private final static String fairyRingHeadName = FairyRingViewHandler.class.getSimpleName();
    private final static String diaryCalcHeadName = DiaryCalculatorViewHandler.class.getSimpleName();
    private final static String rswikiHeadName = RSWikiViewHandler.class.getSimpleName();
    private final static String rsnewsHeadName = OSRSNewsViewHandler.class.getSimpleName();
    private final static String timersHeadName = TimersViewHandler.class.getSimpleName();
    private final static String worldmapHeadName = WorldmapViewHandler.class.getSimpleName();
    private final static String todoHeadName = TodoViewHandler.class.getSimpleName();
    private final static String bestiaryHeadName = BestiaryViewHandler.class.getSimpleName();

    private DefaultChatHeadManager chatHeadManager;
    private WindowManagerContainer windowManagerContainer;
    private Map<String, View> viewCache = new HashMap<>();
    private Map<String, Integer> iconsMap = new HashMap<>();
    private Map<String, String> namesMap = new HashMap<>();

    private NotesViewHandler notesViewHandler;
    private TimersViewHandler timersViewHandler;
    private WorldmapViewHandler worldmapViewHandler;
    private TodoViewHandler todoViewHandler;

    public FloatingViewService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        initIconsMap();
        initNamesMap();
        if (namesMap.size() != iconsMap.size()) {
            throw new IllegalStateException("Names map or icons map is missing something to initialize the floating views");
        }
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FloatingViewService.this);
        EventBus.getDefault().register(this);
        AdBlocker.init(this);
        windowManagerContainer = new WindowManagerContainer(this);
        windowManagerContainer.setListener(this);
        chatHeadManager = new DefaultChatHeadManager(this, windowManagerContainer, getFloatingViewPreferences(preferences));
        chatHeadManager.setArrangement(MinimizedArrangement.class, null);
        chatHeadManager.setViewAdapter(new ChatHeadViewAdapter() {
            @Override
            public View attachView(String key, ChatHead chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView == null) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    if (key.equals(calcHeadName)) {
                        cachedView = inflater.inflate(R.layout.calculator_layout, parent, false);
                        new CalculatorViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(geHeadName)) {
                        cachedView = inflater.inflate(R.layout.grand_exchange_layout, parent, false);
                        new GrandExchangeViewHandler(FloatingViewService.this, cachedView, true, null);
                    }
                    else if (key.equals(hiscoreLookupHeadName)) {
                        cachedView = inflater.inflate(R.layout.hiscores_lookup_layout, parent, false);
                        new HiscoresLookupViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(hiscoreCompareHeadName)) {
                        cachedView = inflater.inflate(R.layout.hiscores_compare_layout, parent, false);
                        new HiscoresCompareViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(trackerHeadName)) {
                        cachedView = inflater.inflate(R.layout.tracker_layout, parent, false);
                        new TrackerViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(treasureTrailHeadName)) {
                        cachedView = inflater.inflate(R.layout.treasure_trails_layout, parent, false);
                        new TreasureTrailViewHandler(FloatingViewService.this, cachedView, true, null);
                    }
                    else if (key.equals(notesHeadName)) {
                        cachedView = inflater.inflate(R.layout.notes_layout, parent, false);
                        notesViewHandler = new NotesViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(combatCalculatorHeadName)) {
                        cachedView = inflater.inflate(R.layout.combat_calculator_layout, parent, false);
                        new CombatCalculatorViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(expListHeadName)) {
                        cachedView = inflater.inflate(R.layout.exp_calc_layout, parent, false);
                        new ExpCalculatorViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(skillCalcHeadName)) {
                        cachedView = inflater.inflate(R.layout.skill_calculator_layout, parent, false);
                        new SkillCalculatorViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(questHeadName)) {
                        cachedView = inflater.inflate(R.layout.quest_layout, parent, false);
                        new QuestViewHandler(FloatingViewService.this, cachedView, true, null);
                    }
                    else if (key.equals(fairyRingHeadName)) {
                        cachedView = inflater.inflate(R.layout.fairy_ring_layout, parent, false);
                        new FairyRingViewHandler(FloatingViewService.this, cachedView);
                    }
                    else if (key.equals(diaryCalcHeadName)) {
                        cachedView = inflater.inflate(R.layout.diary_calculator_layout, parent, false);
                        new DiaryCalculatorViewHandler(FloatingViewService.this, cachedView, null);
                    }
                    else if (key.equals(rswikiHeadName)) {
                        cachedView = inflater.inflate(R.layout.rswiki_layout, parent, false);
                        new RSWikiViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(rsnewsHeadName)) {
                        cachedView = inflater.inflate(R.layout.rsnews_layout, parent, false);
                        new OSRSNewsViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(timersHeadName)) {
                        cachedView = inflater.inflate(R.layout.timers_layout, parent, false);
                        timersViewHandler = new TimersViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(worldmapHeadName)) {
                        cachedView = inflater.inflate(R.layout.worldmap_layout, parent, false);
                        worldmapViewHandler = new WorldmapViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(todoHeadName)) {
                        cachedView = inflater.inflate(R.layout.todo_layout, parent, false);
                        todoViewHandler = new TodoViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    else if (key.equals(bestiaryHeadName)) {
                        cachedView = inflater.inflate(R.layout.bestiary_layout, parent, false);
                        new BestiaryViewHandler(FloatingViewService.this, cachedView, true);
                    }
                    viewCache.put(key, cachedView);
                }
                parent.addView(cachedView);
                return cachedView;
            }

            @Override
            public void detachView(String key, ChatHead chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView != null) {
                    parent.removeView(cachedView);
                }
            }

            @Override
            public void removeView(String key, ChatHead chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView != null) {
                    viewCache.remove(key);
                    parent.removeView(cachedView);
                }
            }

            @Override
            public Drawable getChatHeadDrawable(String key) {
                Integer resourceId = iconsMap.get(key);
                if (resourceId == null) {
                    resourceId = R.drawable.default_floating_view;
                    Logger.log(new NoSuchElementException("no chathead drawable found for key " + key));
                }
                Drawable drawable = getResources().getDrawable(resourceId);
                return drawable;

            }
        });

        String[] selected = preferences.getString(Constants.PREF_FLOATING_VIEWS, "").split("~");
        for (String selection : selected) {
            chatHeadManager.addChatHead(namesMap.get(selection), false);
        }

        chatHeadManager.setFullscreenChangeListener(new DefaultChatHeadManager.FullscreenChangeListener() {
            @Override
            public void onEnterFullscreen() {
                boolean landScapeOnly = preferences.getBoolean(Constants.PREF_LANDSCAPE_ONLY, false);

                if (landScapeOnly && windowManagerContainer.getOrientation() != Configuration.ORIENTATION_LANDSCAPE) {
                    chatHeadManager.hideAllChatHeads();
                }
                else {
                    chatHeadManager.showAllChatHeads();
                }
            }

            @Override
            public void onExitFullscreen() {
                boolean landScapeOnly = preferences.getBoolean(Constants.PREF_LANDSCAPE_ONLY, false);
                boolean fullscreenOnly = preferences.getBoolean(Constants.PREF_FULLSCREEN_ONLY, false);

                if ((landScapeOnly && windowManagerContainer.getOrientation() != Configuration.ORIENTATION_LANDSCAPE) || fullscreenOnly) {
                    chatHeadManager.hideAllChatHeads();
                }
                else {
                    chatHeadManager.showAllChatHeads();
                }
            }
        });

        runAsForeground();
    }

    private void runAsForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, BuildConfig.APPLICATION_ID)
                .setSmallIcon(R.drawable.persistent_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.osrscompanion4))
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.floating_view_service_running))
                .setContentIntent(pendingIntent);

        startForeground(NOTIFICATION_ID, builder.build());
    }

    @Subscribe
    public void onNoteChangeEvent(NoteChangeEvent event) {
        if (notesViewHandler != null && !event.isFloatingView) {
            notesViewHandler.setNote(event.note);
        }
    }

    @Subscribe
    public void reloadTimers(ReloadTimersEvent event) {
        if (timersViewHandler != null && event.source != ReloadTimerSource.FLOATINTG_VIEW) {
            timersViewHandler.reloadTimers();
        }
    }

    @Subscribe
    public void onWorldmapDownloaded(WorldmapDownloadedEvent event) {
        if (worldmapViewHandler != null) {
            worldmapViewHandler.loadWorldmap(null);
        }
    }

    @Subscribe
    public void reloadTodoList(ReloadTodoListEvent event) {
        if (todoViewHandler != null && (!event.isFloatingView || event.forceReload)) {
            todoViewHandler.reloadTodoList();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(windowManagerContainer.getReceiver());
        windowManagerContainer.destroy();
        stopForeground(true);
        stopSelf();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onArrangementChanged(ChatHeadArrangement arrangement) {

    }

    private FloatingViewPreferences getFloatingViewPreferences(SharedPreferences preferences) {
        float inactiveAlpha = 0.2f + (preferences.getInt(Constants.PREF_OPACITY, 3) * 0.1f);
        boolean startRightSide = preferences.getBoolean(Constants.PREF_RIGHT_SIDE, false);
        boolean alignFloatingViewsLeft = preferences.getBoolean(Constants.PREF_PADDING_SIDE, true);
        int alignmentMargin = preferences.getInt(Constants.PREF_PADDING, 0) * 5;
        alignmentMargin = (int) Utils.convertDpToPixel(alignmentMargin, FloatingViewService.this);
        int sizeDp = 10 + (preferences.getInt(Constants.PREF_SIZE, 8) * 5);
        FloatingViewPreferences floatingViewPreferences = new FloatingViewPreferences(startRightSide, alignFloatingViewsLeft, alignmentMargin, inactiveAlpha, namesMap.size(), sizeDp);
        return floatingViewPreferences;
    }

    private void initIconsMap() {
        iconsMap.put(calcHeadName, R.drawable.calculator_floating_view);
        iconsMap.put(geHeadName, R.drawable.ge_floating_view);
        iconsMap.put(hiscoreLookupHeadName, R.drawable.hiscore_lookup_floating_view);
        iconsMap.put(hiscoreCompareHeadName, R.drawable.hiscore_compare_floating_view);
        iconsMap.put(trackerHeadName, R.drawable.tracker_floating_view);
        iconsMap.put(treasureTrailHeadName, R.drawable.treasure_trails_floating_view);
        iconsMap.put(notesHeadName, R.drawable.notes_floating_view);
        iconsMap.put(combatCalculatorHeadName, R.drawable.cmb_calc_floating_view);
        iconsMap.put(expListHeadName, R.drawable.exp_list_floating_view);
        iconsMap.put(skillCalcHeadName, R.drawable.skill_calc_floating_view);
        iconsMap.put(questHeadName, R.drawable.quest_guide_floating_view);
        iconsMap.put(fairyRingHeadName, R.drawable.fairy_ring_floating_view);
        iconsMap.put(diaryCalcHeadName, R.drawable.diary_calc_floating_view);
        iconsMap.put(rswikiHeadName, R.drawable.rswiki_floating_view);
        iconsMap.put(rsnewsHeadName, R.drawable.rsnews_floating_view);
        iconsMap.put(timersHeadName, R.drawable.timers_floating_view);
        iconsMap.put(worldmapHeadName, R.drawable.worldmap_floating_view);
        iconsMap.put(todoHeadName, R.drawable.todo_floating_view);
        iconsMap.put(bestiaryHeadName, R.drawable.bestiary_floating_view);
    }

    private void initNamesMap() {
        namesMap.put("ge", geHeadName);
        namesMap.put("tracker", trackerHeadName);
        namesMap.put("hiscores_lookup", hiscoreLookupHeadName);
        namesMap.put("hiscores_compare", hiscoreCompareHeadName);
        namesMap.put("math_calc", calcHeadName);
        namesMap.put("treasuretrails", treasureTrailHeadName);
        namesMap.put("notes", notesHeadName);
        namesMap.put("cmb_calc", combatCalculatorHeadName);
        namesMap.put("exp_calc", expListHeadName);
        namesMap.put("skill_calc", skillCalcHeadName);
        namesMap.put("quest_guide", questHeadName);
        namesMap.put("fairy_ring", fairyRingHeadName);
        namesMap.put("diary_calc", diaryCalcHeadName);
        namesMap.put("osrs_wiki", rswikiHeadName);
        namesMap.put("osrs_news", rsnewsHeadName);
        namesMap.put("timers", timersHeadName);
        namesMap.put("worldmap", worldmapHeadName);
        namesMap.put("todo_list", todoHeadName);
        namesMap.put("bestiary", bestiaryHeadName);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}