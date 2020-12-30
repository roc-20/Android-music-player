package com.example.exp4;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
//import android.support.v4.app.ActivityCompat;
import androidx.appcompat.app.ActionBar;
import 	androidx.core.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
import androidx.core.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class PlayActivity extends AppCompatActivity implements View.OnClickListener {
    TextView tv_song,tv_end,tv_begin;
    ImageButton ib_previous,ib_play,ib_next,ib_back,ib_guanzhu;
    SeekBar sb_progress;
    ImageView iv_disk;
    //RotateAnimation animation=new RotateAnimation(0,360,50,50);
    // Button bt_down;
    private String url1;
    private String as1;
    private String name1;
    private int id1;
    ArrayList<String> url_list=new ArrayList<String>();
    ArrayList<String> as_list=new ArrayList<String>();
    //private int id1;
    private MediaPlayer mediaPlayer=new MediaPlayer();//实例化mediaplayer
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

    Timer timer;
    TimerTask timerTask;
    Boolean isChanged=false;
    private long currentPosition=0;

    private DatabaseHelper dbHelper;
    private boolean click=true;//判断是否第一次点击关注
    // Gson gson=new Gson();
    String json_as_list,json_url_list;

    Button bt_load;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.hide();
        }
        dbHelper=new DatabaseHelper(this,"LIKE.db",null,1);
        //绑定监听事件的id
        tv_song=(TextView)findViewById(R.id.tv_song);
        tv_end=(TextView)findViewById(R.id.tv_end);
        tv_begin=(TextView)findViewById(R.id.tv_begin);
        ib_previous=(ImageButton) findViewById(R.id.ib_previous);
        ib_play=(ImageButton) findViewById(R.id.ib_play);
        ib_next=(ImageButton)findViewById(R.id.ib_next);
        ib_back=(ImageButton)findViewById(R.id.ib_back);
        ib_guanzhu=(ImageButton)findViewById(R.id.ib_guanzhu);
        sb_progress=(SeekBar)findViewById(R.id.sb_progress);
        bt_load=(Button)findViewById(R.id.bt_load);
        iv_disk=(ImageView)findViewById(R.id.iv_disk);

        //设置点击事件
        ib_previous.setOnClickListener(this);
        ib_play.setOnClickListener(this);
        ib_next.setOnClickListener(this);
        ib_back.setOnClickListener(this);
        ib_guanzhu.setOnClickListener(this);
        bt_load.setOnClickListener(this);
        //seekebar的监听事件
        sb_progress.setOnSeekBarChangeListener(new MySeekBar());

        //得到下载链接
        Intent intent=this.getIntent();
        url1=intent.getStringExtra("url");//得到下载链接
        //        name1=url1.substring(35);//得到音乐文件名
        String[] sp = url1.split("/");
        name1 = sp[4];
        as1=intent.getStringExtra("as");//设置播放界面的音乐名
        id1=intent.getIntExtra("id",0);
        url_list=intent.getStringArrayListExtra("url_list");
        as_list=intent.getStringArrayListExtra("as_list");
        //打印验证
        Log.e("PlayActivity","url is "+url1);
        Log.e("PlayActivity","as is "+as1);
        Log.e("PlayActivity","name is "+name1);
        Gson gson=new Gson();
        json_as_list=gson.toJson(as_list);
        json_url_list=gson.toJson(url_list);
        Log.e("PlayActivity,","as_list is "+json_as_list);
        Log.e("PlayActivity,","url_list is "+json_url_list);
        //tv_song.setText(as1);
        InitData();

        Intent intent1=new Intent(PlayActivity.this,DownloadService.class);
        startService(intent1);//启动服务
        bindService(intent1, connection, BIND_AUTO_CREATE);//绑定服务
        // 如果没有 WRITE_EXTERNAL_STORAGE 的权限，则请求权限
        int permissionCheck = ContextCompat.checkSelfPermission(PlayActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            int SDK_VERTION_ = Build.VERSION.SDK_INT;
            ActivityCompat.requestPermissions(PlayActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    ERequestCode.WRITE_EXTERNAL_STORAGE);

//            requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
//                    1);

        }else {
            initMediaPlayer();//初始化MediaPlayer
        }

        tv_end.setText(formatime(mediaPlayer.getDuration()));//总时间
        timer=new Timer();
        timerTask=new TimerTask() {
            @Override
            public void run() {
                if (isChanged==true){
                    return;
                }
                currentPosition=mediaPlayer.getCurrentPosition();
                sb_progress.setProgress(mediaPlayer.getCurrentPosition());//设置进度
                showCurrentTime();
            }
        };
        timer.schedule(timerTask,0,10);
    }
    @Override
    public void onClick(View v) {
        if (downloadBinder==null){
            return;
        }
        File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),""+name1+"");//当前歌曲路径
        switch (v.getId()){
            case R.id.ib_back:
                PlayActivity.this.finish();
                break;
            case R.id.ib_guanzhu:
                SQLiteDatabase db=dbHelper.getWritableDatabase();
                ContentValues values=new ContentValues();
                Cursor cursor = db.rawQuery("select * from guanzhu where Aas=?",
                        new String[]{as1});////查询关注数据库中是否存在当前歌曲

                if (!cursor.moveToNext()) {//当前歌曲不存在关注列表中,则将歌曲信息加入到关注列表中，设置图标变为红色
                    values.put("Url",url1);
                    values.put("Aas",as1);
                    values.put("Url_list", json_url_list);
                    values.put("As_list", json_as_list);
                    db.insert("guanzhu",null,values);

                    Log.e("Play,","Url is "+url1);
                    Log.e("Play","Aas is "+as1);
                    Log.e("PlayActivity,","as_list is "+json_as_list);
                    Log.e("PlayActivity,","url_list is "+json_url_list);
                    ib_guanzhu.setImageResource(R.drawable.guanzhu1);
                    Toast.makeText(PlayActivity.this,"关注成功！",Toast.LENGTH_SHORT).show();

                }else {///当前歌曲存在关注列表中,
                    db.delete("guanzhu", "Url=?", new String[]{url1});
                    db.close();
                    //click = false;
                    ib_guanzhu.setImageResource(R.drawable.guanzhu);//图标变为白色
                    Toast.makeText(PlayActivity.this, "取消关注！", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.bt_load:
                if (!file.exists()){
                    downloadBinder.startDownload(url1);
                }else
                {
                    Toast.makeText(PlayActivity.this,"该歌曲已经下载！",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ib_play:
                if (file.exists()) {//文件存在，则可以播放
                    if (!mediaPlayer.isPlaying()) {
                        initMediaPlayer();//刷新界面
                        mediaPlayer.start();//开始播放
                        ib_play.setImageResource(R.drawable.pause);
                        Toast.makeText(PlayActivity.this, "开始播放！", Toast.LENGTH_SHORT).show();
                    } else {
                        mediaPlayer.pause();//暂停播放
                        ib_play.setImageResource(R.drawable.play);
                        Toast.makeText(PlayActivity.this, "暂停播放！", Toast.LENGTH_SHORT).show();
                    }
                }else {//文件不存在，则提示
                    Toast.makeText(PlayActivity.this,"该歌曲未下载！",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ib_previous:
                mediaPlayer.reset();//音乐重置
                ib_play.setImageResource(R.drawable.pause);
                id1=as_list.indexOf(as1);
                if (id1==0){
                    as1=as_list.get(as_list.size()-1);
                    url1=url_list.get(url_list.size()-1);
//                    name1=url1.substring(35);//得到音乐文件名
                    String[] sp = url1.split("/");
                    name1 = sp[4];


                    Log.e("PlayActivity::","url1 is "+url1);
                    Log.e("PlayActivity::","as1 is "+as1);
                }else {
                    as1 = as_list.get(id1 - 1);
                    url1=url_list.get(id1-1);
//                    name1=url1.substring(35);//得到音乐文件名
                    String[] sp = url1.split("/");
                    name1 = sp[4];

                    Log.e("PlayActivity::","url1 is "+url1);
                    Log.e("PlayActivity::","as1 is "+as1);

                }
                Log.e("PlayActivity:","as1 is :"+as1);
                InitData();//修改UI界面的信息
                initMediaPlayer();//初始化MediaPlayer
                break;
            case R.id.ib_next:
                mediaPlayer.reset();
                ib_play.setImageResource(R.drawable.pause);
                id1=as_list.indexOf(as1);
                if (id1==as_list.size()-1){
                    as1=as_list.get(0);
                    url1=url_list.get(0);
//                    name1=url1.substring(35);//得到音乐文件名
                    String[] sp = url1.split("/");
                    name1 = sp[4];


                    Log.e("PlayActivity::","url1 is "+url1);
                    Log.e("PlayActivity::","as1 is "+as1);
                }else {
                    as1 = as_list.get(id1 + 1);
                    url1=url_list.get(id1+1);
//                    name1=url1.substring(35);//得到音乐文件名
                    String[] sp = url1.split("/");
                    name1 = sp[4];


                    Log.e("PlayActivity::","url1 is "+url1);
                    Log.e("PlayActivity::","as1 is "+as1);

                }
                Log.e("PlayActivity:","as1 is :"+as1);
                InitData();
                initMediaPlayer();//初始化MediaPlayer
                break;
        }
    }

    //设置UI界面的显示数据
    private void InitData(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_song.setText(as1);
                //tv_end.setText(formatime(mediaPlayer.getDuration()));
                tv_begin.setText("00:00");
            }
        });
    }

    //初始化音乐播放器
    private void initMediaPlayer(){
        try{
            //  File file=new File(Environment.getExternalStorageDirectory(),""+name+"");
            File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),""+name1+"");

            mediaPlayer.setDataSource(file.getPath());//指定音频文件的路径
            mediaPlayer.prepare();//让mediaPlayer进入到准备状态
            //mediaPlayer.setLooping(true);//循环播放
            // mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            //   @Override
            // public void onPrepared(MediaPlayer mp) {
            sb_progress.setMax(mediaPlayer.getDuration());//设置进度条的最大值
            tv_end.setText(formatime(mediaPlayer.getDuration()));
            //tv_begin.setText("00:00");//开始时间
            // tv_end.setText(formatime(mediaPlayer.getDuration()));//结束时间
            //}
            //});
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //更新播放的时间
    private  void showCurrentTime(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_begin.setText(formatime(currentPosition));
            }
        });
    }

    //时间转换类，将得到的音乐时间毫秒转换为时分秒格式
    private String formatime(long length){
        Date date=new Date(length);
        SimpleDateFormat sdf=new SimpleDateFormat("mm:ss");
        String totaltime=sdf.format(date);
        return totaltime;
    }

    class MySeekBar implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser){

        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar){
            isChanged=true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mediaPlayer.seekTo(seekBar.getProgress());
            isChanged=false;
        }
    }

    //权限申请
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        switch (requestCode) {
            case ERequestCode.WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(PlayActivity.this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    initMediaPlayer();
                }
                break;

            default:
                break;
        }
    }

    //若活动销毁则对服务进行解绑
    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(connection);

        if (timerTask!=null){
            timerTask.cancel();
            timerTask=null;
        }
        if (mediaPlayer!=null){
            if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
        }

    }

}

class ERequestCode {
    public final static int WRITE_EXTERNAL_STORAGE = 255;
}