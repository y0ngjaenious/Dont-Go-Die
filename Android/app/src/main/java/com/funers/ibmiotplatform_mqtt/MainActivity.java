package com.funers.ibmiotplatform_mqtt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

    //final private String PUB_COMMAND_OPEN   = "iot-2/type/Raspberry_Pi/id/kyong_pi/cmd/open/fmt/json";
    //final private String PUB_COMMAND_CLOSE  = "iot-2/type/Raspberry_Pi/id/kyong_pi/cmd/close/fmt/json";
    final private String SUB_EVENT_US       = "iot-2/type/Raspberry_Pi/id/kyong_pi/evt/us_dist/fmt/json";
    //final private String SUB_EVENT_SS       = "iot-2/type/Raspberry_Pi/id/Raspberry_Pi_1/evt/us_dist/fmt/json";
//
    protected MqttAndroidClient mqttAndroidClient;

    protected Button openButton;
    protected Button closeButton;
    protected RecyclerView event_log_view;
    protected RecyclerView.LayoutManager manager;

    protected TextView UserState;
    protected TextView UserStateTime;
    protected TextView UserHomeTempState;
    protected TextView UserHomeHumState;
    protected TextView UserLocationState;
//    protected SimpleAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //텍스트 상자 선언
        UserState = (TextView) findViewById(R.id.UserState);
        UserStateTime = (TextView) findViewById(R.id.UserStateTime);
        UserHomeTempState = (TextView) findViewById(R.id.UserHomeTempState);
        UserHomeHumState = (TextView) findViewById(R.id.UserHomeHumState);
        UserLocationState = (TextView) findViewById(R.id.UserLocationState);


        // 텍스트 상사 안에 들어갈 내용 지
        UserState.setText(getResources().getString(R.string.userStateGreen));
        UserStateTime.setText("10분전");
        UserHomeTempState.setText("25도");
        UserHomeHumState.setText("40%");
        UserLocationState.setText(getResources().getString(R.string.userLocationStateInside));

        //UserStateTime.setText("12분전");
//
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName(userName);
        mqttConnectOptions.setPassword(passWord.toCharArray());
        System.out.println("data: ");
//
//
//
//
//        ////////////////////////////////////////////////////////////////////////////////////////////
//        //mqtt client를 생성 및 callBack 함수 작성
//        ////////////////////////////////////////////////////////////////////////////////////////////
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), url, clientId);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                JSONObject data = new JSONObject(new String(message.getPayload()));
                Log.i(TAG, "topic: " + topic + ", data: " + data.toString());
                //adapter.addLog("topic: " + topic + ", data: " + data.toString());
                System.out.println("data: " + data.toString());
                if (data.getString("IO").equals("Outdoor")) {
                    UserLocationState.setText(getResources().getString(R.string.userLocationStateOutside));
                } else {
                    double time = data.getDouble("Time");
                    if (time > 10) {
                        UserState.setText(getResources().getString(R.string.userStateRed));
                        time = Math.round(time*100)/100.0;
                        UserStateTime.setText(Double.toString(time) + "분전");
                    } else {
                        UserState.setText(getResources().getString(R.string.userStateGreen));
                        time = Math.round(time*100)/100.0;
                        UserStateTime.setText(Double.toString(time) + "분전");
                    }
                }


                //if(topic.contains("us_dist")) {
                //    publishCommandMoveServo(data);
                //}
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "msg delivered");
            }
        });
//        ////////////////////////////////////////////////////////////////////////////////////////////
//        //mqtt client로 ibm_iot_platform에 접속
//        ////////////////////////////////////////////////////////////////////////////////////////////
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
//
//        ////////////////////////////////////////////////////////////////////////////////////////////
//        //MainActivity의 View 동작설정
//        ////////////////////////////////////////////////////////////////////////////////////////////
//        event_log_view = (RecyclerView) findViewById(R.id.event_log_view);
//        adapter = new SimpleAdapter();
//        manager = new LinearLayoutManager(this);
//        event_log_view.setLayoutManager(manager);
//        event_log_view.setAdapter(adapter);
//
//        //button을 누르면 서보모터 회전 command publish
//        openButton = findViewById(R.id.open);
//        openButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                JSONObject command = new JSONObject();
//                try {
//                    command.put("duty", 12);
//                    publishMessage(PUB_COMMAND_OPEN, command);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
//        //button을 누르면 서보모터 회전 command publish
//        closeButton = findViewById(R.id.close);
//        closeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                JSONObject command = new JSONObject();
//                try {
//                    command.put("duty", 2);
//                    publishMessage(PUB_COMMAND_CLOSE, command);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
//    ////////////////////////////////////////////////////////////////////////////////////////////
//    //evnet subscribe
//    ////////////////////////////////////////////////////////////////////////////////////////////
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
//    ////////////////////////////////////////////////////////////////////////////////////////////
//    //publish command
//    ////////////////////////////////////////////////////////////////////////////////////////////
//    public void publishMessage(String Topic, JSONObject data) {
//        try {
//            if (mqttAndroidClient.isConnected() == false) {
//                mqttAndroidClient.connect();
//            }
//
//            MqttMessage message = new MqttMessage();
//            message.setPayload(data.toString().getBytes());
//            message.setQos(0);
//            mqttAndroidClient.publish(Topic, message,null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.i(TAG, "publish succeed!");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.i(TAG, "publish failed!");
//                }
//            });
//        } catch (MqttException e) {
//            Log.e(TAG, e.toString());
//            e.printStackTrace();
//        }
//    }
//
//    ////////////////////////////////////////////////////////////////////////////////////////////
//    //event를 통해 받은 값에 따라 모터를 제어하는 command publish
//    ////////////////////////////////////////////////////////////////////////////////////////////
//    public void publishCommandMoveServo(JSONObject data) throws JSONException {
//        float distance = Float.parseFloat(data.getString("distance"));
//        JSONObject command = new JSONObject();
//        if(distance < 20){
//            command.put("duty", 12);
//            publishMessage(PUB_COMMAND_OPEN, command);
//        }
//        if(distance > 25){
//            command.put("duty", 2);
//            publishMessage(PUB_COMMAND_CLOSE, command);
//        }
//    }
    }