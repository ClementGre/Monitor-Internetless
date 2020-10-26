package fr.themsou.monitorinternetless.ui.about;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import fr.themsou.monitorinternetless.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.menu_about);

        setupLink(R.id.textView4);
        setupLink(R.id.textView5);
        setupLink(R.id.textView6);
        setupLink(R.id.textView7);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isFinishing()){
            overridePendingTransition(R.anim.fab_slide_in_from_left, R.anim.fab_slide_out_to_right);
        }
    }

    private void setupLink(int id){
        TextView linkTextView = findViewById(id);
        linkTextView.setMovementMethod(LinkMovementMethod.getInstance());
        linkTextView.setLinkTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }
}