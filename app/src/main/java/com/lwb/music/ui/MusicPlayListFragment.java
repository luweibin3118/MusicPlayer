package com.lwb.music.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.lwb.music.R;
import com.lwb.music.bean.Song;
import com.lwb.music.interfaces.MusicController;

import java.util.List;

public class MusicPlayListFragment extends DialogFragment {
    protected View rootView;

    private RecyclerView rv;

    List<Song> songList;

    Song song;

    MusicAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialog;
        View view = inflater.inflate(R.layout.fragment_music_play_list, container, false);
        rootView = view;
        initView();
        return view;
    }

    private void initView() {
        rv = rootView.findViewById(R.id.play_list);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        songList = getArguments().getParcelableArrayList("list");
        song = getArguments().getParcelable("song");
        rv.setAdapter(adapter = new MusicAdapter());
        adapter.setPlayList(songList);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        rv.scrollToPosition(songList.indexOf(song) - 2);
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.dimAmount = 1 - 0.6f;
        window.setAttributes(params);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private MusicController musicController;

    public void setMusicController(MusicController musicController) {
        this.musicController = musicController;
    }

    public void changePlaySong(Song nowPlaySong, Song lastPlaySong) {
        if (adapter != null) {
            song = nowPlaySong;
            adapter.notifyItemChanged(songList.indexOf(nowPlaySong));
            adapter.notifyItemChanged(songList.indexOf(lastPlaySong));
        }
    }

    private class MusicAdapter extends RecyclerView.Adapter<MusicItemViewHolder> {
        List<Song> allFiles;

        public void setPlayList(List<Song> allFiles) {
            this.allFiles = allFiles;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MusicItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new MusicItemViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.music_play_list_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MusicItemViewHolder viewHolder, int i) {
            viewHolder.setSong(allFiles.get(i));
            if (allFiles.get(i).equals(song)) {
                viewHolder.animView.setVisibility(View.VISIBLE);
                viewHolder.musicTitle.setTextColor(0xff8B2252);
            } else {
                viewHolder.animView.setVisibility(View.INVISIBLE);
                viewHolder.musicTitle.setTextColor(0xffB9D3EE);
            }
        }

        @Override
        public int getItemCount() {
            if (allFiles == null) {
                return 0;
            } else {
                return allFiles.size();
            }
        }
    }

    private class MusicItemViewHolder extends RecyclerView.ViewHolder {

        TextView musicTitle;

        Song song;

        View animView;

        public MusicItemViewHolder(@NonNull View itemView) {
            super(itemView);
            musicTitle = itemView.findViewById(R.id.music_title);
            animView = itemView.findViewById(R.id.music_anim);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    musicController.playMusic(songList, song);
                    dismissAllowingStateLoss();
                }
            });
        }

        public void setSong(Song song) {
            this.song = song;
            musicTitle.setText(song.getTitle());
        }
    }
}