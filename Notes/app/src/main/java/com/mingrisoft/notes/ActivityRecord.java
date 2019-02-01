package com.mingrisoft.notes;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.mingrisoft.notes.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


@SuppressLint("HandlerLeak") public class ActivityRecord extends Activity {
	private Button btn_record;
	private ImageView iv_microphone;
	private TextView tv_recordTime;
	private ImageView iv_record_wave_left, iv_record_wave_right;
	private AnimationDrawable ad_left, ad_right;
	private int isRecording = 0;
	private int isPlaying = 0;
	private Timer mTimer;//计时器
	// 语音操作对象
	private MediaPlayer mPlayer = null;
	private MediaRecorder mRecorder = null;
	// 语音保存路径
	private String FilePath = null;
	private String newtimes="0:0:0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		TextView title = (TextView) findViewById(R.id.tv_title);
		title.setText("录音");

		Button btn_save = (Button) findViewById(R.id.bt_save);
		btn_save.setOnClickListener(new ClickEvent());
		Button btn_back = (Button) findViewById(R.id.bt_back);
		btn_back.setOnClickListener(new ClickEvent());

		btn_record = (Button) findViewById(R.id.btn_record);
		btn_record.setOnClickListener(new ClickEvent());

		iv_microphone = (ImageView) findViewById(R.id.iv_microphone);
		iv_microphone.setOnClickListener(new ClickEvent());

		iv_record_wave_left = (ImageView) findViewById(R.id.iv_record_wave_left);
		iv_record_wave_right = (ImageView) findViewById(R.id.iv_record_wave_right);

		ad_left = (AnimationDrawable) iv_record_wave_left.getBackground();
		ad_right = (AnimationDrawable) iv_record_wave_right.getBackground();

		tv_recordTime = (TextView) findViewById(R.id.tv_recordTime);
	}

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					String time[] = tv_recordTime.getText().toString().split(":");
					int hour = Integer.parseInt(time[0]);
					int minute = Integer.parseInt(time[1]);
					int second = Integer.parseInt(time[2]);
					if (second < 59) {
						second++;
					} else if (second == 59 && minute < 59) {
						minute++;
						second = 0;
					}
					if (second == 59 && minute == 59 && hour < 98) {
						hour++;
						minute = 0;
						second = 0;
					}
					time[0] = hour + "";
					time[1] = minute + "";
					time[2] = second + "";
					// 调整格式显示到屏幕上
					if (second < 10)
						time[2] = "0" + second;
					if (minute < 10)
						time[1] = "0" + minute;
					if (hour < 10)
						time[0] = "0" + hour;
					newtimes=time[0] + ":" + time[1] + ":" + time[2];
					// 显示在TextView中
					tv_recordTime.setText(time[0] + ":" + time[1] + ":" + time[2]);
					break;
			}
		}
	};

	class ClickEvent implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				// 点击的是开始录音按钮
				case R.id.btn_record:
					// 开始录音
					if (isRecording == 0) {
						// 每一次调用录音，可以录音多次，至多满意为至，最后只将最后一次的录音文件保存，其他的删除
						if (FilePath != null) {
							File oldFile = new File(FilePath);
							oldFile.delete();
						}
						// 获得系统当前时间，并以该时间作为文件名
						SimpleDateFormat formatter = new SimpleDateFormat(
								"yyyyMMddHHmmss");
						Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
						String str = formatter.format(curDate);//以String形式保存日期
						str = str + "record.amr";//文件名
						File dir = new File("/sdcard/notes/");//创建文件夹
						File file = new File("/sdcard/notes/", str);//创建文件
						if (!dir.exists()) {//判断文件夹是否创建成功
							dir.mkdir();//创建文件夹
						} else {
							if (file.exists()) {//判断文件是否创建
								file.delete();//删除文件
							}
						}
						//文件路径
						FilePath = dir.getPath() + "/" + str;
						// 计时器
						mTimer = new Timer();
						// 将麦克图标设置成不可点击，
						iv_microphone.setClickable(false);
						// 将显示的时间设置为00:00:00
						tv_recordTime.setText("00:00:00");
						// 将按钮换成停止录音
						isRecording = 1;
						btn_record
								.setBackgroundResource(R.drawable.tabbar_record_stop);
						//创建视频/音频类
						mRecorder = new MediaRecorder();
						mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置语音录制
						//设置保存格式
						mRecorder
								.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
						mRecorder.setOutputFile(FilePath);//设置保存路径
						mRecorder
								.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);//设置编码格式
						try {
							//准备录制
							mRecorder.prepare();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//开始录制
						mRecorder.start();
						mTimer.schedule(new TimerTask() {
							@Override
							public void run() {
								Message message = new Message();
								message.what = 1;
								handler.sendMessage(message);
							}
						}, 1000, 1000);
						// 播放动画
						ad_left.start();
						ad_right.start();
					}
					// 停止录音
					else {
						// 将按钮换成开始录音
						isRecording = 0;
						btn_record
								.setBackgroundResource(R.drawable.tabbar_record_start);
						mRecorder.stop();
						mTimer.cancel();
						mTimer = null;
						mRecorder.release();
						mRecorder = null;
						// 将麦克图标设置成可点击，
						iv_microphone.setClickable(true);
						// 停止动画
						ad_left.stop();
						ad_right.stop();
						Toast.makeText(ActivityRecord.this, "单击麦克图标试听，再次点击结束试听",
								Toast.LENGTH_LONG).show();
					}
					break;
				// 如果单击的是麦克图标，则可以是进入试听模式，再次点击，停止播放
				case R.id.iv_microphone:
					if (FilePath == null)
						Toast.makeText(ActivityRecord.this, "没有录音广播可以播放，请先录音",
								Toast.LENGTH_LONG).show();
					else {
						// 试听
						if (isPlaying == 0) {
							isPlaying = 1;
							mPlayer = new MediaPlayer();
							tv_recordTime.setText("00:00:00");
							mTimer = new Timer();
							mPlayer.setOnCompletionListener(new MediaCompletion());
							try {
								mPlayer.setDataSource(FilePath);
								mPlayer.prepare();
								mPlayer.start();
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (SecurityException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							mTimer.schedule(new TimerTask() {
								@Override
								public void run() {
									Message message = new Message();
									message.what = 1;
									handler.sendMessage(message);
								}
							}, 1000, 1000);
							// 播放动画
							ad_left.start();
							ad_right.start();
						}
						// 结束试听
						else {
							isPlaying = 0;
							mPlayer.stop();
							mPlayer.release();
							mPlayer = null;
							mTimer.cancel();
							mTimer = null;
							// 停止动画
							ad_left.stop();
							ad_right.stop();
						}
					}
					break;

				// 点击保存按钮
				case R.id.bt_save:
					// 将最终的录音文件的路径返回到新增日记页面
					if (FilePath == null) {
						Toast.makeText(ActivityRecord.this, "没有录音可以保存，请先录音",
								Toast.LENGTH_LONG).show();
					} else {
						//创建意图
						Intent intent = getIntent();
						//信息的传递
						Bundle b = new Bundle();
						//存入文件路径
						b.putString("audio", FilePath);
						//存入音频时长
						b.putString("time", tv_recordTime.getText().toString());
						//开始传递
						intent.putExtras(b);
						//传递给调整到当前页面的类
						setResult(RESULT_OK, intent);
					}
					//关闭当前类
					ActivityRecord.this.finish();
					break;
				case R.id.bt_back:
					// 返回前将录音的文件删除
					if (FilePath != null) {
						File oldFile = new File(FilePath);
						oldFile.delete();
					}
					ActivityRecord.this.finish();
					break;

			}
		}

	}

	class MediaCompletion implements OnCompletionListener {
		@Override
		public void onCompletion(MediaPlayer mp) {
			mTimer.cancel();
			mTimer = null;
			isPlaying = 0;
			// 停止动画
			ad_left.stop();
			ad_right.stop();
			Toast.makeText(ActivityRecord.this, "播放完毕", Toast.LENGTH_LONG)
					.show();
			tv_recordTime.setText(newtimes);
		}
	}
}
