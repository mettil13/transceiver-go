package app_mobili.transceiver_go;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

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
    private ImageView myAvatarSkin;
    private ImageView myAvatarClothes;
    private ImageView myAvatarHat;
    private Latitude avatarLatitude;
    private Longitude avatarLongitude;

    public FragmentGameMap() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // creates the my position avatar
        myAvatarSkin = new ImageView(getContext());
        myAvatarClothes = new ImageView(getContext());
        myAvatarHat = new ImageView(getContext());
        setUpMyAvatar(myAvatarSkin, myAvatarClothes, myAvatarHat);
        /*
        myAvatarSkin.setImageResource(R.drawable.lorenzo_idle);
        ((ViewGroup) getView().findViewById(R.id.fragment_main_map_layout)).addView(myAvatarSkin);
        // resize the image
        ViewGroup.LayoutParams params = myAvatarSkin.getLayoutParams();
        params.height = 250; // pixels
        params.width = 250;
        myAvatarSkin.setLayoutParams(params);
        // hides the avatar until it needs to be shown
        myAvatarSkin.setVisibility(View.INVISIBLE);
         */
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

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    808);
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    809);
            // ask permissions and return, if we don't have 'em we do nothing
            return;
        }
        LocationManager lm = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER); // todo controlla che non sia null

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).startListenForCoordinates(new CoordinateListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    Log.println(Log.ASSERT, "", new Date() + " " + location.getLatitude() + " " + location.getLongitude());
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
                                myAvatarSkin.setVisibility(View.VISIBLE);
                                myAvatarClothes.setVisibility(View.VISIBLE);
                                myAvatarHat.setVisibility(View.VISIBLE);
                                ((AnimatedVectorDrawable) myAvatarSkin.getDrawable()).start();
                                ((AnimatedVectorDrawable) myAvatarClothes.getDrawable()).start();

                                orientationButton.callOnClick();
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

    private void setUpMyAvatar(ImageView avatarSkin, ImageView avatarClothes, ImageView avatarHat) {
        LorenzoHelper.buildLorenzoWalkFromPreferences(getContext(), avatarSkin, avatarClothes, avatarHat);
        ((android.widget.FrameLayout) getView().findViewById(R.id.fragment_main_map_layout)).addView(avatarSkin);
        ((android.widget.FrameLayout) getView().findViewById(R.id.fragment_main_map_layout)).addView(avatarClothes);
        ((android.widget.FrameLayout) getView().findViewById(R.id.fragment_main_map_layout)).addView(avatarHat);

        // resize the images
        ViewGroup.LayoutParams skinParams = avatarSkin.getLayoutParams();
        skinParams.height = 250; // pixels
        skinParams.width = 250;
        avatarSkin.setLayoutParams(skinParams);
        ViewGroup.LayoutParams clothesParams = avatarClothes.getLayoutParams();
        clothesParams.height = 250; // pixels
        clothesParams.width = 250;
        avatarClothes.setLayoutParams(clothesParams);
        ViewGroup.LayoutParams hatParams = avatarHat.getLayoutParams();
        hatParams.height = 250; // pixels
        hatParams.width = 250;
        avatarHat.setLayoutParams(hatParams);

        // hides the avatar until it needs to be shown
        avatarSkin.setVisibility(View.INVISIBLE);
        avatarClothes.setVisibility(View.INVISIBLE);
        avatarHat.setVisibility(View.INVISIBLE);
    }

    private void moveMyAvatar(Latitude newLatitude, Longitude newLongitude) {
        Point myPositionOnScreen = map.getProjection().toScreenLocation(new LatLng(newLatitude.getValue(), newLongitude.getValue()));
        myAvatarSkin.setTranslationX(myPositionOnScreen.x - myAvatarSkin.getLayoutParams().width / 2f);
        myAvatarSkin.setTranslationY(myPositionOnScreen.y - myAvatarSkin.getLayoutParams().height);
        myAvatarClothes.setTranslationX(myPositionOnScreen.x - myAvatarClothes.getLayoutParams().width / 2f);
        myAvatarClothes.setTranslationY(myPositionOnScreen.y - myAvatarClothes.getLayoutParams().height);
        myAvatarHat.setTranslationX(myPositionOnScreen.x - myAvatarHat.getLayoutParams().width / 2f);
        myAvatarHat.setTranslationY(myPositionOnScreen.y - myAvatarHat.getLayoutParams().height - (myAvatarSkin.getLayoutParams().height * 0.1f));
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