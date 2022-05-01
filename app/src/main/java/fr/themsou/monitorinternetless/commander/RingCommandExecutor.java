package fr.themsou.monitorinternetless.commander;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;

import fr.themsou.monitorinternetless.MainActivity;
import fr.themsou.monitorinternetless.R;

public class RingCommandExecutor {

    private final Context context;
    private final CommandExecutor commandExecutor;
    public RingCommandExecutor(Context context, CommandExecutor commandExecutor){
        this.context = context;
        this.commandExecutor = commandExecutor;
    }

    public static MediaPlayer mediaPlayer;
    public static int oldVolume;

    @SuppressLint("MissingPermission")
    public void execute(String[] args){
        int duration = 10;

        // Parse duration
        if(args.length >= 2){
            try{
                duration = Integer.parseInt(args[1]);
            }catch(NumberFormatException ignored){}
        }

        // Start mediaPlayer
        Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);


        final AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        oldVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        try{

            if(mediaPlayer != null) mediaPlayer.stop();

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
            mediaPlayer.setVolume(1, 1);
            mediaPlayer.setLooping(true);
            mediaPlayer.setDataSource(context, defaultRingtoneUri);
            mediaPlayer.prepare();

            mediaPlayer.start();

            Intent intent = new Intent(context,  MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            Notification notification = new NotificationCompat.Builder(context, "ring")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Sonnerie")
                    .setContentText("Le téléphone sonne ! Appuyez pour couper le son.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(1);
            notificationManager.notify(1, notification);



            final int finalDuration = duration;
            new Thread(new Runnable() {
                public void run() {
                    try{ Thread.sleep(1000L * finalDuration); }catch(InterruptedException e){ e.printStackTrace();}
                    mediaPlayer.stop();
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldVolume, 0);
                    notificationManager.cancel(1);
                }
            }).start();

            String durationString = duration < 60 ? (duration + " secondes") : "" + ((duration/60) + " minutes et " + (duration%60) + " secondes");
            commandExecutor.replyAndTerminate("Le téléphone sonne pour " + durationString + " !");

        }catch (IOException | IllegalArgumentException | IllegalStateException | SecurityException e){
            e.printStackTrace();

            commandExecutor.replyAndTerminate("Impossible de faire sonner le téléphone");
        }








    }


}
