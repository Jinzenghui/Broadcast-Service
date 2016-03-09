package com.example.biac.testservice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String LOG_TAG = "com.example.biac.testservice.MainActivity";

    private Button startButton = null;
    private Button stopButton = null;
    private TextView counterText = null;

    private ICounterService counterService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button)findViewById(R.id.button_start);
        stopButton = (Button)findViewById(R.id.button_stop);
        counterText = (TextView)findViewById(R.id.textview_counter);

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);

        startButton.setEnabled(true);
        stopButton.setEnabled(false);

        //bindService()函数把计数器服务启动起来
        //它的第二个参数serviceConnection是一个ServiceConnection实例。
        //计数器服务启动起来后，系统会调用这个实例的onServiceConnected函数将一个Binder对象传回来，通过调用这个Binder
        //对象的getService函数，就可以获得计数器服务接口。
        Intent bindIntent = new Intent(MainActivity.this, CounterService.class);
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        Log.i(LOG_TAG, "Main Activity Created.");
    }

    @Override
    public void onResume(){
        super.onResume();

        //通过调用registerReceiver函数注册一个广播接收器counterActionReceiver,它是一个BroadcastReceiver实例，并且指定
        //了这个广播接收器只对CounterService.BROADCAST_COUNTER_ACTION类型的广播感兴趣。
        //当CounterService发出一个CounterService.BROADCAST_COUNTER_ACTION类型的广播时，系统就会把这个广播发送到
        //counterActionReceiver实例的onReceiver函数中去。在onReceive函数中，从参数intent中取出计数器当前的值，显示在界面上。
        IntentFilter counterActionFilter = new IntentFilter(CounterService.BROADCAST_COUNTER_ACTION);
        registerReceiver(counterActionReceiver, counterActionFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(counterActionReceiver);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public void onClick(View v){
        if(v.equals(startButton)){
            if(counterService != null){
                counterService.startCounter(0);

                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        }else if(v.equals(stopButton)){
            if(counterService != null){
                counterService.stopCounter();

                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        }
    }

    private BroadcastReceiver counterActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int counter = intent.getIntExtra(CounterService.COUNTER_VALUE, 0);
            String text = String.valueOf(counter);
            counterText.setText(text);

            Log.i(LOG_TAG, "Receive counter event");
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            counterService = ((CounterService.CounterBinder)service).getService();

            Log.i(LOG_TAG, "Counter Service Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            counterService = null;
            Log.i(LOG_TAG, "Counter Service Disconnected");
        }
    };

}
