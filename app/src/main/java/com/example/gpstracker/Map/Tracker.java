package com.example.gpstracker.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.example.gpstracker.Model.LoggedUser;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import io.paperdb.Paper;
/**
 * Created by Uri Robinov on 20/7/2019.
 */
public class Tracker extends BroadcastReceiver {

    private FirebaseUser user;
    private FirebaseAuth auth;
    public static final String ACTION_PROCES_UPDATE = "com.example.gpstracker.Map.UPDATE_LOCATION";
    private DatabaseReference databaseReference;
    private String Uid;

    @Override
    public void onReceive(Context context, Intent intent) {

        Paper.init(context);
        Uid = Paper.book().read(LoggedUser.USER_UID_KRY);
        if (intent!=null){
                final String action = intent.getAction();
                if (ACTION_PROCES_UPDATE.equals(action)){
                    LocationResult result = LocationResult.extractResult(intent);
                    if (result!=null){
                        auth = FirebaseAuth.getInstance();
                        user = auth.getCurrentUser();
                        final Location location = result.getLastLocation();
                        databaseReference = FirebaseDatabase.getInstance().getReference().child("User");
                        if (LoggedUser.sCurUser != null){
                            databaseReference.child(user.getUid()).child("lat").setValue(location.getLatitude());
                            databaseReference.child(user.getUid()).child("lng").setValue(location.getLongitude());
                        }
                        else
                        {
                             databaseReference.child(Uid).child("lat").setValue(location.getLatitude());
                            databaseReference.child(Uid).child("lng").setValue(location.getLongitude());
                        }


                    }
                }
        }
    }
}
