package com.lwb.music.ui;

import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lwb.music.R;
import com.lwb.music.base.BaseDialogFragment;
import com.lwb.music.bean.SongSheet;
import com.lwb.music.provider.MusicDataModel;

public class SongSheetAddFragment extends BaseDialogFragment {
    EditText editText;

    @Override
    protected void initView(View rootView) {
        editText = rootView.findViewById(R.id.song_sheet_title);
        rootView.findViewById(R.id.song_sheet_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(text)) {
                    Cursor cursor = MusicDataModel.querySongSheetByName(text);
                    if (cursor.getCount() != 0) {
                        ToastUtils.showShort("该歌单已存在！");
                        return;
                    }

                    SongSheet songSheet = new SongSheet();
                    songSheet.setSheetName(text);
                    songSheet.setSheetCreateTime(System.currentTimeMillis());
                    MusicDataModel.insertSongSheet(songSheet);

                    ToastUtils.showShort("添加歌单成功！");
                    KeyboardUtils.hideSoftInput(getActivity());
                    KeyboardUtils.hideSoftInput(editText);
                    dismiss();
                } else {
                    ToastUtils.showShort("名称不能为空！");
                }
            }
        });
        KeyboardUtils.showSoftInput(getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        KeyboardUtils.hideSoftInput(getActivity());
        KeyboardUtils.hideSoftInput(editText);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_song_sheet_add;
    }

}