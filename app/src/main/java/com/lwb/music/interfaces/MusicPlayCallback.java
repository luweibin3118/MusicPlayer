package com.lwb.music.interfaces;

import com.lwb.music.bean.Song;

import java.util.List;

public interface MusicPlayCallback {
    void onStartPlay(Song song);

    void onPauseCurrentMusic(Song song, boolean notification);

    void onPlayCurrentMusic(Song song);

    void onPlayFinish();

    void onUpdatePlayType();

    void onShowPlayList(List<Song> songList, Song playingSong);

    void onChangePlaySong(Song nowPlaySong, Song lastPlaySong);

    void onLikeChanged(Song song);

    void onLrcLoaded(Song song);
}