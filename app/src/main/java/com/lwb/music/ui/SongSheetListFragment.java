package com.lwb.music.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.lwb.music.App;
import com.lwb.music.R;
import com.lwb.music.base.BaseFragment;
import com.lwb.music.bean.Song;
import com.lwb.music.bean.SongSheet;
import com.lwb.music.interfaces.MusicController;
import com.lwb.music.provider.MusicHelper;
import com.lwb.music.provider.MusicProvider;
import com.lwb.music.utils.SongUtils;

import java.util.Collections;
import java.util.List;

public class SongSheetListFragment extends BaseFragment implements SongSelectorFragment.OnSelectedCallback {
    RecyclerView rv;
    SongSheet songSheet;
    List<Song> list;
    MusicSongAdapter adapter;
    ItemTouchHelper itemTouchHelper;
    boolean isSortChanged = false;

    @Override
    protected int getLayout() {
        return R.layout.fragment_all_music;
    }

    @Override
    protected void initView(View view) {
        rv = (RecyclerView) view;
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter = new MusicSongAdapter());
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof GridLayoutManager) {
                    int dragFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    int swipeFlag = 0;
                    return makeMovementFlags(dragFlag, swipeFlag);
                } else if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                    int orientation = linearLayoutManager.getOrientation();
                    int dragFlag = 0;
                    int swipeFlag = 0;
                    if (orientation == LinearLayoutManager.HORIZONTAL) {
                        swipeFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                        dragFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                    } else if (orientation == LinearLayoutManager.VERTICAL) {
                        dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                        swipeFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                    }
                    return makeMovementFlags(dragFlag, swipeFlag);
                }
                return 0;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Collections.swap(list, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                isSortChanged = true;
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        itemTouchHelper.attachToRecyclerView(rv);
        try {
            songSheet = (SongSheet) getArguments().getSerializable("songsheet");
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadSongList();
    }

    @Override
    protected String getTitle() {
        if (songSheet != null) {
            return songSheet.getSheetName();
        } else {
            return "";
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isSortChanged) {
            isSortChanged = false;
            App.execute(new Runnable() {
                @Override
                public void run() {
                    int i = 0;
                    for (Song song : list) {
                        i++;
                        song.setUpdateTime(System.currentTimeMillis() + i);
                        ContentValues contentValues = SongUtils.songToContentValues(song);
//                        ContentValues contentValues = new ContentValues();
//                        contentValues.put(MediaStore.Audio.AudioColumns.DATE_MODIFIED, System.currentTimeMillis() + i);
                        resolver.update(MusicProvider.MUSIC_SONG_SHEET_DETAIL_URI, contentValues,
                                MusicHelper.SongSheetColumn.SONG_SHEET_ID + "=? and " + MediaStore.Audio.AudioColumns._ID + "=?",
                                new String[]{songSheet.getSheetId() + "", song.getId() + ""});
                    }
                }
            });
        }
    }

    @Override
    protected int getMoreRes() {
        return R.drawable.ic_add;
    }

    @Override
    protected void initMoreView(ImageView moreView) {
        super.initMoreView(moreView);
        int padding = SizeUtils.dp2px(14);
        moreView.setPadding(padding, padding, padding, padding);
    }

    @Override
    protected void onMoreClick(View v) {
        super.onMoreClick(v);
        SongSelectorFragment.startSelect(this, list, this);
    }

    private void loadSongList() {
        if (songSheet != null) {
            App.execute(new Runnable() {
                @Override
                public void run() {
                    Cursor cursor = resolver.query(ContentUris.withAppendedId(MusicProvider.MUSIC_SONG_SHEET_DETAIL_URI, songSheet.getSheetId()), null, null, null, null);
                    final List<Song> songList = SongUtils.cursorToSongList(cursor);
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
    }

    @Override
    public void OnSelected(final List<Song> selectedSongs) {
        if (list != null && songSheet != null) {
            App.execute(new Runnable() {
                @Override
                public void run() {
                    for (Song tempSong : selectedSongs) {
                        if (!list.contains(tempSong)) {
                            tempSong.setSheetId(songSheet.getSheetId());
                            resolver.insert(MusicProvider.MUSIC_SONG_SHEET_DETAIL_URI, SongUtils.songToContentValues(tempSong));
                        }
                    }
                    for (Song tempSong : list) {
                        if (!selectedSongs.contains(tempSong)) {
                            resolver.delete(
                                    MusicProvider.MUSIC_SONG_SHEET_DETAIL_URI,
                                    MediaStore.Audio.AudioColumns.DATA + "=?",
                                    new String[]{tempSong.getPath()});
                        }
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            list = selectedSongs;
                            adapter.setSongList(list);
                        }
                    });
                }
            });
        }
    }

    class MusicSongAdapter extends RecyclerView.Adapter<MusicItemViewHolder> {
        List<Song> songList;

        public void setSongList(List<Song> songList) {
            this.songList = songList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MusicItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new MusicItemViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.music_sheet_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MusicItemViewHolder viewHolder, int i) {
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

    class MusicItemViewHolder extends RecyclerView.ViewHolder {

        TextView musicTitle;

        Song song;

        public MusicItemViewHolder(@NonNull View itemView) {
            super(itemView);
            musicTitle = itemView.findViewById(R.id.music_title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (list != null && !list.isEmpty()) {
                        musicController.playMusic(list, song);
                    }
                }
            });
            itemView.findViewById(R.id.music_drag).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    itemTouchHelper.startDrag(MusicItemViewHolder.this);
                    return false;
                }
            });
        }

        public void setSong(Song song) {
            this.song = song;
            musicTitle.setText(song.getDisplayName());
        }

    }

    public static void startSongSheetList(SongSheet sheet, BaseFragment fragment, MusicController controller) {
        SongSheetListFragment songSheetListFragment = new SongSheetListFragment();
        songSheetListFragment.setMusicController(controller);
        Bundle bundle = new Bundle();
        bundle.putSerializable("songsheet", sheet);
        songSheetListFragment.setArguments(bundle);
        fragment.startFragment(songSheetListFragment);
    }
}