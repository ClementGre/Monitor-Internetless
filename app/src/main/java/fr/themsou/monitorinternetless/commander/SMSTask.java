package fr.themsou.monitorinternetless.commander;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import androidx.core.util.Consumer;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.regex.Pattern;

import fr.themsou.monitorinternetless.ui.numbers.Number;

public class SMSTask extends AsyncTask<String, Integer, String> {

    private final BroadcastReceiver.PendingResult pendingResult;
    private final Intent intent;
    private final Context context;
    private final String messageFrom;
    private final String messageBody;
    private static final String TAG = "SMSTask";

    public SMSTask(BroadcastReceiver.PendingResult pendingResult, Intent intent, Context context, String messageFrom, String messageBody){
        this.pendingResult = pendingResult;
        this.intent = intent;
        this.context = context;
        this.messageFrom = messageFrom;
        this.messageBody = messageBody;
    }

    @Override
    protected String doInBackground(String... strings) {

        if(!isAuthorizedNumber()) return "Not authorized number";

        CommandExecutor executor = new CommandExecutor();
        String resultMessage = executor.execute(messageBody.split(Pattern.quote(" ")), context);
        if(resultMessage == null) return "result message is null, command unknown";
        if(resultMessage.isEmpty()) return "result message is empty, command unknown";
        try{
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendMultipartTextMessage(messageFrom, null, smsManager.divideMessage(resultMessage), null, null);
            Log.i(TAG, "doInBackground: Success while sending SMS : " + resultMessage);
        }catch (Exception e){
            Log.i(TAG, "doInBackground: Error while sending SMS : " + e.getMessage());
        }

        return "completed";
    }

    private boolean isAuthorizedNumber(){
        ArrayList<Number> authorizedNumbers;
        final BlockingQueue<ArrayList<Number>> asyncResult = new SynchronousQueue<>();
        Number.getNumbersOutsideActivity(context, new Consumer<ArrayList<Number>>() {
            @Override public void accept(ArrayList<Number> numbers) {
                try{
                    asyncResult.put(numbers);
                }catch(InterruptedException e){ e.printStackTrace(); }
            }
        });

        try{
            authorizedNumbers = asyncResult.take();



            String messageFromFormatted = messageFrom;
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String countryCodeValue = tm.getSimCountryIso();
            }

            for(Number number : authorizedNumbers){
                if(number.getNumber().equals(messageFrom)){
                    return true;
                }
            }
        }catch(InterruptedException e){ e.printStackTrace(); }
        return false;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        // Must call finish() so the BroadcastReceiver can be recycled.
        if(pendingResult != null){
            pendingResult.finish();
        }
    }
}
