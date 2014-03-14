package com.example.bankauthenticator;

import com.google.android.gms.location.Geofence;

/**
 * A single Geofence object, defined by its center and radius.
 */
public class SimpleGeofence {
        // Instance variables
        private final String mId;
        private final double mLatitude;
        private final double mLongitude;
        private final float mRadius;
        private long mExpirationDuration;

    /**
     * @param geofenceId The Geofence's request ID
     * @param latitude Latitude of the Geofence's center.
     * @param longitude Longitude of the Geofence's center.
     * @param radius Radius of the geofence circle.
     * @param expiration Geofence expiration duration
     * @param transition Type of Geofence transition.
     */
    public SimpleGeofence(
            String geofenceId,
            double latitude,
            double longitude,
            float radius) {
        // Set the instance fields from the constructor
        this.mId = geofenceId;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mRadius = radius;
        this.mExpirationDuration = Geofence.NEVER_EXPIRE;
    }
    // Instance field getters
    public String getId() {
        return mId;
    }
    public double getLatitude() {
        return mLatitude;
    }
    public double getLongitude() {
        return mLongitude;
    }
    public float getRadius() {
        return mRadius;
    }
    public long getExpirationDuration() {
        return mExpirationDuration;
    }
    /**
     * Creates a Location Services Geofence object from a
     * SimpleGeofence.
     *
     * @return A Geofence object
     */
    public Geofence toGeofence() {
    	int enter = Geofence.GEOFENCE_TRANSITION_ENTER;
    	int exit = Geofence.GEOFENCE_TRANSITION_EXIT;
    	int transition = enter ^ exit;
        // Build a new Geofence object
        return new Geofence.Builder()
                .setRequestId(getId())
                .setTransitionTypes(transition)
                .setCircularRegion(
                        getLatitude(), getLongitude(), getRadius())
                .setExpirationDuration(mExpirationDuration)
                .build();
    }
}
