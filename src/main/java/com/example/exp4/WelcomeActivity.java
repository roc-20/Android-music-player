package com.example.exp4;

import android.content.Intent;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {
    Button bt_list;
    Button bt_like;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        bt_like=(Button)findViewById(R.id.bt_like);
        bt_list=(Button)findViewById(R.id.bt_list);
        bt_list.setOnClickListener(this);
        bt_like.setOnClickListener(this);
    }
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.bt_list:
                Intent intent=new Intent(WelcomeActivity.this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.bt_like:
                Intent intent1=new Intent(WelcomeActivity.this,LikeActivity.class);
                startActivity(intent1);
                break;
        }
    }
}
