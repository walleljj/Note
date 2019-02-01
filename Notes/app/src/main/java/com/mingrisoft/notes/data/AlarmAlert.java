package com.mingrisoft.notes.data;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;

import com.mingrisoft.notes.MainActivity;
import com.mingrisoft.notes.R;


public class AlarmAlert extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String remindMsg = bundle.getString("remindMsg");
		if (bundle.getBoolean("ring")) {
			MainActivity.mediaPlayer = MediaPlayer.create(this, R.raw.ring);
			try {
				MainActivity.mediaPlayer.setLooping(true);
				MainActivity.mediaPlayer.prepare();
			} catch (Exception e) {
				setTitle(e.getMessage());
			}
			MainActivity.mediaPlayer.start();
		}
		if (bundle.getBoolean("shake")) {
			MainActivity.vibrator = (Vibrator) getApplication().getSystemService(
					Service.VIBRATOR_SERVICE);
			MainActivity.vibrator.vibrate(new long[] { 1000, 100, 100, 1000 }, -1);
		}
		new AlertDialog.Builder(AlarmAlert.this)
				.setIcon(R.drawable.icon)
				.setTitle("提醒")
				.setMessage(remindMsg)
				.setPositiveButton("关 闭",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int whichButton) {
								AlarmAlert.this.finish();
								if (MainActivity.mediaPlayer != null)
									MainActivity.mediaPlayer.stop();
								if (MainActivity.vibrator != null)
									MainActivity.vibrator.cancel();
							}
						}).show();

	}
}
