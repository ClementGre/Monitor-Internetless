package fr.themsou.monitorinternetless.commander;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.SmsManager;

import java.util.HashSet;
import java.util.Set;

import fr.themsou.monitorinternetless.R;
import fr.themsou.monitorinternetless.ui.numbers.Number;
import fr.themsou.monitorinternetless.ui.settings.Setting;

public class LoginCommandExecutor {

    private final Context context;
    public LoginCommandExecutor(Context context){
        this.context = context;
    }

    @SuppressLint("MissingPermission")
    public void execute(String[] args, String messageFrom){

        String messageFromFormatted = Number.formatNumber(messageFrom, context);
        Setting passwordSetting = new Setting("password", context);
        SmsManager smsManager = SmsManager.getDefault();

        if(args.length >= 2){
            if(args[1].equals(passwordSetting.getValue())){
                SharedPreferences sharedPref = context.getSharedPreferences("fr.themsou.monitorinternetless.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
                Set<String> authorizedNumbers = sharedPref.getStringSet("authorizednumbers", new HashSet<String>());
                authorizedNumbers.add(messageFromFormatted);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putStringSet("authorizednumbers", authorizedNumbers);
                editor.commit();

                smsManager.sendMultipartTextMessage(messageFrom, null, smsManager.divideMessage(context.getString(R.string.command_logged_in)), null, null);
            }else{
                smsManager.sendMultipartTextMessage(messageFrom, null, smsManager.divideMessage(context.getString(R.string.command_wrong_password)), null, null);
            }
        }else{
            smsManager.sendMultipartTextMessage(messageFrom, null, smsManager.divideMessage(context.getString(R.string.command_password_help)), null, null);
        }
    }

}
