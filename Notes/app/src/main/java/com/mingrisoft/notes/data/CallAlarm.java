package com.mingrisoft.notes.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.mingrisoft.notes.bean.SQLBean;
import com.mingrisoft.notes.db.DatabaseOperation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CallAlarm extends BroadcastReceiver {
	private SQLiteDatabase db;
	private DatabaseOperation dop;
	@Override
	public void onReceive(Context context, Intent intent) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm");
		Date curDate = new Date(System.currentTimeMillis());
		String str = formatter.format(curDate);
		dop = new DatabaseOperation(context, db);
		dop.create_db();
		Cursor cursor = dop.query_db();
		if (cursor.getCount() > 0) {
			List<SQLBean> list = new ArrayList<SQLBean>();
			while (cursor.moveToNext()) {
				SQLBean bean = new SQLBean();
				bean.set_id("" + cursor.getInt(cursor.getColumnIndex("_id")));
				bean.setContext(cursor.getString(cursor
						.getColumnIndex("context")));
				bean.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				bean.setTime(cursor.getString(cursor.getColumnIndex("time")));
				bean.setDatatype(cursor.getString(cursor
						.getColumnIndex("datatype")));
				bean.setDatatime(cursor.getString(cursor
						.getColumnIndex("datatime")));
				bean.setLocktype(cursor.getString(cursor
						.getColumnIndex("locktype")));
				bean.setLock(cursor.getString(cursor
						.getColumnIndex("lock")));
				if (bean.getDatatype().equals("1")&&str.equals(bean.getDatatime())) {
					dop.update_db(bean.getTitle(), bean.getContext(), bean.getTime(), "0", "0",bean.getLocktype() ,bean.getLock(),Integer.parseInt(bean.get_id()));
					Intent myIntent = new Intent(context, AlarmAlert.class);
					Bundle bundleRet = new Bundle();
					bundleRet.putString("remindMsg", bean.getTitle());
					bundleRet.putBoolean("shake", true);
					bundleRet.putBoolean("ring", true);
					myIntent.putExtras(bundleRet);
					myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(myIntent);
				}
			}
		}
		dop.close_db();
	}
}
