package com.dennyy.osrscompanion.asynctasks;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.dennyy.osrscompanion.R;
import com.dennyy.osrscompanion.helpers.Constants;

import java.lang.ref.WeakReference;

import static android.content.Context.DOWNLOAD_SERVICE;

public class WorldmapDownloadTask extends AsyncTask<Void, Void, Boolean> {
    private WeakReference<Context> weakContext;

    public WorldmapDownloadTask(final Context context) {
        this.weakContext = new WeakReference<>(context);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Context context = weakContext.get();
        if (context == null) {
            return false;
        }
        Uri url = Uri.parse("https://cdn.runescape.com/assets/img/external/oldschool/web/osrs_world_map_sept06_2018.png");
        DownloadManager.Request request = new DownloadManager.Request(url);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(context.getResources().getString(R.string.downloading_worldmap));
        request.setVisibleInDownloadsUi(false);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS, Constants.WORLDMAP_FILE_PATH);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putLong(Constants.WORLDMAP_DOWNLOAD_KEY, downloadManager.enqueue(request));
            editor.apply();
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean successful) {

    }
}
