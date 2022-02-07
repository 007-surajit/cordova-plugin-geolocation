package org.apache.cordova.geolocation;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.preference.PreferenceActivity;

// import android.support.v7.app.AlertDialog;
// import android.support.v7.app.AppCompatActivity;


public class FusedLocationHelper extends PreferenceActivity implements
        GoogleApiClient.ConnectionCallbacks,
               GoogleApiClient.OnConnectionFailedListener,
               SharedPreferences.OnSharedPreferenceChangeListener,
        LocationListener, ResultCallback<LocationSettingsResult>   {
                   
    protected Activity mActivity = null;
    protected static final String TAG = "fusedlocation-plugin";
    protected CallbackContext mCallBackWhenGotLocation;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected LocationRequest mLocationRequest;
    private Location locationObj;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
     /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL = 20 * 1000; // = 20 minutes

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
     /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 3;

    final static String KEY_USER_ID = "user-id";

    public static DBManager dbManager;

    private ArrayList<String> permissionsToRequest;
	private ArrayList<String> permissionsRejected = new ArrayList<>();
	private ArrayList<String> permissions = new ArrayList<>();
	// integer for permissions results request
	private static final int ALL_PERMISSIONS_RESULT = 1011;
    
    protected boolean mGetAddress;
    protected JSONArray arguments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // we add permissions we need to request location of the users
        /* permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (permissionsToRequest.size() > 0) {
        requestPermissions(permissionsToRequest.toArray(
                new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
        } */
    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermissions) {
        if (!hasPermission(perm)) {
            result.add(perm);
        }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(mActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            &&  ActivityCompat.checkSelfPermission(mActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return;
        }

        // Permissions ok, we get last location
        /* location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null) {
        locationTv.setText("Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude());
        }

        startLocationUpdates(); */
        checkLocationSettings();
    }

    private void startLocationUpdates() {
       
        if (ActivityCompat.checkSelfPermission(mActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            &&  ActivityCompat.checkSelfPermission(mActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
        }
        Log.i(TAG, "Starting location updates");
        try {
            // LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            // LocationRequestHelper.setRequesting(this, false);
            e.printStackTrace();
        }
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(mActivity, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(mActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
       // Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        ErrorHappened("onConnectionFailed. Error code: " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            locationObj = location;
            // locationTv.setText("Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude());
            Log.d(TAG, "Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude());

            JSONObject jsonLocation = new JSONObject();
            try {
                // jsonLocation.put("user_id", arguments.getInt(0));
                jsonLocation.put("latitude", location.getLatitude());
                jsonLocation.put("longitude", location.getLongitude());
                // jsonLocation.put("timestamp", location.getTime());
                // jsonLocation.put("punchout", 0);
                GetAddressFromLocation(location);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            // postLocationDataToServer(jsonLocation);
            
        }
    }

    public static void onLocationReceived(Location location) {
        Log.d(TAG, "Location Event Received!");
//        StringBuilder sb = new StringBuilder();
//        for (Location location : mLocations) {
//            sb.append("(");
//            sb.append(location.getLatitude());
//            sb.append(", ");
//            sb.append(location.getLongitude());
//            sb.append(")");
//            sb.append("\n");
//        }
//        Log.d(TAG, sb.toString());
//        GetAddressFromLocation(location);
    }

    private void postLocationDataToServer(JSONObject jsonLocation) {
        String locationApiUrl = mActivity.getString(mActivity.getResources().getIdentifier( "save_url", "string", mActivity.getPackageName()));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, locationApiUrl, jsonLocation,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response);
    //                        hideProgressDialog();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
    //                            hideProgressDialog();
                        // save location to database
                        saveLocationOffline(jsonLocation);
                    }
                });
        jsonObjectRequest.setShouldCache(false);
        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(mActivity).addToRequestQueue(jsonObjectRequest);
    }
    

    public void saveLocationOffline(JSONObject jsonLocation) {
        dbManager = new DBManager(mActivity);
        dbManager.open();
        ContentValues contentValue = new ContentValues();
        try {
            contentValue.put("user_id", getUserId());
            contentValue.put("latitude", jsonLocation.getString("latitude"));
            contentValue.put("longitude", jsonLocation.getString("longitude"));
            contentValue.put("timestamp", jsonLocation.getString("timestamp"));
            contentValue.put("punchout", jsonLocation.getString("punchout"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if(dbManager.insertLocationData(contentValue) != -1){
            Log.i(TAG, "Location saved to database");
        }
        dbManager.close();
    }


    public FusedLocationHelper(Activity activity) {
        mActivity = activity;
    }

    public void startLocationTracking(CallbackContext cb, JSONArray args) {
        mGetAddress = false;
		mCallBackWhenGotLocation = cb;
        arguments = args;
        setUserId();
        PreferenceManager.getDefaultSharedPreferences(mActivity)
                .registerOnSharedPreferenceChangeListener(this);
		CheckForPlayServices();
        SetupLocationFetching(cb);
    }

    public void stopLocationTracking(CallbackContext cb) {
        mGetAddress = false;
		mCallBackWhenGotLocation = cb;
        PreferenceManager.getDefaultSharedPreferences(mActivity)
                .unregisterOnSharedPreferenceChangeListener(this);
        StopLocationFetching(cb);
    }

    public void setUserId() {
        try {
            PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext())
                    .edit()
                    .putString(KEY_USER_ID, String.valueOf(arguments.getInt(0)))
                    .apply();
        }catch (JSONException ex) {
            Log.e(TAG, "Could not set user id in preference");
        }
    }

    public String getUserId() {
        return PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext())
                .getString(KEY_USER_ID, "");
    }
    
    public void GetAddress(CallbackContext cb) {
        mGetAddress = true;
		mCallBackWhenGotLocation = cb;
		CheckForPlayServices();
        SetupLocationFetching(cb);
    }

	protected void CheckForPlayServices() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
		if (status != ConnectionResult.SUCCESS) {
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(status, mActivity, 10, new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
              ErrorHappened("onCancel called on ErrorDialog. ");
            }
			});
			 if (errorDialog != null) {
                errorDialog.show();
            } else {
				ErrorHappened("CheckForPlayServices failed. Error code: " + status);
			}
		}
	}

    protected void ErrorHappened(String msg) {
        Log.i(TAG, msg);
        if(locationObj != null) {
            /* JSONObject jsonLocation = new JSONObject();
            try {
                jsonLocation.put("latitude", String.valueOf(locationObj.getLatitude()));
                jsonLocation.put("longitude", String.valueOf(locationObj.getLongitude()));
                jsonLocation.put("timestamp", String.valueOf(locationObj.getTime()));
                if (mGoogleApiClient != null  &&  mGoogleApiClient.isConnected()) {
                    jsonLocation.put("punchout", 0);
                }else {
                    jsonLocation.put("punchout", 1); // true if location tracking stopped due to punch out
                }
                mCallBackWhenGotLocation.success(jsonLocation);
            }catch (JSONException ex) {
                ErrorHappened("Error generating JSON from location"); 
            }  */
        }else {
            mCallBackWhenGotLocation.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, msg));
        }
    }

    protected void SetupLocationFetching(CallbackContext cb) {

         buildGoogleApiClient();
		 createLocationRequest();
         buildLocationSettingsRequest();
         mGoogleApiClient.connect();
    }

    protected void StopLocationFetching(CallbackContext cb) {
        // stop location updates
        if (mGoogleApiClient != null  &&  mGoogleApiClient.isConnected()) {
            Log.i(TAG, "Removing location updates");
            // LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, getPendingIntent());
            mGoogleApiClient.disconnect();
        }
        GetLastLocation();
        // postLocationDataToServer(jsonLocation);
    }

    protected synchronized void buildGoogleApiClient() {
        // we build google api client
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

	protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
    }
    
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }
    
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }
        
    protected void GetLastLocation() {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            try {
		    JSONObject jsonLocation = new JSONObject();
            jsonLocation.put("lat", String.valueOf(lastLocation.getLatitude()));
            jsonLocation.put("lon", String.valueOf(lastLocation.getLongitude()));
            if (mGetAddress) {
                GetAddressFromLocation(lastLocation);
            } else {
                mCallBackWhenGotLocation.success(jsonLocation);
            }
            }
            catch (JSONException ex) {
                 ErrorHappened("Error generating JSON from location"); 
            }         
        } else {
            ErrorHappened("no location available");         
        }
    }

    protected void GetAddressFromLocation(Location lastLocation) {
        Geocoder geocoder = new Geocoder(mActivity, Locale.getDefault());

        List<Address> addresses = null;
        JSONObject jsonLocation = new JSONObject();
        
        try {
            jsonLocation.put("user_id", getUserId());
            jsonLocation.put("latitude", lastLocation.getLatitude());
            jsonLocation.put("longitude", lastLocation.getLongitude());
            jsonLocation.put("timestamp", lastLocation.getTime());
            if (mGoogleApiClient != null  &&  mGoogleApiClient.isConnected()) {
                jsonLocation.put("punchout", 0);
            }else {
                jsonLocation.put("punchout", 1);
            }
//            try {
//                addresses = geocoder.getFromLocation(
//                        lastLocation.getLatitude(),
//                        lastLocation.getLongitude(),
//                        1);
//            } catch (IOException ioException) {
//                ErrorHappened("Service not available");
//                return;
//            } catch (IllegalArgumentException illegalArgumentException) {
//                ErrorHappened("Invalid location params used");
//                return;
//            }
//            // Handle case where no address was found.
//            if (addresses == null || addresses.size()  == 0) {
//                ErrorHappened("NoLocationUpdatesBroadcastReceiver address found");
//            } else {
//                Address address = addresses.get(0);
//                ArrayList<String> addressFragments = new ArrayList<String>();
//                for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
//                    addressFragments.add(address.getAddressLine(i));
//                }
//                jsonLocation.put("address", TextUtils.join(System.getProperty("line.separator"), addressFragments));
//            }
        }catch (JSONException ex) {
            ErrorHappened("Error generating JSON from location"); 
        }
        postLocationDataToServer(jsonLocation);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(LocationResultHelper.KEY_LOCATION_UPDATES_RESULT)) {
//            mLocationUpdatesResultView.setText(LocationResultHelper.getSavedLocationResult(this));
            Log.i(TAG, getSavedLocationResult());
            String[] values = getSavedLocationResult().split(",");
            Location targetLocation = new Location("");//provider name is unnecessary
            targetLocation.setLatitude(Double.valueOf(values[0]));
            targetLocation.setLongitude(Double.valueOf(values[1]));
            targetLocation.setTime(Long.valueOf(values[2]));
            locationObj = targetLocation;
            GetAddressFromLocation(locationObj);
        }
    }
   
    protected String getSavedLocationResult() {
        return PreferenceManager.getDefaultSharedPreferences(mActivity)
                .getString(LocationResultHelper.KEY_LOCATION_UPDATES_RESULT, "");
    }
   
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                GetLastLocation();
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(mActivity, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {                   
                    ErrorHappened("PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:              
                ErrorHappened("Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  Log.i(TAG, "onActivityResult called with reqestCode " + requestCode + " and resultCode " +resultCode);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        GetLastLocation();
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        ErrorHappened("User chose not to make required location settings changes.");                       
                        break;
                }
                break;
          }		
    }
    
}


