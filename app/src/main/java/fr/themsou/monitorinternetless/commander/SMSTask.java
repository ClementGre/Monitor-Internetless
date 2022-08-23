package fr.themsou.monitorinternetless.commander;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.regex.Pattern;

import fr.themsou.monitorinternetless.R;
import fr.themsou.monitorinternetless.ui.commands.Command;
import fr.themsou.monitorinternetless.ui.commands.CommandsFragment;
import fr.themsou.monitorinternetless.ui.numbers.Number;
import fr.themsou.monitorinternetless.ui.settings.Setting;

public class SMSTask extends AsyncTask<String, Integer, String> {

    private final BroadcastReceiver.PendingResult pendingResult;
    private final Intent intent;
    private final Context context;
    private final String messageFrom;
    private final String messageBody;
    private static final String TAG = "SMSTask";

    private final int AUTHORIZED = 0;
    private final int PASSWORD_REQUIRED = 1;
    private final int ACCESS_DENY = 2;

    public SMSTask(BroadcastReceiver.PendingResult pendingResult, Intent intent, Context context, String messageFrom, String messageBody){
        this.pendingResult = pendingResult;
        this.intent = intent;
        this.context = context;
        this.messageFrom = messageFrom;
        this.messageBody = messageBody;
    }

    @Override
    protected String doInBackground(String... strings) {

        int authorized = isAuthorizedNumber();
        if(authorized == ACCESS_DENY){
            Log.d(TAG, "doInBackground: The access is denied");
            return "Not authorized number";
        }
        if(authorized == PASSWORD_REQUIRED) {
            if(messageBody.split(Pattern.quote(" "))[0].equalsIgnoreCase("!login")){
                new LoginCommandExecutor(context).execute(messageBody.split(Pattern.quote(" ")), messageFrom);
            }else{
                ArrayList<Command> commands = CommandsFragment.getCommands(context);
                for(Command command : commands){
                    if(context.getString(command.getTitle()).split(Pattern.quote(" "))[0].equalsIgnoreCase(messageBody.split(Pattern.quote(" "))[0])){
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendMultipartTextMessage(messageFrom, null, smsManager.divideMessage(context.getString(R.string.command_password_required)), null, null);
                        break;
                    }
                }
            }
        }else if(authorized == AUTHORIZED) {
            Log.d(TAG, "doInBackground: command " + messageBody);

            CommandExecutor executor = new CommandExecutor(messageBody.split(Pattern.quote(" ")), context, messageFrom);
            String result = executor.executeAuto();
            Log.d(TAG, "doInBackground: CommandExecutor have return \"" +  result + "\"");

            return "completed";
        }
        return "error : no authorisation status";
    }
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d(TAG, "Post Execute");
        // Must call finish() so the BroadcastReceiver can be recycled.
        if(pendingResult != null){
            pendingResult.finish();
        }
    }


    private int isAuthorizedNumber(){

        String messageFromFormatted = Number.formatNumber(messageFrom, context);
        Setting everyoneAllowed = new Setting("everyone-allowed", context);
        Setting password = new Setting("password", context);

        if(!everyoneAllowed.isEnabled()){
            boolean authorized = false;
            ArrayList<Number> authorizedNumbers;
            final BlockingQueue<ArrayList<Number>> asyncResult = new SynchronousQueue<>();
            Number.getNumbersOutsideActivity(context, numbers -> {
                try{
                    asyncResult.put(numbers);
                }catch(InterruptedException e){ e.printStackTrace(); }
            });

            try{
                authorizedNumbers = asyncResult.take();
                for(Number number : authorizedNumbers){
                    if(number.getNumber().equals(messageFromFormatted)){
                        authorized = true; break;
                    }
                }
                if(!authorized) return ACCESS_DENY;
            }catch(InterruptedException e){ e.printStackTrace(); }
        }

        if(password.isEnabled()){
            SharedPreferences sharedPref = context.getSharedPreferences("fr.themsou.monitorinternetless.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
            boolean isLogin = sharedPref.getStringSet("authorizednumbers", new HashSet<String>()).contains(messageFromFormatted);
            if(!isLogin) return PASSWORD_REQUIRED;
        }

        return AUTHORIZED;
    }
}
