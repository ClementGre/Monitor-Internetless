package fr.themsou.monitorinternetless.ui.commands;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CommandsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CommandsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Commands fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}