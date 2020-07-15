package fr.themsou.monitorinternetless.ui.numbers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import fr.themsou.monitorinternetless.R;

public class NumbersFragment extends Fragment {

    private NumbersViewModel numbersViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_numbers_full));
        //((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("your subtitle");

        numbersViewModel = ViewModelProviders.of(this).get(NumbersViewModel.class);
        View root = inflater.inflate(R.layout.fragment_numbers, container, false);

        final TextView textView = root.findViewById(R.id.text_numbers);

        numbersViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
    }
}