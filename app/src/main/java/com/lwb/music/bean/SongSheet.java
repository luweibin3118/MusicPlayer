package com.lwb.music.bean;

import java.io.Serializable;

public class SongSheet implements Serializable {
    private int sheetId;

    private String sheetName;

    private int sheetCount;

    private long sheetCreateTime;

    public int getSheetId() {
        return sheetId;
    }

    public void setSheetId(int sheetId) {
        this.sheetId = sheetId;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public long getSheetCreateTime() {
        return sheetCreateTime;
    }

    public void setSheetCreateTime(long sheetCreateTime) {
        this.sheetCreateTime = sheetCreateTime;
    }

    public int getSheetCount() {
        return sheetCount;
    }

    public void setSheetCount(int sheetCount) {
        this.sheetCount = sheetCount;
    }
}