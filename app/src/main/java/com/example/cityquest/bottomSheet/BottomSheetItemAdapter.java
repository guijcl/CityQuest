package com.example.cityquest.bottomSheet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.cityquest.R;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetItemAdapter extends ArrayAdapter<BottomSheetItem> {

    private Context ct;
    private ArrayList<BottomSheetItem> arr;

    public BottomSheetItemAdapter(Context context, int resource, List<BottomSheetItem> objects){
        super(context, resource, objects);
        this.ct = context;
        this.arr = new ArrayList<>(objects);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater i = (LayoutInflater) ct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = i.inflate(R.layout.bottom_sheet_item_layout, null);
        }

        if (arr.size() > 0) {
            BottomSheetItem bsi = arr.get(position);
        }

        return convertView;
    }

}
