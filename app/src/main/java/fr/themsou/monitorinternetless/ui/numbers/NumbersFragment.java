package fr.themsou.monitorinternetless.ui.numbers;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import fr.themsou.monitorinternetless.MainActivity;
import fr.themsou.monitorinternetless.R;

import static android.app.Activity.RESULT_OK;

public class NumbersFragment extends Fragment {

    private static final int CONTACT_PICKER_RESULT = 1001;

    private ListView listView;
    private NumbersListAdapter adapter;
    private String TAG = "NumbersFragment";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ((MainActivity) getActivity()).getTopToolBar().setTitle(getString(R.string.title_numbers_full));

        View root = inflater.inflate(R.layout.fragment_numbers, container, false);
        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        listView = root.findViewById(R.id.numbers_listview);
        listView.addHeaderView(layoutInflater.inflate(R.layout.header_numbers, null));

        Number.getNumbers(getActivity(), new Consumer<ArrayList<Number>>() {
            @Override public void accept(ArrayList<Number> numbers) {
                adapter = new NumbersListAdapter(getContext(), numbers);
                listView.setAdapter(adapter);
            }
        });

        root.findViewById(R.id.action_button_contact).setOnClickListener(new View.OnClickListener() {
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

        root.findViewById(R.id.action_button_capture).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(final View v) {
                final View inputs = layoutInflater.inflate(R.layout.inputs_numbers, null);
                new AlertDialog.Builder(v.getContext())
                    .setTitle(getString(R.string.add_number_title))
                    .setMessage(getString(R.string.add_number_dialog))
                    .setView(inputs)
                    .setPositiveButton(getString(R.string.message_add), new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            adapter.addItem(new Number(((EditText) inputs.findViewById(R.id.editTextTextPersonName)).getText().toString(), ((EditText) inputs.findViewById(R.id.editTextPhone)).getText().toString()));
                        }
                    }).setNegativeButton(getString(R.string.message_cancel), new DialogInterface.OnClickListener(){ @Override public void onClick(DialogInterface dialog, int which){} }).show();
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
                        if(contactInfo != null){
                            adapter.addItem(new Number(contactInfo[0], contactInfo[1]));
                        }else{
                            Toast.makeText(getContext(), R.string.number_do_not_have_number, Toast.LENGTH_LONG).show();
                        }
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