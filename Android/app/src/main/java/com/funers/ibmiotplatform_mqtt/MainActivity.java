package com.funers.ibmiotplatform_mqtt;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "IBMiotMqtt";

    final private String orgId = "ymmd2u"; //조직id
    final private String url = "tcp://" + orgId + ".messaging.internetofthings.ibmcloud.com:1883";
    final private String clientId = "a:"+orgId+":test_app";   //a:조직id:appId
    final private String userName = "a-ymmd2u-aov7srgf6g";    //API key
    final private String passWord = "y7P0bwvO+Hhlef8Qwa";     //token

    final private String SUB_EVENT_US       = "iot-2/type/Raspberry_Pi/id/kyong_pi/evt/danger_signal/fmt/json";

    NotificationManager notiManager;
    NotificationCompat.Builder builder;

    private static String CHANNEL_ID = "channel1";
    private static String CHANEL_NAME = "Channel1";

    protected MqttAndroidClient mqttAndroidClient;

    protected TextView UserState;
    protected TextView UserStateTime;
    protected TextView UserHomeTempState;
    protected TextView UserHomeHumState;
    protected TextView UserLocationState;
    protected TextView StateText;

    private static final int SINGLE_PERMISSION = 1004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 문자 보내기 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SINGLE_PERMISSION);
        } else {

        }

        // 텍스트 상자 선언
        TextView UserName = (TextView) findViewById(R.id.UserName);
        TextView UserAddress = (TextView) findViewById(R.id.UserAddress);
        UserState = (TextView) findViewById(R.id.UserState);
        UserStateTime = (TextView) findViewById(R.id.UserStateTime);
        UserHomeTempState = (TextView) findViewById(R.id.UserHomeTempState);
        UserHomeHumState = (TextView) findViewById(R.id.UserHomeHumState);
        UserLocationState = (TextView) findViewById(R.id.UserLocationState);
        StateText = (TextView) findViewById(R.id.textView7);
        // 텍스트 상자 안에 들어갈 내용 지정
        // 상태 위험일 때 : UserState.setText(getResources().getString(R.string.userStateGreen));
        // 상태 좋을 때 : UserState.setText(getResources().getString(R.string.userStateRed));

        // UserState 텍스트 색상 변경
        // 상태 위험일 때 : UserState.setTextColor(getResources().getColor(R.color.colorGreen));
        // 상태 좋을 때 : UserState.setTextColor(getResources().getColor(R.color.colorRed));

        UserState.setText(getResources().getString(R.string.userStateGreen)); // 유저 상태
        UserState.setTextColor(getResources().getColor(R.color.colorGreen)); // 유저 상태 텍스트 색상
        UserLocationState.setText(getResources().getString(R.string.userLocationStateInside)); // 유저 외출인지
        UserName.setText("홍길동"); // 유저 이름
        UserAddress.setText("서울특별시 강서구 마곡중앙10로 30"); // 유저 주소
        UserStateTime.setText("10분전"); // 움직임감지 시간
        UserHomeTempState.setText("25"); // 집 온도
        UserHomeHumState.setText("40"); // 집 습도

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName(userName);
        mqttConnectOptions.setPassword(passWord.toCharArray());
        System.out.println("data: ");


        // 전화걸기 버튼
        Button CallButton = (Button) findViewById(R.id.buttonCalling);
        CallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:12345"));
                startActivity(intent);
            }
        });

        final String phoneNumber = "5554";
        final String message = "위험하다!";

        ////////////////////////////////////////////////////////////////////////////////////////////
        // 문자보내기
        ////////////////////////////////////////////////////////////////////////////////////////////
        // 문자보내기 버튼 --> 지금은 그냥 임의로 만들어 놓은 것. 위험 상황마다 보내는거 해결되면 없앨 코드.
        Button TextButton = (Button) findViewById(R.id.buttonText);
        TextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSMS(phoneNumber, message);
//                Toast.makeText(getBaseContext(), "알림 문자 전송됨.", Toast.LENGTH_SHORT).show();
            }

            private void sendSMS(String phoneNumber, String message) {
                String SENT = "SMS_SENT";
                String DELIVERED="SMS_DELIVERED";

//                PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
//                PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        switch (getResultCode()) {

                            case Activity.RESULT_OK:
                                Toast.makeText(getBaseContext(), "알림 문자 전송됨.", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }, new IntentFilter(SENT));

                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(phoneNumber, null, message, null, null);
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        //mqtt client를 생성 및 callBack 함수 작성
        ////////////////////////////////////////////////////////////////////////////////////////////
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), url, clientId);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connection lost");
            }

            private void sendSMS(String phoneNumber, String message) {
                String SENT = "SMS_SENT";
                String DELIVERED="SMS_DELIVERED";

//                PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
//                PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        switch (getResultCode()) {

                            case Activity.RESULT_OK:
                                Toast.makeText(getBaseContext(), "알림 문자 전송됨.", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }, new IntentFilter(SENT));

                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(phoneNumber, null, message, null, null);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                JSONObject data = new JSONObject(new String(message.getPayload()));
                Log.i(TAG, "topic: " + topic + ", data: " + data.toString());
                //adapter.addLog("topic: " + topic + ", data: " + data.toString());
                System.out.println("data: " + data.toString());

                UserHomeTempState.setText(Integer.toString(data.getInt("Temperature")));
                UserHomeHumState.setText(Integer.toString(data.getInt("Humidity")));

                if (data.getString("IO").equals("Outdoor")) {
                    UserLocationState.setText(getResources().getString(R.string.userLocationStateOutside));
                    UserState.setText("외출 중");
                } else {
                    UserLocationState.setText(getResources().getString(R.string.userLocationStateInside));
                    double time = data.getDouble("Time");
                    time = Math.round(time*100)/100;
                    switch(data.getInt("Danger_code")){
                        case 0:
                            UserState.setTextColor(getResources().getColor(R.color.colorGreen));
                            UserState.setText(getResources().getString(R.string.userStateGreen));
                            UserStateTime.setText("");
                            StateText.setText("");
                            break;

                        case 1:
                            UserState.setTextColor(getResources().getColor(R.color.colorRed));
                            UserState.setText(getResources().getString(R.string.userStateM));
                            StateText.setText(", 가장 마지막 움직임이 감지되었습니다.");
                            UserStateTime.setText(getTimeString(time) + "전");
                            showNoti("움직임이 " + getTimeString(time) + "간 감지되지 않았습니다.");
                            break;

                        case 2:
                            UserState.setTextColor(getResources().getColor(R.color.colorRed));
                            UserState.setText(getResources().getString(R.string.userStateTH));
                            UserStateTime.setText("");
                            StateText.setText("");
                            showNoti("온습도에 이상이 있습니다.");
                            break;

                        case 3:
                            UserState.setTextColor(getResources().getColor(R.color.colorRed));
                            UserState.setText(getResources().getString(R.string.userStateMTH));
                            StateText.setText(", 가장 마지막 움직임이 감지되었습니다.");
                            UserStateTime.setText(getTimeString(time) + "전");
                            showNoti("움직임이 " + getTimeString(time) + "간 감지되지 않고, 온습도에 이상이 있습니다.");
                            break;
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "msg delivered");
            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////
        //mqtt client로 ibm_iot_platform에 접속
        ////////////////////////////////////////////////////////////////////////////////////////////
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "connect succeed");
                    //evnet subscribe
                    subscribeTopic(SUB_EVENT_US);
                    //subscribeTopic(SUB_EVENT_SS); //여러개의 event subcribe 가능
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "connect failed");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private String getTimeString(Double time) {
        String res = "";
        Long hour = Math.round(time / 3600);
        Long minute = Math.round((time - hour * 60) / 60);
        Long seconds = Math.round(time - hour * 60 - minute * 60);
        if(hour > 0) {
            res = hour.toString() + "시간";
        }
        else if(minute > 0) {
            res += minute.toString() + "분";
        }
        else {
            res += seconds.toString() + "초";
        }
        return res;
    }

    private void showNoti(String msg){
        builder = null;
        notiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // 버전 오레오 이상일 경우
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((NotificationManager) notiManager).createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_DEFAULT));
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            //버전 오레오 이하
            builder = new NotificationCompat.Builder(this);
        }

        //알림창 제목
        builder.setContentTitle("위험!");

        //알림창 메시지
        builder.setContentText(msg);

        //알림창 아이콘
        builder.setSmallIcon(R.drawable.noti_icon);

        Notification notification = builder.build();

        //알림창 실행
        notiManager.notify(1,notification);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //evnet subscribe
    ////////////////////////////////////////////////////////////////////////////////////////////
    public void subscribeTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "subscribed succeed");
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "subscribed failed");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}