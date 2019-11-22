package com.example.fatma.forumadmin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class WelcomActivity extends AppCompatActivity {


        Button buttonLogin;


        private ProgressDialog mLogProgress;

        //Firebase Auth
        private FirebaseAuth mAuth;

        //to login
        private EditText emailLogin;
        private EditText passwordLogin;
        private DatabaseReference mUserDatabase;
        private ImageView mLoginError;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_welcom);

            //scroll click
            findViewById(R.id.welcom).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    return false;
                }
            });

            //prog bar
            mLogProgress= new ProgressDialog(this);
            //Firebase instance
            mAuth= FirebaseAuth.getInstance();
            //Firebase reference (for token id louta)
            mUserDatabase= FirebaseDatabase.getInstance().getReference().child("users");




            mLoginError=(ImageView) findViewById(R.id.login_error);

            //login
            emailLogin=(EditText) findViewById(R.id.userEmailLogin);
            passwordLogin=(EditText) findViewById(R.id.userPasswordLogin);
            buttonLogin= (Button) findViewById(R.id.buttonLogin);




            buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = emailLogin.getText().toString();
                    String password = passwordLogin.getText().toString();

                    if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)){


                        mLogProgress.setTitle("Logging In");
                        mLogProgress.setMessage("Please wait while we check your credentials..");
                        mLogProgress.setCanceledOnTouchOutside(false);
                        mLogProgress.show();


                        loginUser(email, password);
                    }

                }
            });



        }

        private void loginUser(String email, String password) {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        mLogProgress.dismiss();

                        //Token ID
                        String current_user_id=mAuth.getCurrentUser().getUid();
                        String device_token = FirebaseInstanceId.getInstance().getToken();

                        mUserDatabase.child(current_user_id).child("device_token").setValue(device_token)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent login = new Intent(WelcomActivity.this, MainActivity.class);
                                        login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(login);
                                        finish();
                                    }
                                });



                    }else{
                        mLogProgress.hide();
                        Toast.makeText(WelcomActivity.this, "Cannot Sign In. Please review the form", Toast.LENGTH_SHORT).show();
                        mLoginError.setBackgroundColor(Color.parseColor("#cc1512"));

                    }

                }
            });

        }
    }
