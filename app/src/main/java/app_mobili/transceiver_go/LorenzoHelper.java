package app_mobili.transceiver_go;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.Log;
import android.widget.ImageView;

import androidx.preference.PreferenceManager;

public class LorenzoHelper {
    public static int nameToColor(Context context, String name) {
        try {
            // I know that getIdentifier is bad for optimization, but I need it in order to keep the app maintainable and the code clean
            return context.getColor(context.getResources().getIdentifier(name, "color", context.getPackageName()));
        } catch (Exception e) {
            Log.println(Log.ASSERT, "", "nameToColor exception:" + e);
            return 0;
        }
    }

    public static void lorenzoIdleSkinBuilder(ImageView skin, int skinColor){
        skin.setImageResource(R.drawable.lorenzo_idle_skin);
        skin.getDrawable().mutate().setColorFilter(skinColor, PorterDuff.Mode.MULTIPLY);
    }

    public static void lorenzoWalkSkinBuilder(ImageView skin, int skinColor){
        skin.setImageResource(R.drawable.lorenzo_walk_skin);
        skin.getDrawable().mutate().setColorFilter(skinColor, PorterDuff.Mode.MULTIPLY);
    }

    public static void buildLorenzoIdleFromPreferences(Context context, ImageView skinView, ImageView clothesView, ImageView hatView){
        String skinColor = PreferenceManager.getDefaultSharedPreferences(context).getString("skin_color", "skin_00");
        lorenzoIdleSkinBuilder(skinView, nameToColor(context, skinColor));

        int clothesIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt("clothes_index", 0);
        TypedArray clothes = context.getResources().obtainTypedArray(R.array.avatar_clothes_idle);
        clothesView.setImageDrawable(clothes.getDrawable(clothesIndex));

        TypedArray hats = context.getResources().obtainTypedArray(R.array.avatar_hats);
        int hatIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt("hat_index", 0);
        hatView.setImageDrawable(hats.getDrawable(hatIndex));

        clothes.recycle();
        hats.recycle();
    }

    public static void buildLorenzoWalkFromPreferences(Context context, ImageView skinView, ImageView clothesView, ImageView hatView){
        String skinColor = PreferenceManager.getDefaultSharedPreferences(context).getString("skin_color", "skin_00");
        lorenzoWalkSkinBuilder(skinView, nameToColor(context, skinColor));

        int clothesIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt("clothes_index", 0);
        TypedArray clothes = context.getResources().obtainTypedArray(R.array.avatar_clothes_walk);
        clothesView.setImageDrawable(clothes.getDrawable(clothesIndex));

        TypedArray hats = context.getResources().obtainTypedArray(R.array.avatar_hats);
        int hatIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt("hat_index", 0);
        hatView.setImageDrawable(hats.getDrawable(hatIndex));

        clothes.recycle();
        hats.recycle();
    }
}
