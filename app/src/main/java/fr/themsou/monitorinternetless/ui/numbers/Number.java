package fr.themsou.monitorinternetless.ui.numbers;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

@Entity
public class Number{

    @ColumnInfo(name = "owner")
    private String owner;

    @PrimaryKey @NonNull
    private String number;

    public Number(String owner, String number){
        this.owner = owner;
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public static void getNumbers(final Activity activity, final Consumer<ArrayList<Number>> callback){
        new Thread(new Runnable() {
            @Override public void run() {
                NumberDatabase db = Room.databaseBuilder(activity.getApplicationContext(), NumberDatabase.class, "authorized_numbers").build();
                final ArrayList<Number> numbers = new ArrayList<>(db.daoAccess().getAll());
                db.close();
                activity.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        callback.accept(numbers);
                    }
                });
            }
        }).start();
    }
    public static void addNumbers(final Context context, final Number... numbers){
        new Thread(new Runnable() {
            @Override public void run() {
                NumberDatabase db = Room.databaseBuilder(context, NumberDatabase.class, "authorized_numbers").build();
                db.daoAccess().insertAll(numbers);
                db.close();
            }
        }).start();
    }
    public static void removeNumber(final Context context, final Number numbers){
        new Thread(new Runnable() {
            @Override public void run() {
                NumberDatabase db = Room.databaseBuilder(context, NumberDatabase.class, "authorized_numbers").build();
                db.daoAccess().delete(numbers);
                db.close();
            }
        }).start();
    }
}
