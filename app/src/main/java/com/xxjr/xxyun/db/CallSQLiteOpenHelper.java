package com.xxjr.xxyun.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CallSQLiteOpenHelper extends SQLiteOpenHelper {

    public CallSQLiteOpenHelper(Context context) {
        super(context, MyCallTb.dbName, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MyCallTb.CreateTableString);

    }

    public interface MyCallTb {
        String dbName = "myCall.db";
        String tableName = "callRecord";
        /**
         * id
         */
        String ID = "id";
        /**
         * 类型名称
         */
        String NAME = "name";
        /**
         * 文件名称
         */
        String FILENAME = "fileName";
        /**
         * 腾讯云路径
         */
        String TENCENT_URL = "tencentUrl";

        /**
         * 第几步，0未上传服务器， 1 是腾讯云 ， 2服务器
         */
        String stepFlag = "step";
        String RULEKEY = "ruleKey";

        String CreateTableString = "CREATE TABLE if not exists " + tableName + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + NAME + " VARCHAR(20),"
                + FILENAME + " VARCHAR(100)," + RULEKEY + " VARCHAR(100),"
                + TENCENT_URL + " VARCHAR(100)," + stepFlag + " INTEGER" + ")";

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }

}
