package com.lwb.music.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.lwb.music.App;
import com.lwb.music.bean.LrcFile;
import com.lwb.music.bean.Song;
import com.lwb.music.provider.MusicDataModel;
import com.lwb.music.provider.MusicHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongUtils {

    public static final int TYPE_1 = 0;
    public static final int TYPE_2 = 1;
    public static final int TYPE_3 = 2;

    public static Song getPlaySongByType(List<Song> songList, Song playSong, int playType) {
        Song rootSong = null, firstSong = null;
        switch (playType) {
            case TYPE_1:
            case TYPE_2:
                for (Song temp : songList) {
                    if (rootSong == null) {
                        rootSong = temp;
                        firstSong = temp;
                    } else {
                        rootSong.setNextSong(temp);
                        temp.setLastSong(rootSong);
                        rootSong = temp;
                    }
                }
                firstSong.setLastSong(rootSong);
                rootSong.setNextSong(firstSong);
                break;
            case TYPE_3:
                List<Song> temp = new ArrayList<>();
                for (Song song : songList) {
                    temp.add(song);
                }
                Collections.shuffle(temp);
                for (Song s : temp) {
                    if (rootSong == null) {
                        rootSong = s;
                        firstSong = s;
                    } else {
                        rootSong.setNextSong(s);
                        s.setLastSong(rootSong);
                        rootSong = s;
                    }
                }
                firstSong.setLastSong(rootSong);
                rootSong.setNextSong(firstSong);
                break;
            default:
                break;
        }
        return playSong;
    }

    public static byte[] getMusicEmbeddedPicture(File file) {
        if (file == null || file.length() == 0 || !file.exists()) {
            return null;
        }
        try {
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(file.getAbsolutePath());
            String keyDuration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (keyDuration == null || !keyDuration.matches("\\d+")) {
                return null;
            }
            byte[] data = metadataRetriever.getEmbeddedPicture();
            metadataRetriever.release();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final String[] projection = {
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.TITLE
    };

    private static final String UNKNOWN = "<未知>";

    public static Song fileToMusic(File file) {
        if (file == null || file.length() == 0 || !file.exists()) {
            return null;
        }
        try {
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(file.getAbsolutePath());
            final int duration;

            String keyDuration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (keyDuration == null || !keyDuration.matches("\\d+")) {
                return null;
            }
            duration = Integer.parseInt(keyDuration);

            final String title = extractMetadata(metadataRetriever, MediaMetadataRetriever.METADATA_KEY_TITLE, file.getName());
            final String artist = extractMetadata(metadataRetriever, MediaMetadataRetriever.METADATA_KEY_ARTIST, UNKNOWN);
            final String album = extractMetadata(metadataRetriever, MediaMetadataRetriever.METADATA_KEY_ALBUM, UNKNOWN);

            final Song song = new Song();
            song.setTitle(title);
            song.setArtist(artist);
            song.setPath(file.getAbsolutePath());
            song.setAlbum(album);
            song.setDuration(duration);
            song.setSize((int) file.length());
            metadataRetriever.release();
            return song;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String extractMetadata(MediaMetadataRetriever retriever, int key, String defaultValue) {
        String value = retriever.extractMetadata(key);
        if (TextUtils.isEmpty(value)) {
            value = defaultValue;
        }
        return value;
    }

    public static void scanMp3File(File file, List<Song> store) {
        String fileName = file.getAbsolutePath();
        if (file.isDirectory()) {
            if (file.listFiles() != null) {
                for (File temp : file.listFiles()) {
                    scanMp3File(temp, store);
                }
            }
        } else {
            if (fileName.toLowerCase().endsWith(".mp3")) {
                Song song = SongUtils.fileToMusic(file);
                if (song != null && song.getDuration() / 1000 > 60) {
                    store.add(song);
                }
            }
        }
    }

    public static void scanLrcFile(File file, List<LrcFile> store) {
        String filePath = file.getAbsolutePath();
        if (file.isDirectory()) {
            if (file.listFiles() != null) {
                for (File temp : file.listFiles()) {
                    scanLrcFile(temp, store);
                }
            }
        } else {
            if (filePath.toLowerCase().endsWith(".lrc")) {
                LrcFile lrcFile = new LrcFile();
                lrcFile.setFileName(file.getName());
                lrcFile.setPath(filePath);
                store.add(lrcFile);
            }
        }
    }

    public static ArrayList<Song> scanSongFile() {
        Cursor cursor = MusicDataModel.queryAll();
        return cursorToSongList(cursor);
//        ArrayList<Song> songs = new ArrayList<>();
//        scanMp3File(Environment.getExternalStorageDirectory(), songs);
//        return songs;
    }

    public static ArrayList<LrcFile> scanLrcFile() {
        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = null;
        String[] selectionArgs = null;
        selection = "(" + MediaStore.Files.FileColumns.DATA + " LIKE '%.lrc'" + ")";
        selectionArgs = null;
        Cursor cursor = App.resolver.query(uri, projection, selection, selectionArgs, null);

        ArrayList<LrcFile> lrcFileArrayList = new ArrayList<>();
        while (cursor.moveToNext()) {
            LrcFile lrcFile = new LrcFile();
            lrcFile.setPath(cursor.getString(0));
            lrcFile.setFileName(cursor.getString(1));
            lrcFileArrayList.add(lrcFile);
        }
        return lrcFileArrayList;

    }

    public static boolean isSongLrc(LrcFile lrcFile, Song song) {
        try {
            File l = new File(lrcFile.getPath());
            if (l.getName().contains(song.getTitle())) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<Song> cursorToSongList(Cursor cursor) {
        ArrayList<Song> musicList = new ArrayList<>();
        while (cursor.moveToNext()) {
            Song song = new Song();
            song.setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)));
            if (song.getDuration() < 1000 * 80) {
                continue;
            }
            song.setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID)));
            song.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)));
            song.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST)));
            song.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)));
            song.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)));
            song.setDisplayName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)));
            song.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.SIZE)));
            song.setUpdateTime(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATE_MODIFIED)));
            int sheetIndex = cursor.getColumnIndex(MusicHelper.SongSheetColumn.SONG_SHEET_ID);
            if (sheetIndex >= 0) {
                song.setSheetId(cursor.getInt(sheetIndex));
            }
            musicList.add(song);
        }
        return musicList;
    }

    public static ContentValues songToContentValues(Song song) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.AudioColumns.TITLE, song.getTitle());
        contentValues.put(MediaStore.Audio.AudioColumns.ARTIST, song.getArtist());
        contentValues.put(MediaStore.Audio.AudioColumns.ALBUM, song.getAlbum());
        contentValues.put(MediaStore.Audio.AudioColumns.DURATION, song.getDuration());
        contentValues.put(MediaStore.Audio.AudioColumns.DISPLAY_NAME, song.getDisplayName());
        contentValues.put(MediaStore.Audio.AudioColumns.DATA, song.getPath());
        contentValues.put(MediaStore.Audio.AudioColumns.SIZE, song.getSize());
        contentValues.put(MediaStore.Audio.AudioColumns.DATE_MODIFIED, System.currentTimeMillis());
        if (song.getId() >= 0) {
            contentValues.put(MusicHelper.SongSheetColumn.SONG_SHEET_ID, song.getSheetId());
        }
        return contentValues;
    }

}