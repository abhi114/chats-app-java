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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {


    private Button CreateAccountButton;
    private EditText UserEmail , UserPassword;
    private TextView AlreadyHaveAccountLink;
    private FirebaseAuth mAuth;
    //The entry point for accessing a Firebase Database. You can get an instance by calling getInstance(). To access a location in the database and read or write data, use getReference().
    private DatabaseReference rootRef;

    private ProgressDialog loadingBar ; //A dialog showing a progress indicator and an optional text message or view.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance(); // initialize the firebase authentication manager
        rootRef = FirebaseDatabase.getInstance().getReference();
        InitializeFields();

        //if have an account send to login screen
        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        //when clicks on create account button get the fields of the email and password

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });

    }

    private void CreateNewAccount() {
        //getting the email and password
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"please enter the email",Toast.LENGTH_SHORT).show();

        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"please enter the password",Toast.LENGTH_SHORT).show();

        }
        else{

            loadingBar.setTitle("Creating new Account");
            loadingBar.setMessage("Please Wait, while we are creating the new account ....");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    //if user is created successfully

                    if(task.isSuccessful()){
                        //get the token for notification


                        //SendUserToLoginActivity();
                        //getting the id of the current user
                        final String currentUserId = mAuth.getCurrentUser().getUid();
                        rootRef.child("Users").child(currentUserId).setValue("");

                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                String  deviceToken = task.getResult();
                                rootRef.child("Users").child(currentUserId).child("device_token").setValue(deviceToken);

                            }
                        });



                        SendUserToMainActivity(); // so that the user can directly use the app when he creates the account
                        Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                    }
                    else{
                        //if error occurs
                        //which type of exception
                        String msg = task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        //to prevent the user to go back to the login activity after logging in
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(mainIntent);
        finish();
    }

    //initializing all the fields
    private void InitializeFields() {

        CreateAccountButton = (Button) findViewById(R.id.register_button);

        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);

        AlreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_account_link);

        loadingBar = new ProgressDialog(this);

    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }
}