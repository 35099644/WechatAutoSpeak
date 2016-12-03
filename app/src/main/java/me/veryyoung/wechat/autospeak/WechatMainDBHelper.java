package me.veryyoung.wechat.autospeak;

import android.database.Cursor;

import static de.robv.android.xposed.XposedHelpers.callMethod;

/**
 * Created by veryyoung on 2016/12/3.
 */

public class WechatMainDBHelper {

    private Object SQLDB;

    public WechatMainDBHelper(Object dbObject) {
        SQLDB = dbObject;
    }


    public Cursor getLastMsg() {
        return rawQuery("SELECT * FROM message order by msgId desc limit 0,1");
    }

    public Cursor rawQuery(String query) {
        return rawQuery(query, null);
    }

    public Cursor rawQuery(String query, String[] args) {
        return (Cursor) callMethod(SQLDB, "rawQuery", query, args);
    }

}


