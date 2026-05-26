package com.example.coderescue.navar;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.coderescue.Activities.BeyondARWorld;
import com.example.coderescue.Activities.CameraWithGoogleMapsActivity;
import com.example.coderescue.Fragments.HomeFragment;
import com.example.coderescue.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.SphericalUtil;
import com.example.coderescue.navar.network.PlaceResponse;
import com.example.coderescue.navar.network.PoiResponse;
import com.example.coderescue.navar.network.RetrofitInterface;
import com.example.coderescue.navar.network.poi.Result;
import com.example.coderescue.navar.utils.UtilsCheck;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;


import org.bson.Document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static com.mongodb.client.model.Filters.eq;

public class PoiBrowserActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks
    ,GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG="PoiBrowserActivity";

    private TextView textView;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LayoutInflater layoutInflater;
    public static RemoteMongoClient mongoClient;

    CardView poi_cardview;
    ImageButton poi_cardview_close_btn;
    TextView poi_place_name;
    TextView poi_place_addr;
    ImageView poi_place_image;
    Button poi_place_ar_btn;
    Button poi_place_maps_btn;
    ProgressBar poi_browser_progress;
//    @BindView(R.id.seekBar)
//    SeekBar seekbar;
    CardView seekbar_cardview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_browser);
        poi_cardview = findViewById(R.id.poi_place_detail);
        poi_cardview_close_btn = findViewById(R.id.poi_place_close_btn);
        poi_place_name = findViewById(R.id.poi_place_name);
        poi_place_addr = findViewById(R.id.poi_place_address);
        poi_place_image = findViewById(R.id.poi_place_image);
        poi_place_ar_btn = findViewById(R.id.poi_place_ar_direction);
        poi_place_maps_btn = findViewById(R.id.poi_place_maps_direction);
        poi_browser_progress = findViewById(R.id.poi_brwoser_progress);
        seekbar_cardview = findViewById(R.id.seekbar_cardview);

        seekbar_cardview.setVisibility(View.GONE);
        poi_browser_progress.setVisibility(View.GONE);
        poi_cardview.setVisibility(View.GONE);

        if(!UtilsCheck.isNetworkConnected(this)){
            Snackbar mySnackbar = Snackbar.make(findViewById(R.id.poi_layout),
                    "Turn Internet On", Snackbar.LENGTH_SHORT);
            mySnackbar.show();
        }

        // AR fragment setup disabled - BeyondAR library removed
        // arFragmentSupport = (ArFragmentSupport) getSupportFragmentManager().findFragmentById(R.id.poi_cam_fragment);
        // arFragmentSupport.setOnClickBeyondarObjectListener(this);
        // arFragmentSupport.setOnTouchBeyondarViewListener(this);


        textView=(TextView) findViewById(R.id.loading_text);

        Set_googleApiClient(); //Sets the GoogleApiClient

        poi_cardview_close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekbar_cardview.setVisibility(View.VISIBLE);
                poi_cardview.setVisibility(View.GONE);
                poi_place_image.setImageResource(android.R.color.transparent);
                poi_place_name.setText(" ");
                poi_place_addr.setText(" ");
            }
        });

//        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//
//                if(i==0){
//                    Poi_list_call(300);
//                }else{
//                    Poi_list_call((i+1)*300);
//                }
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                if(seekBar.getProgress()==0){
//                    Toast.makeText(PoiBrowserActivity.this, "Radius: 300 Metres", Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(PoiBrowserActivity.this, "Radius: "+(seekBar.getProgress()+1)*300+" Metres", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

    }

    void Poi_list_call(int radius){
        poi_browser_progress.setVisibility(View.VISIBLE);

        Task task =  BeyondARWorld.getVictims();
        task.addOnCompleteListener( (new OnCompleteListener<Object>() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Log.d("Correct", "yayy yayy yayya yayy");

                    poi_browser_progress.setVisibility(View.GONE);
                    seekbar_cardview.setVisibility(View.VISIBLE);

                    // AR rendering disabled - BeyondAR GeoObject no longer available
                    // List<GeoObject> poiResult=BeyondARWorld.listVictims;
                    // Configure_AR(poiResult);
                    Log.d(TAG, "Poi_list_call: AR display not available (library removed)");
                }
            }
        }));


//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(getResources().getString(R.string.directions_base_url))
//                .client(client)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        RetrofitInterface apiService =
//                retrofit.create(RetrofitInterface.class);
//
//        final Call<PoiResponse> call = apiService.listPOI(String.valueOf(mLastLocation.getLatitude())+","+
//                String.valueOf(mLastLocation.getLongitude()),radius,
//                getResources().getString(R.string.google_maps_key));
//
//        call.enqueue(new Callback<PoiResponse>() {
//            @Override
//            public void onResponse(Call<PoiResponse> call, Response<PoiResponse> response) {
//
//                poi_browser_progress.setVisibility(View.GONE);
//                seekbar_cardview.setVisibility(View.VISIBLE);
//
//                List<Result> poiResult=response.body().getResults();
//
//                Configure_AR(poiResult);
//            }
//
//            @Override
//            public void onFailure(Call<PoiResponse> call, Throwable t) {
//                poi_browser_progress.setVisibility(View.GONE);
//            }
//        });

    }

    void Poi_details_call(String placeid){

        poi_browser_progress.setVisibility(View.VISIBLE);

//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(getResources().getString(R.string.directions_base_url))
//                .client(client)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        RetrofitInterface apiService =
//                retrofit.create(RetrofitInterface.class);
//
//        final Call<PlaceResponse> call = apiService.getPlaceDetail(placeid,
//                getResources().getString(R.string.google_maps_key));
//
//        call.enqueue(new Callback<PlaceResponse>() {
//            @Override
//            public void onResponse(Call<PlaceResponse> call, Response<PlaceResponse> response) {
                String[] arrOfStr = placeid.split("_");
                String victimLatitude = arrOfStr[0];
                String victimLongitude = arrOfStr[1];
//            String victimLatitude = arrOfStr[0];

                seekbar_cardview.setVisibility(View.GONE);
                poi_cardview.setVisibility(View.VISIBLE);
                poi_browser_progress.setVisibility(View.GONE);

//                final com.example.coderescue.navar.network.place.Result result=response.body().getResult();

                poi_place_name.setText("HELP");
                poi_place_addr.setText(victimLatitude + " " + victimLongitude);
Log.d(TAG, placeid);
//                try {
//                    HttpUrl url = new HttpUrl.Builder()
//                            .scheme("https")
//                            .host("maps.googleapis.com")
//                            .addPathSegments("maps/api/place/photo")
//                            .addQueryParameter("maxwidth", "400")
//                            .addQueryParameter("photoreference", result.getPhotos().get(0).getPhotoReference())
//                            .addQueryParameter("key", getResources().getString(R.string.google_maps_key))
//                            .build();
//
//                    new PoiPhotoAsync().execute(url.toString());
//
//                }catch (Exception e){
//                    Log.d(TAG, "onResponse: "+e.getMessage());
//                    Toast.makeText(PoiBrowserActivity.this, "No image available", Toast.LENGTH_SHORT).show();
//                }


            poi_place_maps_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent;
                        try{
                            Uri.Builder builder = new Uri.Builder();
                            builder.scheme("http")
                                    .authority("maps.google.com")
                                    .appendPath("maps")
                                    .appendQueryParameter("saddr", mLastLocation.getLatitude()+","+mLastLocation.getLongitude())
                                    .appendQueryParameter("daddr", Double.parseDouble( victimLatitude)+","+
                                            Double.parseDouble(victimLongitude));

                            intent = new Intent(android.content.Intent.ACTION_VIEW,
                                    Uri.parse( builder.build().toString()));

                            mongoClient = HomeFragment.client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");

                            final RemoteMongoCollection<Document> teams = mongoClient.getDatabase("main").getCollection("victimsneedhelp");
                            System.out.println(HomeFragment.diss_idd + " global diss_id");
                            RemoteFindIterable findResults = teams.find(eq("disaster_id", HomeFragment.diss_idd));
                            Task <List<Document>> itemsTask = findResults.into(new ArrayList<Document>());
                            itemsTask.addOnCompleteListener(new OnCompleteListener <List<Document>> () {
                                @Override
                                public void onComplete(@NonNull Task<List<Document>> task) {
                                    if (task.isSuccessful()) {
                                        List<Document> items = task.getResult();
                                        int numDocs = items.size();
                                        if(numDocs==0){
                                            Log.d("Doesn't exist", "Should not happen");
                                        }
                                        else{
                                            System.out.println(items.get(0));
                                            Document first = items.get(0);
                                            final RemoteMongoCollection<Document> victimneedhelp = mongoClient.getDatabase("main").getCollection("victimsneedhelp");
                                            List<Document> temp = (List<Document>)first.get("victims");
                                            List<Document> temp2 = new ArrayList<Document>();
                                            int count=0;
                                            System.out.println(victimLatitude);
                                            System.out.println(victimLongitude);
                                            for(Document doc: temp){
                                                System.out.println(count + " " + doc.getString("latitude") + " " + doc.getString("longitude"));
                                                if(count==1 || !doc.getString("latitude").equals(victimLatitude) || !doc.getString("longitude").equals(victimLongitude))
                                                {
                                                    temp2.add(doc);
                                                }
                                                else
                                                {

                                                    count=1;
                                                    Document notactive = new Document()
                                                            .append("latitude", victimLatitude)
                                                            .append("longitude", victimLongitude)
                                                            .append("count",doc.getInteger("count"))
                                                            .append("isactive", 0);
                                                    temp2.add(notactive);
                                                }
                                            }
                                            Log.d("Exists", "update");
                                            Document filterDoc = new Document().append("disaster_id", HomeFragment.diss_idd);
                                            Document updateDoc = new Document().append("$set",
                                                    new Document()
                                                            .append("disaster_id", HomeFragment.diss_idd)
                                                            .append("victims", temp2)
                                            );

                                            final Task<RemoteUpdateResult> updateTask =
                                                    victimneedhelp.updateOne(filterDoc, updateDoc);
                                            updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
                                                @Override
                                                public void onComplete(@NonNull Task <RemoteUpdateResult> task) {
                                                    if (task.isSuccessful()) {
                                                        long numMatched = task.getResult().getMatchedCount();
                                                        long numModified = task.getResult().getModifiedCount();
                                                        Log.d("app", String.format("successfully matched %d and modified %d documents",
                                                                numMatched, numModified));
                                                    } else {
                                                        Log.e("app", "failed to update document with: ", task.getException());
                                                    }
                                                }
                                            });
                                        }
                                        Context context = PoiBrowserActivity.this
                                                .getApplicationContext();
                                        CharSequence text = "Opening Maps to guid to the victim!";
                                        int duration = Toast.LENGTH_LONG;

                                        Toast toast = Toast.makeText(context, text, duration);
                                        toast.show();
                                    } else {
                                        Log.e("app", "Failed to count documents with exception: ", task.getException());
                                    }
                                }
                            });
                            startActivity(intent);
                            finish();
                        }catch (Exception e){
                            Log.d(TAG, "onClick: mapNav Exception caught");
                            Toast.makeText(PoiBrowserActivity.this, "Unable to Open Maps Navigation", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                poi_place_ar_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(PoiBrowserActivity.this,ArCamActivity.class);

                        try {
                            intent.putExtra("SRC", "Current Location");
                            intent.putExtra("DEST",  victimLatitude+","+
                                    victimLongitude);
                            intent.putExtra("SRCLATLNG",  mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
                            intent.putExtra("DESTLATLNG", victimLatitude+","+
                                    victimLongitude);
                            startActivity(intent);
                            finish();
                        }catch (NullPointerException npe){
                            Log.d(TAG, "onClick: The IntentExtras are Empty");
                        }
                    }
                });

            }

//            @Override
//            public void onFailure(Call<PlaceResponse> call, Throwable t) {
//                poi_browser_progress.setVisibility(View.GONE);
//            }
//        });
//
//    }

    public class PoiPhotoAsync extends AsyncTask<String,Void,Bitmap> {

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            poi_place_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            poi_place_image.setImageBitmap(bitmap);
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];

            Bitmap bitmap = null;
            try {
                InputStream input = new java.net.URL(imageURL).openStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }

    private void Configure_AR(List<?> pois){
        // AR rendering disabled - BeyondAR library removed from build
        Log.d(TAG, "Configure_AR: AR rendering not available (library removed)");
        textView.setVisibility(View.INVISIBLE);
    }

    private String saveToInternalStorage(Bitmap bitmapImage,String name){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,name);

        Log.d(TAG, "saveToInternalStorage: PATH:"+mypath.toString());

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath.toString();
    }

    public String getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path).toString();


//        ContextWrapper cw = new ContextWrapper(getApplicationContext());
//        // path to /data/data/yourapp/app_data/imageDir
//        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
//        // Create imageDir
//        File mypath=new File(directory,"profile.jpg");
//
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(mypath);
//            // Use the compress method on the BitMap object to write image to the OutputStream
//            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                fos.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return directory.getAbsolutePath();
    }

    public Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    private void Set_googleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

        }
        else {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                try {
                    Poi_list_call(900);
                }catch (Exception e){
                    Log.d(TAG, "onCreate: Intent Error");
                }
            }
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
