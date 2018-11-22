package com.dennyy.osrscompanion.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.dennyy.osrscompanion.AppController;
import com.dennyy.osrscompanion.BuildConfig;
import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.asynctasks.UpdateItemIdListTask;
import com.dennyy.osrscompanion.customviews.CheckboxDialogPreference;
import com.dennyy.osrscompanion.customviews.SeekBarPreference;
import com.dennyy.osrscompanion.helpers.Constants;
import com.dennyy.osrscompanion.helpers.Logger;
import com.dennyy.osrscompanion.helpers.Utils;
import com.dennyy.osrscompanion.interfaces.ItemIdListResultListener;
import com.dennyy.osrscompanion.interfaces.SeekBarPreferenceListener;

import java.util.Arrays;


public class UserPreferenceFragment extends PreferenceFragment implements CheckboxDialogPreference.DialogClosedListener, Preference.OnPreferenceClickListener, SeekBarPreferenceListener {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Toast toast;
    private CharSequence[] currentPrefs;
    private static final String ITEMIDLIST_REQUEST_TAG = "item_id_list_request";
    private static final String LAST_UPDATE_TIME_KEY = "last_item_id_list_update_time";

    public UserPreferenceFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pref_main, container, false);
        ((TextView) view.findViewById(R.id.version_info)).setText(getActivity().getResources().getString(R.string.version_string, BuildConfig.VERSION_NAME));
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_system_settings:
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                catch (ActivityNotFoundException ignored) {
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private String[] getPreferenceKeys() {
        String[] prefs = new String[]{
                Constants.PREF_LANDSCAPE_ONLY,
                Constants.PREF_FULLSCREEN_ONLY,
                Constants.PREF_PADDING_SIDE,
                Constants.PREF_FLOATING_VIEWS,
                Constants.PREF_FEEDBACK,
                Constants.PREF_VIEW_IN_STORE,
                Constants.PREF_VIEW_OTHER_APPS,
                Constants.PREF_SHOW_LIBRARIES,
                Constants.PREF_DOWNLOAD_ITEMIDLIST,
                Constants.PREF_QUEST_SOURCE,
                Constants.PREF_VERSION };
        return prefs;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TextView toolbar = getActivity().findViewById(R.id.toolbar_title);
        toolbar.setTextColor(getResources().getColor(R.color.text));
        toolbar.setText(getResources().getString(R.string.settings));
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = preferences.edit();

        ((CheckboxDialogPreference) findPreference(Constants.PREF_FLOATING_VIEWS)).addListener(this);
        String[] prefs = getPreferenceKeys();
        for (String pref : prefs) {
            findPreference(pref).setOnPreferenceClickListener(this);
        }
        String[] seekBarPref = new String[]{ Constants.PREF_OPACITY, Constants.PREF_SIZE, Constants.PREF_PADDING };
        for (String pref : seekBarPref) {
            ((SeekBarPreference) findPreference(pref)).setListener(this);
        }
        findPreference(Constants.PREF_VERSION).setTitle(getResources().getString(R.string.version_string, BuildConfig.VERSION_NAME));
    }

    @Override
    public void onResume() {
        super.onResume();
        ((CheckBoxPreference) findPreference(Constants.PREF_RIGHT_SIDE)).setChecked(preferences.getBoolean(Constants.PREF_RIGHT_SIDE, true));
        currentPrefs = new CharSequence[]{};
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onClose(CheckboxDialogPreference self, String selection) {
        self.setValueAndEvent(selection);
        String[] newSelections = selection.split(CheckboxDialogPreference.DEFAULT_SEPARATOR);
        Arrays.sort(newSelections);
        Arrays.sort(currentPrefs);
        if (!Arrays.equals(newSelections, currentPrefs)) {
            showToast(getResources().getString(R.string.restart_to_take_effect), Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        AppController.getInstance().cancelPendingRequests(ITEMIDLIST_REQUEST_TAG);
    }

    public void updateItemIdList() {
        showToast(getResources().getString(R.string.checking_for_updated_ge_items), Toast.LENGTH_LONG);
        Utils.getString(Constants.ITEMIDLIST_URL, ITEMIDLIST_REQUEST_TAG, new Utils.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                new UpdateItemIdListTask(getActivity(), result, new ItemIdListResultListener() {
                    @Override
                    public void onItemsUpdated() {
                        showToast(getResources().getString(R.string.updated_list_of_items), Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onItemsNotUpdated() {
                        showToast(getResources().getString(R.string.items_up_to_date), Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onError() {
                        showToast(getResources().getString(R.string.error_please_try_again), Toast.LENGTH_LONG);
                    }
                }).execute();
            }

            @Override
            public void onError(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    showToast(getResources().getString(R.string.failed_to_obtain_data, "updated items", getResources().getString(R.string.network_error)), Toast.LENGTH_LONG);
                    return;
                }
                showToast(getResources().getString(R.string.failed_to_obtain_data, "updated items", error.getMessage()), Toast.LENGTH_LONG);
            }

            @Override
            public void always() {
                editor.putLong(LAST_UPDATE_TIME_KEY, System.currentTimeMillis());
                editor.apply();
            }
        });
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case Constants.PREF_FLOATING_VIEWS:
                currentPrefs = ((CheckboxDialogPreference) findPreference(Constants.PREF_FLOATING_VIEWS)).getCheckedValues();
                break;
            case Constants.PREF_DOWNLOAD_ITEMIDLIST:
                long refreshPeriod = System.currentTimeMillis() - preferences.getLong(LAST_UPDATE_TIME_KEY, 0);
                if (refreshPeriod < Constants.REFRESH_COOLDOWN_LONG_MS) {
                    double timeLeft = (Constants.REFRESH_COOLDOWN_LONG_MS - refreshPeriod) / 1000;
                    showToast(getResources().getString(R.string.wait_before_refresh, (int) Math.ceil(timeLeft + 0.5)), Toast.LENGTH_SHORT);
                }
                else {
                    updateItemIdList();
                }
                break;
            case Constants.PREF_FULLSCREEN_ONLY:
            case Constants.PREF_LANDSCAPE_ONLY:
                showToast(getString(R.string.rotate_to_take_effect), Toast.LENGTH_SHORT);
                break;
            case Constants.PREF_FEEDBACK:
                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                /* Fill it with Data */
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ "info@dennyy.com" });
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);

                /* Send it off to the Activity-Chooser */
                getActivity().startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.choose_mail_app)));
                break;
            case Constants.PREF_VIEW_IN_STORE:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)));
                }
                catch (ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
                }
                break;
            case Constants.PREF_VIEW_OTHER_APPS:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Dennyy")));
                }
                catch (ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Dennyy")));
                }
                break;
            case Constants.PREF_SHOW_LIBRARIES:
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment fragment = new LibrariesFragment();

                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case Constants.PREF_PADDING_SIDE:
                showToast(getResources().getString(R.string.restart_to_take_effect), Toast.LENGTH_LONG);
                break;
            case Constants.PREF_VERSION:
                try {
                    Utils.showChangelogs(getActivity());
                }
                catch (Exception e) {
                    Logger.log("showing changelogs from preferencefragment", e);
                    showToast(getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT);
                }
                break;
        }
        return false;
    }

    @Override
    public void onSeekBarValueSet(SeekBarPreference preference, String key, int value) {
        showToast(getResources().getString(R.string.restart_to_take_effect), Toast.LENGTH_LONG);
    }

    @Override
    public void onSeekBarCancel(SeekBarPreference preference, String key) {

    }

    protected void showToast(String message, int duration) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(getActivity(), message, duration);
        toast.show();
    }

}
