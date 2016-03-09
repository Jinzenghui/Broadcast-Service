package com.example.biac.testservice;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by BIAC on 2016/3/9.
 */

//CounterService实现了ICounterService接口。当这个服务被bindService函数启动时，系统会调用它的onBind函数，这个函数返回一个
//Binder对象给系统。
public class CounterService extends Service implements ICounterService {

    private final static String LOG_TAG = "com.example.biac.testservice.CounterService";

    public final static String BROADCAST_COUNTER_ACTION = "com.example.biac.testservice.COUNTER_ACTION";
    public final static String COUNTER_VALUE = "com.example.biac.testservice.value";

    private boolean stop = false;

    private final IBinder binder = new CounterBinder();

    public class CounterBinder extends Binder {

        public CounterService getService(){

            return CounterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent){
        return binder;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        Log.i(LOG_TAG, "Counter Service Created.");
    }

    @Override
    public void onDestroy(){
        Log.i(LOG_TAG, "Counter Service Destroyed.");
    }

    //当MainActivity调用计数器服务接口的startCounter函数时，计数器服务并不是直接进入计数状态，而是通过使用异步任务(AsyncTask)在后台线程中进行计数。
    //当我们调用异步任务实例的excute(task.excute)方法时，当前调用线程就返回了，系统启动一个后台线程来执行这个异步任务实例的doInBackground()函数，这个函数就是我们用来
    //执行耗时计算的地方了。
    //在计算的过程中，可以通过调用publishProgress函数来通知调用者当前计算的进度，好让调用者来更新界面。调用publishProgress函数的效果最终就是直接到这个异步任务实例的
    //onProgressUpdate函数中，这里就可以把这个进度值以广播的形式(sendBroadcast)发送出去了。

    public void startCounter(int initVal){

        AsyncTask<Integer, Integer, Integer> task = new AsyncTask<Integer, Integer, Integer>() {
            @Override
            protected Integer doInBackground(Integer... params) {

                Integer initCounter = params[0];

                stop = false;
                while(!stop){
                    publishProgress(initCounter);

                    try{
                        Thread.sleep(1000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }

                    initCounter++;
                }

                return initCounter;

            }

            @Override
            protected void onProgressUpdate(Integer... values){
                super.onProgressUpdate(values);

                int counter = values[0];

                Intent intent = new Intent(BROADCAST_COUNTER_ACTION);
                intent.putExtra(COUNTER_VALUE, counter);

                sendBroadcast(intent);
            }

            @Override
            protected void onPostExecute(Integer val){
                int counter = val;

                Intent intent = new Intent(BROADCAST_COUNTER_ACTION);
                intent.putExtra(COUNTER_VALUE, counter);

                sendBroadcast(intent);
            }
        };

        task.execute(0);
    }

    public void stopCounter(){
        stop = true;
    }

}
