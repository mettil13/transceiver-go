package app_mobili.transceiver_go;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

public class LorenzoHelper {
    public static int nameToColor(Context context, String name) {
        try {
            return context.getColor(context.getResources().getIdentifier(name, "color", context.getPackageName()));
        } catch (Exception e) {
            Log.println(Log.ASSERT, "", e.toString());
            return 0;
        }
    }

    public static void lorenzoSkinBuilder(ImageView skin, Context context, int skinColor){
        skin.getDrawable().mutate().setColorFilter(skinColor, PorterDuff.Mode.MULTIPLY);
    }

    public static void buildLorenzoFromPreferences(Context context, ImageView skinView, ImageView clothesView, ImageView hatView){
        String skinColor = PreferenceManager.getDefaultSharedPreferences(context).getString("skin_color", "skin_00");
        lorenzoSkinBuilder(skinView, context, nameToColor(context, skinColor));

        int clothesIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt("clothes_index", 0);
        TypedArray clothes = context.getResources().obtainTypedArray(R.array.avatar_clothes_idle);
        clothesView.setImageDrawable(clothes.getDrawable(clothesIndex));

        TypedArray hats = context.getResources().obtainTypedArray(R.array.avatar_hats);
        int hatIndex = PreferenceManager.getDefaultSharedPreferences(context).getInt("hat_index", 0);
        hatView.setImageDrawable(hats.getDrawable(hatIndex));
    }
}
