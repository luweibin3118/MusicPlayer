package com.lwb.music.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;

public class MusicHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "music.db" ;

    private static final int VERSION = 1;

    public static final String TABLE_LIKE_MUSIC = "table_like_music" ;
    public static final String TABLE_RECENT_MUSIC = "table_recent_music" ;

    public MusicHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_LIKE_MUSIC + "(" + getMusicCol() + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RECENT_MUSIC + "(" + getMusicCol() + ")");
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
                MediaStore.Audio.AudioColumns.SIZE + " INTEGER" ;
    }
}