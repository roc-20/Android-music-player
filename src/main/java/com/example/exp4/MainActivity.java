package com.example.exp4;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
//import android.support.v4.app.ActivityCompat;
import 	androidx.core.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
import androidx.core.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView lv;
    ImageButton ib_back1;
    ArrayList<String> url_list=new ArrayList<String>();
    ArrayList<String> as_list=new ArrayList<String>();
    ArrayList<Integer> id_list=new ArrayList<Integer>();

    private String url1,as1;
    private DownloadService.DownloadBinder downloadBinder;//服务与活动间的通信
    private ServiceConnection connection=new ServiceConnection() {//ServiceConnection匿名类，
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder=(DownloadService.DownloadBinder) service;//获取downloadBinder实例，用于在活动中调用服务提供的各种方法
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.hide();
        }
        ib_back1=(ImageButton)findViewById(R.id.ib_back1);
        ib_back1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.finish();
            }
        });
        Intent intent1=new Intent(MainActivity.this,DownloadService.class);
        startService(intent1);//启动服务
        bindService(intent1,connection,BIND_AUTO_CREATE);//绑定服务
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);}

        parseHTMLwithJSOUP();//将html数据解析出来并传到界面上
    }

    //解析html数据
    public void parseHTMLwithJSOUP() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect("https://freemusicarchive.org/genre/Classical/").timeout(60000).get();
                    Elements urls = doc.select("a.icn-arrow");
                    Elements artists = doc.select("span.ptxt-artist");
                    Elements songs = doc.select("span.ptxt-track");
                    for (int i = 0; i < urls.size(); i++) {
//                        String url = urls.get(i).attr("href"); //
                        String url = urls.get(i).attr("data-url");

                        String artist = artists.get(i + 1).text();
                        String song = songs.get(i + 1).text();
                        Log.e("URL:", url);
                        Log.e("ARTIST:", artist);
                        Log.e("SONG:", song);
                        id_list.add(i);
                        url_list.add(url);
                        as_list.add(song + "\n" + artist);
                    }
                    showResponse();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //显示在界面上
    private  void showResponse() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lv=(ListView)findViewById(R.id.lv);
                ArrayAdapter<String> adapter=new ArrayAdapter<String>(
                        MainActivity.this,android.R.layout.simple_list_item_1,as_list);
                lv.setAdapter(adapter);

                //点击item事件
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        //得到当前歌曲的相关信息
                        url1=url_list.get(position);//得到歌曲下载链接
//                        String name=url1.substring(35);
                        String[] sp = url1.split("/");
                        String name = sp[4];

                        // int id1=id_list.get(position);
                        as1=as_list.get(position);//得到song和artist
                        Log.d("MainActivity:","url is " +url1);

                        //如果歌曲不存在，则先下载，如果存在，则直接跳转
                        if (downloadBinder==null){
                            Toast.makeText(MainActivity.this, "歌曲不存在", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),""+name+"");

                        if (!file.exists()) {
                            downloadBinder.startDownload(url1);//若音乐文件不存在，则进行下载
                        }

                        Intent intent=new Intent(MainActivity.this,PlayActivity.class);
                        intent.putExtra("url",url1);
                        intent.putExtra("as",as1);
                        // intent.putExtra("id",id1);
                        intent.putExtra("url_list",url_list);
                        intent.putExtra("as_list",as_list);

                        startActivity(intent);

                    }
                });
            }
        });
    }

}
