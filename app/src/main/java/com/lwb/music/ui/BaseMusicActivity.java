package com.lwb.music.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.blankj.utilcode.util.BarUtils;
import com.lwb.music.base.BaseActivity;
import com.lwb.music.bean.Song;
import com.lwb.music.interfaces.MusicController;
import com.lwb.music.interfaces.MusicPlayCallback;
import com.lwb.music.service.MusicService;

import java.util.List;

public abstract class BaseMusicActivity extends BaseActivity implements MusicController, MusicPlayCallback {

    protected Intent musicServiceIntent;

    private MusicService.MusicBinder musicBinder;

    protected Song currentDisplaySong = null;

    private boolean reAnim = false;

    private Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            onUpdateProcess(msg.arg1);
        }
    };

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicBinder = (MusicService.MusicBinder) service;
            musicBinder.addMusicPlayCallback(BaseMusicActivity.this);
            musicBinder.refreshPlayStatus();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicServiceIntent = new Intent(this, MusicService.class);
        bindService(musicServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        BarUtils.setStatusBarAlpha(this, 0);
        BarUtils.setStatusBarLightMode(this, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        stopTimer();
        if (musicBinder != null) {
            musicBinder.removeMusicPlayCallback(BaseMusicActivity.this);
            musicBinder = null;
        }
    }

    private void startTimer() {
        timeHandler.removeCallbacksAndMessages(null);
        timeHandler.post(new Runnable() {
            @Override
            public void run() {
                if (musicBinder != null) {
                    Message message = timeHandler.obtainMessage();
                    message.arg1 = musicBinder.getCurrentPosition();
                    message.sendToTarget();
                }
                timeHandler.postDelayed(this, 1000);
            }
        });
    }

    private void stopTimer() {
        timeHandler.removeCallbacksAndMessages(null);
    }


    @Override
    public final void playMusic(List<Song> songList, Song song) {
        reAnim = true;
        if (musicBinder != null) {
            musicBinder.playMusic(songList, song);
        }
    }

    @Override
    public final void playLast() {
        reAnim = true;
        if (musicBinder != null) {
            musicBinder.playLast();
        }
    }

    @Override
    public final void playNext() {
        reAnim = true;
        if (musicBinder != null) {
            musicBinder.playNext();
        }
    }

    @Override
    public final void seekTo(int progress) {
        if (musicBinder != null) {
            musicBinder.seekTo(progress);
        }
    }

    @Override
    public final void pauseOrStartMusic() {
        if (musicBinder != null && musicBinder.getCurrentPlayingSong() != null) {
            musicBinder.pauseOrStartMusic();
        }
    }

    @Override
    public final void updatePlayType() {
        if (musicBinder != null) {
            musicBinder.updatePlayType();
        }
    }

    @Override
    public final void showPlayList() {
        if (musicBinder != null) {
            musicBinder.showPlayList();
        }
    }

    @Override
    public final Song getPlayingSong() {
        if (musicBinder != null) {
            return musicBinder.getCurrentPlayingSong();
        }
        return null;
    }

    @Override
    public final List<Song> getPlayList() {
        if (musicBinder != null) {
            return musicBinder.getPlayList();
        }
        return null;
    }

    @Override
    public final boolean isPlaying() {
        if (musicBinder != null) {
            return musicBinder.isPlaying();
        }
        return false;
    }

    @Override
    public final void stopServiceIfNotPlay() {
        if (musicBinder != null) {
            musicBinder.stopServiceIfNotPlay();
        }
    }

    @Override
    public final void onStartPlay(Song song) {
        onRefreshDisplaySong(song);
        onStartPlayAnim(reAnim);
        if (reAnim) {
            reAnim = false;
        }
        startTimer();
    }


    @Override
    public final void onPauseCurrentMusic(Song song, boolean notification) {
        onRefreshDisplaySong(song);
        stopTimer();
        onPauseUpdateUi();
    }

    @Override
    public final void onPlayCurrentMusic(Song song) {
        onRefreshDisplaySong(song);
        onStartPlayAnim(reAnim);
        if (reAnim) {
            reAnim = false;
        }
        startTimer();
        onPlayUpdateUi();
    }

    @Override
    public void onPlayFinish() {

    }

    @Override
    public void onLikeClick() {
        if (musicBinder != null) {
            musicBinder.onLikeClick();
        }
    }

    @Override
    public final void onUpdatePlayType() {
        onUpdatePlayTypeUi();
    }

    @Override
    public final void onShowPlayList(List<Song> songList, Song playingSong) {
        onShowPlayListUi(songList, playingSong);
    }

    @Override
    public final void onChangePlaySong(Song nowPlaySong, Song lastPlaySong) {
        onChangePlaySongUi(nowPlaySong, lastPlaySong);
    }

    @Override
    public final void onLikeChanged(Song song) {
        onLikeChangedUi(song);
    }

    @Override
    public final void onLrcLoaded(Song song) {
        onLrcLoadedUi(song);
    }

    @Override
    public final void onPrepareMusic(Song song) {
        onRefreshDisplaySong(song);
    }

    protected int getCurrentProcess() {
        if (musicBinder == null) {
            return 0;
        }
        return musicBinder.getCurrentPosition();
    }

    protected abstract void onPauseUpdateUi();

    protected abstract void onPlayUpdateUi();

    protected abstract void onRefreshDisplaySong(Song song);

    protected abstract void onStartPlayAnim(boolean reStart);

    protected abstract void onUpdateProcess(int process);

    protected abstract void onUpdatePlayTypeUi();

    protected abstract void onShowPlayListUi(List<Song> songList, Song playingSong);

    protected void onChangePlaySongUi(Song nowPlaySong, Song lastPlaySong) {

    }

    protected void onLikeChangedUi(Song song) {

    }

    protected void onLrcLoadedUi(Song song) {

    }

}