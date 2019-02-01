package com.mingrisoft.notes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.mingrisoft.notes.adapter.MainAdapter;
import com.mingrisoft.notes.bean.SQLBean;
import com.mingrisoft.notes.data.CallAlarm;
import com.mingrisoft.notes.db.DatabaseOperation;
import com.mingrisoft.notes.doubledatepicker.DoubleDatePickerDialog;
import com.mingrisoft.notes.view.MyGridView;
import com.mingrisoft.notes.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Button bt_add;// 添加按钮
	private SQLiteDatabase db;//数据库对象
	private DatabaseOperation dop;//自定义数据库类
	private MyGridView lv_notes;// 消息列表
	private TextView tv_note_id, tv_locktype, tv_lock;
	public static MediaPlayer mediaPlayer;// 音乐播放器
	public static Vibrator vibrator;//手机震动器
	public AlarmManager am;// 消息管理者
	public EditText et_keyword;// 搜索框
	public MainAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bt_add = (Button) findViewById(R.id.bt_add);
		bt_add.setOnClickListener(new ClickEvent());
		et_keyword = (EditText) findViewById(R.id.et_keyword);
		// 数据库操作
		dop = new DatabaseOperation(this, db);
		lv_notes = (MyGridView) findViewById(R.id.lv_notes);
		if (am == null) {
			am = (AlarmManager) getSystemService(ALARM_SERVICE);
		}
		try {
			Intent intent = new Intent(MainActivity.this, CallAlarm.class);
			PendingIntent sender = PendingIntent.getBroadcast(
					MainActivity.this, 0, intent, 0);
			am.setRepeating(AlarmManager.RTC_WAKEUP, 0, 60 * 1000, sender);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		// 创建或打开数据库 获取数据
		dop.create_db();
		//获取数据库内容
		Cursor cursor = dop.query_db();
		if (cursor.getCount() > 0) {
			List<SQLBean> list = new ArrayList<SQLBean>();//日记信息集合里
			while (cursor.moveToNext()) {// 光标移动成功
				// 把数据取出
				SQLBean bean = new SQLBean();//创建数据库实体类
				//保存日记信息id到实体类
				bean.set_id("" + cursor.getInt(cursor.getColumnIndex("_id")));
				bean.setContext(cursor.getString(cursor
						.getColumnIndex("context")));//保存日记内容到实体类
				//保存日记标题到实体类
				bean.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				//保存日记记录时间到实体类
				bean.setTime(cursor.getString(cursor.getColumnIndex("time")));
				bean.setDatatype(cursor.getString(cursor
						.getColumnIndex("datatype")));//保存日记是否设置提醒时间到实体类
				bean.setDatatime(cursor.getString(cursor
						.getColumnIndex("datatime")));//保存日记提醒时间到实体类
				bean.setLocktype(cursor.getString(cursor
						.getColumnIndex("locktype")));//保存日记是否设置了日记锁到实体类
				//保存日记锁秘密到实体类
				bean.setLock(cursor.getString(cursor.getColumnIndex("lock")));
				list.add(bean);//把保存日记信息实体类保存到日记信息集合里
			}
			//倒序显示数据
			Collections.reverse(list);
			adapter = new MainAdapter(list, this);//装载日记信息到首页
			lv_notes.setAdapter(adapter);//日记列表设置日记信息适配器
		}
		dop.close_db();//关闭数据库
	}
	// 记事列表长按监听器
	class ItemLongClickEvent implements OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
									   int position, long id) {
			//初始化日记id保存控件
			tv_note_id = (TextView) view.findViewById(R.id.tv_note_id);
			//初始化是否添加日记锁保存控件
			tv_locktype = (TextView) view.findViewById(R.id.tv_locktype);
			//初始化日记锁秘密保存信息
			tv_lock = (TextView) view.findViewById(R.id.tv_lock);
			//获取控件上是否设置日记锁信息
			String locktype = tv_locktype.getText().toString();
			//获取控件上日记密码信息
			String lock = tv_lock.getText().toString();
			//获取控件上id信息转换成int类型
			int item_id = Integer.parseInt(tv_note_id.getText().toString());
			//弹出选择操作框方法
			simpleList(item_id, locktype, lock);
			return true;
		}
	}
	// 简单列表对话框，用于选择操作
	public void simpleList(final int item_id, final String locktype,
						   final String lock) {
		//实例化AlertDialog
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,
				R.style.custom_dialog);
		//设置弹窗标题
		alertDialogBuilder.setTitle("选择操作");
		//设置弹窗图片
		alertDialogBuilder.setIcon(R.mipmap.ic_launcher);
		//设置弹窗选项内容
		alertDialogBuilder.setItems(R.array.itemOperation,
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							// 编辑
							case 0:
								if ("0".equals(locktype)) {//判断是否添加了秘密锁0没有
									Intent intent = new Intent(MainActivity.this,
											AddActivity.class);//跳转到添加日记页
									intent.putExtra("editModel", "update");//传递编辑信息
									intent.putExtra("noteId", item_id);//传递id信息
									startActivity(intent);//开始跳转
								} else {//有秘密锁
									// 弹出输入密码框
									inputTitleDialog(lock, 0, item_id);
								}
								break;
							// 删除
							case 1:
								if ("0".equals(locktype)) {// 判断是否是加密日记 0没有
									dop.create_db();// 打开数据库
									dop.delete_db(item_id);//删除数据
									dop.close_db();// 关闭数据库
									// 刷新列表显示
									lv_notes.invalidate();
									showNotesList();
								} else {//有秘密锁
									// 弹出输入密码框
									inputTitleDialog(lock, 1, item_id);
									// 刷新列表显示
									lv_notes.invalidate();
									//显示日记列表信息
									showNotesList();
								}
								break;
						}
					}
				});
		alertDialogBuilder.create();//创造弹窗
		alertDialogBuilder.show();//显示弹窗
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
				Intent intent = new Intent(MainActivity.this, AddActivity.class);
				intent.putExtra("editModel", "update");
				intent.putExtra("noteId", item_id);
				startActivity(intent);
			} else {
				inputTitleDialog(lock, 0, item_id);
			}
		}
	}
	// 加密日记打开弹出的输入密码框
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
					Toast.makeText(MainActivity.this, "密码不能为空请重新输入！",
							Toast.LENGTH_LONG).show();
				} else {
					if (inputName.equals(lock)) {
						if (0 == idtype) {
							Intent intent = new Intent(MainActivity.this,
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
						Toast.makeText(MainActivity.this, "密码不正确！",
								Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		builder.show();
	}
	// 点击事件
	class ClickEvent implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				// 添加记事
				case R.id.bt_add:
					Intent intent = new Intent(MainActivity.this, AddActivity.class);
					intent.putExtra("editModel", "newAdd");
					startActivity(intent);
			}
		}
	}
	// 搜索功能
	public void onSearch(View v) {
		//获取搜索关键词
		String ek = et_keyword.getText().toString();
		if ("".equals(ek)) {//判断搜索关键词是否为空
			Toast.makeText(MainActivity.this, "请输入关键词！", Toast.LENGTH_LONG)
					.show();
		} else {//搜索不为空
			//进入搜索结果页
			Intent intent = new Intent(MainActivity.this, SearchActivity.class);
			intent.putExtra("keword", ek);//传递关键词
			startActivity(intent);//开始跳转
		}
	}
	// 日期范围搜索
	public void onData(View v) {
		// 最后一个false表示不显示日期，如果要显示日期，最后参数可以是true或者不用输入
		Calendar c = Calendar.getInstance();
		new DoubleDatePickerDialog(MainActivity.this, 0,
				new DoubleDatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker startDatePicker,
										  int startYear, int startMonthOfYear,
										  int startDayOfMonth, DatePicker endDatePicker,
										  int endYear, int endMonthOfYear, int endDayOfMonth) {
						if (startYear < endYear || startYear == endYear
								&& startMonthOfYear <= endMonthOfYear) {
							int st = startMonthOfYear + 1;
							int et = endMonthOfYear + 1;
							Intent intent = new Intent(MainActivity.this,
									DataSearchActivity.class);
							// sql判断 需要在月份前补0 否则sql语句判断不正确。
							if (st < 10) {
								intent.putExtra("startData", startYear + "-0"
										+ st + "-" + "01");
							} else {
								intent.putExtra("startData", startYear + "-"
										+ st + "-" + "01");
							}
							if (et < 10) {
								intent.putExtra("endData", endYear + "-0" + et
										+ "-" + "30");
							} else {
								intent.putExtra("endData", endYear + "-" + et
										+ "-" + "30");
							}
							startActivity(intent);
						} else {
							Toast.makeText(MainActivity.this, "日期选择错误请重新选择！",
									Toast.LENGTH_LONG).show();
						}
					}
				}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
				.get(Calendar.DATE), false).show();
	}
	// 进入关于页
	public void onAbout(View v) {
		Intent intent = new Intent(MainActivity.this, About.class);
		startActivity(intent);
	}
}
