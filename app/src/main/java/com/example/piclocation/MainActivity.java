package com.example.piclocation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{

    Uri image_uri;
    private static  final int All_PER_REQUEST_CODE=100;
    private static  final int CAMEA_REQUEST_CODE=200;
    private static  final int STORAGE_REQUEST_CODE=400;
    private static  final int IMAGE_PICK_GALLERY_CODE=1000;
    private static  final int IMAGE_PICK_CAMERA_CODE=1001;
    String cameraPermission[];
    String storagePermission[];
    String allPermission[];
    ImageView imageView;
    Bitmap bitmap;
    public  static  final String TAG=MainActivity.class.getName();
    private long l;

    private FusedLocationProviderClient client;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocatqionSettingsRequest;
    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    // fastest updates interval - 5 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private SettingsClient mSettingsClient;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private Context context;
    ImageView sendImv;
    RelativeLayout relLayout;
    private  Uri resultUri;
    TextView locTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=MainActivity.this;
        client= LocationServices.getFusedLocationProviderClient(this);
        imageView=(ImageView)findViewById(R.id.imv);
        relLayout=(RelativeLayout)findViewById(R.id.rel);
        sendImv=(ImageView)findViewById(R.id.sendPicBtn);
        locTv=(TextView)findViewById(R.id.locationTv);
        sendImv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(!locTv.getText().toString().isEmpty() && imageView.getDrawable()!=null)
                {
                    takeSS(relLayout);
                    //Log.v("Uri",""+image_uri);
                }
                else
                {
                    Toast.makeText(context, "something went Wrong", Toast.LENGTH_SHORT).show();
                }

                //Toast.makeText(context, ""+image_uri, Toast.LENGTH_SHORT).show();
                //shareImage(v);
                //onShareOnePhoto();;
            }
        });
        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION};
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION};
        allPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION};
        init();

        final LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
        requestAllPermission();
        //locationNikalo();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id=item.getItemId();
        if(id==R.id.pickFromCamera)
        {
            if(! checkCameraPermission())
            {
                requestCameraPermission();
            }
            else
            {
                if(checkGps())
                {
                    //locationNikalo();
                    //pickCamera();

                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback, Looper.myLooper());
                    client.getLastLocation().addOnSuccessListener(MainActivity.this,new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            if(location!=null)
                            {
                                //Toast.makeText(context,""+location,Toast.LENGTH_SHORT).show();
                                double lat=location.getLatitude();
                                double log=location.getLongitude();
                                //Toast.makeText(context,"Lat:"+lat+"\nLog:"+log,Toast.LENGTH_SHORT).show();



                                String errorMessage = "";
                                ////
                                List<Address> addresses = null;
                                Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                                try {
                                    addresses = geocoder.getFromLocation(

                                            lat,
                                            log,
                                            // In this sample, get just a single address.
                                            1);
                                } catch (IOException ioException) {
                                    // Catch network or other I/O problems.
                                    errorMessage = "Service not available";
                                    Log.e(TAG, errorMessage, ioException);
                                } catch (IllegalArgumentException illegalArgumentException) {
                                    // Catch invalid latitude or longitude values.
                                    errorMessage ="Invalid lat log used";
                                    Log.e(TAG, errorMessage + ". " +
                                            "Latitude = " + location.getLatitude() +
                                            ", Longitude = " +
                                            location.getLongitude(), illegalArgumentException);
                                }

                                // Handle case where no address was found.
                                if (addresses == null || addresses.size()  == 0) {
                                    if (errorMessage.isEmpty()) {
                                        errorMessage = "no address found";
                                        Toast.makeText(context, ""+errorMessage, Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, errorMessage);
                                    }
                                    //deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
                                } else {
                                    Address address = addresses.get(0);
                                    ArrayList<String> addressFragments = new ArrayList<String>();

                                    // Fetch the address lines using getAddressLine,
                                    // join them, and send them to the thread.
                                    for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                                        addressFragments.add(address.getAddressLine(i));
                                    }
                                    Log.i(TAG,"Address Found");
                                    //Toast.makeText(context, ""+addressFragments.toString(), Toast.LENGTH_SHORT).show();
                                    ((TextView)findViewById(R.id.locationTv)).setText(addressFragments.toString());
                                    //deliverResultToReceiver(Constants.SUCCESS_RESULT,
                                    TextUtils.join(System.getProperty("line.separator"),
                                            addressFragments);
                                    pickCamera();
                                }

                            }
                            else
                            {
                                Toast.makeText(context,"Location not fetched...Try Again",Toast.LENGTH_SHORT).show();
                                return;
                            }

                        }

                    });

                    client.getLastLocation().addOnFailureListener(MainActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context,""+e,Toast.LENGTH_SHORT).show();
                        }
                    });


                }
                else
                {
                    //Toast.makeText(this, "Enable GPS", Toast.LENGTH_SHORT).show();
                    locationNikalo();
                    //return;
                }
            }
        }
        if(id==R.id.pickFromGallery)
        {
            if(! checkStoragePermission())
            {
                requestStoragePermission();
            }
            else
            {
                if(checkGps())
                {
                    //locationNikalo();
                    //pickGallery();
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback, Looper.myLooper());
                    client.getLastLocation().addOnSuccessListener(MainActivity.this,new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            if(location!=null)
                            {
                                //Toast.makeText(context,""+location,Toast.LENGTH_SHORT).show();
                                double lat=location.getLatitude();
                                double log=location.getLongitude();
                                //Toast.makeText(context,"Lat:"+lat+"\nLog:"+log,Toast.LENGTH_SHORT).show();



                                String errorMessage = "";
                                ////
                                List<Address> addresses = null;
                                Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                                try {
                                    addresses = geocoder.getFromLocation(

                                            lat,
                                            log,
                                            // In this sample, get just a single address.
                                            1);
                                } catch (IOException ioException) {
                                    // Catch network or other I/O problems.
                                    errorMessage = "Service not available";
                                    Log.e(TAG, errorMessage, ioException);
                                } catch (IllegalArgumentException illegalArgumentException) {
                                    // Catch invalid latitude or longitude values.
                                    errorMessage ="Invalid lat log used";
                                    Log.e(TAG, errorMessage + ". " +
                                            "Latitude = " + location.getLatitude() +
                                            ", Longitude = " +
                                            location.getLongitude(), illegalArgumentException);
                                }

                                // Handle case where no address was found.
                                if (addresses == null || addresses.size()  == 0) {
                                    if (errorMessage.isEmpty()) {
                                        errorMessage = "no address found";
                                        Toast.makeText(context, ""+errorMessage, Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, errorMessage);
                                    }
                                    //deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
                                } else {
                                    Address address = addresses.get(0);
                                    ArrayList<String> addressFragments = new ArrayList<String>();

                                    // Fetch the address lines using getAddressLine,
                                    // join them, and send them to the thread.
                                    for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                                        addressFragments.add(address.getAddressLine(i));
                                    }
                                    Log.i(TAG,"Address Found");
                                    //Toast.makeText(context, ""+addressFragments.toString(), Toast.LENGTH_SHORT).show();
                                    ((TextView)findViewById(R.id.locationTv)).setText(addressFragments.toString());
                                    //deliverResultToReceiver(Constants.SUCCESS_RESULT,
                                    TextUtils.join(System.getProperty("line.separator"),
                                            addressFragments);
                                    pickGallery();
                                }

                            }
                            else
                            {
                                Toast.makeText(context,"Location not fetched...Try Again",Toast.LENGTH_SHORT).show();
                                return;
                            }

                        }

                    });

                    client.getLastLocation().addOnFailureListener(MainActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context,""+e,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    //Toast.makeText(this, "Enable GPS", Toast.LENGTH_SHORT).show();
                    locationNikalo();
                    //return;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkCameraPermission() {
        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED;
        boolean result1= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
        boolean result2= ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED;

        return  result && result1 && result2;
    }

    private boolean checkStoragePermission() {

        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
        boolean result1= ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED;
        return  result && result1 ;
    }

    private void requestAllPermission() {
        ActivityCompat.requestPermissions(this,allPermission,All_PER_REQUEST_CODE);
    }
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMEA_REQUEST_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }


    private void pickCamera() {
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"NewPic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Image to Text");
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);

    }

    private void pickGallery() {
        Intent b=new Intent(Intent.ACTION_PICK);
        b.setType("image/*");
        startActivityForResult(b,IMAGE_PICK_GALLERY_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case CAMEA_REQUEST_CODE:
                if(grantResults.length>0)
                {

                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    boolean locationFineAccepted=grantResults[2]==PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && writeStorageAccepted && locationFineAccepted)
                    {
                        if(checkGps())
                        {
                            //locationNikalo();
                            //pickCamera();
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                    mLocationCallback, Looper.myLooper());
                            client.getLastLocation().addOnSuccessListener(MainActivity.this,new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {

                                    if(location!=null)
                                    {
                                        //Toast.makeText(context,""+location,Toast.LENGTH_SHORT).show();
                                        double lat=location.getLatitude();
                                        double log=location.getLongitude();
                                        //Toast.makeText(context,"Lat:"+lat+"\nLog:"+log,Toast.LENGTH_SHORT).show();



                                        String errorMessage = "";
                                        ////
                                        List<Address> addresses = null;
                                        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                                        try {
                                            addresses = geocoder.getFromLocation(

                                                    lat,
                                                    log,
                                                    // In this sample, get just a single address.
                                                    1);
                                        } catch (IOException ioException) {
                                            // Catch network or other I/O problems.
                                            errorMessage = "Service not available";
                                            Log.e(TAG, errorMessage, ioException);
                                        } catch (IllegalArgumentException illegalArgumentException) {
                                            // Catch invalid latitude or longitude values.
                                            errorMessage ="Invalid lat log used";
                                            Log.e(TAG, errorMessage + ". " +
                                                    "Latitude = " + location.getLatitude() +
                                                    ", Longitude = " +
                                                    location.getLongitude(), illegalArgumentException);
                                        }

                                        // Handle case where no address was found.
                                        if (addresses == null || addresses.size()  == 0) {
                                            if (errorMessage.isEmpty()) {
                                                errorMessage = "no address found";
                                                Toast.makeText(context, ""+errorMessage, Toast.LENGTH_SHORT).show();
                                                Log.e(TAG, errorMessage);
                                            }
                                            //deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
                                        } else {
                                            Address address = addresses.get(0);
                                            ArrayList<String> addressFragments = new ArrayList<String>();

                                            // Fetch the address lines using getAddressLine,
                                            // join them, and send them to the thread.
                                            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                                                addressFragments.add(address.getAddressLine(i));
                                            }
                                            Log.i(TAG,"Address Found");
                                            //Toast.makeText(context, ""+addressFragments.toString(), Toast.LENGTH_SHORT).show();
                                            ((TextView)findViewById(R.id.locationTv)).setText(addressFragments.toString());
                                            //deliverResultToReceiver(Constants.SUCCESS_RESULT,
                                            TextUtils.join(System.getProperty("line.separator"),
                                                    addressFragments);
                                            pickCamera();
                                        }

                                    }
                                    else
                                    {
                                        Toast.makeText(context,"Location not fetched...Try Again",Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                }

                            });

                            client.getLastLocation().addOnFailureListener(MainActivity.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                     Toast.makeText(context,""+e,Toast.LENGTH_SHORT).show();
                                }
                            });

                                                    }
                        else
                        {
                            //Toast.makeText(this, "Enable GPS", Toast.LENGTH_SHORT).show();
                            locationNikalo();
                            //return;
                        }

                    }
                    else
                    {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if(grantResults.length>0)
                {

                    boolean writeStorageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean locationFineAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted && locationFineAccepted)
                    {
                        if(checkGps())
                        {
                            //locationNikalo();
                            //pickGallery();
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                    mLocationCallback, Looper.myLooper());
                            client.getLastLocation().addOnSuccessListener(MainActivity.this,new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {

                                    if(location!=null)
                                    {
                                        //Toast.makeText(context,""+location,Toast.LENGTH_SHORT).show();
                                        double lat=location.getLatitude();
                                        double log=location.getLongitude();
                                        //Toast.makeText(context,"Lat:"+lat+"\nLog:"+log,Toast.LENGTH_SHORT).show();



                                        String errorMessage = "";
                                        ////
                                        List<Address> addresses = null;
                                        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                                        try {
                                            addresses = geocoder.getFromLocation(

                                                    lat,
                                                    log,
                                                    // In this sample, get just a single address.
                                                    1);
                                        } catch (IOException ioException) {
                                            // Catch network or other I/O problems.
                                            errorMessage = "Service not available";
                                            Log.e(TAG, errorMessage, ioException);
                                        } catch (IllegalArgumentException illegalArgumentException) {
                                            // Catch invalid latitude or longitude values.
                                            errorMessage ="Invalid lat log used";
                                            Log.e(TAG, errorMessage + ". " +
                                                    "Latitude = " + location.getLatitude() +
                                                    ", Longitude = " +
                                                    location.getLongitude(), illegalArgumentException);
                                        }

                                        // Handle case where no address was found.
                                        if (addresses == null || addresses.size()  == 0) {
                                            if (errorMessage.isEmpty()) {
                                                errorMessage = "no address found";
                                                Toast.makeText(context, ""+errorMessage, Toast.LENGTH_SHORT).show();
                                                Log.e(TAG, errorMessage);
                                            }
                                            //deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
                                        } else {
                                            Address address = addresses.get(0);
                                            ArrayList<String> addressFragments = new ArrayList<String>();

                                            // Fetch the address lines using getAddressLine,
                                            // join them, and send them to the thread.
                                            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                                                addressFragments.add(address.getAddressLine(i));
                                            }
                                            Log.i(TAG,"Address Found");
                                            //Toast.makeText(context, ""+addressFragments.toString(), Toast.LENGTH_SHORT).show();
                                            ((TextView)findViewById(R.id.locationTv)).setText(addressFragments.toString());
                                            //deliverResultToReceiver(Constants.SUCCESS_RESULT,
                                            TextUtils.join(System.getProperty("line.separator"),
                                                    addressFragments);
                                            pickGallery();
                                        }

                                    }
                                    else
                                    {
                                        Toast.makeText(context,"Location not fetched...Try Again",Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                }

                            });

                            client.getLastLocation().addOnFailureListener(MainActivity.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context,""+e,Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                        {
                            //Toast.makeText(this, "Enable GPS", Toast.LENGTH_SHORT).show();
                            locationNikalo();
                            //return;
                        }
                    }
                    else
                    {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            case All_PER_REQUEST_CODE:
                if(grantResults.length>0)
                {
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    boolean locationFineAccepted=grantResults[2]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted && locationFineAccepted)
                    {
                        if(checkGps())
                        {
                            locationNikalo();
                        }
                        else
                        {
                            //Toast.makeText(this, "Enable GPS", Toast.LENGTH_SHORT).show();
                            locationNikalo();
                            //return;
                        }
                    }
                    else
                    {
                        return;
                        //Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }


        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
                //Bitmap photo = (Bitmap) data.getExtras().get("data");
                //imageView.setImageBitmap(photo);

            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this);
                //Bitmap photo = (Bitmap) data.getExtras().get("data");
               //imageView.setImageBitmap(photo);


            }

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {

                    resultUri = result.getUri();
                    imageView.setImageURI(resultUri);
                    deleteFile(resultUri);
                    //BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                    //bitmap = bitmapDrawable.getBitmap();


                }
            }
        }
    }

    public void deleteFile(Uri uri)
    {
        if(uri==null)
        {

        }
        else {

            /*File fdelete = new File(uri.getPath());
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    System.out.println("file Deleted :" + image_uri.getPath());
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    System.out.println("file not Deleted :" + image_uri.getPath());
                    Toast.makeText(this, " Not Deleted", Toast.LENGTH_SHORT).show();
                }
            }*/
            try {
                getApplicationContext().getContentResolver().delete(image_uri, null, null);
                //return true;
            } catch (Throwable e) {
                e.printStackTrace();
                Toast.makeText(this, ""+e.toString(), Toast.LENGTH_SHORT).show();
                //return false;
            }
        }

    }

    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                //mCurrentLocation = locationResult.getLastLocation();
                //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                //updateLocationUI();
            }
        };

        //mRequestingLocationUpdates = false;

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }
    private  void locationNikalo()
    {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        //Toast.makeText(HomeActivity.this, "Already on hai", Toast.LENGTH_LONG).show();

                        // updateLoca tionUI();
                        //locationNikalo(); this method code is written below

                        client.getLastLocation().addOnSuccessListener(MainActivity.this,new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {

                                if(location!=null)
                                {
                                    //Toast.makeText(context,""+location,Toast.LENGTH_SHORT).show();
                                    double lat=location.getLatitude();
                                    double log=location.getLongitude();
                                    //Toast.makeText(context,"Lat:"+lat+"\nLog:"+log,Toast.LENGTH_SHORT).show();



                                    String errorMessage = "";
                                    ////
                                    List<Address> addresses = null;
                                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                                    try {
                                        addresses = geocoder.getFromLocation(

                                                lat,
                                                log,
                                                // In this sample, get just a single address.
                                                1);
                                    } catch (IOException ioException) {
                                        // Catch network or other I/O problems.
                                        errorMessage = "Service not available";
                                        Log.e(TAG, errorMessage, ioException);
                                    } catch (IllegalArgumentException illegalArgumentException) {
                                        // Catch invalid latitude or longitude values.
                                        errorMessage ="Invalid lat log used";
                                        Log.e(TAG, errorMessage + ". " +
                                                "Latitude = " + location.getLatitude() +
                                                ", Longitude = " +
                                                location.getLongitude(), illegalArgumentException);
                                    }

                                    // Handle case where no address was found.
                                    if (addresses == null || addresses.size()  == 0) {
                                        if (errorMessage.isEmpty()) {
                                            errorMessage = "no address found";
                                            Toast.makeText(context, ""+errorMessage, Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, errorMessage);
                                        }
                                        //deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
                                    } else {
                                        Address address = addresses.get(0);
                                        ArrayList<String> addressFragments = new ArrayList<String>();

                                        // Fetch the address lines using getAddressLine,
                                        // join them, and send them to the thread.
                                        for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                                            addressFragments.add(address.getAddressLine(i));
                                        }
                                        Log.i(TAG,"Address Found");
                                        //Toast.makeText(context, ""+addressFragments.toString(), Toast.LENGTH_SHORT).show();
                                        ((TextView)findViewById(R.id.locationTv)).setText(addressFragments.toString());
                                        //deliverResultToReceiver(Constants.SUCCESS_RESULT,
                                        TextUtils.join(System.getProperty("line.separator"),
                                                addressFragments);
                                        //pickCamera();
                                    }

                                }
                                else
                                {
                                    //Toast.makeText(context,"null loc"+location,Toast.LENGTH_SHORT).show();
                                    return;
                                }

                            }

                        });

                        client.getLastLocation().addOnFailureListener(MainActivity.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Toast.makeText(context,""+e,Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        //updateLocationUI();
                    }
                }
                );
    }
    private  boolean checkGps()
    {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return statusOfGPS;
    }

    private void handleExit() {
        if (System.currentTimeMillis() - l < 700) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            finish();
        } else {
            //H.showMessage(this, "Press again to exit.");
            Toast.makeText(this, "Press again to exit.", Toast.LENGTH_SHORT).show();
            l = System.currentTimeMillis();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        handleExit();
    }
    private void takeSS(View v){
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

         try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpeg";

            // create bitmap screen capture

            v.setDrawingCacheEnabled(true);
            v.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
            Bitmap bitmap = Bitmap.createBitmap(v.getDrawingCache());
            v.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);


            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            //Uri uri=Uri.fromFile(imageFile);
            Uri locuri=FileProvider.getUriForFile(this,getApplicationContext().getPackageName(),imageFile);
            //setting screenshot in imageview
            String filePath = imageFile.getPath();

            Bitmap ssbitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            //imageView.setImageBitmap(ssbitmap);
            //ivpl.setImageBitmap(ssbitmap);
            //sharePath = filePath;
            onShareOnePhoto(locuri);

        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }
    private void shareImage(View v)
    {
        try {

        File file =new File(image_uri.getPath());
        Log.v("Uri",""+image_uri);
        //Toast.makeText(context, ""+image_uri, Toast.LENGTH_SHORT).show();

        Intent i=new Intent(Intent.ACTION_SEND);
        Uri myUri= FileProvider.getUriForFile(this,getApplicationContext().getPackageName()+".provider",file);
        i.setDataAndType(myUri,"image/*");
        i.putExtra(Intent.EXTRA_STREAM,myUri);
        startActivity(i);


        }
        catch (Exception e)
        {
            Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void onShareOnePhoto(Uri uri) {
        //Uri path = FileProvider.getUriForFile(this, "com.example.piclocation", new File(ic_android_green));//

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        //shareIntent.putExtra(Intent.EXTRA_TEXT, "This is one image I'm sharing.");
        //shareIntent.putExtra(Intent.EXTRA_STREAM, path);
        shareIntent.putExtra(Intent.EXTRA_STREAM,uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "Share..."));
    }

}

