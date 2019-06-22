package com.lwb.music.ui;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blankj.utilcode.util.BarUtils;
import com.lwb.music.R;
import com.lwb.music.bean.Song;
import com.lwb.music.utils.TimeUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MusicActivity extends BaseMusicActivity implements View.OnClickListener {
    ImageView play_music_btn, home_bg;
    CircleImageView play_music_img;
    TextView play_music_title, play_music_album,
            play_sum_duration, play_current_duration;
    ImageView play_last_music_btn, play_next_music_btn;
    ProgressBar play_music_process;
    ObjectAnimator playAnimator;
    ViewGroup music_container;

    @Override
    protected void onUpdateProcess(int process) {
        play_current_duration.setText(TimeUtils.formatDuration(process) + "/");
        play_music_process.setProgress(getCurrentProcess());
    }

    @Override
    protected void onUpdatePlayTypeUi() {

    }

    @Override
    protected void onShowPlayListUi(List<Song> songList, Song playingSong) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        music_container = findViewById(R.id.music_container);
        music_container.setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);
        home_bg = findViewById(R.id.home_bg);
//        Glide.with(this).load(R.drawable.bg_black).into(home_bg);
        play_music_img = findViewById(R.id.play_music_img);
        play_music_btn = findViewById(R.id.play_music_btn);
        play_music_title = findViewById(R.id.play_music_title);
        play_music_album = findViewById(R.id.play_music_album);
        play_sum_duration = findViewById(R.id.play_sum_duration);
        play_current_duration = findViewById(R.id.play_current_duration);
        play_last_music_btn = findViewById(R.id.play_last_music_btn);
        play_next_music_btn = findViewById(R.id.play_next_music_btn);
        play_music_process = findViewById(R.id.play_music_process);
        play_music_btn.setOnClickListener(this);
        play_last_music_btn.setOnClickListener(this);
        play_next_music_btn.setOnClickListener(this);
        play_music_img.setOnClickListener(this);
        findViewById(R.id.play_music_info).setOnClickListener(this);
        MusicHomeFragment fragment = new MusicHomeFragment();
        fragment.setMusicController(this);
        startFragment(fragment, false);
        if (musicServiceIntent != null) {
            startService(musicServiceIntent);
        }
    }

    @Override
    protected void onRefreshDisplaySong(Song song) {
        if (currentDisplaySong == null || currentDisplaySong != song) {
            currentDisplaySong = song;
            if (song.getBitmap() != null) {
                play_music_img.setImageBitmap(song.getBitmap());
            } else {
                play_music_img.setImageResource(R.drawable.ic_play_music);
            }
            play_music_btn.setImageResource(isPlaying() ? R.drawable.ic_play_pause : R.drawable.ic_play_start);
            play_music_title.setText(song.getTitle() + " - " + song.getArtist());
            play_music_album.setText(song.getAlbum());
            play_sum_duration.setText(TimeUtils.formatDuration(song.getDuration()));
            play_current_duration.setText(TimeUtils.formatDuration(getCurrentProcess()) + "/");
            play_music_process.setVisibility(View.VISIBLE);
        }
        play_music_process.setMax((int) song.getDuration());
        play_music_process.setProgress(getCurrentProcess());
    }

    @Override
    protected void onStartPlayAnim(boolean reStart) {
        if (playAnimator != null) {
            if (reStart) {
                playAnimator.cancel();
                playAnimator.start();
            } else {
                playAnimator.resume();
            }
        } else {
            play_current_duration.setText(TimeUtils.formatDuration(0) + "/");
            play_music_process.setProgress(0);
            if (playAnimator == null) {
                playAnimator = ObjectAnimator.ofFloat(play_music_img, "rotation", 0.0f, 359.0f);
                playAnimator.setDuration(10000);
                playAnimator.setInterpolator(new LinearInterpolator());
                playAnimator.setRepeatCount(Animation.INFINITE);
                playAnimator.setRepeatMode(ObjectAnimator.RESTART);
            } else {
                playAnimator.cancel();
            }
            playAnimator.start();
        }
    }

    @Override
    protected void onPauseUpdateUi() {
        if (playAnimator != null) {
            playAnimator.pause();
        }
        play_music_btn.setImageResource(R.drawable.ic_play_start);
    }

    @Override
    protected void onPlayUpdateUi() {
        play_music_btn.setImageResource(R.drawable.ic_play_pause);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_music_btn:
                pauseOrStartMusic();
                break;
            case R.id.play_last_music_btn:
                playLast();
                break;
            case R.id.play_next_music_btn:
                playNext();
                break;
            case R.id.play_music_img:
            case R.id.play_music_info:
                startActivity(new Intent(this, MusicPlayActivity.class));
                overridePendingTransition(R.anim.right_in_anim, R.anim.no_anim);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        stopServiceIfNotPlay();
        MusicAllListFragment.destroy();
        super.onDestroy();
    }
}