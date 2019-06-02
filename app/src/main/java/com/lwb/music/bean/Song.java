package com.lwb.music.bean;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.lwb.music.utils.SongUtils;

import java.io.File;
import java.lang.ref.WeakReference;


public class Song implements Parcelable {

    private int id;

    private String title;

    private String artist;

    private String album;

    private String path;

    private long duration;

    private long size;

    private String displayName;

    private Song lastSong;

    private Song nextSong;

    private File songFile;

    private File lrcFile;

    public Song() {
        // Empty
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", path='" + path + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                '}';
    }

    public Song(Parcel in) {
        readFromParcel(in);
    }

    WeakReference<Bitmap> bitmapWeakReference;

    public Bitmap getBitmap() {
        try {
            if (bitmapWeakReference == null || bitmapWeakReference.get() == null) {
                byte[] bytes = SongUtils.getMusicEmbeddedPicture(getSongFile());
                bitmapWeakReference = new WeakReference<>(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            }
            return bitmapWeakReference.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public File getLrcFile() {
        return lrcFile;
    }

    public void setLrcFile(File lrcFile) {
        this.lrcFile = lrcFile;
    }

    public File getSongFile() {
        if (songFile == null) {
            songFile = new File(path);
        }
        return songFile;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Song getLastSong() {
        return lastSong;
    }

    public void setLastSong(Song lastSong) {
        this.lastSong = lastSong;
    }

    public Song getNextSong() {
        return nextSong;
    }

    public void setNextSong(Song nextSong) {
        this.nextSong = nextSong;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.title);
        dest.writeString(this.artist);
        dest.writeString(this.album);
        dest.writeString(this.path);
        dest.writeString(this.displayName);
        dest.writeLong(this.duration);
        dest.writeLong(this.size);
    }

    public void readFromParcel(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.artist = in.readString();
        this.album = in.readString();
        this.path = in.readString();
        this.displayName = in.readString();
        this.duration = in.readInt();
        this.size = in.readInt();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}