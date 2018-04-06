package jadomican.a4thyearproject;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by jadom on 05/04/2018.
 */

// Simple class used to statically get application context
public class MediApp extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        MediApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MediApp.context;
    }

    public static void customToast(String message) {

        Toast toast = Toast.makeText(getAppContext(), message, Toast.LENGTH_LONG);
        View view = toast.getView();
        // Set toast BG colour
        view.getBackground().setColorFilter(Color.rgb(0, 153, 255), PorterDuff.Mode.SRC_IN);
        TextView toastMessage = (TextView) toast.getView().findViewById(android.R.id.message);
        toastMessage.setTextColor(Color.WHITE);
        toast.show();
    }

}