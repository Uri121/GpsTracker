package com.example.gpstracker.UserSetUp;

import android.app.ActivityOptions;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gpstracker.Map.MyNavigationDrawer;
import com.example.gpstracker.Model.LoggedUser;
import com.example.gpstracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karan.churi.PermissionManager.PermissionManager;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

import io.paperdb.Paper;

/**
 * Created by Uri Robinov on 20/7/2019.
 */
public class MainActivity extends AppCompatActivity {

    private PermissionManager permissionManager;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private EditText editEmail, editPassword;
    private AVLoadingIndicatorView avi;
    private Button btLogin;
    private TextView forgotPassword, register;
    private ImageView logo, background;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null)
        {
            Paper.init(this);
            setContentView(R.layout.activity_main);
            permissionManager = new PermissionManager() {};
            permissionManager.checkAndRequestPermissions(this);
        }
        else {
            Paper.init(this);
            Paper.book().write(LoggedUser.USER_UID_KRY,user.getUid());
            Intent intent = new Intent(MainActivity.this, MyNavigationDrawer.class);
            startActivity(intent);
            finish();
            return;

        }

        forgotPassword = findViewById(R.id.forgot_password);
        background = findViewById(R.id.background_main);
        logo = findViewById(R.id.map_main);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        register = findViewById(R.id.go_to_register);
        avi = findViewById(R.id.avi);
        stopAnim();
        btLogin = findViewById(R.id.inSignIn);
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editEmail.getText().length()>1)
                {
                    if (editPassword.getText().length()>6){
                        login();
                    }
                }

            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, Register.class);
                Pair[] pairs = new Pair[2];
                pairs[0]=new Pair<View, String>(background,"image_trans");
                pairs[1]=new Pair<View, String>(logo,"logo_trans");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, pairs);

                startActivity(intent, options.toBundle());
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ForgotPassword.class);
                startActivity(i);
            }
        });

    }

    private void login(){
        startAnim();
        auth.signInWithEmailAndPassword(editEmail.getText().toString(),editPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            stopAnim();
                            FirebaseUser user = auth.getCurrentUser();
                            if (user.isEmailVerified()){
                                Paper.book().write(LoggedUser.USER_UID_KRY, user.getUid());
                                finish();
                                Intent intent = new Intent(MainActivity.this, MyNavigationDrawer.class);
                                startActivity(intent);
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Email is not Verified", Toast.LENGTH_SHORT).show();
                            }

                        }
                        else {
                            stopAnim();
                            Toast.makeText(MainActivity.this, "Wrong Email or Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void stopAnim() {
        avi.hide();
    }

    private void startAnim() {
        avi.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode,permissions, grantResults);

        ArrayList<String> deneid_per = permissionManager.getStatus().get(0).denied;

        if (deneid_per.isEmpty()){
            Toast.makeText(this, "permission enabled", Toast.LENGTH_SHORT).show();
        }
    }
}
