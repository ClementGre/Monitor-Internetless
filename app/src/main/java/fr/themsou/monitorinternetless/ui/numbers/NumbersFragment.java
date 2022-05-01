package fr.themsou.monitorinternetless.ui.numbers;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fr.themsou.monitorinternetless.MainActivity;
import fr.themsou.monitorinternetless.R;

public class NumbersFragment extends Fragment {

    private static final int CONTACT_PICKER_RESULT = 1001;

    private ListView listView;
    private NumbersListAdapter adapter;
    private String TAG = "NumbersFragment";

    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState){
        if(((MainActivity) requireActivity()).getTopToolBar() != null) ((MainActivity) requireActivity()).getTopToolBar().setTitle(getString(R.string.title_numbers_full));

        View root = inflater.inflate(R.layout.fragment_numbers, container, false);
        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        listView = root.findViewById(R.id.numbers_listview);
        listView.addHeaderView(layoutInflater.inflate(R.layout.header_numbers, null));

        Number.getNumbers(getActivity(), numbers -> {
            adapter = new NumbersListAdapter(getContext(), numbers);
            listView.setAdapter(adapter);
        });

        root.findViewById(R.id.action_add_number_contacts).setOnClickListener(v -> ((MainActivity) requireActivity()).permissionRequester.grantOnly(Manifest.permission.READ_CONTACTS, aBoolean -> {
            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
            startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
        }));

        root.findViewById(R.id.action_add_number_manually).setOnClickListener(v -> {
            final View inputs = layoutInflater.inflate(R.layout.inputs_numbers, null);
            new AlertDialog.Builder(v.getContext())
                .setTitle(getString(R.string.add_number_title))
                .setMessage(getString(R.string.add_number_dialog))
                .setView(inputs)
                .setPositiveButton(getString(R.string.message_add), (dialog, which) -> {
                    adapter.addItem(new Number(
                            ((EditText) inputs.findViewById(R.id.editTextTextPersonName)).getText().toString(),
                            ((EditText) inputs.findViewById(R.id.editTextPhone))
                                    .getText().toString(), getActivity()));
                }).setNegativeButton(getString(R.string.message_cancel), (dialog, which) -> {}).show();
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == CONTACT_PICKER_RESULT){
                assert data != null;
                Uri result = data.getData();
                if(result != null){
                    String[] contactInfo = getContactInfo(result.getLastPathSegment());
                    if(contactInfo != null){
                        adapter.addItem(new Number(contactInfo[0], contactInfo[1], getActivity()));
                    }else{
                        Toast.makeText(getContext(), R.string.number_do_not_have_number, Toast.LENGTH_LONG).show();
                    }
                }
            }
            if(requestCode >= 2000 && requestCode < 3000){
                Log.w(TAG, "Warning: received false requestCode");
            }
        }else{
            Log.w(TAG, "Warning: activity result not ok");
        }
    }

    public String getContactName(String number){
        return "";
    }
    public String[] getContactInfo(String uriId){

        Cursor cursor = requireActivity().getContentResolver().query(
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