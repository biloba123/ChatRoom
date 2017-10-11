package com.lvqingyang.chatroom;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lvqingyang.chatroom.bean.Message;
import com.lvqingyang.chatroom.mqtt.MqttListener;
import com.lvqingyang.chatroom.mqtt.MqttService;
import com.lvqingyang.chatroom.mqtt.MyMqtt;
import com.lvqingyang.chatroom.tool.MessageAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String mNick;
    private String mRoomName;
    private static final String KEY_NICK = "NICK";
    private static final String KEY_ROOM_NAME = "ROOM_NAME";
    private android.support.v7.widget.RecyclerView rvmessage;
    private android.widget.EditText etmessage;
    private android.widget.ImageView ivsend;
    private MessageAdapter mAdapter;
    private List<Message> mMessageList=new ArrayList<>();
    private Gson mGson=new Gson();
    private MqttListener mMqttListener=new MqttListener() {
        @Override
        public void onConnected() {
            //订阅聊天室
            MqttService.getMyMqtt().subTopic(mRoomName, 0);
        }

        @Override
        public void onFail() {
            Toast.makeText(ChatActivity.this, "Connect fail", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLost() {
            Toast.makeText(ChatActivity.this, "Connect lost", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onReceive(String message) {
            //收到消息
            Message msg=mGson.fromJson(message, Message.class);
            if (msg.getClientID().equals(MyMqtt.getClientID())) {
                msg.setReceive(false);
            }
            mMessageList.add(msg);
            mAdapter.notifyItemInserted(mMessageList.size()-1);
        }

        @Override
        public void onSendSucc() {

        }
    };

    public static void start(Context context, String nick, String roomName) {
        Intent starter = new Intent(context, ChatActivity.class);
        starter.putExtra(KEY_NICK, nick);
        starter.putExtra(KEY_ROOM_NAME, roomName);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //init view
        this.etmessage = (EditText) findViewById(R.id.et_message);
        this.rvmessage = (RecyclerView) findViewById(R.id.rv_message);
        this.ivsend = (ImageView) findViewById(R.id.iv_send);
        ivsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content=etmessage.getText().toString();
                if (!TextUtils.isEmpty(content)) {
                    Message message=new Message(mNick, content, true);
                    MqttService.getMyMqtt().pubMsg(mRoomName, mGson.toJson(message), 0);
                }
            }
        });

        //init data
        Intent data=getIntent();
        mNick=data.getStringExtra(KEY_NICK);
        mRoomName=data.getStringExtra(KEY_ROOM_NAME);

        initMqtt();

        initeList();
    }


    private void initMqtt() {
        MqttService.start(this);
        MqttService.addMqttListener(mMqttListener);
    }

    private void initeList() {
        mAdapter=new MessageAdapter(this, mMessageList);
        rvmessage.setAdapter(mAdapter);
        rvmessage.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MqttService.removeMqttListener(mMqttListener);
        MqttService.stop(this);
    }
}
