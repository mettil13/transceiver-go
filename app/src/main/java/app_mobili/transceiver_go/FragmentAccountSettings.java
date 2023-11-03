package app_mobili.transceiver_go;

import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class FragmentAccountSettings extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    ImageView avatarSkin;
    ImageView avatarClothes;
    ImageView avatarHat;
    int hatIndex = 0;
    TypedArray hats;
    TypedArray hatPrices;
    int clothesIndex = 0;
    TypedArray clothes;
    TypedArray clothesPrices;
    ImageView hatLock;
    ImageView clothesLock;

    Button purchaseHatButton;
    Button purchaseClothesButton;

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

        hatLock = getView().findViewById(R.id.player_hat_lock);
        clothesLock = getView().findViewById(R.id.player_clothes_lock);

        avatarSkin = getView().findViewById(R.id.player_skin);
        avatarClothes = getView().findViewById(R.id.player_clothes);
        avatarHat = getView().findViewById(R.id.player_hat);
        hats = getResources().obtainTypedArray(R.array.avatar_hats);
        hatPrices = getResources().obtainTypedArray(R.array.avatar_hats_price);
        clothes = getResources().obtainTypedArray(R.array.avatar_clothes_idle);
        clothesPrices = getResources().obtainTypedArray(R.array.avatar_clothes_price);

        LorenzoHelper.buildLorenzoIdleFromPreferences(getContext(), avatarSkin, avatarClothes, avatarHat);

        hatIndex = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("hat_index", 0);
        clothesIndex = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("clothes_index", 0);

        ImageButton nextHatButton = getView().findViewById(R.id.button_next_hat);
        ImageButton prevHatButton = getView().findViewById(R.id.button_prev_hat);
        ImageButton nextClothesButton = getView().findViewById(R.id.button_next_clothes);
        ImageButton prevClothesButton = getView().findViewById(R.id.button_prev_clothes);

        purchaseHatButton = getView().findViewById(R.id.player_hat_lock_button);
        purchaseClothesButton = getView().findViewById(R.id.player_clothes_lock_button);

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
        purchaseHatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int coins = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("coins", 0);
                int price = hatPrices.getInt(hatIndex, 0);
                if( coins >= price){
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("coins", coins - price).apply();
                    String hatResourceName = getResources().getResourceEntryName(hats.getResourceId(hatIndex, 0));
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("has_purchased_" + hatResourceName, true).apply();
                    setHat(hatIndex);
                } else {
                    Toast.makeText(getContext(), R.string.purchase_denied, Toast.LENGTH_LONG).show();
                }
            }
        });
        purchaseClothesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int coins = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("coins", 0);
                int price = clothesPrices.getInt(clothesIndex, 0);
                if( coins >= price){
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("coins", coins - price).apply();
                    String clothesResourceName = getResources().getResourceEntryName(clothes.getResourceId(clothesIndex, 0));
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("has_purchased_" + clothesResourceName, true).apply();
                    setClothes(clothesIndex);
                } else {
                    Toast.makeText(getContext(), R.string.purchase_denied, Toast.LENGTH_LONG).show();
                }
            }
        });

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    public void setNextHat() {
        hatIndex++;
        if (hatIndex >= hats.length()) {
            hatIndex = 0;
        }
        setHat(hatIndex);
    }

    public void setPrevHat() {
        hatIndex--;
        if (hatIndex < 0) {
            hatIndex = hats.length() - 1;
        }
        setHat(hatIndex);
    }

    private void setHat(int index) {
        avatarHat.setImageDrawable(hats.getDrawable(index));
        String hatResourceName = getResources().getResourceEntryName(hats.getResourceId(index, 0));
        if (hatPrices.getInt(index, 0) <= 0 || PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("has_purchased_" + hatResourceName, false)) {
            hatLock.setVisibility(View.INVISIBLE);
            purchaseHatButton.setVisibility(View.INVISIBLE);
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("hat_index", index).apply();
        } else {
            hatLock.setVisibility(View.VISIBLE);
            purchaseHatButton.setText(String.valueOf(hatPrices.getInt(index, 0)));
            purchaseHatButton.setVisibility(View.VISIBLE);
        }
    }

    public void setNextClothes() {
        clothesIndex++;
        if (clothesIndex >= clothes.length()) {
            clothesIndex = 0;
        }
        setClothes(clothesIndex);
    }

    public void setPrevClothes() {
        clothesIndex--;
        if (clothesIndex < 0) {
            clothesIndex = clothes.length() - 1;
        }
        setClothes(clothesIndex);
    }

    private void setClothes(int index) {
        avatarClothes.setImageDrawable(clothes.getDrawable(index));
        String clothesResourceName = getResources().getResourceEntryName(clothes.getResourceId(index, 0));
        if (clothesPrices.getInt(index, 0) <= 0 || PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("has_purchased_" + clothesResourceName, false)) {
            clothesLock.setVisibility(View.INVISIBLE);
            purchaseClothesButton.setVisibility(View.INVISIBLE);
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("clothes_index", index).apply();
        } else {
            clothesLock.setVisibility(View.VISIBLE);
            purchaseClothesButton.setText(String.valueOf(clothesPrices.getInt(index, 0)));
            purchaseClothesButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("skin_color")) {
            LorenzoHelper.lorenzoIdleSkinBuilder(avatarSkin, getContext(), LorenzoHelper.nameToColor(getContext(), sharedPreferences.getString(s, "skin_00")));
            ((AnimatedVectorDrawable) avatarSkin.getDrawable()).start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hats.recycle();
        hatPrices.recycle();
        clothes.recycle();
        clothesPrices.recycle();
    }
}