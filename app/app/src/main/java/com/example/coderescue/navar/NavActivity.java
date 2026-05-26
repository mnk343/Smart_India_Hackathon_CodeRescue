package com.example.coderescue.navar;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.example.coderescue.navar.utils.UtilsCheck;

import com.example.coderescue.R;
import com.google.android.material.snackbar.Snackbar;

/**
 * Created by Amal Krishnan on 26-02-2017.
 */

public class NavActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    final static private String TAG="NavActivity";
    private final int SOURCE_PLACE_PICKER_REQUEST = 12;
    private final int DEST_PLACE_PICKER_REQUEST = 22;

    private Intent intent;

    private GoogleApiClient mGoogleApiClient;

    private LatLng srcLatLong;
    private LatLng destLatLong;

    Button sourcePickBtn;
    Button destPickBtn;
    Button navStartBtn;
    TextView sourceResultText;
    TextView destResultText;
    Button mapNavStartBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, this)
                .build();

        setContentView(R.layout.activity_nav);
        sourcePickBtn = findViewById(R.id.source_pick_btn);
        destPickBtn = findViewById(R.id.dest_pick_btn);
        navStartBtn = findViewById(R.id.nav_start_btn);
        sourceResultText = findViewById(R.id.source_result_text);
        destResultText = findViewById(R.id.dest_result_text);
        mapNavStartBtn = findViewById(R.id.non_ar_nav_start_btn);

        if(!UtilsCheck.isNetworkConnected(this)){
            Snackbar mySnackbar = Snackbar.make(findViewById(R.id.nav_coord_layout),
                    "Turn Internet On", Snackbar.LENGTH_SHORT);
            mySnackbar.show();
        }


        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Snackbar mySnackbar = Snackbar.make(findViewById(R.id.nav_coord_layout),
                    "Turn GPS ON", Snackbar.LENGTH_LONG);
            mySnackbar.show();
        }

        intent=new Intent();

        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sourcePickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(NavActivity.this, "Place Picker not available", Toast.LENGTH_SHORT).show();
            }
        });

        destPickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(NavActivity.this, "Place Picker not available", Toast.LENGTH_SHORT).show();
            }
        });

        navStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(NavActivity.this,ArCamActivity.class);

                try {
                    intent.putExtra("SRC", sourceResultText.getText());
                    intent.putExtra("DEST", destResultText.getText());
                    intent.putExtra("SRCLATLNG", srcLatLong.latitude + "," + srcLatLong.longitude);
                    intent.putExtra("DESTLATLNG", destLatLong.latitude + "," + destLatLong.longitude);
                    startActivity(intent);
                }catch (NullPointerException npe){
                    Snackbar mySnackbar = Snackbar.make(findViewById(R.id.nav_coord_layout),
                            "Source/Destination Fields are Invalid", Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                    Log.d(TAG, "onClick: The IntentExtras are Empty");
                }
            }
        });

        mapNavStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                try{
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("http")
                            .authority("maps.google.com")
                            .appendPath("maps")
                            .appendQueryParameter("saddr", srcLatLong.latitude + "," + srcLatLong.longitude)
                            .appendQueryParameter("daddr", destLatLong.latitude + "," + destLatLong.longitude);

                    intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse( builder.build().toString()));
                    startActivity(intent);
                }catch (Exception e){
                    Log.d(TAG, "onClick: mapNav Exception caught");
                    Snackbar mySnackbar = Snackbar.make(findViewById(R.id.nav_coord_layout),
                            "Source/Destination Fields are Invalid", Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                }
            }
        });

    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // PlacePicker removed — onActivityResult is now a no-op for place picking
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected:  GoogleApiClient");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectedFailed:  GoogleApiClient");
    }
}
