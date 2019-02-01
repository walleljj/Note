package com.mingrisoft.notes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mingrisoft.notes.adapter.MainAdapter;
import com.mingrisoft.notes.bean.SQLBean;
import com.mingrisoft.notes.data.CallAlarm;
import com.mingrisoft.notes.db.DatabaseOperation;
import com.mingrisoft.notes.view.MyGridView;
import com.mingrisoft.notes.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity {
	private SQLiteDatabase db;
	private DatabaseOperation dop;
	private MyGridView lv_notes;
	private TextView tv_note_id, tv_locktype, tv_lock;
	public EditText et_keyword;
	public String keword;
	Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		et_keyword = (EditText) findViewById(R.id.et_keyword);
		intent = getIntent();//获取当前传递对象
		keword = intent.getStringExtra("keword");//获取传递参数
		et_keyword.setText(keword);
		// 数据库操作
		dop = new DatabaseOperation(this, db);
		lv_notes = (MyGridView) findViewById(R.id.lv_notes);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// 显示记事列表
		showNotesList();
		// 为记事列表添加监听器
		lv_notes.setOnItemClickListener(new ItemClickEvent());
		// 为记事列表添加长按事件
		lv_notes.setOnItemLongClickListener(new ItemLongClickEvent());
	}

	// 显示记事列表
	private void showNotesList() {
		// 打开数据库
		dop.create_db();
		Cursor cursor = dop.query_db(keword);//模糊查询
		if (cursor.getCount() > 0) {
			List<SQLBean> list = new ArrayList<SQLBean>();//实例化数据列表
			while (cursor.moveToNext()) {// 光标移动成功
				SQLBean bean = new SQLBean();//创建数据库实体类
				//保存日记信息id到实体类
				bean.set_id("" + cursor.getInt(cursor.getColumnIndex("_id")));
				//保存日记内容到实体类
				bean.setContext(cursor.getString(cursor
						.getColumnIndex("context")));
				//保存日记标题到实体类
				bean.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				//保存日记记录时间到实体类
				bean.setTime(cursor.getString(cursor.getColumnIndex("time")));
				//保存日记是否设置提醒时间到实体类
				bean.setDatatype(cursor.getString(cursor
						.getColumnIndex("datatype")));
				//保存日记提醒时间到实体类
				bean.setDatatime(cursor.getString(cursor
						.getColumnIndex("datatime")));
				//保存日记是否设置了日记锁到实体类
				bean.setLocktype(cursor.getString(cursor
						.getColumnIndex("locktype")));
				//保存日记锁秘密到实体类
				bean.setLock(cursor.getString(cursor.getColumnIndex("lock")));
				//把保存日记信息实体类保存到日记信息集合里
				list.add(bean);
			}
			//倒序显示数据
			Collections.reverse(list);
			//装载日记信息到首页
			MainAdapter adapter = new MainAdapter(list, this);
			//日记列表设置日记信息适配器
			lv_notes.setAdapter(adapter);
		}else{
			Toast.makeText(SearchActivity.this, "暂无记事！",
					Toast.LENGTH_LONG).show();
		}
		//关闭数据库
		dop.close_db();
	}

	// 记事列表长按监听器
	class ItemLongClickEvent implements OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
									   int position, long id) {
			tv_note_id = (TextView) view.findViewById(R.id.tv_note_id);
			tv_locktype = (TextView) view.findViewById(R.id.tv_locktype);
			tv_lock = (TextView) view.findViewById(R.id.tv_lock);
			String locktype = tv_locktype.getText().toString();
			String lock = tv_lock.getText().toString();
			int item_id = Integer.parseInt(tv_note_id.getText().toString());
			simpleList(item_id, locktype, lock);
			return true;
		}
	}

	// 简单列表对话框，用于选择操作
	public void simpleList(final int item_id, final String locktype,
						   final String lock) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,
				R.style.custom_dialog);
		alertDialogBuilder.setTitle("选择操作");
		alertDialogBuilder.setIcon(R.drawable.icon);
		alertDialogBuilder.setItems(R.array.itemOperation,
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						switch (which) {
							// 编辑
							case 0:
								if ("0".equals(locktype)) {
									Intent intent = new Intent(SearchActivity.this,
											AddActivity.class);
									intent.putExtra("editModel", "update");
									intent.putExtra("noteId", item_id);
									startActivity(intent);
								} else {
									inputTitleDialog(lock, 0, item_id);
								}
								break;
							// 删除
							case 1:
								if ("0".equals(locktype)) {
									dop.create_db();
									dop.delete_db(item_id);
									dop.close_db();
									// 刷新列表显示
									lv_notes.invalidate();
									showNotesList();
								} else {
									inputTitleDialog(lock, 1, item_id);
									// 刷新列表显示
									lv_notes.invalidate();
									showNotesList();
								}
								break;
						}
					}
				});
		alertDialogBuilder.create();
		alertDialogBuilder.show();
	}

	public void inputTitleDialog(final String lock, final int idtype,
								 final int item_id) {// 密码输入框
		final EditText inputServer = new EditText(this);
		inputServer.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		inputServer.setFocusable(true);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("请输入密码").setView(inputServer)
				.setNegativeButton("取消", null);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String inputName = inputServer.getText().toString();
				if ("".equals(inputName)) {
					Toast.makeText(SearchActivity.this, "密码不能为空请重新输入！",
							Toast.LENGTH_LONG).show();
				} else {
					if (inputName.equals(lock)) {
						if (0 == idtype) {
							Intent intent = new Intent(SearchActivity.this,
									AddActivity.class);
							intent.putExtra("editModel", "update");
							intent.putExtra("noteId", item_id);
							startActivity(intent);
						} else if (1 == idtype) {
							dop.create_db();
							dop.delete_db(item_id);
							dop.close_db();
							// 刷新列表显示
							lv_notes.invalidate();
							showNotesList();
						}
					} else {
						Toast.makeText(SearchActivity.this, "密码不正确！",
								Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		builder.show();
	}

	// 记事列表单击监听器
	class ItemClickEvent implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
								long id) {
			tv_note_id = (TextView) view.findViewById(R.id.tv_note_id);
			tv_locktype = (TextView) view.findViewById(R.id.tv_locktype);
			tv_lock = (TextView) view.findViewById(R.id.tv_lock);
			String locktype = tv_locktype.getText().toString();
			String lock = tv_lock.getText().toString();
			int item_id = Integer.parseInt(tv_note_id.getText().toString());
			if ("0".equals(locktype)) {
				Intent intent = new Intent(SearchActivity.this,
						AddActivity.class);
				intent.putExtra("editModel", "update");
				intent.putExtra("noteId", item_id);
				startActivity(intent);
			} else {
				inputTitleDialog(lock, 0, item_id);
			}
		}
	}


	// 搜索
	public void onSearch(View v) {
		String ek = et_keyword.getText().toString();
		if ("".equals(ek)) {
			Toast.makeText(SearchActivity.this, "请输入关键词！", Toast.LENGTH_LONG)
					.show();
		} else {
			keword=ek;
			showNotesList();
		}
	}

	public void onBack(View v) {
		SearchActivity.this.finish();
	}
}
