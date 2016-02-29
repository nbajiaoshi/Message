package com.ihs.demo.message_2013011320;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.message_2013011320.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SettingMenuAdapter extends ArrayAdapter<String> {

    private List<String> options;
    private Context context;

    public SettingMenuAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        options = objects;
        options.add("屏蔽此人消息");
        this.context = context;        
    }

    public View getView(int position, View convertView, ViewGroup parent) {
    	Log.i("luyu", "luyu:laji begin");
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null){
        	convertView = inflater.inflate(R.layout.text_option, parent, false);
        }
        TextView textView = (TextView )convertView.findViewById(R.id.text_option);
        
        textView.setText(options.get(position));
        Log.i("luyu", "luyu:laji end");
        return convertView;
    }
}
