package com.example.ntut.weshare.viewpage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.ntut.weshare.R;

/**
 * Created by rick on 2016/7/9.
 */

public class PageOne extends PageView {
    public PageOne(Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.page_content, null);
        TextView textView = (TextView) view.findViewById(R.id.text);
        textView.setText("Page one");
        addView(view);

    }

    @Override
    public void refresh() {

    }
}