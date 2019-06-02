package com.lwb.music.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.PermissionUtils;
import com.lwb.music.App;
import com.lwb.music.R;
import com.lwb.music.base.BaseFragment;
import com.lwb.music.bean.Song;
import com.lwb.music.provider.MusicProvider;
import com.lwb.music.utils.SongUtils;

import java.util.ArrayList;

public class MusicHomeFragment extends BaseFragment {

    private TextView musicAll;

    private ArrayList<Song> allMusicFiles;

    private MusicRecentFragment musicRecentFragment;

    private ContentResolver resolver;

    Handler scanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    allMusicFiles = (ArrayList<Song>) msg.obj;
                    musicAll.setText("所有歌曲(" + allMusicFiles.size() + ")");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected int getLayout() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initView(View view) {
        resolver = getContext().getContentResolver();
        musicAll = view.findViewById(R.id.home_music_all);
        musicAll.setOnClickListener(this);
        view.findViewById(R.id.home_music_recent).setOnClickListener(this);
        view.findViewById(R.id.home_music_like).setOnClickListener(this);
        synchronized (App.class) {
            PermissionUtils.permission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE).callback(new PermissionUtils.SimpleCallback() {
                @Override
                public void onGranted() {
                    loadMusicFile();
                }

                @Override
                public void onDenied() {

                }
            }).request();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_music_all:
                MusicAllListFragment.start(getActivity(), allMusicFiles, musicController);
                break;
            case R.id.home_music_like:
                App.execute(new Runnable() {
                    @Override
                    public void run() {
                        Cursor cursor = resolver.query(MusicProvider.MUSIC_LIKE_URI, null,
                                null, null, null);
                        final ArrayList<Song> likeList = SongUtils.cursorToSongList(cursor);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MusicAllListFragment.start(getActivity(), likeList, musicController);
                            }
                        });
                    }
                });
                break;
            case R.id.home_music_recent:
                App.execute(new Runnable() {
                    @Override
                    public void run() {
                        final Cursor cursor = resolver.query(MusicProvider.MUSIC_RECENT_URI, null, null, null, null);
                        final ArrayList<Song> recentList = SongUtils.cursorToSongList(cursor);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MusicAllListFragment.start(getActivity(), recentList, musicController);
                            }
                        });
                    }
                });
                break;
            default:
                break;
        }
    }

    private void loadMusicFile() {
        App.execute(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.IS_MUSIC);
                Message message = scanHandler.obtainMessage();
                message.what = 1;
                message.obj = SongUtils.cursorToSongList(cursor);
                message.sendToTarget();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scanHandler.removeCallbacksAndMessages(null);
    }

}