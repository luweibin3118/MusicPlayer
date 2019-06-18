package com.lwb.music.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.lwb.music.App;
import com.lwb.music.R;
import com.lwb.music.base.BaseFragment;
import com.lwb.music.bean.Song;
import com.lwb.music.utils.SongUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SongSelectorFragment extends BaseFragment {
    RecyclerView rv;
    ArrayList<Song> selectList;
    List<Song> list;
    SelectSongAdapter adapter;
    ImageView selectorAllIv;
    TextView selectorAllTv;

    @Override
    protected int getLayout() {
        return R.layout.fragment_music_selector;
    }

    @Override
    protected void initView(View view) {
        selectorAllIv = view.findViewById(R.id.music_selector);
        selectorAllTv = view.findViewById(R.id.music_title);
        selectorAllTv.setText("全选");
        ((ViewGroup) selectorAllIv.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectorAllIv.setSelected(!selectorAllIv.isSelected());
                if (selectorAllIv.isSelected()) {
                    selectList.clear();
                    selectList.addAll(list);
                } else {
                    selectList.clear();
                }
                adapter.notifyDataSetChanged();
            }
        });
        rv = view.findViewById(R.id.music_selector_rv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter = new SelectSongAdapter());
        try {
            List<Song> tempList = (List<Song>) getArguments().getSerializable("selectList");
            selectList = new ArrayList<>();
            for (Song tempSong : tempList) {
                selectList.add(tempSong);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadSongList();
    }

    @Override
    protected String getTitle() {
        return "选择歌曲";
    }

    @Override
    protected int getMoreRes() {
        return R.drawable.ic_ok;
    }

    @Override
    protected void initMoreView(ImageView moreView) {
        super.initMoreView(moreView);
        int padding = SizeUtils.dp2px(12);
        moreView.setPadding(padding, padding, padding, padding);
    }

    @Override
    protected void onMoreClick(View v) {
        if (onSelectedCallback != null) {
            onSelectedCallback.OnSelected(selectList);
        }
        popBack();
    }

    private void loadSongList() {
        App.execute(new Runnable() {
            @Override
            public void run() {
                final List<Song> songList = SongUtils.scanSongFile();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list = songList;
                        adapter.setSongList(list);
                    }
                });
            }
        });
    }

    private void setSelectAllBtn() {
        selectorAllIv.setSelected(list.size() == selectList.size());
    }

    class SelectSongAdapter extends RecyclerView.Adapter<SelectItemViewHolder> {
        List<Song> songList;

        public void setSongList(List<Song> songList) {
            this.songList = songList;
            notifyDataSetChanged();
            setSelectAllBtn();
        }

        @NonNull
        @Override
        public SelectItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new SelectItemViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.music_selector_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SelectItemViewHolder viewHolder, int i) {
            viewHolder.setSong(songList.get(i));
        }

        @Override
        public int getItemCount() {
            if (songList == null) {
                return 0;
            } else {
                return songList.size();
            }
        }
    }

    class SelectItemViewHolder extends RecyclerView.ViewHolder {

        TextView musicTitle;

        ImageView musicSelector;

        Song song;

        public SelectItemViewHolder(@NonNull final View itemView) {
            super(itemView);
            musicTitle = itemView.findViewById(R.id.music_title);
            musicSelector = itemView.findViewById(R.id.music_selector);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectList != null) {
                        if (selectList.contains(song)) {
                            Iterator<Song> iterator = selectList.iterator();
                            while (iterator.hasNext()) {
                                Song tempSong = iterator.next();
                                if (tempSong.getPath().equals(song.getPath())) {
                                    iterator.remove();
                                    adapter.notifyItemChanged(getAdapterPosition());
                                    setSelectAllBtn();
                                    return;
                                }
                            }
                        } else {
                            selectList.add(song);
                            adapter.notifyItemChanged(getAdapterPosition());
                            setSelectAllBtn();
                        }
                    }
                }
            });
        }

        public void setSong(Song song) {
            this.song = song;
            musicTitle.setText(song.getDisplayName());
            if (selectList != null) {
                musicSelector.setSelected(selectList.contains(song));
            }
        }
    }

    public static void startSelect(BaseFragment fragment, List<Song> selectList, OnSelectedCallback onSelectedCallback) {
        SongSelectorFragment songSheetListFragment = new SongSelectorFragment();
        songSheetListFragment.setOnSelectedCallback(onSelectedCallback);
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectList", (Serializable) selectList);
        songSheetListFragment.setArguments(bundle);
        fragment.startFragment(songSheetListFragment);
    }

    public interface OnSelectedCallback {
        public void OnSelected(List<Song> selectedSongs);
    }

    private OnSelectedCallback onSelectedCallback;

    public void setOnSelectedCallback(OnSelectedCallback onSelectedCallback) {
        this.onSelectedCallback = onSelectedCallback;
    }
}