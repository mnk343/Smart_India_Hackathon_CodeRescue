package com.example.coderescue.navar;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.coderescue.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.
import com.example.coderescue.navar.ar.ArFragmentSupport;
import com.example.coderescue.navar.network.DirectionsResponse;
import com.example.coderescue.navar.network.RetrofitInterface;
import com.example.coderescue.navar.network.model.Step;

import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ArCamActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener {

    TextView srcDestText;
    TextView dirDistance;
    TextView dirTime;
    CardView cardVisibility;
    ImageButton visibility_close_btn;

    private final static String TAG="ArCamActivity";
    private String srcLatLng;
    private String destLatLng;
    private Step steps[];

    private LocationManager locationManager;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private ArFragmentSupport arFragmentSupport;
    // World from BeyondAR - AR library removed
    // private World world;

    private Intent intent;

    private String visibility = "LOW";
    private ArrayList<Pair<String, Integer>> visi_options = new ArrayList<>(
            Arrays.asList(
                    new Pair<>("LOW", 3),
                    new Pair<>("MEDIUM", 5),
                    new Pair<>("HIGH", 8)
            ));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_camera);
        srcDestText = findViewById(R.id.ar_source_dest);
        dirDistance = findViewById(R.id.ar_dir_distance);
        dirTime = findViewById(R.id.ar_dir_time);
        cardVisibility = findViewById(R.id.ar_card_visibility);
        visibility_close_btn = findViewById(R.id.visibility_close_btn);

        cardVisibility.setVisibility(View.GONE);

        TextView change_visibility = (TextView) findViewById(R.id.ar_visibility);
        change_visibility.setOnClickListener(new TextView.OnClickListener() {
            public void onClick(View v) {
                cardVisibility.setVisibility(View.VISIBLE);
            }
        });

        visibility_close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardVisibility.setVisibility(View.GONE);
            }
        });

        RadioGroup radioVisibility = (RadioGroup)findViewById(R.id.radio_visibility);
        radioVisibility.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                RadioButton checkedRadioButton = (RadioButton)group.findViewById(checkedId);
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked)
                {
                    Log.d("visibility", (String) checkedRadioButton.getText());
                    visibility = (String) checkedRadioButton.getText();
                    cardVisibility.setVisibility(View.GONE);
                    Configure_AR();
                }
            }
        });

        Set_googleApiClient(); //Sets the GoogleApiClient

        //Configure_AR(); //Configure AR Environment

//        Directions_call();
    }

    private void Set_googleApiClient(){
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public static Drawable setTint(Drawable d, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(d);
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }

    private void Configure_AR(){
        // AR rendering disabled - BeyondAR library removed from build
        Log.d(TAG, "Configure_AR: AR rendering not available (library removed)");
        Toast.makeText(this, "AR view not available", Toast.LENGTH_SHORT).show();
    }

    private void Get_intent(){
        if(getIntent()!=null) {
            intent = getIntent();

            srcDestText.setText(intent.getStringExtra("SRC")+" -> "+intent.getStringExtra("DEST"));
            srcLatLng=intent.getStringExtra("SRCLATLNG");
            destLatLng=intent.getStringExtra("DESTLATLNG");

            System.out.println(srcLatLng + " ---> " + destLatLng);
            Directions_call(); //HTTP Google Directions API Call
        }
    }

    private void Directions_call(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getString(R.string.directions_base_url))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitInterface apiService =
                retrofit.create(RetrofitInterface.class);

        final Call<DirectionsResponse> call = apiService.getDirections(srcLatLng, destLatLng,
                getResources().getString(R.string.google_maps_key));

        Log.d(TAG, "Directions_call: srclat lng:"+srcLatLng+"\n"+"destLatlng:"+destLatLng);

        call.enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                DirectionsResponse directionsResponse = response.body();
                int step_array_size=directionsResponse.getRoutes().get(0).getLegs().get(0).getSteps().size();

                dirDistance.setVisibility(View.VISIBLE);
                dirDistance.setText(directionsResponse.getRoutes().get(0).getLegs().get(0)
                        .getDistance().getText());

                dirTime.setVisibility(View.VISIBLE);
                dirTime.setText(directionsResponse.getRoutes().get(0).getLegs().get(0)
                        .getDuration().getText());

                steps=new Step[step_array_size];

                for(int i=0;i<step_array_size;i++) {
                    steps[i] = directionsResponse.getRoutes().get(0).getLegs().get(0).getSteps().get(i);
                    Log.d(TAG, "onResponse: STEP "+i+": "+steps[i].getEndLocation().getLat()
                    +" "+steps[i].getEndLocation().getLng());
                }
                Configure_AR();

            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                Log.d(TAG, "onFailure: FAIL" + t.getMessage());
                new AlertDialog.Builder(getApplicationContext()).setMessage("Fetch Failed").show();
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

    @Override
    protected void onPause() {
        super.onPause();
        // BeyondarLocationManager.disable() - AR library removed
    }

    @Override
    protected void onResume() {
        super.onResume();
        // BeyondarLocationManager.enable() - AR library removed
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

        }
        else {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            String locationProvider = LocationManager.NETWORK_PROVIDER;

           // mLastLocation = locationManager.getLastKnownLocation(locationProvider);

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                try {
                    Get_intent(); //Fetch Intent Values
                }catch (Exception e){
                    Log.d(TAG, "onCreate: Intent Error");
                }
            }
        }

        startLocationUpdates();
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, createLocationRequest(), this);

        }catch (SecurityException e){
            Toast.makeText(this, "Location Permission not granted . Please Grant the permissions",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        // AR world update disabled - BeyondAR library removed
    }
}