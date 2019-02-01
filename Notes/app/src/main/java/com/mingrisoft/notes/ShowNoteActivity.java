package com.mingrisoft.notes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mingrisoft.notes.db.DatabaseOperation;
import com.mingrisoft.notes.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.TextView;

public class ShowNoteActivity extends Activity {

	private SQLiteDatabase db;
	private DatabaseOperation dop;
	private TextView tv_note;
	private static String IMGPATH = "/sdcard/notes/yyyyMMddHHmmsspaint.png";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shownote);

		tv_note = (TextView)findViewById(R.id.tv_note_content);
		dop = new DatabaseOperation(this,db);

		Intent intent = this.getIntent();
		int item_Id = intent.getIntExtra("noteId", 0);
		dop.create_db();
		Cursor cursor = dop.query_db(item_Id);
		cursor.moveToFirst();
		//取出数据库中相应的字段内容
		String context = cursor.getString(cursor.getColumnIndex("context"));
		SpannableString ss = new SpannableString(IMGPATH);
		//定义正则表达式，用于匹配路径
		Pattern p=Pattern.compile("/sdcard/notes/.+?\\.\\w{3}");
		Matcher m=p.matcher(context);
		int startIndex = 0;
		while(m.find()){
			//取出路径前的文字
			if(m.start() > 0){
				tv_note.append(context.substring(startIndex, m.start()));
			}
			//取出图片，并添加在textView中
			Bitmap bm = BitmapFactory.decodeFile(m.group());
			Bitmap rbm = resizeImage(bm,480);
			System.out.println(rbm.getWidth()+"-------"+rbm.getHeight());
			ImageSpan span = new ImageSpan(this, rbm);
			ss.setSpan(span,0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			tv_note.append(ss);
			startIndex = m.end();
		}
		//将最后一个图片之后的文字添加在TextView中
		tv_note.append(context.substring(startIndex,context.length()));
		dop.close_db();

	}
	private Bitmap resizeImage(Bitmap bitmap,int S) {
		int imgWidth = bitmap.getWidth();
		int imgHeight = bitmap.getHeight();
		double partion = imgWidth*1.0/imgHeight;
		double sqrtLength = Math.sqrt(partion*partion + 1);
		//新的缩略图大小
		double newImgW = S*(partion / sqrtLength);
		double newImgH = S*(1 / sqrtLength);
		float scaleW = (float) (newImgW/imgWidth);
		float scaleH = (float) (newImgH/imgHeight);

		Matrix mx = new Matrix();
		//对原图片进行缩放
		mx.postScale(scaleW, scaleH);
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx, true);
		return bitmap;
	}

}
