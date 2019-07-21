package com.example.gpstracker.Map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;

import com.example.gpstracker.Circle.JoinCircle;
import com.example.gpstracker.Model.LoggedUser;
import com.example.gpstracker.Sqlite.DbHelper;
import com.example.gpstracker.Model.User;
import com.example.gpstracker.R;
import com.example.gpstracker.Model.SosPhoneContact;
import com.example.gpstracker.UserSetUp.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.telephony.SmsManager;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Uri Robinov on 20/7/2019.
 */

public class MyNavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, ValueEventListener {

    private static final int REQUEST_CODE_PERMISSION = 2;
    public User curUser;
    private FirebaseAuth auth;
    private GoogleMap mMap;
    private DatabaseReference databaseReference, friendRef;
    private FirebaseUser user;
    private TextView userName, userEmail;
    private CircleImageView imageViewUser;
    private ImageView sos;
    private String battery;
    static MyNavigationDrawer instance;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker userMarker;

    public static MyNavigationDrawer getInstance() {
        return instance;
    }

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            battery = String.valueOf(level);
            databaseReference.child("battery").setValue(battery);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_navigation_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        setSupportActionBar(toolbar);

        instance = this;
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        View header = navigationView.getHeaderView(0);
        userName = header.findViewById(R.id.user_name_text);
        userEmail = header.findViewById(R.id.email_text);
        imageViewUser = header.findViewById(R.id.imageViewUser);
        sos = findViewById(R.id.sos_bt);

        sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfSms();

            }
        });

        //receive the current battery of the user
        this.registerReceiver(this.batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        navigationView.setNavigationItemSelectedListener(this);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("User").child(user.getUid());
        friendRef = FirebaseDatabase.getInstance().getReference().child("User");

        registerEventRealTime();
    }

    private void checkIfSms() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        sendSmsWithYourLocation();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to send" +
                " sos message to your contacts?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void sendSmsWithYourLocation() {
        List<SosPhoneContact> phoneContactList;
        phoneContactList = DbHelper.getInstance(this).GetList();
        String messege = "Im in danger My location is: " + "http://maps.google.com/?q=" + curUser.getLat() + "," + curUser.getLng();
        if (checkPermissionSms(Manifest.permission.SEND_SMS)) {
            if (phoneContactList.size() > 0) {
                for (SosPhoneContact sosPhoneContact : phoneContactList) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(sosPhoneContact.getPhone(), null, messege, null, null);
                    Toast.makeText(this, "Message sent!!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No contact's in the sos list", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private boolean checkPermissionSms(String sendSms) {
        int check = ContextCompat.checkSelfPermission(this, sendSms);
        return check == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_signOut) {
            auth.signOut();
            this.unregisterReceiver(this.batteryReceiver);
            Intent intent = new Intent(MyNavigationDrawer.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_joinCircle) {

            Intent join = new Intent(MyNavigationDrawer.this, JoinCircle.class);
            startActivity(join);
//            finish();

        } else if (id == R.id.nav_share_Location) {
            Intent sendLocation = new Intent(Intent.ACTION_SEND);
            sendLocation.putExtra(Intent.EXTRA_TEXT, "My location is: " + "http://maps.google.com/?q=" + curUser.getLat() + "," + curUser.getLng());
            sendLocation.setType("text/plain");
            Intent chooser = Intent.createChooser(sendLocation, "Share using: ");
            if (chooser != null) {
                startActivity(chooser);
            }


        } else if (id == R.id.nav_myCircle) {
            Intent MyCircle = new Intent(MyNavigationDrawer.this, com.example.gpstracker.Circle.MyCircle.class);
            startActivity(MyCircle);
//            finish();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_PERMISSION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_PERMISSION);
            }
            return false;
        } else {
            return true;
        }
    }
    //making user's marker based on user location and adding to the map

    private void getMarker(User user) throws IOException {
        String res = null;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(user.getLat(), user.getLng(), 1);

        if (user != null) {
            if (userMarker != null) {
                userMarker.remove();
            }
            try {
                if (addresses.size() > 0) {

                    Address address = addresses.get(0);
                    StringBuilder street = new StringBuilder();
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        street.append(address.getAddressLine(i)).append("\n");
                    }
                    street.append(address.getLocality()).append("\n");
                    res = street.toString();
                }
                LatLng latLng = new LatLng(user.getLat(), user.getLng());
                Bitmap userBit = new GetPhoto().execute(user.getImage()).get();
                Bitmap resized = Bitmap.createScaledBitmap(userBit, 200, 200, true);
                userMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(user.getName() + "'s" + " " + "location " + res)
                        .icon(BitmapDescriptorFactory.fromBitmap(resized)));

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);


    }

    @SuppressLint("MissingPermission")
    private void updateLocation() {
        buildLocationRequset();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPend());
    }

    private PendingIntent getPend(){
       Intent i = new Intent(this,Tracker.class);
       i.setAction(Tracker.ACTION_PROCES_UPDATE);
       return PendingIntent.getBroadcast(this,0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void buildLocationRequset() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);

    }

    private void registerEventRealTime(){
        databaseReference.addValueEventListener(this);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        curUser = dataSnapshot.getValue(User.class);
        LoggedUser.sCurUser= curUser;
        userName.setText(curUser.getName());
        userEmail.setText(curUser.getEmail());
        Picasso.get()
                .load(curUser.getImage())
                .resize(100, 100)
                .centerCrop()
                .into(imageViewUser);
        try {
            getMarker(curUser);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateLocation();
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(userMarker.getPosition(), 17);
        mMap.animateCamera(yourLocation);

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    @Override
    protected void onStop() {
        databaseReference.removeEventListener(this);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseReference.addValueEventListener(this);
    }
}



















//keep getting the location of the user
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (broadcastReceiver == null) {
//            broadcastReceiver = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//
//                    Double lat = (Double) intent.getExtras().get("lat");
//                    Double lng = (Double) intent.getExtras().get("lng");
//                    databaseReference.child("lat").setValue(lat).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                        }
//                    });
//                    databaseReference.child("lng").setValue(lng).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                        }
//                    });
//                }
//            };
//            registerReceiver(broadcastReceiver, new IntentFilter("Location_update"));
//        }
//    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (broadcastReceiver != null) {
//            unregisterReceiver(broadcastReceiver);
//        }
//    }