package com.lvqingyang.chatroom.mqtt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.lvqingyang.chatroom.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class MqttService extends Service implements MqttListener {

    private static MyMqtt mMyMqtt;
    private static List<MqttListener> mMqttListenerList=new ArrayList<>();
    private static final String TAG = "MqttService";

    public static void start(Context context) {
        Intent starter = new Intent(context, MqttService.class);
        context.startService(starter);
    }

    public static void stop(Context context) {
        Intent starter = new Intent(context, MqttService.class);
        context.stopService(starter);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Log.d(TAG, "onCreate: ");
        if (mMyMqtt==null) {
            mMyMqtt=new MyMqtt(this);
        }
        mMyMqtt.connectMqtt();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMyMqtt.disConnectMqtt();
        mMyMqtt=null;
        mMqttListenerList.clear();
    }

    public static MyMqtt getMyMqtt(){
        return mMyMqtt;
    }

    public static void addMqttListener(MqttListener listener){
        if (!mMqttListenerList.contains(listener)) {
            mMqttListenerList.add(listener);
        }
    }

    public static void removeMqttListener(MqttListener listener){
        mMqttListenerList.remove(listener);
    }


    @Override
    public void onConnected() {
        for (MqttListener mqttListener : mMqttListenerList) {
            mqttListener.onConnected();
        }
    }

    @Override
    public void onFail() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mMyMqtt.connectMqtt();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        for (MqttListener mqttListener : mMqttListenerList) {
            mqttListener.onFail();
        }
    }

    @Override
    public void onLost() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mMyMqtt.connectMqtt();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        for (MqttListener mqttListener : mMqttListenerList) {
            mqttListener.onLost();
        }
    }

    @Override
    public void onReceive(String message) {
        for (MqttListener mqttListener : mMqttListenerList) {
            mqttListener.onReceive(message);
        }
    }

    @Override
    public void onSendSucc() {
        for (MqttListener mqttListener : mMqttListenerList) {
            mqttListener.onSendSucc();
        }
    }
}
