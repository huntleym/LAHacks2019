package com.greenla.app.greenla;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private EditText name;
    private EditText password;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        name = (EditText)findViewById(R.id.etUsername);
        password = (EditText)findViewById(R.id.etPassword);
        login = (Button)findViewById(R.id.btnLogin);
    }

    private void validate(String username, String password){
        if((username == "Admin") && (password == "Admin")){
            Intent intent = new Intent(LoginActivity.this, MainMapActivity.class);
        }
    }
}
