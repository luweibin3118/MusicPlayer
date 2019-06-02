package com.lwb.music.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lwb.music.R;
import com.lwb.music.base.BaseActivity;
import com.lwb.music.base.BaseFragment;
import com.lwb.music.bean.Song;
import com.lwb.music.interfaces.MusicController;

import java.util.ArrayList;
import java.util.List;

public class MusicAllListFragment extends BaseFragment {
    RecyclerView musicAllList;

    MusicAllAdapter adapter;

    List<Song> allFiles;

    @Override
    protected int getLayout() {
        return R.layout.fragment_all_music;
    }

    @Override
    protected void initView(View view) {
        musicAllList = (RecyclerView) view;
        musicAllList.setLayoutManager(new LinearLayoutManager(getContext()));
        musicAllList.setAdapter(adapter = new MusicAllAdapter());
    }

    @Override
    protected void initData() {
        allFiles = getArguments().getParcelableArrayList("list");
        adapter.setAllFiles(allFiles);
    }

    @Override
    public void onResume() {
        super.onResume();
        musicAllList.scrollToPosition(0);
    }

    class MusicAllAdapter extends RecyclerView.Adapter<MusicItemViewHolder> {
        List<Song> allFiles;

        public void setAllFiles(List<Song> allFiles) {
            this.allFiles = allFiles;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MusicItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new MusicItemViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.music_all_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MusicItemViewHolder viewHolder, int i) {
            viewHolder.setSong(allFiles.get(i));
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

    class MusicItemViewHolder extends RecyclerView.ViewHolder {

        TextView musicTitle;

        Song song;

        public MusicItemViewHolder(@NonNull View itemView) {
            super(itemView);
            musicTitle = itemView.findViewById(R.id.music_title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    musicController.playMusic(allFiles, song);
                }
            });
        }

        public void setSong(Song song) {
            this.song = song;
            musicTitle.setText(song.getDisplayName());
        }
    }

    private static MusicAllListFragment allListFragment;

    public static void start(Activity activity, ArrayList<Song> songList, MusicController controller) {
        if (allListFragment == null) {
            allListFragment = new MusicAllListFragment();
        }
        allListFragment.setMusicController(controller);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("list", songList);
        allListFragment.setArguments(bundle);
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).startFragment(allListFragment);
        }
    }

    public static void destroy() {
        allListFragment = null;
    }
}