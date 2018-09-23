package com.dennyy.osrscompanion.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dennyy.osrscompanion.enums.HiscoreType;
import com.dennyy.osrscompanion.enums.TrackDurationType;
import com.dennyy.osrscompanion.models.GrandExchange.GrandExchangeData;
import com.dennyy.osrscompanion.models.GrandExchange.GrandExchangeGraphData;
import com.dennyy.osrscompanion.models.GrandExchange.GrandExchangeUpdateData;
import com.dennyy.osrscompanion.models.Hiscores.UserStats;
import com.dennyy.osrscompanion.models.OSBuddy.OSBuddySummaryDTO;
import com.dennyy.osrscompanion.models.OSRSNews.OSRSNewsDTO;
import com.dennyy.osrscompanion.models.Tracker.TrackData;

public class AppDb extends SQLiteOpenHelper {
    private static AppDb instance;

    public static synchronized AppDb getInstance(Context context) {
        if (instance == null) {
            instance = new AppDb(context.getApplicationContext());
        }
        return instance;
    }

    private AppDb(Context context) {
        super(context, DB.name, null, DB.version);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // run when the database file did not exist and was just created
        String createUserStatsTable = "CREATE TABLE " + DB.UserStats.tableName + " (" +
                DB.UserStats.id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DB.UserStats.rsn + " TEXT NOT NULL COLLATE NOCASE, " +
                DB.UserStats.stats + " TEXT, " +
                DB.UserStats.hiscoreType + " INTEGER NOT NULL, " +
                DB.UserStats.dateModified + " INTEGER NOT NULL);";
        String createTrackTable = "CREATE TABLE " + DB.Track.tableName + " (" +
                DB.Track.id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DB.Track.rsn + " TEXT NOT NULL COLLATE NOCASE, " +
                DB.Track.data + " TEXT, " +
                DB.Track.durationType + " INTEGER NOT NULL, " +
                DB.Track.dateModified + " INTEGER NOT NULL);";
        String createGrandExchangeTable = "CREATE TABLE " + DB.GrandExchange.tableName + " (" +
                DB.GrandExchange.itemId + " INTEGER PRIMARY KEY, " +
                DB.GrandExchange.data + " TEXT, " +
                DB.GrandExchange.dateModified + " INTEGER NOT NULL);";
        String createGrandExchangeUpdateTable = "CREATE TABLE " + DB.GrandExchangeUpdate.tableName + " (" +
                DB.GrandExchangeUpdate.id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DB.GrandExchangeUpdate.data + " TEXT NOT NULL, " +
                DB.GrandExchangeUpdate.dateModified + " INTEGER NOT NULL);";
        String createGrandExchangeGraphTable = "CREATE TABLE " + DB.GrandExchangeGraph.tableName + " (" +
                DB.GrandExchangeGraph.itemId + " INTEGER PRIMARY KEY, " +
                DB.GrandExchangeGraph.data + " TEXT NOT NULL, " +
                DB.GrandExchangeGraph.dateModified + " INTEGER NOT NULL);";
        String createOSBuddyExchangeSummaryTable = "CREATE TABLE " + DB.OSBuddyExchangeSummary.tableName + " (" +
                DB.OSBuddyExchangeSummary.id + " INTEGER PRIMARY KEY, " +
                DB.OSBuddyExchangeSummary.data + " TEXT, " +
                DB.OSBuddyExchangeSummary.dateModified + " INTEGER NOT NULL);";
        String createOSRSNewsTable = "CREATE TABLE " + DB.OSRSNews.tableName + " (" +
                DB.OSRSNews.id + " INTEGER PRIMARY KEY, " +
                DB.OSRSNews.data + " TEXT, " +
                DB.OSRSNews.dateModified + " INTEGER NOT NULL);";

        db.execSQL(createUserStatsTable);
        db.execSQL(createTrackTable);
        db.execSQL(createGrandExchangeTable);
        db.execSQL(createGrandExchangeUpdateTable);
        db.execSQL(createGrandExchangeGraphTable);
        db.execSQL(createOSBuddyExchangeSummaryTable);
        db.execSQL(createOSRSNewsTable);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        setWriteAheadLoggingEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 7) {
            db.execSQL("DROP TABLE IF EXISTS " + DB.UserStats.tableName);
            db.execSQL("DROP TABLE IF EXISTS " + DB.Track.tableName);
            db.execSQL("DROP TABLE IF EXISTS " + DB.GrandExchange.tableName);
            db.execSQL("DROP TABLE IF EXISTS " + DB.GrandExchangeUpdate.tableName);
            db.execSQL("DROP TABLE IF EXISTS " + DB.GrandExchangeGraph.tableName);
            db.execSQL("DROP TABLE IF EXISTS " + DB.OSBuddyExchangeSummary.tableName);
            db.execSQL("DROP TABLE IF EXISTS " + DB.OSBuddyExchange.tableName);
            db.execSQL("DROP TABLE IF EXISTS " + DB.OSRSNews.tableName);
            onCreate(db);
        }
        if (oldVersion < 8) {
            String createOSRSNewsTable = "CREATE TABLE " + DB.OSRSNews.tableName + " (" +
                    DB.OSRSNews.id + " INTEGER PRIMARY KEY, " +
                    DB.OSRSNews.data + " TEXT, " +
                    DB.OSRSNews.dateModified + " INTEGER NOT NULL);";
            db.execSQL(createOSRSNewsTable);
        }
        if (oldVersion < 9) {
            db.execSQL("DROP TABLE IF EXISTS " + DB.OSBuddyExchange.tableName);
        }
        if (oldVersion < 10) {
            db.execSQL("DROP TABLE IF EXISTS " + DB.OSBuddyExchangeSummary.tableName);
            String createOSBuddyExchangeSummaryTable = "CREATE TABLE " + DB.OSBuddyExchangeSummary.tableName + " (" +
                    DB.OSBuddyExchangeSummary.id + " INTEGER PRIMARY KEY, " +
                    DB.OSBuddyExchangeSummary.data + " TEXT, " +
                    DB.OSBuddyExchangeSummary.dateModified + " INTEGER NOT NULL);";
            db.execSQL(createOSBuddyExchangeSummaryTable);
        }
    }

    public UserStats getUserStats(String rsn, HiscoreType mode) {
        String query = "SELECT * FROM " + DB.UserStats.tableName + " WHERE " + DB.UserStats.rsn + " = ? AND " + DB.UserStats.hiscoreType + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ rsn, String.valueOf(mode.getValue()) });
        UserStats userStats = null;
        if (cursor.moveToFirst()) {
            int hiscoreType = cursor.getInt(cursor.getColumnIndex(DB.UserStats.hiscoreType));
            String stats = cursor.getString(cursor.getColumnIndex(DB.UserStats.stats));
            userStats = new UserStats(rsn, stats, HiscoreType.fromValue(hiscoreType));
            userStats.dateModified = cursor.getLong(cursor.getColumnIndex(DB.UserStats.dateModified));
        }
        cursor.close();
        return userStats;
    }

    public void insertOrUpdateUserStats(UserStats userStats) {
        String query = "SELECT * FROM " + DB.UserStats.tableName + " WHERE " + DB.UserStats.rsn + " = ? AND " + DB.UserStats.hiscoreType + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ userStats.rsn, String.valueOf(userStats.hiscoreType) });
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(DB.UserStats.rsn, userStats.rsn);
            cv.put(DB.UserStats.stats, userStats.stats);
            cv.put(DB.UserStats.hiscoreType, userStats.hiscoreType);
            cv.put(DB.UserStats.dateModified, System.currentTimeMillis());
            getWritableDatabase().update(DB.UserStats.tableName, cv, DB.UserStats.rsn + " = ? AND " + DB.UserStats.hiscoreType + " = ?", new String[]{ userStats.rsn, String.valueOf(userStats.hiscoreType) });
            cursor.close();
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(DB.UserStats.rsn, userStats.rsn);
            cv.put(DB.UserStats.stats, userStats.stats);
            cv.put(DB.UserStats.hiscoreType, userStats.hiscoreType);
            cv.put(DB.UserStats.dateModified, System.currentTimeMillis());
            getWritableDatabase().insert(DB.UserStats.tableName, null, cv);
            cursor.close();
        }
    }

    public TrackData getTrackData(String rsn, TrackDurationType mode) {
        String query = "SELECT * FROM " + DB.Track.tableName + " WHERE " + DB.Track.rsn + " = ? AND " + DB.Track.durationType + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ rsn, String.valueOf(mode.getValue()) });
        TrackData trackData = null;
        if (cursor.moveToFirst()) {
            trackData = new TrackData();
            trackData.rsn = rsn;
            trackData.data = cursor.getString(cursor.getColumnIndex(DB.Track.data));
            trackData.durationType = TrackDurationType.fromValue(cursor.getInt(cursor.getColumnIndex(DB.Track.durationType)));
            trackData.dateModified = cursor.getLong(cursor.getColumnIndex(DB.Track.dateModified));

        }
        cursor.close();
        return trackData;
    }

    public void insertOrUpdateTrackData(TrackData trackData) {
        insertOrUpdateTrackData(trackData.rsn, trackData.durationType, trackData.data);
    }

    public void insertOrUpdateTrackData(String rsn, TrackDurationType type, String data) {
        String query = "SELECT * FROM " + DB.Track.tableName + " WHERE " + DB.Track.rsn + " = ? AND " + DB.Track.durationType + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ rsn, String.valueOf(type.getValue()) });
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(DB.Track.rsn, rsn);
            cv.put(DB.Track.data, data);
            cv.put(DB.Track.durationType, type.getValue());
            cv.put(DB.Track.dateModified, System.currentTimeMillis());
            getWritableDatabase().update(DB.Track.tableName, cv, DB.Track.rsn + " = ? AND " + DB.Track.durationType + " = ?", new String[]{ rsn, String.valueOf(type.getValue()) });
            cursor.close();
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(DB.Track.rsn, rsn);
            cv.put(DB.Track.data, data);
            cv.put(DB.Track.durationType, type.getValue());
            cv.put(DB.Track.dateModified, System.currentTimeMillis());
            getWritableDatabase().insert(DB.Track.tableName, null, cv);
            cursor.close();
        }
    }

    public GrandExchangeData getGrandExchangeData(int itemId) {
        String query = "SELECT * FROM " + DB.GrandExchange.tableName + " WHERE " + DB.GrandExchange.itemId + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ String.valueOf(itemId) });
        GrandExchangeData grandExchangeData = null;
        if (cursor.moveToFirst()) {
            grandExchangeData = new GrandExchangeData();
            grandExchangeData.itemId = itemId;
            grandExchangeData.data = cursor.getString(cursor.getColumnIndex(DB.GrandExchange.data));
            grandExchangeData.dateModified = cursor.getLong(cursor.getColumnIndex(DB.GrandExchange.dateModified));

        }
        cursor.close();
        return grandExchangeData;
    }

    public void insertOrUpdateGrandExchangeData(String itemId, String newData) {
        String query = "SELECT * FROM " + DB.GrandExchange.tableName + " WHERE " + DB.GrandExchange.itemId + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ itemId });
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(DB.GrandExchange.data, newData);
            cv.put(DB.GrandExchange.dateModified, System.currentTimeMillis());
            getWritableDatabase().update(DB.GrandExchange.tableName, cv, DB.GrandExchange.itemId + " = ?", new String[]{ itemId });
            cursor.close();
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(DB.GrandExchange.itemId, itemId);
            cv.put(DB.GrandExchange.data, newData);
            cv.put(DB.GrandExchange.dateModified, System.currentTimeMillis());
            getWritableDatabase().insert(DB.GrandExchange.tableName, null, cv);
            cursor.close();
        }
    }

    public OSBuddySummaryDTO getOSBuddyExchangeSummary() {
        String query = "SELECT * FROM " + DB.OSBuddyExchangeSummary.tableName + " WHERE " + DB.OSBuddyExchangeSummary.id + " = 1";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        OSBuddySummaryDTO osBuddyExchangeData = null;
        if (cursor.moveToFirst()) {
            osBuddyExchangeData = new OSBuddySummaryDTO();
            osBuddyExchangeData.id = 1;
            osBuddyExchangeData.data = cursor.getString(cursor.getColumnIndex(DB.OSBuddyExchangeSummary.data));
            osBuddyExchangeData.dateModified = cursor.getLong(cursor.getColumnIndex(DB.OSBuddyExchangeSummary.dateModified));
        }
        cursor.close();
        return osBuddyExchangeData;
    }

    public void insertOrUpdateOSBuddyExchangeSummaryData(String newData) {
        String query = "SELECT * FROM " + DB.OSBuddyExchangeSummary.tableName + " WHERE " + DB.OSBuddyExchangeSummary.id + " = 1";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(DB.OSBuddyExchangeSummary.data, newData);
            cv.put(DB.OSBuddyExchangeSummary.dateModified, System.currentTimeMillis());
            getWritableDatabase().update(DB.OSBuddyExchangeSummary.tableName, cv, DB.OSBuddyExchangeSummary.id + " = 1", null);
            cursor.close();
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(DB.OSBuddyExchangeSummary.data, newData);
            cv.put(DB.OSBuddyExchangeSummary.dateModified, System.currentTimeMillis());
            getWritableDatabase().insert(DB.OSBuddyExchangeSummary.tableName, null, cv);
            cursor.close();
        }
    }

    public GrandExchangeUpdateData getGrandExchangeUpdateData() {
        String query = "SELECT * FROM " + DB.GrandExchangeUpdate.tableName + " WHERE " + DB.GrandExchangeUpdate.id + " = 1";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        GrandExchangeUpdateData result = null;
        if (cursor.moveToFirst()) {
            result = new GrandExchangeUpdateData();
            result.data = cursor.getString(cursor.getColumnIndex(DB.GrandExchangeUpdate.data));
            result.dateModified = cursor.getLong(cursor.getColumnIndex(DB.GrandExchangeUpdate.dateModified));
        }
        cursor.close();
        return result;
    }

    public void updateGrandExchangeUpdateData(String newData) {
        String query = "SELECT * FROM " + DB.GrandExchangeUpdate.tableName + " WHERE " + DB.GrandExchangeUpdate.id + " = 1";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(DB.GrandExchangeUpdate.data, newData);
            cv.put(DB.GrandExchangeUpdate.dateModified, System.currentTimeMillis());
            getWritableDatabase().update(DB.GrandExchangeUpdate.tableName, cv, DB.GrandExchangeUpdate.id + " = 1", null);
            cursor.close();
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(DB.GrandExchange.data, newData);
            cv.put(DB.GrandExchange.dateModified, System.currentTimeMillis());
            getWritableDatabase().insert(DB.GrandExchangeUpdate.tableName, null, cv);
            cursor.close();
        }
    }

    public GrandExchangeGraphData getGrandExchangeGraphData(int itemId) {
        String query = "SELECT * FROM " + DB.GrandExchangeGraph.tableName + " WHERE " + DB.GrandExchangeGraph.itemId + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ String.valueOf(itemId) });
        GrandExchangeGraphData result = null;
        if (cursor.moveToFirst()) {
            result = new GrandExchangeGraphData();
            result.data = cursor.getString(cursor.getColumnIndex(DB.GrandExchangeGraph.data));
            result.dateModified = cursor.getLong(cursor.getColumnIndex(DB.GrandExchangeGraph.dateModified));
        }
        cursor.close();
        return result;
    }

    public void insertOrupdateGrandExchangeGraphData(String itemId, String newData) {
        String query = "SELECT * FROM " + DB.GrandExchangeGraph.tableName + " WHERE " + DB.GrandExchangeGraph.itemId + " = ?";
        Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{ itemId });
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(DB.GrandExchangeGraph.data, newData);
            cv.put(DB.GrandExchangeGraph.dateModified, System.currentTimeMillis());
            getWritableDatabase().update(DB.GrandExchangeGraph.tableName, cv, DB.GrandExchangeGraph.itemId + " = ?", new String[]{ itemId });
            cursor.close();
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(DB.GrandExchangeGraph.itemId, itemId);
            cv.put(DB.GrandExchangeGraph.data, newData);
            cv.put(DB.GrandExchangeGraph.dateModified, System.currentTimeMillis());
            getWritableDatabase().insert(DB.GrandExchangeGraph.tableName, null, cv);
            cursor.close();
        }
    }

    public OSRSNewsDTO getOSRSNews() {
        String query = "SELECT * FROM " + DB.OSRSNews.tableName + " WHERE " + DB.OSRSNews.id + " = 1";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        OSRSNewsDTO result = null;
        if (cursor.moveToFirst()) {
            result = new OSRSNewsDTO();
            result.id = 1;
            result.data = cursor.getString(cursor.getColumnIndex(DB.OSRSNews.data));
            result.dateModified = cursor.getLong(cursor.getColumnIndex(DB.OSRSNews.dateModified));
        }
        cursor.close();
        return result;
    }

    public void insertOrUpdateOSRSNewsData(String newData) {
        String query = "SELECT * FROM " + DB.OSRSNews.tableName + " WHERE " + DB.OSRSNews.id + " = 1";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        if (cursor.moveToFirst()) {
            ContentValues cv = new ContentValues();
            cv.put(DB.OSRSNews.data, newData);
            cv.put(DB.OSRSNews.dateModified, System.currentTimeMillis());
            getWritableDatabase().update(DB.OSRSNews.tableName, cv, DB.OSRSNews.id + " = 1", null);
            cursor.close();
        }
        else {
            ContentValues cv = new ContentValues();
            cv.put(DB.OSRSNews.data, newData);
            cv.put(DB.OSRSNews.dateModified, System.currentTimeMillis());
            getWritableDatabase().insert(DB.OSRSNews.tableName, null, cv);
            cursor.close();
        }
    }

    private static class DB {
        private static final String name = "osrscompanion.db";
        private static final int version = 9;

        private static class UserStats {
            private static final String tableName = "UserStats";

            private static final String id = "id";
            private static final String rsn = "rsn";
            private static final String stats = "stats";
            private static final String hiscoreType = "hiscoreType";
            private static final String dateModified = "dateModified";
        }

        private static class Track {
            private static final String tableName = "Tracker";

            private static final String id = "id";
            private static final String rsn = "rsn";
            private static final String durationType = "durationType";
            private static final String data = "data";
            private static final String dateModified = "dateModified";
        }

        private static class GrandExchange {
            private static final String tableName = "GrandExchange";

            private static final String itemId = "itemId";
            private static final String data = "data";
            private static final String dateModified = "dateModified";
        }

        private static class GrandExchangeUpdate {
            private static final String tableName = "GrandExchangeUpdate";

            private static final String id = "id";
            private static final String data = "data";
            private static final String dateModified = "dateModified";
        }

        private static class GrandExchangeGraph {
            private static final String tableName = "GrandExchangeGraph";

            private static final String itemId = "itemId";
            private static final String data = "data";
            private static final String dateModified = "dateModified";
        }

        private static class OSBuddyExchange {
            private static final String tableName = "OSBuddyExchange";
        }

        private static class OSBuddyExchangeSummary {
            private static final String tableName = "OSBuddyExchangeSummary";

            private static final String id = "id";
            private static final String data = "data";
            private static final String dateModified = "dateModified";
        }

        private static class OSRSNews {
            private static final String tableName = "OSRSNews";

            private static final String id = "id";
            private static final String data = "data";
            private static final String dateModified = "dateModified";
        }
    }
}
