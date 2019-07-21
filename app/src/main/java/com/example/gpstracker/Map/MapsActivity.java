package com.example.gpstracker.Map;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import com.example.gpstracker.Model.CurrentFriend;
import com.example.gpstracker.Model.User;
import com.example.gpstracker.R;
import com.example.gpstracker.UserSetUp.MainActivity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
/**
 * Created by Uri Robinov on 20/7/2019.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference ref, userRef,friendRef;
    private GeoFire geoFire;
    private User curUser;
    private Marker marker;
    private SearchView searchView;
    private Address safeLocation;
    private CurrentFriend friend;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        searchView = findViewById(R.id.search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String location = searchView.getQuery().toString();
                List<Address> addresses = null;
                if (location!=null || !location.equals("")){
                    Geocoder geocoder = new Geocoder(MapsActivity.this);

                    try {
                        addresses = geocoder.getFromLocationName(location,1);
                        safeLocation = addresses.get(0);
                        friend = new CurrentFriend(userId,safeLocation.getLatitude(),safeLocation.getLongitude());
                        friendRef.child(userId).setValue(friend).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mMap.clear();
                                displayLocation();
                                drawCircleAndCheckIfExit();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        ref = FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire = new GeoFire(ref);

        friendRef = FirebaseDatabase.getInstance().getReference("Tracker");
        userRef = FirebaseDatabase.getInstance().getReference().child("User");

        if (getIntent().hasExtra("userId")) {
           userId = getIntent().getStringExtra("userId");
            userRef.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    curUser = dataSnapshot.getValue(User.class);
                    displayLocation();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        final double latitude = curUser.getLat();
        final double longitude = curUser.getLng();

        geoFire.setLocation(curUser.getUserId(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (marker != null)
                    marker.remove();
                Bitmap userBit = null;
                try {
                    userBit = new GetPhoto().execute(curUser.getImage()).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LatLng coordinate = new LatLng(latitude, longitude);
                Bitmap resized = Bitmap.createScaledBitmap(userBit,200,200,true);
                marker = mMap.addMarker(new MarkerOptions()
                        .position(coordinate)
                        .title(curUser.getName() + "'s" + " " + "location " )
                        .icon(BitmapDescriptorFactory.fromBitmap(resized)));

                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 17);
                mMap.animateCamera(yourLocation);
                drawCircleAndCheckIfExit();

            }
        });

    }

        @Override
        public void onMapReady (GoogleMap googleMap){
            mMap = googleMap;
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
        }

        private void drawCircleAndCheckIfExit(){

            friendRef.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                     CurrentFriend currentFriend = dataSnapshot.getValue(CurrentFriend.class);

                    if (currentFriend!=null){
                        LatLng safe_area = new LatLng(currentFriend.getLat(), currentFriend.getLng());
                        mMap.addCircle(new CircleOptions()
                                .center(safe_area)
                                .radius(500)
                                .strokeColor(Color.BLUE)
                                .fillColor(0x220000FF)
                                .strokeWidth(5.0f));

                        //add GeoQuery here
                        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(safe_area.latitude, safe_area.longitude), 0.5f);
                        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                            @Override
                            public void onKeyEntered(String key, GeoLocation location) {
                                sendNotification("Tracker", String.format("%s entered the safe area", curUser.getName()));
                            }

                            @Override
                            public void onKeyExited(String key) {
                                sendNotification("Tracker", String.format("%s exit the safe area", curUser.getName()));
                            }

                            @Override
                            public void onKeyMoved(String key, GeoLocation location) {
                                Log.d("MOVE", String.format("%s move within the dangerous area [%f/%f]", key, location.latitude, location.longitude));
                            }

                            @Override
                            public void onGeoQueryReady() {

                            }

                            @Override
                            public void onGeoQueryError(DatabaseError error) {
                                Log.d("ERROR", "" + error);
                            }
                        });
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        private void sendNotification (String title, String content){

            int NOTIFICATION_ID = 234;

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                String CHANNEL_ID = "my_channel_01";
                CharSequence name = "my_channel";
                String Description = "This is my channel";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                mChannel.setDescription(Description);
                mChannel.enableLights(true);
                mChannel.setLightColor(Color.RED);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                mChannel.setShowBadge(false);
                notificationManager.createNotificationChannel(mChannel);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(content);

                Intent resultIntent = new Intent(this, MapsActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplication());
                stackBuilder.addParentStack(MapsActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentIntent(resultPendingIntent);

                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }

    }
}
