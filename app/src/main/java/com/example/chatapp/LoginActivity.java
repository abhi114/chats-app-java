package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    //private FirebaseUser currentUser;
    private Button LoginButton, PhoneLoginButton; // one for email login and other for number login
    private EditText Useremail , UserPassword;
    private TextView NeedNewAccountLink,ForgetPasswordLink;

    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        //to get the current user
        //currentUser = mAuth.getCurrentUser();


        InitializeFields();

        //clickListener for the need new account and forget password

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send the user to register Activity
                SendUserToRegisterActivity();
            }
        });

        //when user clicks on the login button
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });

    }

    private void AllowUserToLogin() {
        String email = Useremail.getText().toString();
        String password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"please enter the email",Toast.LENGTH_SHORT).show();

        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"please enter the password",Toast.LENGTH_SHORT).show();

        }
        else{

            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please Wait");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(LoginActivity.this, "Logged in Successfully ... ", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }else {
                        //if error occurs
                        //which type of exception
                        String msg = task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }
    }

    private void InitializeFields() {
        LoginButton = (Button) findViewById(R.id.login_button);
        PhoneLoginButton = (Button) findViewById(R.id.phone_login_button);

        Useremail = (EditText) findViewById(R.id.login_email);
        UserPassword = (EditText) findViewById(R.id.login_password);

        NeedNewAccountLink = (TextView) findViewById(R.id.need_new_account_link);
        ForgetPasswordLink = (TextView) findViewById(R.id.forget_password_link);

        loadingBar = new ProgressDialog(this);
    }

    //@Override
    //protected void onStart() {
      //  super.onStart();
        //it means the user in  authenticated
        //if(currentUser != null){
          //  SendUserToMainActivity();
        //}
    //}

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        //to prevent the user to go back to the login activity after logging in
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(mainIntent);
        finish();
    }
    //when the user clicks on need new account
    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }
}