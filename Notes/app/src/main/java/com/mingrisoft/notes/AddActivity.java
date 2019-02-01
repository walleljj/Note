package com.mingrisoft.notes;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mingrisoft.notes.data.DateTimePickerDialog;
import com.mingrisoft.notes.data.DateTimePickerDialog.OnDateTimeSetListener;
import com.mingrisoft.notes.db.DatabaseOperation;
import com.mingrisoft.notes.view.LineEditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AddActivity extends Activity {
    private Button bt_back;
    private Button bt_save;
    private TextView tv_title;
    private SQLiteDatabase db;//数据库操作类
    private DatabaseOperation dop;//自定义数据库
    private LineEditText et_Notes;
    private GridView bottomMenu;
    // 底部按钮菜单按钮图片集合
    private int[] bottomItems = {R.drawable.tabbar_handwrite,
            R.drawable.tabbar_paint, R.drawable.tabbar_photo, R.drawable.tabbar_camera,
            R.drawable.tabbar_appendix};
    InputMethodManager imm;//控制手机键盘
    Intent intent;
    String editModel = null;
    int item_Id;
    String title;
    String time;
    String context;
    public String datatype = "0";// 判断是否开启记录开启了提醒功能
    public String datatime = "0";// 提醒时间
    public String locktype = "0";// 判断是否打开密码锁
    public String lock = "0";// 密码
    private RelativeLayout datarl;
    private TextView datatv;
    private ScrollView sclv;
    // 记录editText中的图片，用于单击时判断单击的是那一个图片
    private List<Map<String, String>> imgList = new ArrayList<Map<String, String>>();
    private ImageButton ib_lk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        bt_back = (Button) findViewById(R.id.bt_back);
        bt_back.setOnClickListener(new ClickEvent());
        bt_save = (Button) findViewById(R.id.bt_save);
        bt_save.setOnClickListener(new ClickEvent());
        tv_title = (TextView) findViewById(R.id.tv_title);
        et_Notes = (LineEditText) findViewById(R.id.et_note);
        bottomMenu = (GridView) findViewById(R.id.bottomMenu);
        datarl = (RelativeLayout) findViewById(R.id.datarl);
        datatv = (TextView) findViewById(R.id.datatv);
        sclv = (ScrollView) findViewById(R.id.sclv);
        ib_lk = (ImageButton) findViewById(R.id.ib_lk);
        // 配置菜单
        initBottomMenu();
        // 为菜单设置监听器
        bottomMenu.setOnItemClickListener(new MenuClickEvent());
        // 默认关闭软键盘,可以通过失去焦点设置
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_Notes.getWindowToken(), 0);
        dop = new DatabaseOperation(this, db);
        intent = getIntent();
        editModel = intent.getStringExtra("editModel");
        item_Id = intent.getIntExtra("noteId", 0);
        // 加载数据
        loadData();
        // 给editText添加单击事件
        et_Notes.setOnClickListener(new TextClickEvent());
    }

    // 加载数据
    private void loadData() {
        // 如果是新增记事模式，则将editText清空
        if (editModel.equals("newAdd")) {
            et_Notes.setText("");
        }
        // 如果编辑的是已存在的记事，则将数据库的保存的数据取出，并显示在EditText中
        else if (editModel.equals("update")) {
            tv_title.setText("编辑记事");
            dop.create_db();
            Cursor cursor = dop.query_db(item_Id);
            cursor.moveToFirst();
            // 取出数据库中相应的字段内容
            context = cursor.getString(cursor.getColumnIndex("context"));
            datatype = cursor.getString(cursor.getColumnIndex("datatype"));
            datatime = cursor.getString(cursor.getColumnIndex("datatime"));
            locktype = cursor.getString(cursor.getColumnIndex("locktype"));
            lock = cursor.getString(cursor.getColumnIndex("lock"));
            if ("0".equals(locktype)) {
                ib_lk.setBackgroundResource(R.drawable.un_locky);
            } else {
                ib_lk.setBackgroundResource(R.drawable.locky);
            }
            if ("0".equals(datatype)) {
                datarl.setVisibility(View.GONE);
            } else {
                datarl.setVisibility(View.VISIBLE);
                datatv.setText("提醒时间：" + datatime);
            }
            // 定义正则表达式，用于匹配路径
            Pattern p = Pattern.compile("/([^\\.]*)\\.\\w{3}");
            Matcher m = p.matcher(context);
            int startIndex = 0;
            while (m.find()) {
                // 取出路径前的文字
                if (m.start() > 0) {
                    et_Notes.append(context.substring(startIndex, m.start()));
                }
                SpannableString ss = new SpannableString(m.group().toString());
                // 取出路径
                String path = m.group().toString();
                // 取出路径的后缀
                String type = path.substring(path.length() - 3, path.length());
                Bitmap bm = null;
                Bitmap rbm = null;
                // 判断附件的类型，如果是录音文件，则从资源文件中加载图片
                if (type.equals("amr")) {
                    bm = BitmapFactory.decodeResource(getResources(),
                            R.drawable.record_icon);
                    // 缩放图片
                    rbm = resize(bm, 400);
                } else {
                    // 取出图片
                    bm = BitmapFactory.decodeFile(m.group());
                    // 缩放图片
                    rbm = resize(bm, 480);
                    // 为图片添加边框效果
                    rbm = getBitmapHuaSeBianKuang(rbm);
                }
                ImageSpan span = new ImageSpan(this, rbm);
                ss.setSpan(span, 0, m.end() - m.start(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                System.out.println(m.start() + "-------" + m.end());
                et_Notes.append(ss);
                startIndex = m.end();
                // 用List记录该录音的位置及所在路径，用于单击事件
                Map<String, String> map = new HashMap<String, String>();
                map.put("location", m.start() + "-" + m.end());
                map.put("path", path);
                imgList.add(map);
            }
            // 将最后一个图片之后的文字添加在TextView中
            et_Notes.append(context.substring(startIndex, context.length()));
            dop.close_db();
        }
    }

    // 为EidtText设置监听器
    class TextClickEvent implements OnClickListener {
        @Override
        public void onClick(View v) {
            Spanned s = et_Notes.getText();
            ImageSpan[] imageSpans;
            imageSpans = s.getSpans(0, s.length(), ImageSpan.class);
            int selectionStart = et_Notes.getSelectionStart();
            for (ImageSpan span : imageSpans) {
                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                // 找到图片
                if (selectionStart >= start && selectionStart < end) {
                    // 查找当前单击的图片是哪一个图片
                    String path = null;
                    for (int i = 0; i < imgList.size(); i++) {
                        Map map = imgList.get(i);
                        // 找到了
                        if (map.get("location").equals(start + "-" + end)) {
                            path = imgList.get(i).get("path");
                            break;
                        }
                    }
                    // 接着判断当前图片是否是录音，如果为录音，则跳转到试听录音的Activity，如果不是，则跳转到查看图片的界面
                    // 录音，则跳转到试听录音的Activity
                    if (path.substring(path.length() - 3, path.length())
                            .equals("amr")) {
                        Intent intent = new Intent(AddActivity.this,
                                ShowRecord.class);
                        intent.putExtra("audioPath", path);
                        startActivity(intent);
                    }
                    // 图片，则跳转到查看图片的界面
                    else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        File file = new File(path);
                        Uri uri = Uri.fromFile(file);
                        intent.setDataAndType(uri, "image/*");
                        startActivity(intent);
                    }
                } else
                    // 如果单击的是空白出或文字，则获得焦点，即打开软键盘
                    imm.showSoftInput(et_Notes, 0);
            }
        }
    }

    // 给编辑区域设置触摸监听器
    class TextTouchEvent implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Spanned s = et_Notes.getText();
            ImageSpan[] imageSpans;
            imageSpans = s.getSpans(0, s.length(), ImageSpan.class);
            int selectionStart = et_Notes.getSelectionStart();
            for (ImageSpan span : imageSpans) {
                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                int inType = et_Notes.getInputType(); // backup the input type
                // 找到图片
                if (selectionStart >= start && selectionStart < end) {
                    Bitmap bitmap = ((BitmapDrawable) span.getDrawable())
                            .getBitmap();
                    et_Notes.setInputType(InputType.TYPE_NULL); // disable soft
                    et_Notes.onTouchEvent(event); // call native handler
                    et_Notes.setInputType(inType); // restore input type
                    AddActivity.this.finish();
                } else {
                    // 如果单击的是空白出或文字，则获得焦点，即打开软键盘
                    imm.showSoftInput(et_Notes, 0);
                    et_Notes.setInputType(inType);
                }
            }
            return true;
        }
    }

    // 设置按钮监听器
    class ClickEvent implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_back:
                    // 当前Activity结束，则返回上一个Activity
                    AddActivity.this.finish();
                    break;
                // 将记事添加到数据库中
                case R.id.bt_save:
                    // 取得EditText中的内容
                    context = et_Notes.getText().toString();
                    if (context.isEmpty()) {
                        Toast.makeText(AddActivity.this, "记事为空!", Toast.LENGTH_LONG)
                                .show();
                    } else {
                        // 取得当前时间
                        SimpleDateFormat formatter = new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm");
                        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
                        time = formatter.format(curDate);
                        // 截取EditText中的前一部分作为标题，用于显示在主页列表中
                        title = getTitle(context);
                        // 打开数据库
                        dop.create_db();
                        // 判断是更新还是新增记事
                        if (editModel.equals("newAdd")) {
                            // 将记事插入到数据库中
                            dop.insert_db(title, context, time, datatype, datatime,
                                    locktype, lock);
                        }
                        // 如果是编辑则更新记事即可
                        else if (editModel.equals("update")) {
                            dop.update_db(title, context, time, datatype, datatime,
                                    locktype, lock, item_Id);
                        }
                        dop.close_db();
                        // 结束当前activity
                        AddActivity.this.finish();
                    }
                    break;
            }
        }
    }

    // 截取EditText中的前一部分作为标题，用于显示在主页列表中
    private String getTitle(String context) {
        // 定义正则表达式，用于匹配路径
        Pattern p = Pattern.compile("/([^\\.]*)\\.\\w{3}");
        Matcher m = p.matcher(context);
        StringBuffer strBuff = new StringBuffer();
        String title = "";
        int startIndex = 0;
        while (m.find()) {
            // 取出路径前的文字
            if (m.start() > 0) {
                strBuff.append(context.substring(startIndex, m.start()));
            }
            // 取出路径
            String path = m.group().toString();
            // 取出路径的后缀
            String type = path.substring(path.length() - 3, path.length());
            // 判断附件的类型
            if (type.equals("amr")) {
                strBuff.append("[录音]");
            } else {
                // strBuff.append("");
            }
            startIndex = m.end();
            // 只取出前15个字作为标题
            if (strBuff.length() > 15) {
                // 统一将回车,等特殊字符换成空格
                title = strBuff.toString().replaceAll("\r|\n|\t", " ");
                return title;
            }
        }
        strBuff.append(context.substring(startIndex, context.length()));
        // 统一将回车,等特殊字符换成空格
        title = strBuff.toString().replaceAll("\r|\n|\t", " ");
        return title;
    }

    // 配置菜单
    private void initBottomMenu() {
        //菜单集合
        ArrayList<Map<String, Object>> menus = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < bottomItems.length; i++) {//循环菜单集合
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("image", bottomItems[i]);//循环图片集合添加到菜单中
            menus.add(item);//添加图片菜单到底部菜单
        }
        //菜单长度
        bottomMenu.setNumColumns(bottomItems.length);
        //底部菜单
        bottomMenu.setSelector(R.drawable.bottom_item);
        //实例化底部菜单适配器
        SimpleAdapter mAdapter = new SimpleAdapter(AddActivity.this, menus,
                R.layout.item_button, new String[]{"image"},
                new int[]{R.id.item_image});
        bottomMenu.setAdapter(mAdapter);//为底部菜单添加适配器
    }

    // 设置菜单项监听器
    class MenuClickEvent implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Intent intent;
            switch (position) {
                // 手写
                case 0:
                    intent = new Intent(AddActivity.this, HandWriteActivity.class);
                    startActivityForResult(intent, 5);
                    break;
                // 绘图
                case 1:
                    intent = new Intent(AddActivity.this, PaintActivity.class);
                    startActivityForResult(intent, 3);
                    break;

                // 照片
                case 2:
                    // 添加图片的主要代码
                    intent = new Intent();
                    // 设定类型为image
                    intent.setType("image/*");
                    // 设置action
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    // 选中相片后返回本Activity
                    startActivityForResult(intent, 1);
                    break;
                // 拍照
                case 3:
                    if (Build.VERSION.SDK_INT >= 23) {

                        if(ContextCompat.checkSelfPermission(AddActivity.this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
                            //申请权限
                            ActivityCompat.requestPermissions(AddActivity.this,
                                    new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    1);
                        }else {
                            // 调用系统拍照界面
                            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            // 区分选择相片
                            startActivityForResult(intent, 2);
                        }
                    } else {
                        // Pre-Marshmallow
                    // 调用系统拍照界面
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    // 区分选择相片
                    startActivityForResult(intent, 2);
                    }

                    break;
                // 提醒设置
                case 4:
                    setReminder();
                    break;

            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 调用系统拍照界面
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 区分选择相片
                startActivityForResult(intent, 2);
            } else {
                // Permission Denied
            }
        }
    }
    // 提醒设置
    private void setReminder() {
        DateTimePickerDialog d;
        if ("0".equals(datatime)) {//判断是否设置过事件０没有设置过提醒时间
            d = new DateTimePickerDialog(this, System.currentTimeMillis());//设置自定义时间弹出显示系统时间
        } else {
            d = new DateTimePickerDialog(this, getdaytime(datatime));//设置自定义时间弹出显示设置过的时间
        }
        d.setOnDateTimeSetListener(new OnDateTimeSetListener() {
            public void OnDateTimeSet(AlertDialog dialog, long date) {
                // 取得当前时间
                SimpleDateFormat formatter = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm");//设计格式
                datatime = formatter.format(date);//以自己设置的时间格式显示时间 date为当前选择的时间
                datatype = "1";
                datarl.setVisibility(View.VISIBLE);
                datatv.setText("提醒时间：" + datatime);
            }
        });
        d.show();
    }

    public static long getdaytime(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date dt2 = null;
        try {
            dt2 = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt2.getTime();
    }
    //数据回调方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 取得数据
            Uri uri = data.getData();
            //实例化数据表
            ContentResolver cr = AddActivity.this.getContentResolver();
            //图片用于储存选择后转换成Bitmap类型
            Bitmap bitmap = null;
            //接收返回信息
            Bundle extras = null;
            // 如果是选择照片
            if (requestCode == 1) {
                // 取得选择照片的路径
                String[] proj = {MediaStore.Images.Media.DATA};
                //用于查询指定图片位置
                Cursor actualimagecursor = managedQuery(uri, proj, null, null,
                        null);
                //读取媒体数据库
                int actual_image_column_index = actualimagecursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                //移动数据到第一个
                actualimagecursor.moveToFirst();
                //图片路径
                String path = actualimagecursor
                        .getString(actual_image_column_index);
                try {
                    // 将对象存入Bitmap中
                    bitmap = BitmapFactory
                            .decodeStream(cr.openInputStream(uri));
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // 插入图片
                InsertBitmap(bitmap, 480, path);
            }
            // 选择的是拍照
            else
            if (requestCode == 2) {
                try {
                    if (uri != null){
                        // 这个方法是根据Uri获取Bitmap图片的静态方法
                        bitmap = MediaStore.Images.Media.getBitmap(cr, uri);
                        // 这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片
                    }else {
                        //得到返回数据
                        extras = data.getExtras();
                        //获取图片
                        bitmap = extras.getParcelable("data");
                    }
                    // 将拍的照片存入指定的文件夹下
                    // 获得系统当前时间，并以该时间作为文件名
                    SimpleDateFormat formatter = new SimpleDateFormat(
                            "yyyyMMddHHmmss");
                    // 获取当前时间
                    Date curDate = new Date(System.currentTimeMillis());
                    // 当前时间保存成String类型
                    String str = formatter.format(curDate);
                    //用于记录图片路径
                    String paintPath = "";
                    //图片路径
                    str = str + "paint.png";
                    //新建文件夹
                    File dir = new File("/sdcard/notes/");
                    //新建文件
                    File file = new File("/sdcard/notes/", str);
                    if (!dir.exists()) {// 判断文件夹创建是否成功
                        dir.mkdir();// 创建文件夹
                    } else {
                        if (file.exists()) {// 判断文件是否创建
                            file.delete();// 删除文件
                        }
                    }
                    //新建文件流
                    FileOutputStream fos = new FileOutputStream(file);
                    // 将 bitmap 压缩成其他格式的图片数据
                    bitmap.compress(CompressFormat.PNG, 100, fos);
                    fos.flush();//结束流传输
                    fos.close();//关闭流
                    //图片路径
                    String path = "/sdcard/notes/" + str;
                    //插入图片
                    InsertBitmap(bitmap, 480, path);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // 返回的是绘图后的结果
            else if (requestCode == 3) {
                //创建接收器
                extras = data.getExtras();
                //接收返回的信息
                String path = extras.getString("paintPath");
                // 通过路径取出图片，放入bitmap中
                bitmap = BitmapFactory.decodeFile(path);
                // 插入绘图文件
                InsertBitmap(bitmap, 480, path);
            }
            // 返回的是录音文件
            else if (requestCode == 4) {
                //创建接收器
                extras = data.getExtras();
                //接收返回的信息
                String path = extras.getString("audio");
                //转换图片成bitmap形式
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.record_icon);
                // 插入录音图标
                InsertBitmap(bitmap, 200, path);
            }
            // 返回的是手写文件
            else if (requestCode == 5) {
                extras = data.getExtras();
                String path = extras.getString("handwritePath");
                // 通过路径取出图片，放入bitmap中
                bitmap = BitmapFactory.decodeFile(path);
                // 插入绘图文件
                InsertBitmap(bitmap, 480, path);
            }
        }
    }

    // 将图片等比例缩放到合适的大小并添加在EditText中
    void InsertBitmap(Bitmap bitmap, int S, String imgPath) {
        bitmap = resize(bitmap, S);
        // 添加边框效果
        // bitmap = getBitmapHuaSeBianKuang(bitmap);
        // bitmap = addBigFrame(bitmap,R.drawable.line_age);
        final ImageSpan imageSpan = new ImageSpan(this, bitmap);
        SpannableString spannableString = new SpannableString(imgPath);
        spannableString.setSpan(imageSpan, 0, spannableString.length(),
                SpannableString.SPAN_MARK_MARK);
        // 光标移到下一行
        // et_Notes.append("\n");
        Editable editable = et_Notes.getEditableText();
        int selectionIndex = et_Notes.getSelectionStart();
        spannableString.getSpans(0, spannableString.length(), ImageSpan.class);
        // 将图片添加进EditText中
        editable.insert(selectionIndex, spannableString);
        // 添加图片后自动空出两行
        et_Notes.append("\n");
        // 用List记录该录音的位置及所在路径，用于单击事件
        Map<String, String> map = new HashMap<String, String>();
        map.put("location", selectionIndex + "-"
                + (selectionIndex + spannableString.length()));
        map.put("path", imgPath);
        imgList.add(map);
    }

    // 等比例缩放图片
    private Bitmap resize(Bitmap bitmap, int S) {
        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();
        double partion = imgWidth * 1.0 / imgHeight;
        double sqrtLength = Math.sqrt(partion * partion + 1);
        // 新的缩略图大小
        double newImgW = S * (partion / sqrtLength);
        double newImgH = S * (1 / sqrtLength);
        float scaleW = (float) (newImgW / imgWidth);
        float scaleH = (float) (newImgH / imgHeight);
        Matrix mx = new Matrix();
        // 对原图片进行缩放
        mx.postScale(scaleW, scaleH);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx,
                true);
        return bitmap;
    }

    // 给图片加边框，并返回边框后的图片
    public Bitmap getBitmapHuaSeBianKuang(Bitmap bitmap) {
        float frameSize = 0.2f;
        Matrix matrix = new Matrix();

        // 用来做底图
        Bitmap bitmapbg = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        // 设置底图为画布
        Canvas canvas = new Canvas(bitmapbg);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));

        float scale_x = (bitmap.getWidth() - 2 * frameSize - 2) * 1f
                / (bitmap.getWidth());
        float scale_y = (bitmap.getHeight() - 2 * frameSize - 2) * 1f
                / (bitmap.getHeight());
        matrix.reset();
        matrix.postScale(scale_x, scale_y);
        // 对相片大小处理(减去边框的大小)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1);
        paint.setStyle(Style.FILL);

        // 绘制底图边框
        canvas.drawRect(
                new Rect(0, 0, bitmapbg.getWidth(), bitmapbg.getHeight()),
                paint);

        // 绘制灰色边框
        paint.setColor(Color.GRAY);
        canvas.drawRect(
                new Rect((int) (frameSize), (int) (frameSize), bitmapbg
                        .getWidth() - (int) (frameSize), bitmapbg.getHeight()
                        - (int) (frameSize)), paint);

        canvas.drawBitmap(bitmap, frameSize + 1, frameSize + 1, paint);
        return bitmapbg;
    }

    // 取消闹钟
    public void onDataCancel(View v) {
        datarl.setVisibility(View.GONE);
        datatype = "0";// 判断是否开启记录开启了提醒功能
        datatime = "0";
    }

    // 修改闹钟提醒时间
    public void onDataChange(View v) {
        setReminder();
    }

    // 添加日记锁 取消日记锁
    public void onLOCK(View v) {
        if ("0".equals(locktype)) {//判断是否设置了密码
            inputlockDialog();//弹出设置密码弹窗
        } else {
            inputunlockDialog();//弹出取消密码弹窗
        }
    }

    //取消密码
    private void inputunlockDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//创建弹出框
        builder.setTitle("是否取消密码")
                .setNegativeButton("取消", null);//在弹窗上设置标题设置取消按钮
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {//设置确认按钮
                locktype = "0";//设置没有设置密码
                lock = "0";//设置密码
                ib_lk.setBackgroundResource(R.drawable.un_locky);
                Toast.makeText(AddActivity.this, "密码已取消",
                        Toast.LENGTH_LONG).show();
            }
        });
        builder.show();//弹出取消密码弹窗
    }

    //设置密码弹窗
    private void inputlockDialog() {
        final EditText inputServer = new EditText(this);//创建EditText输入框
        inputServer.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);//设置输入框类型
        inputServer.setFocusable(true);//获取焦点
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//创建弹出框
        builder.setTitle("设置密码").setView(inputServer)
                .setNegativeButton("取消", null);//在弹窗上设置标题添加输入框
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {//设置确认按钮
                String inputName = inputServer.getText().toString();
                if ("".equals(inputName)) {//判断输入框内容是否为空
                    Toast.makeText(AddActivity.this, "密码不能为空 请重新输入！",
                            Toast.LENGTH_LONG).show();
                } else {//输入框内容不为空　
                    lock = inputName;//密码
                    locktype = "1";//添加了密码锁
                    ib_lk.setBackgroundResource(R.drawable.locky);//设置添加锁图案
                    Toast.makeText(AddActivity.this, "密码设置成功！",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.show();//弹出设置密码弹窗
    }

    // 分享功能
    public void onFX(View v) {
        Bitmap c = getBitmapByView(sclv);//获取到图片
        try {
            saveMyBitmap("notesimge", c);//保存图片
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String imagePath = Environment.getExternalStorageDirectory()
                + File.separator + "notesimge.jpg";//图片路径
        // 由文件得到uri
        Uri imageUri = Uri.fromFile(new File(imagePath));
        Log.d("share", "uri:" + imageUri);//打印图片路径
        Intent shareIntent = new Intent();//创建意图
        shareIntent.setAction(Intent.ACTION_SEND);//过滤条件允许分享的
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);//分享图片
        shareIntent.setType("image/*");//设置类型
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//默认跳转类型
        startActivity(Intent.createChooser(shareIntent, "分享到："));
    }

    //保存图片
    public void saveMyBitmap(String bitName, Bitmap mBitmap) throws IOException {
        File f = new File(Environment.getExternalStorageDirectory()
                + File.separator + "notesimge.jpg");//初始化文件
        f.createNewFile();//创建图片文件
        FileOutputStream fOut = null;//创建文件流
        try {
            fOut = new FileOutputStream(f);//实例化文件流
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);//图片保存到文件中
        try {
            fOut.flush();//文件写写入操作结束
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();//关闭文件流
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //截取scrollview的屏幕
    public static Bitmap getBitmapByView(ScrollView scrollView) {
        int h = 0;//设置高度0
        Bitmap bitmap = null;//设置空的图片
        // 获取scrollview实际高度
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            h += scrollView.getChildAt(i).getHeight();//计算scrollView实际高度
            scrollView.getChildAt(i).setBackgroundColor(
                    Color.parseColor("#FFFFFF"));//设置scrollView背景颜色
        }
        // 创建scrollView大小的bitmap
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h,
                Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);//绘制图片
        scrollView.draw(canvas);//绘制scrollView
        return bitmap;//返回图片
    }
}
