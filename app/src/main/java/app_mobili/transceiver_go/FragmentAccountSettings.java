package app_mobili.transceiver_go;

import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

public class FragmentAccountSettings extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    ImageView avatarSkin;
    ImageView avatarClothes;
    ImageView avatarHat;
    int hatIndex = 0;
    TypedArray hats;
    int clothesIndex = 0;
    TypedArray clothes;

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

        avatarSkin = getView().findViewById(R.id.player_skin);
        avatarClothes = getView().findViewById(R.id.player_clothes);
        avatarHat = getView().findViewById(R.id.player_hat);
        hats = getResources().obtainTypedArray(R.array.avatar_hats);
        clothes = getResources().obtainTypedArray(R.array.avatar_clothes_idle);

        LorenzoHelper.buildLorenzoFromPreferences(getContext(), avatarSkin, avatarClothes, avatarHat);

        hatIndex = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("hat_index", 0);
        clothesIndex = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("clothes_index", 0);

        ImageButton nextHatButton = getView().findViewById(R.id.button_next_hat);
        ImageButton prevHatButton = getView().findViewById(R.id.button_prev_hat);
        ImageButton nextClothesButton = getView().findViewById(R.id.button_next_clothes);
        ImageButton prevClothesButton = getView().findViewById(R.id.button_prev_clothes);

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
        nextClothesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNextClothes();
            }
        });
        prevClothesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPrevClothes();
            }
        });

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    public void setNextHat() {
        hatIndex++;
        if (hatIndex >= hats.length()) {
            hatIndex = 0;
        }
        avatarHat.setImageDrawable(hats.getDrawable(hatIndex));
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("hat_index", hatIndex).apply();
    }

    public void setPrevHat() {
        hatIndex--;
        if (hatIndex < 0) {
            hatIndex = hats.length() - 1;
        }
        avatarHat.setImageDrawable(hats.getDrawable(hatIndex));
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("hat_index", hatIndex).apply();
    }

    public void setNextClothes() {
        clothesIndex++;
        if (clothesIndex >= clothes.length()) {
            clothesIndex = 0;
        }
        avatarClothes.setImageDrawable(clothes.getDrawable(clothesIndex));
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("clothes_index", clothesIndex).apply();
    }

    public void setPrevClothes() {
        clothesIndex--;
        if (clothesIndex < 0) {
            clothesIndex = clothes.length() - 1;
        }
        avatarClothes.setImageDrawable(clothes.getDrawable(clothesIndex));
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("clothes_index", clothesIndex).apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("skin_color")) {
            LorenzoHelper.lorenzoSkinBuilder(avatarSkin, getContext(), LorenzoHelper.nameToColor(getContext(), sharedPreferences.getString(s, "skin_00")));
            ((AnimatedVectorDrawable) avatarSkin.getDrawable()).start();
        }
    }
}