package com.lvqingyang.chatroom.bean;

import com.lvqingyang.chatroom.mqtt.MyMqtt;

/**
 * 消息
 * @author Lv Qingyang
 * @date 2017/10/11
 * @email biloba12345@gamil.com
 * @github https://github.com/biloba123
 * @blog https://biloba123.github.io/
 */
public class Message {
    private String clientID;
    private String nick;
    private String content;
    private boolean isReceive;

    public Message(String nick, String content, boolean isReceive) {
        this.nick = nick;
        this.content = content;
        this.isReceive = isReceive;
        clientID= MyMqtt.getClientID();
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isReceive() {
        return isReceive;
    }

    public void setReceive(boolean receive) {
        isReceive = receive;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }
}
