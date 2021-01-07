package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button SendVerificationCodeButton , VerifyButton;
    private EditText InputPhoneNumber,InputVerificationCode;
    //callback
    //Registered callbacks for the different phone auth events. Requires implementing two mandatory callbacks and provides default no-op implementations for optional callbacks.
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    //progress dialog for the signing in
    private ProgressDialog loadingBar;
    // verification id
    private String mVerificationId;
    //A 'token' that can be used to force re-sending an SMS verification code.
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    //firebase auth
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        SendVerificationCodeButton = (Button) findViewById(R.id.send_verification_code_button);
        VerifyButton = (Button) findViewById(R.id.verify_button);
        InputPhoneNumber = (EditText) findViewById(R.id.phone_number_input);
        InputVerificationCode = (EditText) findViewById(R.id.verification_code_input);
        loadingBar = new ProgressDialog(this);


        //setting click listener

        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //taking the input phone number
                String phoneNumber = InputPhoneNumber.getText().toString();

                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "Phone Number is Required", Toast.LENGTH_SHORT).show();
                }else{

                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please Wait ..... ");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    //send verification to the user
                    //Represents the phone number authentication mechanism. Use this class to obtain PhoneAuthCredentials.
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,       //phone number to verify
                            60,    //time duration
                            TimeUnit.SECONDS , //unit of timeout
                            PhoneLoginActivity.this,
                            mCallbacks);

                }
            }
        });

        //now verify the code with the number entered in the edit field
        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //make sure that these two fields are invisible
                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                String verificationCode = InputVerificationCode.getText().toString();

                if(TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActivity.this, "Please write the code", Toast.LENGTH_SHORT).show();

                }else{
                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Please Wait ..... ");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    //create PhoneauthCredential object to verify the code
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });

        //defining the methods for various callbacks conditions
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            //these two are defaults

            //This callback must be implemented. It will trigger when an SMS is auto-retrieved or
            // the phone number has been instantly verified. The callback will provide a (@link AuthCredential).
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                //pass the phoneAuth credential to sign in method
                //this is in the case for auto-detection of the code and logging in
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number Please Enter Correct phone Number with your Country Code...", Toast.LENGTH_SHORT).show();
                //make the phone number field visible if verification code sending fails
                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);
                //set the code one to be visible
                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);

            }

            @Override
            //here string s is the verification id
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                //verification token and resend token
                mVerificationId = s;
                mResendToken = forceResendingToken;
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been Sent to Your Number", Toast.LENGTH_SHORT).show();

                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);
                //set the code one to be visible
                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);
            }
        };
    }
    //Wraps phone number and verification information for authentication purposes.
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        // here credential is Represents a credential that the Firebase Authentication server can use to authenticate a user.
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    loadingBar.dismiss();
                    Toast.makeText(PhoneLoginActivity.this, "You are Logged in Successfully", Toast.LENGTH_SHORT).show();
                    SendUserToMainActivity();
                }else{
                    //error
                    String message = task.getException().toString();
                    Toast.makeText(PhoneLoginActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}