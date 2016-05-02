package com.example.user.simpleui;

import io.realm.RealmObject;

/**
 * Created by user on 2016/4/25.
 */
public class Order extends RealmObject{
    private String note;
    private String menuResults;
    private String storeInfo;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getMenuResults() {
        return menuResults;
    }

    public void setMenuResults(String menuResults) {
        this.menuResults = menuResults;
    }

    public String getStoreInfo() {
        return storeInfo;
    }

    public void setStoreInfo(String storeInfo) {
        this.storeInfo = storeInfo;
    }
}
