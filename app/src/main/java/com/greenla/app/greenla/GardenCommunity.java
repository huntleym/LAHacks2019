package com.greenla.app.greenla;

import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class GardenCommunity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garden_community);
        TextView textview = (TextView)findViewById(R.id.members);
        textview.setPaintFlags(textview.getPaintFlags()|Paint.UNDERLINE_TEXT_FLAG);
    }
}
