package com.application.watch.watchdrip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/*import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;*/

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        // Create Intent
        Intent serviceIntent = new Intent(context, MyService.class);
        // Start service
        /*if(Build.VERSION.SDK_INT >= 34)
        {
            WorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(UploadWorker.class).build();
            androidx.work.WorkManager.getInstance(context).enqueue(uploadWorkRequest);
        }
        else*/ if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        //context.startService(serviceIntent);

    }
}

