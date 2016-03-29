package edu.illinois.entm.sawbodeployer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import java.util.StringTokenizer;

import edu.illinois.entm.sawbodeployer.FilterFragment;
import edu.illinois.entm.sawbodeployer.R;

/**
 * Created by Pakpoomb.
 */
public class BrowseFilterArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String [] values;

    public BrowseFilterArrayAdapter(Context context, String[] values) {
        super(context, R.layout.filter_row, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.filter_row, parent, false);
                final CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.filter_checkBox);

        StringTokenizer tokens = new StringTokenizer(values[position], "::");
        checkBox.setText(tokens.nextToken());
        String checkedst = tokens.nextToken();
        boolean checked = checkedst.equals("1");
        checkBox.setChecked(checked);
        //set listener
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This gets the correct item to work with.
                FilterFragment ff = new FilterFragment();
                if(checkBox.isChecked()) {
                    ff.setFilter(String.valueOf(checkBox.getText()), false);
                } else {
                    ff.setFilter(String.valueOf(checkBox.getText()), true);
                }
            }
        });
        return rowView;
    }
}

