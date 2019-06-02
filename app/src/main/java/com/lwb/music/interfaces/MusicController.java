package com.lwb.music.interfaces;

import com.lwb.music.bean.Song;

import java.util.List;

public interface MusicController {
    void playMusic(List<Song> songList, Song file);

    void playLast();

    void playNext();

    void seekTo(int progress);

    void pauseOrStartMusic();

    void updatePlayType();

    void showPlayList();

    Song getPlayingSong();

    List<Song> getPlayList();

    boolean isPlaying();

    void stopServiceIfNotPlay();

    void onLikeClick();

}