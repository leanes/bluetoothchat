package com.example.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

public class MyChatActivity extends AppCompatActivity {
    private EditText et_content;
    private LinearLayout ll_content;
    private String address;
    public static final int ACCEPT_MESSAGE = 1;
    public static final int FAILED = 2;
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == ACCEPT_MESSAGE) {
                // 展示到界面
                String message = (String) msg.obj;
                if (!TextUtils.isEmpty(message)) {
                    addSheContent(message);
                }
            } else if (msg.what == FAILED) {
                String message = (String) msg.obj;
                Toast.makeText(MyChatActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        };
    };
    private BluetoothAdapter defaultAdapter;
    private ClientThread clientThread;
    private ServerThread serverThread;
    private BluetoothDevice remoteDevice;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        TextView tv_address = (TextView) findViewById(R.id.tv_address);
        et_content = (EditText) findViewById(R.id.et_content);
        ll_content = (LinearLayout) findViewById(R.id.ll_content);
        // 获取要聊天对象的mac地址
        address = getIntent().getStringExtra("address");
        tv_address.setText(address);
        // 获取defaultAdapter
        defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        // 创建服务端
        serverThread = new ServerThread(handler, defaultAdapter);
        // 开启服务端线程，等待接收客户端
        serverThread.start();
        // 获取要连接到蓝牙设备
        remoteDevice = defaultAdapter.getRemoteDevice(address);
        // 一开始就进行连接服务器，如果连接失败，可以手动去连
        connect();

    }

    public void send(View v) {
        if (et_content.getText().toString().equals("")){
            Toast.makeText(MyChatActivity.this , "发送内容不能为空，请重新输入！" , Toast.LENGTH_SHORT).show();
        }else {
            String msg = et_content.getText().toString().trim();
            if (ClientThread.flag) {
                clientThread.sendMessage(msg);
            } else {
                serverThread.sendMessage(msg);
            }
            addMeContent(msg);
            et_content.setText("");
        }
    }

    public void connect(View v) {
        connect();
    }


    private void connect() {
        clientThread = new ClientThread(remoteDevice, handler);
        clientThread.start();
    }

    public void addMeContent(String content) {
        View view = View.inflate(MyChatActivity.this, R.layout.list_say_me_item, null);
        TextView tv_me_time = (TextView) view.findViewById(R.id.tv_me_time);

        TextView tv_me_content = (TextView) view.findViewById(R.id.tv_me_content);
        tv_me_time.setText(getTime());
        tv_me_content.setText(content);
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        ll_content.addView(view);

    }

    public void addSheContent(String content) {
        View view = View.inflate(MyChatActivity.this, R.layout.list_say_she_item, null);
        TextView tv_she_time = (TextView) view.findViewById(R.id.tv_she_time);

        TextView tv_she_content = (TextView) view.findViewById(R.id.tv_she_content);
        tv_she_time.setText(getTime());
        tv_she_content.setText(content);
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        ll_content.addView(view);

    }

    public String getTime() {
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String format = dateFormat.format(currentTimeMillis);
        return format;
    }
}
