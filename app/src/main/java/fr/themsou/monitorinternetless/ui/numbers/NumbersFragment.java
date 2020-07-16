package fr.themsou.monitorinternetless.ui.numbers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import fr.themsou.monitorinternetless.MainActivity;
import fr.themsou.monitorinternetless.R;

import static android.app.Activity.RESULT_OK;

public class NumbersFragment extends Fragment {

    private static final int CONTACT_PICKER_RESULT = 1001;

    private ListView listView;
    private NumbersListAdapter adapter;
    private FloatingActionButton actionButton;
    private String TAG = "NumbersFragment";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ((MainActivity) getActivity()).getTopToolBar().setTitle(getString(R.string.title_numbers_full));

        View root = inflater.inflate(R.layout.fragment_numbers, container, false);

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        ArrayList<Number> numbers = Number.getNumbers();
        adapter = new NumbersListAdapter(getContext(), numbers);

        listView = root.findViewById(R.id.numbers_listview);
        listView.addHeaderView(layoutInflater.inflate(R.layout.header_numbers, null));
        listView.setAdapter(adapter);

        actionButton = root.findViewById(R.id.floating_action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ((MainActivity) getActivity()).permissionRequester.grantOnly(Manifest.permission.READ_CONTACTS, new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {
                        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
                        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
                    }
                });
            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch(requestCode){
                case CONTACT_PICKER_RESULT:
                    assert data != null;
                    Uri result = data.getData();
                    if(result != null){
                        String[] contactInfo = getContactInfo(result.getLastPathSegment());
                        adapter.addItem(new Number(contactInfo[0], contactInfo[1]));
                    }
                    break;
            }
            if(requestCode >= 2000 && requestCode < 3000){
                Log.w(TAG, "Warning: receive false requestCode");
            }
        }else{
            Log.w(TAG, "Warning: activity result not ok");
        }
    }

    public String getContactName(String number){
        return "";
    }
    public String[] getContactInfo(String uriId){

        Cursor cursor = getActivity().getContentResolver().query(
                CommonDataKinds.Phone.CONTENT_URI, new String[]{CommonDataKinds.Phone.NUMBER, CommonDataKinds.Identity.DISPLAY_NAME},
                CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{uriId},null);

        if(cursor.moveToFirst()){
            int numberIdx = cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER);
            int ownerIdx = cursor.getColumnIndex(CommonDataKinds.Identity.DISPLAY_NAME);
            return new String[]{cursor.getString(ownerIdx), cursor.getString(numberIdx)};
        }
        return null;
    }

}