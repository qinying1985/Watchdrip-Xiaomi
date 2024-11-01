package com.application.watch.watchdrip;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import test.invoke.sdk.XiaomiWatchHelper;

/**
 * @author user
 */
public class MainActivity extends AppCompatActivity {
    public boolean salida=false;
    private static final String TAG = MainActivity.class.getName();
    public static String urlBaseHTTP="http://localhost:29863/info.json?graph=1";
    Thread hiloChequeoApp;
    //public XiaomiWatchHelper instance=null;
    long lastTime=0;
    TextView bg=null;
    TextView timeTextView=null;
    TextView deltaTextView=null;
    String jsonString="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Intent serviceIntent = new Intent(this, MyService.class);
        // Start service
        this.startService(serviceIntent);

        setContentView(R.layout.activity_main);
        bg = findViewById(R.id.bg);
        timeTextView = findViewById(R.id.time);
        deltaTextView = findViewById(R.id.delta);
        //instance = XiaomiWatchHelper.getInstance(this);

        /*instance.setReceiver((id, message) -> {
            try {
                String cadena=message.toString();
                Toast.makeText(this,"Recibido Mensaje: "+ cadena, Toast.LENGTH_LONG).show();
                lastTime=Long.parseLong(cadena);
            }
            catch(Exception e)
            {
                Toast.makeText(this,"Recibido Mensaje Excepcion "+ e.getMessage(), Toast.LENGTH_LONG).show();
            }
            runOnUiThread(() -> Log.e(TAG, new String(message, StandardCharsets.UTF_8)));
        });*/

//        instance.registerMessageReceiver();
    }

    public String obtenerCadenaTiempo(long t)
    {
        String unit = "sec";
        t = t / 1000;
        if (t != 1) unit = "sec";
        if (t > 59)
        {
            unit = "min";
            t = t / 60;
            if (t != 1) unit = "mins";
            if (t > 59) {
                unit = "hour";
                t = t / 60;
                if (t != 1) unit = "hours";
                if (t > 24) {
                    unit = "day";
                    t = t / 24;
                    if (t != 1) unit = "days";
                    if (t > 28) {
                        unit = "week";
                        t = t / 7;
                        if (t != 1) unit = "weeks";
                    }
                }
            }
        }
        else
        {
            return "now";
        }
        return t + " " + unit;
    }

    public String getArrowText(String tendencia) {
        switch (tendencia) {
            case "FortyFiveDown":
                return "↘";
            case "FortyFiveUp":
                return "↗";
            case "Flat":
                return "→";
            case "SingleDown":
                return "↓";
            case "DoubleDown":
                return "↓↓";
            case "SingleUp":
                return "↑";
            case "DoubleUp":
                return "↑↑";
            default:
                return "";
        }
    }


    public void accesoServlet(){//XiaomiWatchHelper instance) {
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
            Toast.makeText(this,"Error de acceso a Servlet: "+ e.getMessage(), Toast.LENGTH_LONG).show();
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
                String val = bgObject.getString("val");
                String delta = bgObject.getString("delta");
                String isHigh = bgObject.getString("isHigh");
                String isLow = bgObject.getString("isLow");
                String trend = bgObject.getString("trend");

                long milis=(new Date()).getTime()-time;
                String timeStr = obtenerCadenaTiempo(milis);
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        timeTextView.setText(timeStr);
                    }
                });
                if(lastTime!=time) {
                    //String timeStr = obtenerCadenaTiempo(milis);
                            //new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").
                            //format(new Date(time));

                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            if(isHigh.equals("true"))
                                bg.setTextColor(Color.YELLOW);
                            else if(isLow.equals("true"))
                                bg.setTextColor(Color.RED);
                            else
                                bg.setTextColor(Color.WHITE);
                            bg.setText(val+" "+getArrowText(trend));
                            deltaTextView.setText(delta);
                        }
                    });

                    /*instance.launchApp("com.application.watch.watchdrip",obj -> {
                        if(obj.isSuccess()) {
                            Log.e(TAG, "Init message send");
                            try {
                                Thread.sleep(1000);
                            }catch(Exception e)
                            {
                            }
                            instance.sendMessageToWear(jsonString, obj2 ->{
                                if(obj2.isSuccess()) {
                                    Log.e(TAG, "send -> " + obj.isSuccess());
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
                                    Log.e(TAG, "send -> " + obj.isSuccess());
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
                                    Log.e(TAG, "send -> " + obj.isSuccess());
                                    lastTime=time;
                                }
                            });
                        }
                    });*/
                }
            }
            catch(Exception e)
            {

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check that it is the SecondActivity with an OK result
        if (resultCode == RESULT_OK) {
            if (data.getBooleanExtra("EXIT", false)) {
                salida=true;
                finish();
                System.exit(0);
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        hiloChequeoApp = new Thread(new HiloChequeoApp(this));
        hiloChequeoApp.start();
    }
}

class HiloChequeoApp implements Runnable {
    MainActivity padre;
    public HiloChequeoApp(MainActivity p) {
        padre = p;
    }

    public void run() {
        while (!padre.salida) {
            try {
                padre.accesoServlet();//padre.instance);
                Thread.sleep(10000);
            }catch (Exception e)
            {
                System.out.println("Excepcion "+e.getMessage());
            }
        }
    }
}
