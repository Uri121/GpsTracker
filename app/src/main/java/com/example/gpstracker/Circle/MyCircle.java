package com.example.gpstracker.Circle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.example.gpstracker.Adapters.CircleAdapter;
import com.example.gpstracker.Fragments.ContactsFragment;
import com.example.gpstracker.Model.SosPhoneContact;
import com.example.gpstracker.Sqlite.DbHelper;
import com.example.gpstracker.Map.MapsActivity;
import com.example.gpstracker.Model.User;
import com.example.gpstracker.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.ArrayList;

public class MyCircle extends AppCompatActivity implements CircleAdapter.OnCardClickedListener {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private ArrayList<User> usersList;
    private User curUser;
    private TextView title,email,code;
    private ImageView imageView;
    private String circleId;
    private DatabaseReference reference, userRef;
    private Switch bt_tracker;
    private Button btSos, btContactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_circle);
        recyclerView = findViewById(R.id.recycler);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        usersList = new ArrayList<>();

        title = findViewById(R.id.profile_name_text);
        email = findViewById(R.id.profile_mail_text);
        code = findViewById(R.id.profile_code_text);
        imageView = findViewById(R.id.circleImage_profile);
        bt_tracker = findViewById(R.id.button_profile);
        btSos = findViewById(R.id.add_contact);
        btContactList = findViewById(R.id.show_contacts_list);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        btSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });
        btContactList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final FragmentManager fragmentManager = getSupportFragmentManager();
                ArrayList<SosPhoneContact> list = DbHelper.getInstance(getApplication()).GetList();
                final ContactsFragment contactsFragment = new ContactsFragment(list);
                contactsFragment.show(fragmentManager,"Contacts");
            }

        });

        userRef = FirebaseDatabase.getInstance().getReference().child("User");

        reference = FirebaseDatabase.getInstance().getReference().child("User").child(user.getUid()).child("CircleMembers");

        userRef.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getKey().equals(user.getUid())) {
                    curUser = dataSnapshot.getValue(User.class);
                    title.setText("Name: "+ " " + curUser.getName());
                    email.setText("Email: "+ " "+curUser.getEmail());
                    code.setText("Code: "+ " "+ curUser.getCode());
                    Picasso.get()
                            .load(curUser.getImage())
                            .resize(100, 100)
                            .centerCrop()
                            .into(imageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();

                if (dataSnapshot.exists()){
                    for (DataSnapshot ds: dataSnapshot.getChildren())
                    {
                       circleId = ds.child("CircleMemberId").getValue(String.class);

                       userRef.child(circleId).addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                               User curUser = dataSnapshot.getValue(User.class);
                               usersList.add(curUser);
                               adapter.notifyDataSetChanged();
                           }

                           @Override
                           public void onCancelled(@NonNull DatabaseError databaseError) {
                               Toast.makeText(MyCircle.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                           }
                       });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MyCircle.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        userRef.child(user.getUid()).child("isSharing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String checked = dataSnapshot.getValue(String.class);
                if (checked.equals("true")){
                    bt_tracker.setChecked(true);
                }else {
                    bt_tracker.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        adapter = new CircleAdapter(usersList, this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        bt_tracker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkIfCanTrack();
            }
        });
    }

    private void checkIfCanTrack() {
        Boolean switchState = bt_tracker.isChecked();

        if (switchState){
            userRef.child(user.getUid()).child("isSharing").setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    bt_tracker.setChecked(true);
                    Toast.makeText(MyCircle.this, "Sharing Location is Enabled", Toast.LENGTH_SHORT).show();
                }
            });

        }else {
            userRef.child(user.getUid()).child("isSharing").setValue("false").addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    bt_tracker.setChecked(false);
                    Toast.makeText(MyCircle.this, "Sharing Location was Disabled", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    public void onCardClick(int pos) {
        Intent intent = new Intent(MyCircle.this, MapsActivity.class);
        intent.putExtra("userId", usersList.get(pos).getUserId());
//        intent.putExtra("radius", "500");
        startActivity(intent);
    }

    // making alert dialog for the user to enter his address and send his order to firebase
    private void showAlertDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MyCircle.this);

        View v = getLayoutInflater().inflate(R.layout.contacts_layout, null);
        final EditText name = v.findViewById(R.id.contact_name);
        final EditText phone = v.findViewById(R.id.contact_number);
        Button save = v.findViewById(R.id.contact_button);
        dialog.setView(v);
        final AlertDialog d = dialog.create();
        d.show();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!name.getText().toString().isEmpty() && !phone.getText().toString().isEmpty()) {
                    DbHelper.getInstance(getApplication()).AddContact(name.getText().toString(), phone.getText().toString());
                    Toast.makeText(MyCircle.this, "Contact Was added", Toast.LENGTH_SHORT).show();
                    d.dismiss();
                } else {
                    Toast.makeText(MyCircle.this, "Please fill the missing fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
