package com.lvqingyang.chatroom.mqtt;

/**
 * Author：LvQingYang
 * Date：2017/8/29
 * Email：biloba12345@gamil.com
 * Github：https://github.com/biloba123
 * Info：
 */
public interface MqttListener {
    void onConnected();//连接成功
    void onFail();//连接失败
    void onLost();//丢失连接
    void onReceive(String message);//接收到消息
    void onSendSucc();//消息发送成功
}
