package app_mobili.transceiver_go;

import static android.content.Context.LOCATION_SERVICE;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Interpolator;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentGameMap#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentGameMap extends FragmentMainMap {

    private Latitude myLocationLatitude;
    private Longitude myLocationLongitude;
    private Latitude oldLocationLatitude;
    private Longitude oldLocationLongitude;
    private float myLocationBearing;
    private ImageView myAvatar;
    private Latitude avatarLatitude;
    private Longitude avatarLongitude;

    public FragmentGameMap() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // creates the my position avatar
        myAvatar = new ImageView(getContext());
        myAvatar.setImageResource(R.drawable.lorenzo_idle);
        ((ViewGroup) getView().findViewById(R.id.fragment_main_map_layout)).addView(myAvatar);
        // resize the image
        ViewGroup.LayoutParams params = myAvatar.getLayoutParams();
        params.height = 250; //pixels
        params.width = 250;
        myAvatar.setLayoutParams(params);
        // hides the avatar until it needs to be shown
        myAvatar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        super.onMapReady(googleMap);
        CameraPosition position = map.getCameraPosition();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(position.target).tilt(90).zoom(position.zoom).build()));

        View defaultOrientationButton = getView().findViewById((int) 5);
        orientationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myLocationLatitude != null && myLocationLongitude != null) {
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(myLocationLatitude.getValue(), myLocationLongitude.getValue())).tilt(getMaximumTilt(19)).zoom(19).bearing(myLocationBearing).build()));
                }
            }
        });

        // LUIZO TI PREGO FA QUALCOSA, QUESTO MOSTRO L'HA GENERATO INTELLIJ
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager lm = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER); // todo controlla che non sia null

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).startListenForCoordinates(new CoordinateListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    Log.println(Log.ASSERT, "", new Date() + " " + location.getLatitude() + " " + location.getLongitude());
                    Toast.makeText(getContext(), new Date() + " " + location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    if (myLocationLatitude == null || myLocationLongitude == null) { // if a previous location does not exist, make the old location equal to the current one
                        oldLocationLatitude = new Latitude(location.getLatitude());
                        oldLocationLongitude = new Longitude(location.getLongitude());
                    } else {
                        oldLocationLatitude = new Latitude(myLocationLatitude.getValue());
                        oldLocationLongitude = new Longitude(myLocationLongitude.getValue());
                    }

                    myLocationLatitude = new Latitude(location.getLatitude());
                    myLocationLongitude = new Longitude(location.getLongitude());
                    myLocationBearing = location.getBearing();
                    //moveMyAvatar(myLocationLatitude, myLocationLongitude);


                    ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                    valueAnimator.setDuration(1000); // duration 1 second
                    valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            try {
                                float v = animation.getAnimatedFraction();

                                avatarLatitude = new Latitude((myLocationLatitude.getValue() - oldLocationLatitude.getValue()) * v + oldLocationLatitude.getValue());

                                // Take the shortest path across the 180th meridian.
                                double lngDelta = myLocationLongitude.getValue() - oldLocationLongitude.getValue();
                                if (Math.abs(lngDelta) > 180) {
                                    lngDelta -= Math.signum(lngDelta) * 360;
                                }
                                avatarLongitude = new Longitude(lngDelta * v + oldLocationLongitude.getValue());


                            } catch (Exception e) {
                                avatarLatitude = new Latitude(myLocationLatitude.getValue());
                                avatarLongitude = new Longitude(myLocationLongitude.getValue());
                            } finally {
                                moveMyAvatar(avatarLatitude, avatarLongitude);
                                myAvatar.setVisibility(View.VISIBLE);
                            }
                        }
                    });

                    valueAnimator.start();
                }
            });
        }


    }

    @Override
    public void onCameraMove() {
        super.onCameraMove();
        if (avatarLatitude != null && avatarLongitude != null) {
            moveMyAvatar(avatarLatitude, avatarLongitude);
        }
    }

    @Override
    public void onCameraIdle() {
        super.onCameraIdle();
        CameraPosition pos = map.getCameraPosition();
        if (pos.tilt != getMaximumTilt(pos.zoom)) {
            map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(pos.target).tilt(getMaximumTilt(pos.zoom)).bearing(pos.bearing).zoom(pos.zoom).build()));
        }

    }

    private void moveMyAvatar(Latitude newLatitude, Longitude newLongitude) {
        Point myPositionOnScreen = map.getProjection().toScreenLocation(new LatLng(newLatitude.getValue(), newLongitude.getValue()));
        myAvatar.setTranslationX(myPositionOnScreen.x - myAvatar.getLayoutParams().width / 2f);
        myAvatar.setTranslationY(myPositionOnScreen.y - myAvatar.getLayoutParams().height);
    }

    // code from https://stackoverflow.com/questions/18053156/set-image-from-drawable-as-marker-in-google-map-version-2
    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable, int width, int height) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // code from https://gist.github.com/bharris47/5057910
    public static float getMaximumTilt(float zoom) {
        // for tilt values, see:
        // https://developers.google.com/maps/documentation/android/reference/com/google/android/gms/maps/model/CameraPosition.Builder?hl=fr

        float tilt = 30.0f;

        if (zoom > 15.5f) {
            tilt = 67.5f;
        } else if (zoom >= 14.0f) {
            tilt = (((zoom - 14.0f) / 1.5f) * (67.5f - 45.0f)) + 45.0f;
        } else if (zoom >= 10.0f) {
            tilt = (((zoom - 10.0f) / 4.0f) * (45.0f - 30.0f)) + 30.0f;
        }

        return tilt;
    }

}