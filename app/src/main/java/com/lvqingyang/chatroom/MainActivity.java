package com.lvqingyang.chatroom;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private android.widget.EditText etnick;
    private android.widget.EditText etchatroom;
    private android.widget.Button btncomein;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init view
        this.btncomein = (Button) findViewById(R.id.btn_come_in);
        this.etchatroom = (EditText) findViewById(R.id.et_chat_room);
        this.etnick = (EditText) findViewById(R.id.et_nick);

        btncomein.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转聊天界面
                ChatActivity.start(MainActivity.this, etnick.getText().toString(),
                        etchatroom.getText().toString());
            }
        });
    }

}
