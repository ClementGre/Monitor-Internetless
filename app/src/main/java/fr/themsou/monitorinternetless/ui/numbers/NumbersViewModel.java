package fr.themsou.monitorinternetless.ui.numbers;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NumbersViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public NumbersViewModel() {

        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}