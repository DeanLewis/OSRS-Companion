package com.dennyy.osrscompanion.fragments;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.Toast;

import com.dennyy.osrscompanion.FloatingViewService;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.adapters.TileAdapter;
import com.dennyy.osrscompanion.customviews.CheckboxDialogPreference;
import com.dennyy.osrscompanion.enums.AppStart;
import com.dennyy.osrscompanion.fragments.calculators.CalculatorsFragment;
import com.dennyy.osrscompanion.fragments.hiscores.HiscoresFragment;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Logger;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.models.General.TileData;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
public class HomeFragment extends BaseTileFragment implements AdapterView.OnItemClickListener{
    private Switch mainSwitch;
    private long lastSwitchTimeMs;
    private SharedPreferences preferences;

    public HomeFragment() {
        super(2, 4);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.home_layout, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbarTitle.setText(getResources().getString(R.string.app_name));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        initializeTiles();
        initializeChangelog();
    }

    private void initializeChangelog() {
        if (checkAppStart() == AppStart.FIRST_TIME_VERSION) {
            try {
                Utils.showChangelogs(getActivity());
            }
            catch (Exception e) {
                Logger.log("showing changelogs from homefragment", e);
            }
        }
    }

    @Override
    public void onOptionsMenuCreated() {
        String currentPreferences = preferences.getString(Constants.PREF_FLOATING_VIEWS, null);
        if (Utils.isNullOrEmpty(currentPreferences)) {
            new MaterialShowcaseView.Builder(getActivity())
                    .setTarget(mainSwitch)
                    .setDismissOnTargetTouch(true)
                    .setMaskColour(Color.parseColor("#E6335075"))
                    .setDismissText(getResources().getString(R.string.got_it))
                    .setContentText(getResources().getString(R.string.first_start_info))
                    .setDelay(500)
                    .singleUse(Constants.FIRST_STARTUP)
                    .show();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getActivity() != null) {
            initializeTiles();
        }
    }

    @Override
    public void initializeTiles() {
        if (tiles.isEmpty()) {
            tiles.add(new TileData(getString(R.string.grandexchange), getDrawable(R.drawable.coins)));
            tiles.add(new TileData(getString(R.string.tracker), getDrawable(R.drawable.tracker)));
            tiles.add(new TileData(getString(R.string.hiscores), getDrawable(R.drawable.hiscores)));
            tiles.add(new TileData(getString(R.string.calculators), getDrawable(R.drawable.calculators)));
            tiles.add(new TileData(getString(R.string.treasure_trails), getDrawable(R.drawable.clue_scroll_clear)));
            tiles.add(new TileData(getString(R.string.notes), getDrawable(R.drawable.notes)));
            tiles.add(new TileData(getString(R.string.quest_guide), getDrawable(R.drawable.quest_icon)));
            tiles.add(new TileData(getString(R.string.fairy_rings), getDrawable(R.drawable.fairy_rings)));
            tiles.add(new TileData(getString(R.string.osrs_wiki), getDrawable(R.drawable.rswiki_logo)));
            tiles.add(new TileData(getString(R.string.rsnews), getDrawable(R.drawable.newspaper)));
            tiles.add(new TileData(getString(R.string.timers), getDrawable(R.drawable.stopwatch)));
            tiles.add(new TileData(getString(R.string.worldmap), getDrawable(R.drawable.worldmap)));
            tiles.add(new TileData(getString(R.string.settings), getDrawable(R.drawable.settings)));
        }

        GridView gridView = view.findViewById(R.id.home_grid_layout);
        gridView.setNumColumns(currentColumns);
        gridView.setOnItemClickListener(this);
        if (gridView.getAdapter() == null) {
            TileAdapter tileAdapter = new TileAdapter(getActivity(), tiles);
            gridView.setAdapter(tileAdapter);
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        final String selected = preferences.getString(Constants.PREF_FLOATING_VIEWS, "");
        mainSwitch = menu.findItem(R.id.myswitch).getActionView().findViewById(R.id.switchForActionBar);

        final Switch mainSwitch = menu.findItem(R.id.myswitch).getActionView().findViewById(R.id.switchForActionBar);
        mainSwitch.setChecked(Utils.isMyServiceRunning(getActivity(), FloatingViewService.class));
        mainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                boolean drawPermissionDisabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity());
                if (drawPermissionDisabled) {
                    final Intent drawIntent = new Intent(Constants.PERMISSION_ACTIVITY, Uri.parse("package:" + getActivity().getPackageName()));
                    String drawPermissionTitle = getResources().getString(R.string.draw_on_screen_permission_required);
                    showInfoDialog(drawPermissionTitle, getResources().getString(R.string.draw_dialog_info), getResources().getString(R.string.turn_on), false, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                startActivityForResult(drawIntent, Constants.CODE_DRAW_OVER_OTHER_APP_PERMISSION);
                            }
                            catch (ActivityNotFoundException ignored) {
                                startActivity(new Intent(Settings.ACTION_SETTINGS));
                            }
                        }
                    }, null);
                    mainSwitch.setChecked(false);
                }
                else if (System.currentTimeMillis() - lastSwitchTimeMs < 1000) {
                    showToast(getResources().getString(R.string.wait_for_service), Toast.LENGTH_LONG);
                    mainSwitch.setChecked(!isChecked);
                }
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity())) {
                    showToast(getResources().getString(R.string.draw_on_screen_permission_required), Toast.LENGTH_SHORT);
                    mainSwitch.setChecked(false);
                }
                else if (isChecked && selected.length() < 1) {
                    showToast(getResources().getString(R.string.enable_floating_views_first), Toast.LENGTH_LONG);
                    mainSwitch.setChecked(false);
                }
                else {
                    Intent intent = new Intent(getActivity(), FloatingViewService.class);
                    if (isChecked && !Utils.isMyServiceRunning(getActivity(), FloatingViewService.class)) {
                        int length = selected.split(CheckboxDialogPreference.DEFAULT_SEPARATOR).length;
                        if (length < 1) {
                            showToast(getResources().getString(R.string.no_floating_views_selected), Toast.LENGTH_SHORT);
                            mainSwitch.setChecked(false);
                            return;
                        }
                        showToast(getResources().getString(R.string.service_started), Toast.LENGTH_SHORT);
                        getActivity().startService(intent);
                    }
                    else {
                        showToast(getResources().getString(R.string.service_stopped), Toast.LENGTH_SHORT);
                        getActivity().stopService(intent);
                    }
                    lastSwitchTimeMs = System.currentTimeMillis();
                }
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            boolean drawPermissionDisabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity());
            if (drawPermissionDisabled) {
                showToast(getResources().getString(R.string.draw_permission_not_granted), Toast.LENGTH_SHORT);
            }
            else {
                showToast(getResources().getString(R.string.draw_permission_granted), Toast.LENGTH_SHORT);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (!isTransactionSafe()) {
            return;
        }
        TileData tileData = tiles.get(i);
        Fragment fragment = null;
        String tag = "";
        if (tileData.text.equals(getString(R.string.grandexchange))) {
            fragment = new GrandExchangeFragment();
        }
        else if (tileData.text.equals(getString(R.string.tracker))) {
            fragment = new TrackerFragment();
        }
        else if (tileData.text.equals(getString(R.string.hiscores))) {
            fragment = new HiscoresFragment();
        }
        else if (tileData.text.equals(getString(R.string.calculators))) {
            fragment = new CalculatorsFragment();
        }
        else if (tileData.text.equals(getString(R.string.treasure_trails))) {
            fragment = new TreasureTrailFragment();
        }
        else if (tileData.text.equals(getString(R.string.notes))) {
            fragment = new NotesFragment();
        }
        else if (tileData.text.equals(getString(R.string.settings))) {
            fragment = new UserPreferenceFragment();
        }
        else if (tileData.text.equals(getString(R.string.quest_guide))) {
            fragment = new QuestFragment();
        }
        else if (tileData.text.equals(getString(R.string.fairy_rings))) {
            fragment = new FairyRingFragment();
        }
        else if (tileData.text.equals(getString(R.string.osrs_wiki))) {
            fragment = new RSWikiFragment();
        }
        else if (tileData.text.equals(getString(R.string.rsnews))) {
            fragment = new OSRSNewsFragment();
        }
        else if (tileData.text.equals(getString(R.string.timers))) {
            fragment = new TimersFragment();
        }
        else if (tileData.text.equals(getString(R.string.worldmap))) {
            fragment = new WorldmapFragment();
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    private AppStart checkAppStart() {
        AppStart appStart = AppStart.NORMAL;
        try {
            String packageName = getActivity().getPackageName();
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(packageName, 0);
            int previousVersionCode = preferences.getInt(Constants.PREVIOUS_APP_VERSION, -1);
            int currentVersionCode = pInfo.versionCode;
            appStart = checkAppStart(currentVersionCode, previousVersionCode);
            preferences.edit().putInt(Constants.PREVIOUS_APP_VERSION, currentVersionCode).apply();
        }
        catch (PackageManager.NameNotFoundException e) {
            Logger.log(e);
        }
        return appStart;
    }

    private AppStart checkAppStart(int currentVersionCode, int previousVersionCode) {
        if (previousVersionCode == -1) {
            return AppStart.FIRST_TIME;
        }
        else if (previousVersionCode < currentVersionCode) {
            return AppStart.FIRST_TIME_VERSION;
        }
        else {
            return AppStart.NORMAL;
        }
    }

}
