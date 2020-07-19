package fr.themsou.monitorinternetless.commander;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.regex.Pattern;

public class SMSService extends Service {

    private static final String TAG = "SMSService";
    private String messageFrom;
    private String messageBody;


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate: STARTED !");
        Toast.makeText(this, "onCreate " + messageBody, Toast.LENGTH_LONG).show();

        if(messageBody.startsWith("!")){
            CommandExecutor executor = new CommandExecutor();
            String resultMessage = executor.execute(messageBody.split(Pattern.quote(" ")), this);
            if(resultMessage == null) return;
            if(resultMessage.isEmpty()) return;
            try{
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendMultipartTextMessage(messageFrom, null, smsManager.divideMessage(resultMessage), null, null);
                Log.d("SMSReceiver", "doInBackground: Success while sending SMS : " + resultMessage);
            }catch (Exception e){
                Log.d("SMSReceiver", "doInBackground: Error while sending SMS : " + e.getMessage());
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        this.messageFrom = extras.getString("from");
        this.messageBody = extras.getString("body");

        Log.d(TAG, "onStart: STARTING !");

        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

