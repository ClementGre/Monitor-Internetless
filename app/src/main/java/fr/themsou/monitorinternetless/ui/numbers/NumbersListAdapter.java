package fr.themsou.monitorinternetless.ui.numbers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fr.themsou.monitorinternetless.R;

public class NumbersListAdapter extends BaseAdapter {

    private Context context; //context
    private ArrayList<Number> items; //data source of the list adapter

    //public constructor
    public NumbersListAdapter(Context context, ArrayList<Number> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Number getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void removeItem(int position){
        Number.removeNumber(context, items.get(position));
        items.remove(position);
        notifyDataSetChanged();
    }
    public void addItem(Number number){
        Number corresponding = getWithNumber(number);
        if(corresponding != null){
            if(corresponding.getOwner().equals(number.getOwner())){
                Toast.makeText(context, R.string.number_already_exist, Toast.LENGTH_LONG).show();
            }else{
                Number.removeNumber(context, corresponding);
                Number.addNumbers(context, number);
                corresponding.setOwner(number.getOwner());
                notifyDataSetChanged();
            }
        }else{
            items.add(number);
            Number.addNumbers(context, number);
            notifyDataSetChanged();
        }
    }
    public Number getWithNumber(Number number){
        for(Number item : items){
            if(item.getNumber().equals(number.getNumber())) return item;
        }
        return null;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.listitem_numbers, parent, false);

            ((TextView) convertView.findViewById(R.id.number_number)).setText(getItem(position).getNumber());
            ((TextView) convertView.findViewById(R.id.number_owner)).setText(getItem(position).getOwner());

            ImageButton button = convertView.findViewById(R.id.delete_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v){
                    removeItem(position);
                }
            });

        }
        return convertView;
    }
}
