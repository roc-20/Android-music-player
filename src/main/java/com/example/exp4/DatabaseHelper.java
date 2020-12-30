package com.example.exp4;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String CREATE_LIKE="create table guanzhu("
            +"Id integer primary key autoincrement,"
            +"Url text ,"
            +"Aas text,"
            +"Url_list text,"
            +"As_list text)";
    public DatabaseHelper(Context context, String name,
                          SQLiteDatabase.CursorFactory factory, int version){
        //四个参数：context是必须的，name是数据库的名字，
        // factory允许我们在查询数据时返回一个自定义的Cursor，
        // 一般多为null，version是数据库的版本号
        super(context,name,factory,version);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_LIKE);///执行数据库建表语句
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVision){

    }
}
