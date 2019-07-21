package com.example.gpstracker.Circle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.gpstracker.Map.MyNavigationDrawer;
import com.example.gpstracker.Model.User;
import com.example.gpstracker.R;
import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
/**
 * Created by Uri Robinov on 20/7/2019.
 */
public class JoinCircle extends AppCompatActivity {

    private Pinview pinView;
    private DatabaseReference reference,circleRef;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private Button btSubmit;
    private String joinMemberId, curId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_circle);
        pinView = findViewById(R.id.pinView);
        btSubmit = findViewById(R.id.submit);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("User");
        curId = user.getUid();

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonSubmit();
            }
        });

    }

    private void buttonSubmit() {

        Query query = reference.orderByChild("code").equalTo(pinView.getValue());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User curUser;
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        curUser = dataSnapshot1.getValue(User.class);
                        joinMemberId = curUser.getUserId();

                        circleRef = FirebaseDatabase.getInstance().getReference()
                                .child("User").child(joinMemberId).child("CircleMembers");

                        JoinToCircle circle = new JoinToCircle(curId);

                        circleRef.child(user.getUid()).setValue(circle)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(JoinCircle.this, "User has been " +
                                                            "joined to your circle successfully",
                                                    Toast.LENGTH_SHORT).show();
                                            Intent i = new Intent(JoinCircle.this, MyNavigationDrawer.class);
                                            startActivity(i);
                                            finish();
                                        }
                                    }
                                });


                    }

                }else {
                    Toast.makeText(JoinCircle.this, "Circle code is invalid", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
