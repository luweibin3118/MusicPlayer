package com.lwb.music.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.MediaStore;

import com.lwb.music.App;
import com.lwb.music.bean.Song;
import com.lwb.music.bean.SongSheet;
import com.lwb.music.utils.SongUtils;

import java.util.ArrayList;
import java.util.List;

public class MusicDataModel {
    public static List<Song> queryAll() {
        Cursor cursor = App.resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.IS_MUSIC);
        ArrayList<Song> result = SongUtils.cursorToSongList(cursor);
        cursor.close();
        return result;
    }

    public static ArrayList<Song> queryLike() {
        Cursor cursor = App.resolver.query(MusicProvider.MUSIC_LIKE_URI, null, null, null, null);
        ArrayList<Song> result = SongUtils.cursorToSongList(cursor);
        cursor.close();
        return result;
    }

    public static ArrayList<Song> queryRecent() {
        Cursor cursor = App.resolver.query(MusicProvider.MUSIC_RECENT_URI, null, null, null, null);
        ArrayList<Song> result = SongUtils.cursorToSongList(cursor);
        cursor.close();
        return result;
    }

    public static List<Song> queryPlayingList() {
        Cursor cursor = App.resolver.query(MusicProvider.MUSIC_PLAYING_LIST_URI, null, null, null, null);
        ArrayList<Song> result = SongUtils.cursorToSongList(cursor);
        cursor.close();
        return result;
    }

    public static Cursor querySongSheetByName(String text) {
        return App.resolver.query(MusicProvider.MUSIC_SONG_SHEET_URI,
                null,
                MusicHelper.SongSheetColumn.SONG_SHEET_NAME + "=?",
                new String[]{text},
                null
        );
    }

    public static List<SongSheet> querySongSheet() {
        Cursor cursor = App.resolver.query(MusicProvider.MUSIC_SONG_SHEET_URI,
                null, null, null, null);
        final List<SongSheet> songSheets = new ArrayList<>();
        while (cursor.moveToNext()) {
            SongSheet songSheet = new SongSheet();
            songSheet.setSheetId(cursor.getInt(0));
            songSheet.setSheetName(cursor.getString(1));
            songSheet.setSheetCreateTime(cursor.getLong(2));
            songSheet.setSheetCount(MusicDataModel.querySongListBySheetId(songSheet.getSheetId()).size());
            songSheets.add(songSheet);
        }
        cursor.close();
        return songSheets;
    }

    public static List<Song> querySongListBySheetId(int sheetId) {
        Cursor cursor = App.resolver.query(
                ContentUris.withAppendedId(MusicProvider.MUSIC_SONG_SHEET_DETAIL_URI, sheetId),
                null, null, null, null);
        ArrayList<Song> result = SongUtils.cursorToSongList(cursor);
        cursor.close();
        return result;
    }

    public static List<Song> queryLikeSongByName(String name) {
        Cursor cursor = App.resolver.query(MusicProvider.MUSIC_LIKE_URI, null,
                MediaStore.Audio.AudioColumns.DISPLAY_NAME + "=?", new String[]{name}, null);
        ArrayList<Song> result = SongUtils.cursorToSongList(cursor);
        cursor.close();
        return result;
    }

    public static void insertRecentSong(Song song) {
        App.resolver.insert(MusicProvider.MUSIC_RECENT_URI, SongUtils.songToContentValues(song));
    }

    public static void insertPlayingSong(Song song) {
        App.resolver.insert(MusicProvider.MUSIC_PLAYING_LIST_URI, SongUtils.songToContentValues(song));
    }

    public static void insertLikeSong(Song song) {
        App.resolver.insert(MusicProvider.MUSIC_LIKE_URI, SongUtils.songToContentValues(song));
    }

    public static void insertSongSheet(SongSheet songSheet) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MusicHelper.SongSheetColumn.SONG_SHEET_NAME, songSheet.getSheetName());
        contentValues.put(MusicHelper.SongSheetColumn.SONG_SHEET_UPDATE_TIME, songSheet.getSheetCreateTime());
        App.resolver.insert(MusicProvider.MUSIC_SONG_SHEET_URI, contentValues);
    }

    public static void insertSongToSongSheet(Song song) {
        App.resolver.insert(MusicProvider.MUSIC_SONG_SHEET_DETAIL_URI, SongUtils.songToContentValues(song));
    }

    public static void deleteAllPlayingList() {
        App.resolver.delete(MusicProvider.MUSIC_PLAYING_LIST_URI, null, null);
    }

    public static void deleteSongSheetById(int songSheetId) {
        App.resolver.delete(
                MusicProvider.MUSIC_SONG_SHEET_URI,
                MusicHelper.SongSheetColumn.SONG_SHEET_ID + "=?",
                new String[]{songSheetId + ""}
        );
        App.resolver.delete(MusicProvider.MUSIC_SONG_SHEET_DETAIL_URI,
                MusicHelper.SongSheetColumn.SONG_SHEET_ID + "=?",
                new String[]{songSheetId + ""});
    }

    public static void deleteSongFromSheetByPath(String path) {
        App.resolver.delete(
                MusicProvider.MUSIC_SONG_SHEET_DETAIL_URI,
                MediaStore.Audio.AudioColumns.DATA + "=?",
                new String[]{path});
    }

    public static void updateSongSheetSort(Song song, SongSheet songSheet) {
        ContentValues contentValues = SongUtils.songToContentValues(song);
        App.resolver.update(MusicProvider.MUSIC_SONG_SHEET_DETAIL_URI, contentValues,
                MusicHelper.SongSheetColumn.SONG_SHEET_ID + "=? and " + MediaStore.Audio.AudioColumns._ID + "=?",
                new String[]{songSheet.getSheetId() + "", song.getId() + ""});
    }
}