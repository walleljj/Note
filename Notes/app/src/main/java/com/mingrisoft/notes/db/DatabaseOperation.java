package com.mingrisoft.notes.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class DatabaseOperation {
    private SQLiteDatabase db;
    private Context context;

    public DatabaseOperation(Context context, SQLiteDatabase db) {
        this.db = db;
        this.context = context;
    }


    public void create_db() {

        db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir()
                .toString() + "/mynotes.db3", null);
        if (db == null) {
            Toast.makeText(context, "数据库创建不成功", Toast.LENGTH_LONG).show();
        }
        db.execSQL("create table if not exists notes(_id integer primary key autoincrement,"
                + "title text,"//标题title
                + "context text,"//内容context
                + "time varchar(20),"//创建时间time
                + "datatype text,"//是否设置了提醒时间datatype
                + "datatime varchar(20),"//提醒时间datatype
                + "locktype text,"//是否添加了锁locktype
                + "lock text" + ")");//锁秘密lock
    }

    public void insert_db(String title, String text, String time,
                          String datatype, String datatime, String locktype, String lock) {
        if (text.isEmpty()) {//判断是否有内容
            Toast.makeText(context, "内容不能为空", Toast.LENGTH_LONG).show();
        } else {
            db.execSQL("insert into notes(title,context,time,datatype,datatime,locktype,lock) values('"
                    + title//日记标题
                    + "','"
                    + text//日记内容
                    + "','"
                    + time//日记添加时间
                    + "','"
                    + datatype//日记是否设置了时间提醒
                    + "','"
                    + datatime//日记提醒时间
                    + "','"
                    + locktype//日记是否设置类日记锁
                    + "','"
                    + lock//日记锁密码
                    + "');");
        }
    }
    public void update_db(String title, String text, String time,
                          String datatype, String datatime, String locktype, String lock,
                          int item_ID) {
        if (text.isEmpty()) {
            Toast.makeText(context, "各字段不能为空", Toast.LENGTH_LONG).show();
        } else {
            db.execSQL("update notes set context='" + text + "',title='"
                    + title + "',time='" + time + "',datatype='" + datatype
                    + "',datatime='" + datatime + "',locktype='" + locktype
                    + "',lock='" + lock + "'where _id='" + item_ID + "'");
            Toast.makeText(context, "修改成功", Toast.LENGTH_LONG).show();
        }
    }
    public Cursor query_db() {
        Cursor cursor = db.rawQuery("select * from notes", null);
        return cursor;
    }

    public Cursor query_db(int item_ID) {
        Cursor cursor = db.rawQuery("select * from notes where _id='" + item_ID
                + "';", null);
        return cursor;

    }


    public Cursor query_db(String keword) {
        Cursor cursor = db.rawQuery("select * from notes where context like '%"
                + keword + "%';", null);
        return cursor;
    }


    public Cursor query_db(String starttime, String endtime) {
        Cursor cursor = db.rawQuery("select * from notes where time >='" + starttime + "'and time<='"
                + endtime + "';", null);
        return cursor;
    }


    public void delete_db(int item_ID) {
        db.execSQL("delete from notes where _id='" + item_ID + "'");
    }


    public void close_db() {
        db.close();
    }
}
