/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cordova.geolocation;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.app.NotificationChannel;
import android.text.TextUtils;
import android.util.Log;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Class to process location results.
 */
class LocationResultHelper {

    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";

    final private static String PRIMARY_CHANNEL = "default";

    final private static String UNKNOWN_LOCATION = "Unknown location";

    private Context mContext;
    private List<Location> mLocations;
    private NotificationManager mNotificationManager;

    LocationResultHelper(Context context, List<Location> locations) {
        mContext = context;
        mLocations = locations;

//        NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL,
//                context.getString(R.string.default_channel), NotificationManager.IMPORTANCE_DEFAULT);
//        channel.setLightColor(Color.GREEN);
//        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
//        getNotificationManager().createNotificationChannel(channel);
    }

    /**
     * Returns the title for reporting about a list of {@link Location} objects.
     */
//    private String getLocationResultTitle() {
//        String numLocationsReported = mContext.getResources().getQuantityString(
//                R.plurals.num_locations_reported, mLocations.size(), mLocations.size());
//        return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(new Date());
//    }
//

    private String getLocationResultText() {
        if (mLocations.isEmpty()) {
            return UNKNOWN_LOCATION;
        }
        StringBuilder sb = new StringBuilder();
        for (Location location : mLocations) {
            // sb.append("(");
            sb.append(location.getLatitude());
            sb.append(",");
            sb.append(location.getLongitude());
            // sb.append(")");
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Saves location result as a string to {@link android.content.SharedPreferences}.
     */
    void saveResults() {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultText())
                .apply();
 //        if (!mLocations.isEmpty()) {
 //            GetAddressFromLocation(mLocations.get(0));
 //        }
     }

    /**
     * Fetches location results from {@link android.content.SharedPreferences}.
     */
    static String getSavedLocationResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }

    /**
     * Get the notification mNotificationManager.
     * <p>
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }
}
