package com.mingrisoft.notes;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class XiugaimimaActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xiugaimima);
    }
    public void onTui(View view){
        XiugaimimaActivity.this.finish();
    }
    public void onOk(View view){
        EditText oldT = (EditText)findViewById(R.id.editText3);
        EditText newT1 = (EditText)findViewById(R.id.editText4);
        EditText newT2 = (EditText)findViewById(R.id.editText5);
        String old = oldT.getText().toString();
        String new1 = newT1.getText().toString();
        String new2 = newT2.getText().toString();
        String yuan = getPass();
        if(yuan.equals(old)){
            if(new1.equals(new2)){
                writePass(new1);
                Toast.makeText(XiugaimimaActivity.this,"密码修改成功 新密码："+new1,Toast.LENGTH_LONG).show();
                XiugaimimaActivity.this.finish();
            }else
                Toast.makeText(XiugaimimaActivity.this,"两次密码不同",Toast.LENGTH_LONG).show();

        }else{
            Toast.makeText(XiugaimimaActivity.this,"原始密码错误",Toast.LENGTH_LONG).show();
        }

    }

    public String getPass() {
        String str = null;
        File f = new File( getFilesDir(), "abc.txt");
        FileInputStream fstream = null;
        try{
            fstream = new FileInputStream(f);
            byte[] data = new byte[1024]; // 当文件很大时，应分作多次读取, 这里是简易写法仅作演示
            int n = fstream.read(data);
            if(n> 0)
                // 当读取的到byte[]转成String
                str = new String(data,0, n, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally
        {
            try{ fstream.close();}catch (Exception e){}
        }
        return str;
    }

    public void writePass(String str) {
        String mi = str;
        File appDir = getFilesDir();
        File f = new File(appDir, "abc.txt");
        FileOutputStream fstream = null;
        try {
            fstream = new FileOutputStream(f);
            fstream.write(mi.getBytes("UTF-8"));
        } catch (Exception e) {
        } finally {
            try {
                fstream.close();
            } catch (Exception e2) {
            }
        }
    }
}
