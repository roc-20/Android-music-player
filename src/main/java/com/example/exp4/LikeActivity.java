package com.example.exp4;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class LikeActivity extends AppCompatActivity {
    ListView lv_like;
    ImageButton iv_back2;
    DatabaseHelper dbHelper;
    ArrayList<Integer> id_list=new ArrayList<Integer>();
    ArrayList<String> u_list=new ArrayList<String>();
    ArrayList<String> a_list=new ArrayList<String>();
    ArrayList<String>  J_url_list=new ArrayList<String>();
    ArrayList<String>  J_as_list=new ArrayList<String>();
    private String Url,As;
    private String json_url_list,json_as_list;
    //private String J_url_list,J_as_list;
    private int Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like);
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.hide();
        }
        iv_back2=(ImageButton)findViewById(R.id.ib_back2);
        iv_back2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LikeActivity.this.finish();
            }
        });
        dbHelper = new DatabaseHelper(this,"LIKE.db",null,1);

        InitData();
        lv_like=(ListView)findViewById(R.id.lv_like);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(
                LikeActivity.this,
                android.R.layout.simple_list_item_1,
                a_list);
        lv_like.setAdapter(adapter);

        lv_like.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent=new Intent(LikeActivity.this,PlayActivity.class);
            intent.putExtra("url",Url);
            intent.putExtra("as",As);
            // intent.putExtra("id",id1);
            intent.putExtra("url_list",J_url_list);
            intent.putExtra("as_list",J_as_list);
            startActivity(intent);
        });

    }
    public void InitData(){
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        Cursor cursor=db.query("guanzhu",new String[]{"Id",
                        "Url","Aas","Url_list","As_list"},null,null,
                null,null,"Id desc");
        id_list.clear();
        u_list.clear();
        a_list.clear();
        if (cursor.moveToFirst()){
            do{
                Id=cursor.getInt(cursor.getColumnIndex("Id"));
                Url=cursor.getString(cursor.getColumnIndex("Url"));
                As=cursor.getString(cursor.getColumnIndex("Aas"));
                json_url_list=cursor.getString(cursor.getColumnIndex("Url_list"));
                json_as_list=cursor.getString(cursor.getColumnIndex("As_list"));
                Gson gson=new Gson();
                Type type=new TypeToken<ArrayList<String>>(){}.getType();
                J_url_list=gson.fromJson(json_url_list, type);//得到所有的歌曲链接list
                J_as_list=gson.fromJson(json_as_list,type);//得到所有的song和artist列表
                Log.e("LikeActivity,","Url is "+Url);
                Log.e("LikeActivity,","As is "+As);
                id_list.add(Id);
                u_list.add(Url);
                a_list.add(As);
                Log.e("Like:","a_list is "+a_list);
            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    @Override
    public void onRestart(){
        super.onRestart();
        dbHelper=new DatabaseHelper(this,"LIKE.db",null,1);
        InitData();
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(
                LikeActivity.this,android.R.layout.simple_list_item_1,a_list);
        lv_like=(ListView)findViewById(R.id.lv_like);
        lv_like.setAdapter(adapter);
        lv_like.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(LikeActivity.this,PlayActivity.class);
                intent.putExtra("url",Url);
                intent.putExtra("as",As);
                // intent.putExtra("id",id1);
                intent.putExtra("url_list",J_url_list);
                intent.putExtra("as_list",J_as_list);
                startActivity(intent);
            }
        });
    }
}
