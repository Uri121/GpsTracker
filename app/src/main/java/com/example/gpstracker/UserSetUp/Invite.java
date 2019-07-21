package com.example.gpstracker.UserSetUp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gpstracker.Circle.MyCircle;
import com.example.gpstracker.Map.MyNavigationDrawer;
import com.example.gpstracker.Model.LoggedUser;
import com.example.gpstracker.Model.User;
import com.example.gpstracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wang.avi.AVLoadingIndicatorView;
/**
 * Created by Uri Robinov on 20/7/2019.
 */
public class Invite extends AppCompatActivity {

    private String email,password,name, isSharing, code, userId, battery;
    private Button btRegister;
    private Uri imageUri;
    private StorageReference storageReference;
    private AVLoadingIndicatorView avi;
    private TextView invite;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference reference;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
            battery = String.valueOf(level);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        avi = findViewById(R.id.avi_invite);
        stopAnim();
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("User");
        storageReference = FirebaseStorage.getInstance().getReference().child("User_images");
        this.registerReceiver(this.broadcastReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        invite= findViewById(R.id.invite_text);
        btRegister = findViewById(R.id.invite_bt);

        Intent intent = getIntent();
        if (intent != null){
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
            code = intent.getStringExtra("code");
            name = intent.getStringExtra("name");
            isSharing = intent.getStringExtra("isSharing");
            imageUri = intent.getParcelableExtra("image");
        }
        invite.setText(code);

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser(){
        startAnim();
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            user = auth.getCurrentUser();
                            User newUser = new User(name,   email,  password, code,"false",0.0, 0.0,"na", user.getUid(), battery);
                            LoggedUser.sCurUser = newUser;
                            userId = user.getUid();

                            reference.child(userId).setValue(newUser)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull final Task<Void> task) {
                                            if (task.isSuccessful()){

                                                final StorageReference sr = storageReference.child(user.getUid()+ ".jpg");
                                                sr.putFile(imageUri)
                                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                sr.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                    @Override
                                                                    public void onSuccess(Uri uri) {

                                                                        reference.child(user.getUid()).child("image").setValue(String.valueOf(uri)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {

                                                                                stopAnim();
                                                                                sendVerificationEmail();
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        });
                                            }
                                            else{
                                                stopAnim();
                                                Toast.makeText(Invite.this, "couldn't insert data to data base", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void sendVerificationEmail(){
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(Invite.this, "Email sent to verification ", Toast.LENGTH_SHORT).show();
                            finish();
                            auth.signOut();
                        }
                        else {
                            Toast.makeText(Invite.this, "Couldn't Send Email", Toast.LENGTH_SHORT).show();
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
}
