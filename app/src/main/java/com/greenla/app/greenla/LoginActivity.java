package com.greenla.app.greenla;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText name;
    private EditText password;
    private Button loginBtn;
    private EditText incorrect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        name = (EditText)findViewById(R.id.etUsername);
        password = (EditText)findViewById(R.id.etPassword);
        loginBtn = (Button)findViewById(R.id.btnLogin);
        incorrect = (EditText)findViewById(R.id.editText4);
        
        if (getIntent().getSerializableExtra("INCORRECT_LOGIN") != null) {
            boolean upIncorrect = (boolean)getIntent().getSerializableExtra("INCORRECT_LOGIN");
            if (upIncorrect) {
                incorrect.setVisibility(View.VISIBLE);
            }
        }

        loginBtn.setText("    ");
        loginBtn.setOnClickListener(this);

        /*
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate(name.getText().toString(), password.getText().toString());
            }
        });
        */
    }

    public void onClick(View v){

        switch(v.getId())
        {
            case R.id.btnLogin:
            {
                //open login screen
                Intent i = new Intent(this, MapActivity.class);
                startActivity(i);
                break;
            }
            case R.id.etUsername:
            {
                break;
            }
            case R.id.etPassword:
            {
                break;
            }
            case R.id.editText4:
            {
                break;
            }

        }
    }

    private void validate(String username, String pass){

        if((username.equals("Admin")) && (pass.equals("Admin"))){
            Intent intent = new Intent(LoginActivity.this, MapActivity.class);
            startActivity(intent);
        }
        else {
            //incorrect.setVisibility(View.VISIBLE);
            Intent intent = new Intent(LoginActivity.this, MapActivity.class);
            boolean upIncorrect = true;
            intent.putExtra("INCORRECT_LOGIN", upIncorrect);
            startActivity(intent);
        }

    }
}
