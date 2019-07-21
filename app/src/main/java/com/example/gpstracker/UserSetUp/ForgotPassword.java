package com.example.gpstracker.UserSetUp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gpstracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.wang.avi.AVLoadingIndicatorView;
/**
 * Created by Uri Robinov on 20/7/2019.
 */
public class ForgotPassword extends AppCompatActivity {

    private EditText email;
    private Button bt_forgot;
    private FirebaseAuth auth;
    private AVLoadingIndicatorView avi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);


        email = findViewById(R.id.forgot_email);
        bt_forgot = findViewById(R.id.forgot_bt);
        avi = findViewById(R.id.avi_forgot);
        stopAnim();

        auth = FirebaseAuth.getInstance();
        bt_forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEmailAndResetPassword();
            }
        });
    }

    private void getEmailAndResetPassword() {

        String mail = email.getText().toString().trim();
        startAnim();
        if (!mail.isEmpty()){
            auth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        stopAnim();
                        Toast.makeText(ForgotPassword.this, "Password reset mail has been sent", Toast.LENGTH_SHORT).show();
                        finish();
                        Intent i = new Intent(ForgotPassword.this, MainActivity.class);
                        startActivity(i);
                    }
                    else {
                        Toast.makeText(ForgotPassword.this, "Error in sending password reset", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            Toast.makeText(this, "Please enter the registered mail", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAnim() {
        avi.hide();
    }

    private void startAnim() {
        avi.show();

    }
}
