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
            return context.getColor(context.getResources().getIdentifier(name, "color", context.getPackageName()));
        } catch (Exception e) {
            Log.println(Log.ASSERT, "", e.toString());
            return 0;
        }
    }

    public static void lorenzoIdleSkinBuilder(ImageView skin, Context context, int skinColor){
        skin.setImageResource(R.drawable.lorenzo_idle_skin);
        skin.getDrawable().mutate().setColorFilter(skinColor, PorterDuff.Mode.MULTIPLY);
    }

    public static void lorenzoWalkSkinBuilder(ImageView skin, Context context, int skinColor){
        skin.setImageResource(R.drawable.lorenzo_walk_skin);
        skin.getDrawable().mutate().setColorFilter(skinColor, PorterDuff.Mode.MULTIPLY);
    }

    public static void buildLorenzoIdleFromPreferences(Context context, ImageView skinView, ImageView clothesView, ImageView hatView){
        String skinColor = PreferenceManager.getDefaultSharedPreferences(context).getString("skin_color", "skin_00");
        lorenzoIdleSkinBuilder(skinView, context, nameToColor(context, skinColor));

        int clothesIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt("clothes_index", 0);
        TypedArray clothes = context.getResources().obtainTypedArray(R.array.avatar_clothes_idle);
        clothesView.setImageDrawable(clothes.getDrawable(clothesIndex));

        TypedArray hats = context.getResources().obtainTypedArray(R.array.avatar_hats);
        int hatIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt("hat_index", 0);
        hatView.setImageDrawable(hats.getDrawable(hatIndex));
    }

    public static void buildLorenzoWalkFromPreferences(Context context, ImageView skinView, ImageView clothesView, ImageView hatView){
        String skinColor = PreferenceManager.getDefaultSharedPreferences(context).getString("skin_color", "skin_00");
        lorenzoWalkSkinBuilder(skinView, context, nameToColor(context, skinColor));

        int clothesIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt("clothes_index", 0);
        TypedArray clothes = context.getResources().obtainTypedArray(R.array.avatar_clothes_walk);
        clothesView.setImageDrawable(clothes.getDrawable(clothesIndex));

        TypedArray hats = context.getResources().obtainTypedArray(R.array.avatar_hats);
        int hatIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt("hat_index", 0);
        hatView.setImageDrawable(hats.getDrawable(hatIndex));
    }
}
