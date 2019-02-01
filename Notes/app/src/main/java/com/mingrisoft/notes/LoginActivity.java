package com.mingrisoft.notes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // 查看本app的私有目录

    }

    //登录按钮点击事件
    public void onLogin(View view){
        EditText edt = (EditText)findViewById(R.id.mima);
        String pass2 = edt.getText().toString();
        if(!fileIsExists()){
            //创建密码“0000”
            String mi = "0000";
            File appDir = getFilesDir();
            File f = new File(appDir, "abc.txt");
            FileOutputStream fstream = null;
            try{
                fstream = new FileOutputStream(f);
                fstream.write( mi.getBytes("UTF-8"));
            }catch(Exception e){
            }
            finally {
                try{ fstream.close();} catch (Exception e2){}
            }
            //Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
            if(pass2.equals("0")){
                Log.d("测试","登录成功！！！");
                Toast.makeText(LoginActivity.this,"登录成功1",Toast.LENGTH_LONG).show();
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                finish();
            }else
                Toast.makeText(LoginActivity.this,"密码错误！！",Toast.LENGTH_LONG).show();
        } else {
                File f = new File( getFilesDir(), "abc.txt");
                FileInputStream fstream = null;
                try{
                    fstream = new FileInputStream(f);
                    byte[] data = new byte[1024]; // 当文件很大时，应分作多次读取, 这里是简易写法仅作演示
                    int n = fstream.read(data);
                    if(n> 0)
                    {
                        // 当读取的到byte[]转成String
                        String str = new String(data,0, n, "UTF-8");
                        Log.d("测试", "读取密码: " + str);
                        if(pass2.equals(str)){
                            Log.d("测试","登录成功23123123！！！");
                            Toast.makeText(LoginActivity.this,"登录成功2",Toast.LENGTH_LONG).show();
                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                            finish();
                        }else
                            Toast.makeText(LoginActivity.this,"密码错误！！！002"+str,Toast.LENGTH_LONG).show();
                    }
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


            }
//        if(pass.equals("1111")){
//            Log.d("测试","登录成功！！！");
//            Toast.makeText(LoginActivity.this,"登录成功",Toast.LENGTH_LONG).show();
//            startActivity(new Intent(LoginActivity.this,Main2Activity.class));
//            finish();
//
//        }else {
//            Toast.makeText(LoginActivity.this,"登录失败，密码错误",Toast.LENGTH_LONG).show();
//        }
    }
    //退出按钮点击事件
    public void onTuichu(View view){
        this.finish();

    }
    //点击关于按钮的事件
    public void onAbout(View view){
        Toast.makeText(LoginActivity.this,"关于！",Toast.LENGTH_LONG).show();
        startActivity(new Intent(LoginActivity.this,Main2Activity.class));


        Log.d("测试","点击了关于按钮");

    }
    //判断是否设置过密码
    public boolean fileIsExists(){
        try{
            File f=new File(getFilesDir()+"/abc.txt");
            if(!f.exists()){
                Log.d("测试","不存在");
                return false;
            }
        }catch (Exception e) {
            // TODO: handle exception
            Log.d("测试","不存在chucuo");
            return false;
        }
        Log.d("测试","存在");
        return true;
    }

}
