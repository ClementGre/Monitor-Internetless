package fr.themsou.monitorinternetless.commander;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {

    private SharedPreferences preferences;

    @Override
    public void onReceive(Context context, Intent intent){

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras(); // get the SMS message passed in
            SmsMessage[] msgs = null;
            if(bundle != null){
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];

                    for(int i = 0; i < msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        if(msgs[i].getMessageBody().startsWith("!")){
                            final PendingResult pendingResult = goAsync();
                            AsyncTask<String, Integer, String> asyncTask = new SMSTask(pendingResult, intent, context.getApplicationContext(), msgs[i].getOriginatingAddress(), msgs[i].getMessageBody());
                            asyncTask.execute();
                        }
                    }
                }catch(Exception e){
                    Log.d("Exception caught", e.getMessage());
                }
            }
        }
    }

}
