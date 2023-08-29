package fr.themsou.monitorinternetless.commander;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Pattern;

import fr.themsou.monitorinternetless.R;
import fr.themsou.monitorinternetless.ui.commands.Command;
import fr.themsou.monitorinternetless.ui.commands.CommandsFragment;

public class CommandExecutor {



    public static final String TAG = "CommandExecutor";
    private String terminatedMessage = null;

    private final String[] args;
    private final Context context;
    public final String fromNumber;
    public CommandExecutor(String[] args, Context context, String fromNumber){
        this.args = args;
        this.context = context;
        this.fromNumber = fromNumber;
    }
    public String executeAuto() {

        Log.d(TAG, "executeAuto: Receiving command " + args[0]);

        ArrayList<Command> commands = CommandsFragment.getCommands(context);
        //final BlockingQueue<String> asyncResult = new SynchronousQueue<>();

        for(Command command : commands){
            if(context.getString(command.getTitle()).split(Pattern.quote(" "))[0].equalsIgnoreCase(args[0])){
                if(command.isEnabled()) {
                    if(command.hasPermission(context)){

                        switch (context.getString(command.getTitle()).split(Pattern.quote(" "))[0]){
                            case "!info":
                                new InfoCommandExecutor(context, this).execute(args);
                                break;
                            case "!locate":
                                new LocateCommandExecutor(context, this).execute(args);
                                break;
                            case "!ring":
                                new RingCommandExecutor(context, this).execute(args);
                                break;
                        }

                    }else replyAndTerminate(context.getString(R.string.command_error_no_permission));
                }else replyAndTerminate(context.getString(R.string.command_error_not_enabled));
                break;
            }
        }
        if(terminatedMessage == null) return "No terminated message";
        else return terminatedMessage;
    }


    public void replyAndTerminate(String message){
        reply(fromNumber, message);
        terminate("From replyAndTerminate() \"" + message + "\"");
    }
    public static void reply(String fromNumber, String message){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendMultipartTextMessage(fromNumber, null, smsManager.divideMessage(message), null, null);
    }
    public void terminate(String message){
        terminatedMessage = message;
    }

}
