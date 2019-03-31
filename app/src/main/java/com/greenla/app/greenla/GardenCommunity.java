package com.greenla.app.greenla;

import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class GardenCommunity extends AppCompatActivity {

    private Button members;
    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garden_community);

        members = (Button)findViewById(R.id.members);
        members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GardenCommunity.this, MembersActivity.class);
                startActivity(intent);
            }
        });

        back = (ImageButton)findViewById(R.id.comBack);
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GardenCommunity.this, GardenPage.class);
                startActivity(intent);
            }
        });
    }
}
