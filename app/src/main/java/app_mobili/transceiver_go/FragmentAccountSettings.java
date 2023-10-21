package app_mobili.transceiver_go;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentAccountSettings extends Fragment {

    ImageView avatarHat;
    int hatIndex = 0;
    TypedArray hats;

    public FragmentAccountSettings() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        avatarHat = getView().findViewById(R.id.player_hat);
        hats = getResources().obtainTypedArray(R.array.avatar_hats);

        ImageButton nextHatButton = getView().findViewById(R.id.button_next_hat);
        ImageButton prevHatButton = getView().findViewById(R.id.button_prev_hat);

        nextHatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNextHat();
            }
        });
        prevHatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPrevHat();
            }
        });

        hatIndex = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("hat_index", 0);
        avatarHat.setImageDrawable(hats.getDrawable(hatIndex));
    }

    public void setNextHat() {
        hatIndex++;
        if(hatIndex >= hats.length()) {
            hatIndex = 0;
        }
        avatarHat.setImageDrawable(hats.getDrawable(hatIndex));
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("hat_index", hatIndex).apply();
    }

    public void setPrevHat() {
        hatIndex--;
        if(hatIndex < 0) {
            hatIndex = hats.length() - 1;
        }
        avatarHat.setImageDrawable(hats.getDrawable(hatIndex));
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("hat_index", hatIndex).apply();

    }
}