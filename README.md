# ChatRoom

### Android中使用MQTT协议
#### 添加依赖
在gradle中添加依赖，引入相应的库
```
dependencies {
  ...
  compile 'commons-codec:commons-codec:1.5'
  compile 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.0.2'
}
```

#### 权限声明
```
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```
这些权限不是危险权限，不用运行时申请

#### MQTT支持类
MqttAsyncClient是mqtt支持类，创建时要要传入服务器host，port以及设备标识clientID（不同设备id不能相同）
```
MqttAsyncClient mqttClient=new MqttAsyncClient("tcp://"+this.host+":"+this.port ,
                    "ClientID"+this.clientID, new MemoryPersistence());
```

接着连接服务器，并对连接状态进行监听
```
//注意接口回调都是在非主线程，不能直接进行ui操作
mqttClient.connect(getOptions(), null, new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            //连接成功
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            //连接失败
        }
    });
```
getOptions()方法是对连接信息进行配置，如用户名，密码（一般服务器内不设）

```
/**
     * 设置Mqtt的连接信息
     */
    private MqttConnectOptions getOptions(){
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);//重连不保持状态
        if(this.userID!=null && this.userID.length()>0 && this.passWord!=null && this.passWord.length()>0){
            options.setUserName(this.userID);//设置服务器账号密码
            options.setPassword(this.passWord.toCharArray());
        }
        options.setConnectionTimeout(10);//设置连接超时时间
        options.setKeepAliveInterval(30);//设置保持活动时间，超过时间没有消息收发将会触发ping消息确认
        return options;
    }
```
最后对消息及连接进行监听
```
//注意接口回调都是在非主线程，不能直接进行ui操作
mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //丢失连接
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    //接到推送消息
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //发送消息成功到达
                }
            });
```
获取并配置好MqttAsyncClient 后就可以调用相应方法订阅Topic接收消息
subscribe(String topicFilter, int qos)
发送某个Topic的消息
publish(String topic, byte[] payload, int qos, boolean retained)

#### 封装
为了方便使用，对相关操作进行封装
```
/**
 * Author：LvQingYang
 * Date：2017/8/29
 * Email：biloba12345@gamil.com
 * Github：https://github.com/biloba123
 * Info：MQTT操作类
 */

public class MyMqtt {
    private String TAG = "MyMqtt";

    /**MQTT配置参数**/
    private static String host = "****************";
    private static String port = "*****";
    private static String userID = "";
    private static String passWord = "";
    private static String clientID =  UUID.randomUUID().toString();


    /**MQTT状态信息**/
    private boolean isConnect = false;

    /**MQTT支持类**/
    private MqttAsyncClient mqttClient=null;

    private MqttListener mMqttListener;

    private Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.arg1) {
                case MqttTag.MQTT_STATE_CONNECTED:
                    if (BuildConfig.DEBUG) Log.d(TAG, "handleMessage: connected");
                    mMqttListener.onConnected();
                    break;
                case MqttTag.MQTT_STATE_FAIL:
                    if (BuildConfig.DEBUG) Log.d(TAG, "handleMessage: fail");
                    mMqttListener.onFail();
                    break;
                case MqttTag.MQTT_STATE_LOST:
                    if (BuildConfig.DEBUG) Log.d(TAG, "handleMessage: lost");
                    mMqttListener.onLost();
                    break;
                case MqttTag.MQTT_STATE_RECEIVE:
                    if (BuildConfig.DEBUG) Log.d(TAG, "handleMessage: receive");
                    mMqttListener.onReceive((String) message.obj);
                    break;
                case MqttTag.MQTT_STATE_SEND_SUCC:
                    if (BuildConfig.DEBUG) Log.d(TAG, "handleMessage: send");
                    mMqttListener.onSendSucc();
                    break;
            }
            return true;
        }
    });

    /**
     * 自带的监听类，判断Mqtt活动变化
     */
    private IMqttActionListener mIMqttActionListener=new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            isConnect=true;
            Message msg=new Message();
            msg.arg1=MqttTag.MQTT_STATE_CONNECTED;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            isConnect=false;
            Message msg=new Message();
            msg.arg1=MqttTag.MQTT_STATE_FAIL;
            mHandler.sendMessage(msg);
        }
    };

    /**
     * 自带的监听回传类
     */
    private MqttCallback mMqttCallback=new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            isConnect=false;
            Message msg=new Message();
            msg.arg1=MqttTag.MQTT_STATE_LOST;
            mHandler.sendMessage(msg);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Message msg=new Message();
            msg.arg1=MqttTag.MQTT_STATE_RECEIVE;
            msg.obj=new String(message.getPayload());
            mHandler.sendMessage(msg);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Message msg=new Message();
            msg.arg1=MqttTag.MQTT_STATE_SEND_SUCC;
            mHandler.sendMessage(msg);
        }
    };

    public MyMqtt(MqttListener lis){
        mMqttListener=lis;
    }

    public static void setMqttSetting(String host, String port, String userID, String passWord, String clientID){
        MyMqtt.host = host;
        MyMqtt.port = port;
        MyMqtt.userID = userID;
        MyMqtt.passWord = passWord;
        MyMqtt.clientID = clientID;
    }

    /**
     * 进行Mqtt连接
     */
    public void connectMqtt(){
        try {
            mqttClient=new MqttAsyncClient("tcp://"+this.host+":"+this.port ,
                    "ClientID"+this.clientID, new MemoryPersistence());
            mqttClient.connect(getOptions(), null, mIMqttActionListener);
            mqttClient.setCallback(mMqttCallback);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 断开Mqtt连接重新连接
     */
    public void reStartMqtt(){
        disConnectMqtt();
        connectMqtt();
    }

    /**
     * 断开Mqtt连接
     */
    public void disConnectMqtt(){
        try {
            mqttClient.disconnect();
            mqttClient = null;
            isConnect = false;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向Mqtt服务器发送数据
     */
    public void pubMsg(String Topic, String Msg, int Qos){
        if(!isConnect){
            Log.d(TAG,"Mqtt连接未打开");
            return;
        }
        try {
            /** Topic,Msg,Qos,Retained**/
            mqttClient.publish(Topic,Msg.getBytes(),Qos,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向Mqtt服务器发送数据
     */
    public void pubMsg(String Topic, byte[] Msg, int Qos){
        if(!isConnect){
            Log.d(TAG,"Mqtt连接未打开");
            return;
        }
        try {
            /** Topic,Msg,Qos,Retained**/
            mqttClient.publish(Topic,Msg,Qos,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向Mqtt服务器订阅某一个Topic
     */
    public void subTopic(String Topic, int Qos){
        if(!isConnect){
            Log.d(TAG,"Mqtt连接未打开");
            return;
        }
        try {
            mqttClient.subscribe(Topic,Qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置Mqtt的连接信息
     */
    private MqttConnectOptions getOptions(){
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);//重连不保持状态
        if(this.userID!=null&&this.userID.length()>0&&this.passWord!=null&&this.passWord.length()>0){
            options.setUserName(this.userID);//设置服务器账号密码
            options.setPassword(this.passWord.toCharArray());
        }
        options.setConnectionTimeout(10);//设置连接超时时间
        options.setKeepAliveInterval(30);//设置保持活动时间，超过时间没有消息收发将会触发ping消息确认
        return options;
    }

    public boolean isConnect() {
        return isConnect;
    }
}
```
相关标识MqttTag
```
public class MqttTag {
    public final static int MQTT_STATE_CONNECTED=1;
    public final static int MQTT_STATE_LOST=2;
    public final static int MQTT_STATE_FAIL=3;
    public final static int MQTT_STATE_RECEIVE=4;
    public final static int MQTT_STATE_SEND_SUCC=5;
}
```
接口MqttListener
```
interface MqttListener {
    void onConnected();//连接成功
    void onFail();//连接失败
    void onLost();//丢失连接
    void onReceive(String message);//接收到消息
    void onSendSucc();//消息发送成功
}
```
运用观察者模式，创建一个Service后台监听相关状态（写的不太规范...）
```
public class MqttService extends Service implements MqttListener {

    private static MyMqtt mMyMqtt;
    private static List<MqttListener> mMqttListenerList=new ArrayList<>();

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

```
