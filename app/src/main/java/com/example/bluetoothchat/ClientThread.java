package com.example.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by 陈振聪 on 2016/11/18.
 */
public class ClientThread extends Thread {
    private BluetoothDevice bluetoothDevice;
    private Handler handler;
    private static final String TAG = "Chat";
    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    // 通过一个规定好的串号，生成一个UUID号
    private static final UUID MY_UUID = UUID.fromString(SPP_UUID);
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    /**
     * 设定一个标记值，来标记客户端连接是否成功
     */
    public static boolean flag = false;

    private BluetoothSocket mmSocket ;

    public ClientThread(BluetoothDevice bluetoothDevice, Handler handler) {
        this.handler = handler;
        this.bluetoothDevice = bluetoothDevice;

    }

    @Override
    public void run() {
        super.run();
        try {
            // 获取客户端的socket
           // mmSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
           mmSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
           /* Method method = bluetoothDevice.getClass().getMethod("createRfcommSocket" , new Class[]{int.class}) ;
            mmSocket = (BluetoothSocket) method.invoke(bluetoothDevice , 1);*/

            // 连接服务器------要么成功，要么异常
            mmSocket.connect();
            // 如果没有抛出异常，代表连接成功，进行一个标志位

            flag = true;
            bufferedReader = new BufferedReader(new InputStreamReader(mmSocket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(mmSocket.getOutputStream()));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                Log.i(TAG, "收到服务端的消息" + line);
                handler.obtainMessage(MyChatActivity.ACCEPT_MESSAGE, line)
                        .sendToTarget();
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
            Log.i(TAG, "客户端连接失败");
        }
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        if (bufferedWriter != null) {
            try {
                bufferedWriter.write(msg + "\n");
                Log.i(TAG, "客户端发送消息:" + msg);
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "客户端未连接");
            handler.obtainMessage(MyChatActivity.FAILED, "客户端未连接")
                    .sendToTarget();
        }
    }

}
