package com.lwb.music.ui;

import android.content.ContentUris;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.lwb.music.App;
import com.lwb.music.R;
import com.lwb.music.base.BaseDialogFragment;
import com.lwb.music.base.BaseFragment;
import com.lwb.music.bean.SongSheet;
import com.lwb.music.provider.MusicHelper;
import com.lwb.music.provider.MusicProvider;

import java.util.ArrayList;
import java.util.List;

public class SongSheetFragment extends BaseFragment implements PopupMenu.OnMenuItemClickListener, BaseDialogFragment.OnDismissCallback {
    RecyclerView recyclerView;
    SongSheetAdapter adapter;
    boolean isEditModel = false;

    @Override
    protected int getLayout() {
        return R.layout.fragment_all_music;
    }

    @Override
    protected void initView(View view) {
        recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter = new SongSheetAdapter());
    }

    @Override
    protected void initMoreView(ImageView moreView) {
        int padding = SizeUtils.dp2px(15);
        moreView.setPadding(padding, padding, padding, padding);
        refreshSheet();
    }

    @Override
    protected String getTitle() {
        return "我的歌单";
    }

    @Override
    protected int getMoreRes() {
        return R.drawable.ic_menu;
    }

    @Override
    protected void onMoreClick(View v) {
        PopupMenu popupMenu = new PopupMenu(this.getContext(), v);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.song_sheet_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void refreshSheet() {
        App.execute(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = resolver.query(MusicProvider.MUSIC_SONG_SHEET_URI,
                        null, null, null, null);
                final List<SongSheet> songSheets = new ArrayList<>();
                while (cursor.moveToNext()) {
                    SongSheet songSheet = new SongSheet();
                    songSheet.setSheetId(cursor.getInt(0));
                    songSheet.setSheetName(cursor.getString(1));
                    songSheet.setSheetCreateTime(cursor.getLong(2));
                    Cursor countCursor = resolver.query(
                            ContentUris.withAppendedId(MusicProvider.MUSIC_SONG_SHEET_DETAIL_URI, songSheet.getSheetId()),
                            null, null, null, null);
                    songSheet.setSheetCount(countCursor.getCount());
                    songSheets.add(songSheet);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setSongSheets(songSheets);
                    }
                });
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.song_sheet_add:
                SongSheetAddFragment songSheetAddFragment = new SongSheetAddFragment();
                songSheetAddFragment.setOnDismissCallback(this);
                songSheetAddFragment.show(getFragmentManager(), getClass().getName());
                break;
            case R.id.song_sheet_edit:
                isEditModel = !isEditModel;
                adapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onDismiss() {
        refreshSheet();
    }

    class SongSheetAdapter extends RecyclerView.Adapter<SongSheetItemViewHolder> {
        List<SongSheet> songSheets;

        public void setSongSheets(List<SongSheet> songSheets) {
            this.songSheets = songSheets;
            notifyDataSetChanged();
        }

        private void removeItem(SongSheet songSheet) {
            if (!songSheets.isEmpty() && songSheets.contains(songSheet)) {
                int index = songSheets.indexOf(songSheet);
                songSheets.remove(index);
                notifyItemRemoved(index);
            }
        }

        @NonNull
        @Override
        public SongSheetItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new SongSheetItemViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.song_sheet_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SongSheetItemViewHolder viewHolder, int i) {
            viewHolder.setSongSheet(songSheets.get(i));
        }

        @Override
        public int getItemCount() {
            if (songSheets == null) {
                return 0;
            } else {
                return songSheets.size();
            }
        }
    }

    class SongSheetItemViewHolder extends RecyclerView.ViewHolder {

        TextView songSheetTitle;

        ImageView songSheetDel;

        SongSheet songSheet;

        public SongSheetItemViewHolder(@NonNull View itemView) {
            super(itemView);
            songSheetTitle = itemView.findViewById(R.id.song_sheet_title);
            songSheetDel = itemView.findViewById(R.id.song_sheet_del);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            songSheetDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    App.execute(new Runnable() {
                        @Override
                        public void run() {
                            resolver.delete(
                                    MusicProvider.MUSIC_SONG_SHEET_URI,
                                    MusicHelper.SongSheetColumn.SONG_SHEET_ID + "=?",
                                    new String[]{songSheet.getSheetId() + ""}
                            );
                            resolver.delete(MusicProvider.MUSIC_SONG_SHEET_DETAIL_URI,
                                    MusicHelper.SongSheetColumn.SONG_SHEET_ID + "=?",
                                    new String[]{songSheet.getSheetId() + ""});
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.removeItem(songSheet);
                                }
                            });
                        }
                    });
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SongSheetListFragment.startSongSheetList(songSheet, SongSheetFragment.this, musicController);
                }
            });
        }

        public void setSongSheet(SongSheet songSheet) {
            this.songSheet = songSheet;
            songSheetTitle.setText(songSheet.getSheetName() + " (" + songSheet.getSheetCount() + "首)");
            if (isEditModel) {
                songSheetDel.setVisibility(View.VISIBLE);
            } else {
                songSheetDel.setVisibility(View.GONE);
            }
        }
    }
}