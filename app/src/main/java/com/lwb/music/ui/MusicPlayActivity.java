package com.lwb.music.ui;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lwb.music.App;
import com.lwb.music.R;
import com.lwb.music.bean.Song;
import com.lwb.music.constants.MusicConstants;
import com.lwb.music.lrc.MusicLrcView;
import com.lwb.music.provider.MusicDataModel;
import com.lwb.music.utils.FastBlurUtil;
import com.lwb.music.utils.SongUtils;
import com.lwb.music.utils.TimeUtils;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MusicPlayActivity extends BaseMusicActivity implements View.OnClickListener {
    private static final String CURRENT_DISPLAY = "current_display";

    private static final int MUSIC_IMAGE = 0;
    private static final int MUSIC_LRC = 1;

    TextView music_title, music_artist, music_process_time, music_sum_time;

    CircleImageView music_img;

    ImageView music_play_type, music_last, music_play, music_next, music_list;

    SeekBar music_process_bar;

    ImageView music_play_bg;

    ObjectAnimator playAnimator;

    ViewGroup header_container;

    ImageView music_back, music_like;

    MusicLrcView music_lrc;

    ViewPager music_vp;

    Handler lrcHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj != null) {
                music_lrc.setLrc((String) msg.obj);
                music_lrc.setCurrentTime(getCurrentProcess());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);

        header_container = findViewById(R.id.header_container);
        header_container.setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);
        music_back = findViewById(R.id.music_back);
        music_back.setOnClickListener(this);
        music_like = findViewById(R.id.music_like);
        music_like.setOnClickListener(this);

        music_title = findViewById(R.id.music_title);
        music_artist = findViewById(R.id.music_artist);
        music_process_time = findViewById(R.id.music_process_time);
        music_sum_time = findViewById(R.id.music_sum_time);
        music_play_type = findViewById(R.id.music_play_type);
        music_last = findViewById(R.id.music_last);
        music_play = findViewById(R.id.music_play);
        music_next = findViewById(R.id.music_next);
        music_list = findViewById(R.id.music_list);
        music_process_bar = findViewById(R.id.music_process_bar);
        music_play_bg = findViewById(R.id.music_play_bg);
        music_vp = findViewById(R.id.music_vp);

        music_img = new CircleImageView(this);
        int padding = SizeUtils.dp2px(30);
        music_img.setPadding(padding, padding, padding, padding);

        music_lrc = new MusicLrcView(this);
        List<View> viewList = new ArrayList<>();
        viewList.add(music_img);
        viewList.add(music_lrc);
        MusicPagerAdapter adapter = new MusicPagerAdapter(viewList);
        music_vp.setAdapter(adapter);
        music_vp.setCurrentItem(SPUtils.getInstance().getInt(CURRENT_DISPLAY, MUSIC_IMAGE));
        music_vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                SPUtils.getInstance().put(CURRENT_DISPLAY, i == 0 ? MUSIC_IMAGE : MUSIC_LRC);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        music_play_type.setOnClickListener(this);
        music_last.setOnClickListener(this);
        music_play.setOnClickListener(this);
        music_next.setOnClickListener(this);
        music_list.setOnClickListener(this);

        music_process_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekTo(seekBar.getProgress());
            }
        });
        onUpdatePlayTypeUi();
    }

    class MusicPagerAdapter extends PagerAdapter {
        List<View> viewList;

        public MusicPagerAdapter(List<View> viewList) {
            this.viewList = viewList;
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view;
            container.addView(view = viewList.get(position), -1, -1);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lrcHandler.removeCallbacksAndMessages(null);
        if (playAnimator != null) {
            playAnimator.cancel();
            playAnimator = null;
        }
    }

    @Override
    protected void onPauseUpdateUi() {
        if (playAnimator != null) {
            playAnimator.pause();
        }
        music_play.setImageResource(R.drawable.ic_play_start);
    }

    @Override
    protected void onPlayUpdateUi() {
        music_play.setImageResource(R.drawable.ic_play_pause);
    }

    @Override
    protected void onRefreshDisplaySong(final Song song) {
        if (currentDisplaySong == null || currentDisplaySong != song) {
            currentDisplaySong = song;
            if (song.getBitmap() != null) {
                music_img.setImageBitmap(song.getBitmap());
            } else {
                music_img.setImageResource(R.drawable.ic_play_music);
            }
            music_play.setImageResource(R.drawable.ic_play_pause);
            music_artist.setText(song.getArtist());
            music_title.setText(song.getTitle());
            music_like.setSelected(isFavorite(song));
            music_sum_time.setText(TimeUtils.formatDuration(song.getDuration()));
            music_process_time.setText(TimeUtils.formatDuration(getCurrentProcess()));
        }
        music_process_bar.setMax((int) song.getDuration());
        music_process_bar.setProgress(getCurrentProcess());
        if (song.getBitmap() != null) {
            music_play_bg.setImageBitmap(FastBlurUtil.doBlur(song.getBitmap(), 20, false));
        } else {
            music_play_bg.setImageBitmap(null);
        }

        if (song.getLrcFile() != null) {
            if (song.getLrc() != null && song.getLrc().get() != null) {
                music_lrc.setLrc(song.getLrc().get());
            } else {
                loadLru(song);
            }
        } else {
            music_lrc.setLrc(null);
        }
    }

    private boolean isFavorite(Song song) {
        return MusicDataModel.queryLikeSongByName(song.getDisplayName()).size() > 0;
    }

    private void loadLru(final Song song) {
        App.execute(new Runnable() {
            @Override
            public void run() {
                final String lrc = FileIOUtils.readFile2String(
                        song.getLrcFile(),
                        FileUtils.getFileCharsetSimple(song.getLrcFile()));
                Message message = lrcHandler.obtainMessage();
                message.obj = lrc;
                message.sendToTarget();
                song.setLrc(new SoftReference<String>(lrc));
            }
        });
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
            music_process_time.setText(TimeUtils.formatDuration(0));
            music_process_bar.setProgress(0);
            if (playAnimator == null) {
                playAnimator = ObjectAnimator.ofFloat(music_img, "rotation", 0.0f, 359.0f);
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
    protected void onUpdateProcess(int process) {
        music_process_time.setText(TimeUtils.formatDuration(process));
        music_process_bar.setProgress(getCurrentProcess());
        music_lrc.setCurrentTime(process);
    }

    @Override
    protected void onUpdatePlayTypeUi() {
        switch (SPUtils.getInstance().getInt(MusicConstants.MUSIC_PLAY_TYPE)) {
            case SongUtils.TYPE_1:
                music_play_type.setImageResource(R.drawable.ic_play_cycle);
                break;
            case SongUtils.TYPE_2:
                music_play_type.setImageResource(R.drawable.ic_play_only);
                break;
            case SongUtils.TYPE_3:
                music_play_type.setImageResource(R.drawable.ic_play_radom);
                break;
            default:
                break;
        }
    }

    MusicPlayListFragment tag;

    @Override
    protected void onShowPlayListUi(List<Song> songList, Song playingSong) {
        if (tag == null) {
            tag = new MusicPlayListFragment();
            tag.setMusicController(this);
        }
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) songList);
        bundle.putParcelable("song", playingSong);
        tag.setArguments(bundle);
        tag.show(getSupportFragmentManager(), getClass().getName());
    }

    @Override
    protected void onChangePlaySongUi(Song nowPlaySong, Song lastPlaySong) {
        super.onChangePlaySongUi(nowPlaySong, lastPlaySong);
        if (tag != null) {
            tag.changePlaySong(nowPlaySong, lastPlaySong);
        }
    }

    @Override
    public void onLikeChangedUi(Song song) {
        boolean isFav = isFavorite(song);
        ToastUtils.showShort(isFav ? "收藏成功！" : "已取消收藏");
        music_like.setSelected(isFav);
    }

    @Override
    protected void onLrcLoadedUi(Song song) {
        super.onLrcLoadedUi(song);
        loadLru(song);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_anim, R.anim.right_out_anim);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.music_play_type:
                updatePlayType();
                break;
            case R.id.music_play:
                pauseOrStartMusic();
                break;
            case R.id.music_last:
                playLast();
                break;
            case R.id.music_next:
                playNext();
                break;
            case R.id.music_list:
                showPlayList();
                break;
            case R.id.music_back:
                onBackPressed();
                break;
            case R.id.music_like:
                onLikeClick();
                break;
            default:
                break;
        }
    }
}