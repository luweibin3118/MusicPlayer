package com.lwb.music.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;

public class MusicHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "music.db";

    private static final int VERSION = 1;

    public static final String TABLE_LIKE_MUSIC = "table_like_music";
    public static final String TABLE_RECENT_MUSIC = "table_recent_music";
    public static final String TABLE_SONG_SHEET = "table_song_sheet";
    public static final String TABLE_SONG_SHEET_DETAIL = "table_song_sheet_detail";
    public static final String TABLE_MUSIC_PLAYING_LIST = "table_music_playing_list";

    public interface SongSheetColumn {
        public String SONG_SHEET_ID = "song_sheet_id";
        public String SONG_SHEET_NAME = "song_sheet_name";
        public String SONG_SHEET_UPDATE_TIME = "song_sheet_update_time";
    }

    public MusicHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_LIKE_MUSIC + "(" + getMusicCol() + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RECENT_MUSIC + "(" + getMusicCol() + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SONG_SHEET_DETAIL + "(" + getMusicCol() + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MUSIC_PLAYING_LIST + "(" + getMusicCol() + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SONG_SHEET + "(" +
                SongSheetColumn.SONG_SHEET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SongSheetColumn.SONG_SHEET_NAME + " TEXT," +
                SongSheetColumn.SONG_SHEET_UPDATE_TIME + " INTEGER" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private String getMusicCol() {
        return MediaStore.Audio.AudioColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MediaStore.Audio.AudioColumns.TITLE + " TEXT," +
                MediaStore.Audio.AudioColumns.ARTIST + " TEXT," +
                MediaStore.Audio.AudioColumns.ALBUM + " TEXT," +
                MediaStore.Audio.AudioColumns.DATA + " TEXT," +
                MediaStore.Audio.AudioColumns.DISPLAY_NAME + " TEXT," +
                MediaStore.Audio.AudioColumns.DURATION + " INTEGER," +
                MediaStore.Audio.AudioColumns.DATE_MODIFIED + " INTEGER," +
                MediaStore.Audio.AudioColumns.SIZE + " INTEGER," +
                SongSheetColumn.SONG_SHEET_ID + " INTEGER";
    }
}