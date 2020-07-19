package fr.themsou.monitorinternetless.commander;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.regex.Pattern;

import fr.themsou.monitorinternetless.ui.commands.CommandsFragment;

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

                        final PendingResult pendingResult = goAsync();
                        Task asyncTask = new Task(pendingResult, intent, context, msgs[i].getOriginatingAddress(), msgs[i].getMessageBody());
                        asyncTask.execute();
                    }
                }catch(Exception e){
                    Log.d("Exception caught", e.getMessage());
                }
            }
        }
    }

    private static class Task extends AsyncTask<String, Integer, String> {

        private final PendingResult pendingResult;
        private final Intent intent;
        private final Context context;
        private final String messageFrom;
        private final String messageBody;

        private Task(PendingResult pendingResult, Intent intent, Context context, String messageFrom, String messageBody) {
            this.pendingResult = pendingResult;
            this.intent = intent;
            this.context = context;
            this.messageFrom = messageFrom;
            this.messageBody = messageBody;
        }

        @Override
        protected String doInBackground(String... strings) {

            if(messageBody.startsWith("!")){
                CommandExecutor executor = new CommandExecutor();
                executor.execute(messageBody.split(Pattern.quote(" ")), context);
            }

            return "completed !";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Must call finish() so the BroadcastReceiver can be recycled.
            pendingResult.finish();
        }
    }

}
