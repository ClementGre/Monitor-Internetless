package fr.themsou.monitorinternetless.ui.logs;

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

import fr.themsou.monitorinternetless.MainActivity;
import fr.themsou.monitorinternetless.R;

public class LogsFragment extends Fragment {

    private LogsViewModel logsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity) getActivity()).getTopToolBar().setTitle(getString(R.string.title_logs));

        logsViewModel = ViewModelProviders.of(this).get(LogsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_logs, container, false);
        final TextView textView = root.findViewById(R.id.text_logs);
        logsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}