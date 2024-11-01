package com.application.watch.watchdrip;

import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import test.invoke.sdk.XiaomiWatchHelper;

/**
 * An {@link Service} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyService extends Service {

    public boolean salida=false;
    public static String urlBaseHTTP="http://localhost:29863/info.json?graph=1";
    Thread hiloChequeoApp;
    public XiaomiWatchHelper instance=null;
    long lastTime=0;
    String jsonString="";

    @Override
    public int onStartCommand (Intent intent,
                               int flags,
                               int startId)
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        instance = XiaomiWatchHelper.getInstance(this);

        instance.setReceiver((id, message) -> {
            try {
                String cadena=message.toString();
                lastTime=Long.parseLong(cadena);
            }
            catch(Exception e)
            {
            }
        });
        instance.registerMessageReceiver();
        hiloChequeoApp = new Thread(new HiloChequeoAppService(this));
        hiloChequeoApp.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy ()
    {
        salida=true;
        hiloChequeoApp.stop();
        hiloChequeoApp.destroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void accesoServlet() {
        HttpURLConnection connection=null;

        URL urlHttp = null;
        String response = null;
        try
        {
            urlHttp = new URL(urlBaseHTTP);
            connection = (HttpURLConnection) urlHttp.openConnection();
            connection.setConnectTimeout(10000);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                String line = "";
                InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line);
                }
                response = sb.toString();
                isr.close();
                reader.close();
            }
            jsonString=response;
        }
        catch(Exception e)
        {
            jsonString="";
        }
        finally {
            if(connection!=null)
                connection.disconnect();
        }
        if(!jsonString.isEmpty()) {
            try {
                JSONObject objJSON = new JSONObject(jsonString);
                JSONObject bgObject = objJSON.getJSONObject("bg");
                long time = bgObject.getLong("time");
                //String val = bgObject.getString("val");
                //String delta = bgObject.getString("delta");
                //String isHigh = bgObject.getString("isHigh");
                //String isLow = bgObject.getString("isLow");
                //String trend = bgObject.getString("trend");

                long milis=(new Date()).getTime()-time;
                if(lastTime!=time) {
                    instance.launchApp("com.application.watch.watchdrip",obj -> {
                        if(obj.isSuccess()) {
                            //Log.e(TAG, "Init message send");
                            try {
                                Thread.sleep(1000);
                            }catch(Exception e)
                            {
                            }
                            instance.sendMessageToWear(jsonString, obj2 ->{
                                if(obj2.isSuccess()) {
                                    //Log.e(TAG, "send -> " + obj.isSuccess());
                                    lastTime=time;
                                }
                            });
                            try {
                                Thread.sleep(100);
                            }catch(Exception e)
                            {
                            }
                            instance.sendMessageToWear(jsonString, obj2 ->{
                                if(obj2.isSuccess()) {
                                    //Log.e(TAG, "send -> " + obj.isSuccess());
                                    lastTime=time;
                                }
                            });
                            try {
                                Thread.sleep(100);
                            }catch(Exception e)
                            {
                            }
                            instance.sendMessageToWear(jsonString, obj2 ->{
                                if(obj2.isSuccess()) {
                                    //Log.e(TAG, "send -> " + obj.isSuccess());
                                    lastTime=time;
                                }
                            });
                        }
                    });
                    //                    Thread.sleep(600);


                }
            }
            catch(Exception e)
            {
                instance = XiaomiWatchHelper.getInstance(this);

                instance.setReceiver((id, message) -> {
                    try {
                        String cadena=message.toString();
                        lastTime=Long.parseLong(cadena);
                    }
                    catch(Exception ex)
                    {
                    }
                });
                instance.registerMessageReceiver();
            }
        }
    }
}

class HiloChequeoAppService implements Runnable {
    MyService padre;
    public HiloChequeoAppService(MyService p) {
        padre = p;
    }

    public void run() {
        while (!padre.salida) {
            try {
                padre.accesoServlet();
                Thread.sleep(10000);
            }catch (Exception e)
            {
                System.out.println("Excepcion "+e.getMessage());
            }
        }
    }
}
