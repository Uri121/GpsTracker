package com.example.gpstracker.UserSetUp;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.gpstracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
/**
 * Created by Uri Robinov on 20/7/2019.
 */
public class Register extends AppCompatActivity {

    private EditText editEmail, editPassword, editName;
    private Button btNext;
    private FirebaseAuth auth;
    private CircleImageView imageView;
    private AVLoadingIndicatorView avi;
    private Uri resultUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        editEmail = findViewById(R.id.registerEmail);
        avi = findViewById(R.id.avi_register);
        stopAnim();
        btNext = findViewById(R.id.register_bt);
        editName = findViewById(R.id.username1);
        editPassword = findViewById(R.id.edit_password);
        imageView = findViewById(R.id.circleImage1);
        auth = FirebaseAuth.getInstance();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(v);
            }
        });

        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPassword(v);
            }
        });

    }
    private void goToPassword(final View v){
        startAnim();
        auth.fetchSignInMethodsForEmail(editEmail.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()){
                            boolean check = !task.getResult().getSignInMethods().isEmpty();
                            if (!check)
                            {
                                stopAnim();
                                goToNamePicActivity(v);
                            }
                            else {
                                stopAnim();
                                Toast.makeText(Register.this, "This email is already in the system", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void goToNamePicActivity(View v){

        if (editPassword.getText().toString().length() > 6){
           generateCode(v);
        }
        else {
            Toast.makeText(this, "Password length should be more then 6 digits", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateCode(View v){

        Random r = new Random();
        int n = 100000 + r.nextInt(900000);
        String code = String.valueOf(n);

        if (resultUri != null){

            Intent intent = new Intent(Register.this, Invite.class);
            intent.putExtra("name",editName.getText().toString());
            intent.putExtra("email",editEmail.getText().toString());
            intent.putExtra("password",editPassword.getText().toString());
            intent.putExtra("isSharing","false");
            intent.putExtra("code",code);
            intent.putExtra("image",resultUri);

            startActivity(intent);
            finish();
        }
        else {
            Toast.makeText(this, "Please choose and image", Toast.LENGTH_SHORT).show();
        }

    }


    private void selectImage(View v){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,12);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode== 12 && resultCode == RESULT_OK && data != null){
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                imageView.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void stopAnim() {
        avi.hide();
    }

    private void startAnim() {
        avi.show();

    }
}
