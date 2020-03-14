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

import android.app.NotificationManager;
import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import java.util.List;

/**
 * Class to process location results.
 */
class LocationResultHelper {

    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";

    final private static String PRIMARY_CHANNEL = "default";

    final private static String UNKNOWN_LOCATION = "Unknown location";

    private Context mContext;
    private Location mLocation;
    private NotificationManager mNotificationManager;

    LocationResultHelper(Context context, Location location) {
        mContext = context;
        mLocation = location;

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
//        if (mLocation.isEmpty()) {
//            return UNKNOWN_LOCATION;
//        }
        StringBuilder sb = new StringBuilder();
//        for (Location location : mLocations) {
            // sb.append("(");
            sb.append(mLocation.getLatitude());
            sb.append(",");
            sb.append(mLocation.getLongitude());
            sb.append(",");
            sb.append(mLocation.getTime());
            // sb.append(")");
//            sb.append("\n");
//        }
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
