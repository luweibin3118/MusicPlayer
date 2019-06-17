package com.lwb.music.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lwb.music.App;
import com.lwb.music.R;
import com.lwb.music.bean.LrcFile;
import com.lwb.music.bean.Song;
import com.lwb.music.constants.MusicConstants;
import com.lwb.music.interfaces.MusicPlayCallback;
import com.lwb.music.provider.MusicProvider;
import com.lwb.music.ui.MusicActivity;
import com.lwb.music.utils.SongUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private static final float mVolume = 0.05f;
    private Song playingSong;
    private List<Song> songList;
    private final String CHANNEL_ID = "com.lwb.music";
    private final String CHANNEL_NAME = "Music";
    private Notification notification;
    private Notification.Builder builder;
    private RemoteViews remoteViews;
    private Intent musicServiceIntent;
    private PendingIntent lastIntent, playIntent, nextIntent, closeIntent;
    private ContentResolver resolver;
    private ArrayList<LrcFile> lrcFiles = null;
    private AudioManager mAudioManager;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener;
    private Handler musicHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    lrcFiles = (ArrayList<LrcFile>) msg.obj;
                    if (playingSong != null && playingSong.getLrcFile() == null) {
                        playingSong.setLrcFile(getCurrentSonLrc());
                        for (MusicPlayCallback callback : callbacks) {
                            callback.onLrcLoaded(playingSong);
                        }
                    }
                    break;
                case 2:
                    songList = (List<Song>) msg.obj;
                    if (songList != null && !songList.isEmpty()) {
                        String playingPath = SPUtils.getInstance().getString(MusicConstants.MUSIC_PLAYING_PATH);
                        for (Song song : songList) {
                            if (song.getPath().equals(playingPath)) {
                                song = SongUtils.getPlaySongByType(songList, song,
                                        SPUtils.getInstance().getInt(MusicConstants.MUSIC_PLAY_TYPE, SongUtils.TYPE_1));
                                playingSong = song;
                                for (MusicPlayCallback callback : callbacks) {
                                    callback.onPrepareMusic(song);
                                }
                                break;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        musicServiceIntent = new Intent(this, MusicService.class);
        resolver = getContentResolver();
        initNotification();
        PermissionUtils.permission(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE).callback(new PermissionUtils.SimpleCallback() {
            @Override
            public void onGranted() {
                loadPlayingList();
                scanLrc();
            }

            @Override
            public void onDenied() {

            }
        }).request();
        requestTheAudioFocus();
    }

    private void loadPlayingList() {
        App.execute(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = resolver.query(MusicProvider.MUSIC_PLAYING_LIST_URI, null, null, null, null);
                List<Song> playingList = SongUtils.cursorToSongList(cursor);
                Message message = musicHandler.obtainMessage();
                message.obj = playingList;
                message.what = 2;
                message.sendToTarget();
            }
        });
    }

    private int requestTheAudioFocus() {
        if (Build.VERSION.SDK_INT < 8) {
            return 0;
        }
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        }
        if (mAudioFocusChangeListener == null) {
            mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                            //播放操作
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                mediaPlayer.pause();
                            }
                            break;
                        default:
                            break;
                    }
                }
            };
        }
        int requestFocusResult = mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        return requestFocusResult;
    }

    private void releaseTheAudioFocus(AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener) {
        if (mAudioManager != null && mAudioFocusChangeListener != null) {
            mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
        }
    }

    private void scanLrc() {
        App.execute(new Runnable() {
            @Override
            public void run() {
                Message message = musicHandler.obtainMessage();
                message.obj = SongUtils.scanLrcFile(resolver);
                message.what = 1;
                message.sendToTarget();
            }
        });
    }

    private void initNotification() {
        Intent resultIntent = new Intent(this, MusicActivity.class);
        PendingIntent p = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews = new RemoteViews(getPackageName(), R.layout.music_notification_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            chan.enableLights(false);
            chan.enableVibration(false);
            chan.setVibrationPattern(new long[]{0});
            chan.setSound(null, null);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(chan);
            builder = new Notification.Builder(this, CHANNEL_ID);
            notification = builder
                    .setSmallIcon(R.drawable.ic_play_music)
                    .setContentIntent(p)
                    .setCustomContentView(remoteViews)
                    .build();
        } else {
            notification = new Notification.Builder(getApplicationContext())
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(p)
                    .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                    .setVibrate(new long[]{0})
                    .setSound(null)
                    .setContent(remoteViews)
                    .build();
        }
        Intent last = new Intent(this, MusicService.class);
        last.putExtra("type", "last");
        lastIntent = PendingIntent.getService(getApplicationContext(), 0, last, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent play = new Intent(this, MusicService.class);
        play.putExtra("type", "play");
        playIntent = PendingIntent.getService(getApplicationContext(), 1, play, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent next = new Intent(this, MusicService.class);
        next.putExtra("type", "next");
        nextIntent = PendingIntent.getService(getApplicationContext(), 2, next, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent close = new Intent(this, MusicService.class);
        close.putExtra("type", "close");
        closeIntent = PendingIntent.getService(getApplicationContext(), 3, close, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.song_last, lastIntent);
        remoteViews.setOnClickPendingIntent(R.id.song_pause_or_play, playIntent);
        remoteViews.setOnClickPendingIntent(R.id.song_next, nextIntent);
        remoteViews.setOnClickPendingIntent(R.id.song_close, closeIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        String type = intent.getStringExtra("type");
        if (!TextUtils.isEmpty(type)) {
            if (type.endsWith("last")) {
                playLastSong();
            } else if (type.equals("play")) {
                pauseOrStart();
            } else if (type.equals("next")) {
                playNextSong(false);
            } else if (type.equals("close")) {
                pauseCurrent(false);
                stopSelf();
                stopForeground(true);
            }
            return super.onStartCommand(intent, flags, startId);
        }
        if (playingSong != null && remoteViews != null) {
            Bitmap bitmap = playingSong.getBitmap();
            if (bitmap != null) {
                remoteViews.setImageViewBitmap(R.id.song_img, bitmap);
            } else {
                remoteViews.setImageViewResource(R.id.song_img, R.drawable.ic_play_music);
            }
            remoteViews.setTextViewText(R.id.song_title, playingSong.getTitle());
            remoteViews.setTextViewText(R.id.song_artist, playingSong.getArtist());
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    remoteViews.setImageViewResource(R.id.song_pause_or_play, R.drawable.ic_play_pause);
                } else {
                    remoteViews.setImageViewResource(R.id.song_pause_or_play, R.drawable.ic_play_start);
                }
            }
        }
        startForeground(1, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("TTTTT", "onDestory");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopForeground(true);
        musicHandler.removeCallbacksAndMessages(null);
        releaseTheAudioFocus(mAudioFocusChangeListener);
    }

    private void playSong(final Song song) {
        playSong(song, 0);
    }

    private void playSong(final Song song, final int position) {
        Log.i("TTTTTT", "play position: " + position);
        if (song == null) {
            return;
        }
        try {
            SPUtils.getInstance().put(MusicConstants.MUSIC_PLAYING_PATH, song.getPath());
            SPUtils.getInstance().put(MusicConstants.MUSIC_PLAYING_DURATION, 0);
            mediaPlayer.reset();
            if (!song.getSongFile().exists()) {
                return;
            }
            mediaPlayer.setDataSource(song.getPath());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mp) {
                    mp.seekTo(position);
                    mp.start();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(musicServiceIntent);
                    } else {
                        startService(musicServiceIntent);
                    }
                    if (song != null) {
                        App.execute(new Runnable() {
                            @Override
                            public void run() {
                                resolver.insert(MusicProvider.MUSIC_RECENT_URI,
                                        SongUtils.songToContentValues(song));
                            }
                        });
                    }
                    for (MusicPlayCallback callback : callbacks) {
                        callback.onChangePlaySong(song, playingSong);
                    }
                    playingSong = song;

                    if (playingSong.getLrcFile() == null) {
                        playingSong.setLrcFile(getCurrentSonLrc());
                    }

                    for (MusicPlayCallback callback : callbacks) {
                        callback.onStartPlay(playingSong);
                    }
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playNextSong(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playLastSong() {
        if (playingSong == null) {
            return;
        }
        Song song = playingSong.getLastSong();
        if (song != null) {
            playSong(song);
        }
    }

    private void playNextSong(boolean isAutoNext) {
        if (playingSong == null) {
            return;
        }
        Song song;
        if (SPUtils.getInstance().getInt(MusicConstants.MUSIC_PLAY_TYPE, 0) == SongUtils.TYPE_2) {
            if (isAutoNext) {
                song = playingSong;
            } else {
                song = playingSong.getNextSong();
            }
        } else {
            song = playingSong.getNextSong();
        }
        if (song != null) {
            playSong(song);
        } else {
            stopService(musicServiceIntent);
            for (MusicPlayCallback callback : callbacks) {
                callback.onPlayFinish();
            }
        }
    }

    public void pauseOrStart() {
        if (playingSong == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            pauseCurrent(true);
        } else {
            playCurrent();
        }
    }

    public void pauseCurrent(boolean notification) {
        if (playingSong == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        SPUtils.getInstance().put(MusicConstants.MUSIC_PLAYING_PATH, playingSong.getPath());
        SPUtils.getInstance().put(MusicConstants.MUSIC_PLAYING_DURATION, mediaPlayer.getCurrentPosition());
        Log.i("TTTTTT", "saveTime: " + mediaPlayer.getCurrentPosition());
        if (notification) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(musicServiceIntent);
            } else {
                startService(musicServiceIntent);
            }
        }
        for (MusicPlayCallback callback : callbacks) {
            callback.onPauseCurrentMusic(playingSong, notification);
        }
    }

    public void playCurrent() {
        if (playingSong == null) {
            return;
        }
        if (!mediaPlayer.isPlaying()) {
            if (mediaPlayer.getCurrentPosition() > 1000) {
                mediaPlayer.start();
            } else {
                playSong(playingSong, SPUtils.getInstance().getInt(MusicConstants.MUSIC_PLAYING_DURATION));
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(musicServiceIntent);
        } else {
            startService(musicServiceIntent);
        }
        for (MusicPlayCallback callback : callbacks) {
            callback.onPlayCurrentMusic(playingSong);
        }
    }


    private File getCurrentSonLrc() {
        if (lrcFiles == null) {
            return null;
        }
        File result = null;
        for (LrcFile lrcFile : lrcFiles) {
            if (SongUtils.isSongLrc(lrcFile, playingSong)) {
                result = new File(lrcFile.getPath());
                break;
            }
        }
        return result;
    }

    long positionSaveTime = 0;

    int lastPlayDuration = 0;

    public int getCurrentRealPosition() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public int getPosition() {
        if (!mediaPlayer.isPlaying()) {
            if (lastPlayDuration == 0) {
                lastPlayDuration = SPUtils.getInstance().getInt(MusicConstants.MUSIC_PLAYING_DURATION);
            }
            return lastPlayDuration;
        }

        try {
            int position = mediaPlayer.getCurrentPosition();

            if (position > 5000 && (System.currentTimeMillis() - positionSaveTime) > 5000 && (position / 1000) % 8 == 0) {
                SPUtils.getInstance().put(MusicConstants.MUSIC_PLAYING_PATH, playingSong.getPath());
                SPUtils.getInstance().put(MusicConstants.MUSIC_PLAYING_DURATION, position);
                positionSaveTime = System.currentTimeMillis();
                Log.i("TTTTTT", "saveTime: " + position);
            }

            return position;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void savePlayingList(final List<Song> playingList) {
        App.execute(new Runnable() {
            @Override
            public void run() {
                resolver.delete(MusicProvider.MUSIC_PLAYING_LIST_URI, null, null);
                for (Song song : playingList) {
                    resolver.insert(MusicProvider.MUSIC_PLAYING_LIST_URI, SongUtils.songToContentValues(song));
                }
            }
        });
    }

    public class MusicBinder extends Binder {
        public void playMusic(List<Song> songList, Song song) {
            MusicService.this.songList = songList;
            savePlayingList(songList);
            playSong(SongUtils.getPlaySongByType(songList, song,
                    SPUtils.getInstance().getInt(MusicConstants.MUSIC_PLAY_TYPE, SongUtils.TYPE_1)));
        }

        public void playLast() {
            playLastSong();
        }

        public void playNext() {
            playNextSong(false);
        }

        public void seekTo(int seek) {
            mediaPlayer.seekTo(seek);
        }

        public void pauseOrStartMusic() {
            pauseOrStart();
        }

        public int getCurrentPosition() {
            return getPosition();
        }

        public int getRealPosition() {
            return getCurrentRealPosition();
        }

        public void addMusicPlayCallback(MusicPlayCallback callback) {
            callbacks.add(callback);
        }

        public void removeMusicPlayCallback(MusicPlayCallback callback) {
            callbacks.remove(callback);
        }

        public Song getCurrentPlayingSong() {
            return playingSong;
        }

        public List<Song> getPlayList() {
            return songList;
        }

        public boolean isPlaying() {
            if (mediaPlayer != null) {
                return mediaPlayer.isPlaying();
            }
            return false;
        }

        public void stopServiceIfNotPlay() {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                stopForeground(true);
                stopService(musicServiceIntent);
            }
        }

        public void refreshPlayStatus() {
            if (playingSong == null) {
                return;
            }
            if (mediaPlayer.isPlaying()) {
                for (MusicPlayCallback callback : callbacks) {
                    callback.onPlayCurrentMusic(playingSong);
                }
            } else {
                for (MusicPlayCallback callback : callbacks) {
                    callback.onPauseCurrentMusic(playingSong, true);
                }
            }
        }

        public void updatePlayType() {
            if (playingSong == null) {
                return;
            }
            SPUtils.getInstance().put(
                    MusicConstants.MUSIC_PLAY_TYPE,
                    (SPUtils.getInstance().getInt(MusicConstants.MUSIC_PLAY_TYPE, SongUtils.TYPE_1) + 1) % 3);
            SongUtils.getPlaySongByType(songList, playingSong, SPUtils.getInstance().getInt(MusicConstants.MUSIC_PLAY_TYPE));
            String toast = "";
            switch (SPUtils.getInstance().getInt(MusicConstants.MUSIC_PLAY_TYPE)) {
                case SongUtils.TYPE_1:
                    toast = "列表循环";
                    break;
                case SongUtils.TYPE_2:
                    toast = "单曲循环";
                    break;
                case SongUtils.TYPE_3:
                    toast = "随机播放";
                    break;
                default:
                    break;
            }
            ToastUtils.showShort(toast);
            for (MusicPlayCallback callback : callbacks) {
                callback.onUpdatePlayType();
            }
        }

        public void showPlayList() {
            if (playingSong == null) {
                return;
            }
            for (MusicPlayCallback callback : callbacks) {
                callback.onShowPlayList(songList, playingSong);
            }
        }

        public void onLikeClick() {
            if (playingSong == null) {
                return;
            }
            App.execute(new Runnable() {
                @Override
                public void run() {
                    final Song song = playingSong;
                    resolver.insert(MusicProvider.MUSIC_LIKE_URI, SongUtils.songToContentValues(playingSong));
                    musicHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (song != playingSong) {
                                return;
                            }
                            for (MusicPlayCallback callback : callbacks) {
                                callback.onLikeChanged(song);
                            }
                        }
                    });
                }
            });
        }
    }

    private List<MusicPlayCallback> callbacks = new ArrayList<>();

}