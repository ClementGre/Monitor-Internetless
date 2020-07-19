package fr.themsou.monitorinternetless.commander;

import android.content.Context;

import java.util.ArrayList;
import fr.themsou.monitorinternetless.ui.commands.Command;
import fr.themsou.monitorinternetless.ui.commands.CommandsFragment;

public class CommandExecutor {

    public String execute(String[] args, Context context){

        ArrayList<Command> commands = CommandsFragment.getCommands(context);

        for(Command command : commands){
            if(context.getString(command.getTitle()).equals(args[0])){
                return command.execute(args, context);
            }
        }
        return null;
    }

}
