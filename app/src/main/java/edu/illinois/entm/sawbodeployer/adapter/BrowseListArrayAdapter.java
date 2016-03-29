package edu.illinois.entm.sawbodeployer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.StringTokenizer;

import edu.illinois.entm.sawbodeployer.R;

/**
 * Created by Pakpoomb.
 */
public class BrowseListArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String [] values;

    public BrowseListArrayAdapter(Context context, String[] values) {
        super(context, R.layout.browse_row, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.browse_row, parent, false);
        TextView video_title = (TextView) rowView.findViewById(R.id.video_title);
        TextView video_desc = (TextView) rowView.findViewById(R.id.video_desc);


        StringTokenizer tokens = new StringTokenizer(values[position], "^^");
        video_title.setText(tokens.nextToken());
        if (tokens.hasMoreTokens()) {
            video_desc.setText(tokens.nextToken());
        }
        return rowView;
    }
}

