package com.greenla.app.greenla;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private EditText name;
    private EditText password;
    private Button login;
    private EditText incorrect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        name = (EditText)findViewById(R.id.etUsername);
        password = (EditText)findViewById(R.id.etPassword);
        login = (Button)findViewById(R.id.btnLogin);
        incorrect = (EditText)findViewById(R.id.editText4);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate(name.toString(), password.toString());
            }
        });
    }

    private void validate(String username, String pass){

        if((username == "Admin") && (pass == "Admin")){
            Intent intent = new Intent(LoginActivity.this, MainMapActivity.class);
            startActivity(intent);
        }
        else {
            incorrect.setVisibility(View.VISIBLE);
            Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
            startActivity(intent);
        }

    }
}
