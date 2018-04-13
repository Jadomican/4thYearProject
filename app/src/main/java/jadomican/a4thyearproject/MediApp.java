package jadomican.a4thyearproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import jadomican.a4thyearproject.data.Medicine;
import jadomican.a4thyearproject.data.UserDetail;

/**
 * Created by jadom on 05/04/2018.
 */

// Simple class used to statically get application context
public class MediApp extends Application {

    public static final String KEY_NEGATIVE = "negative";
    public static final String KEY_POSITIVE = "positive";
    private static Context context;

    public void onCreate() {
        super.onCreate();
        MediApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MediApp.context;
    }

    // Re-use this code wherever a toast is to be called
    public static void customToast(String message, String type) {
        Toast toast = Toast.makeText(getAppContext(), message, Toast.LENGTH_LONG);
        View view = toast.getView();
        // Set toast BG colour
        switch (type) {
            case KEY_NEGATIVE:
                view.getBackground().setColorFilter(Color.rgb(219, 68, 55), PorterDuff.Mode.SRC_IN);
                break;
            case KEY_POSITIVE:
                view.getBackground().setColorFilter(Color.rgb(0, 153, 255), PorterDuff.Mode.SRC_IN);
                break;
        }
        TextView toastMessage = (TextView) toast.getView().findViewById(android.R.id.message);
        toastMessage.setTextColor(Color.WHITE);
        toast.show();
    }

    // Re-usable code to set the listener for clicking a navigation menu item. This method is called
    // in all activities that use the menu navigation bar as opposed to implementing this code in each activity
    public static void setNavigationListener(NavigationView navigationView, final DrawerLayout drawerLayout, final int currentMenuItemId, final AppCompatActivity act) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // Close the menu on tap of item
                        drawerLayout.closeDrawers();
                        Context context = MediApp.getAppContext();

                        int menuItemId = menuItem.getItemId();
                        if (menuItemId == currentMenuItemId) {
                            return true;

                        } else {
                            Intent intent = new Intent();
                            switch (menuItem.getItemId()) {
                                case R.id.nav_home:
                                    intent = new Intent(context, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    break;
                                case R.id.nav_edit_profile:
                                    Bundle arguments = new Bundle();
                                    arguments.putString(UserDetailFragment.ARG_ITEM_ID, AWSProvider.getInstance().getIdentityManager().getCachedUserID());
                                    intent = new Intent(context, UserDetailActivity.class);
                                    intent.putExtras(arguments);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    break;
                                case R.id.nav_profile_medicines:
                                    intent = new Intent(context, ProfileMedicineListActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    break;
                                case R.id.nav_camera:
                                    intent = new Intent(context, OcrCaptureActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    break;
                            }
                            act.startActivityIfNeeded(intent, 0);
                            return true;
                        }
                    }
                });
    }

    // Return a properly formatted date based on the user's device settings
    static DateFormat df = new SimpleDateFormat(ProfileMedicineListActivity.DATE_FORMAT_NO_ZONE);

    public static String getFormattedDate(String date) {
        try {
            df.setTimeZone(TimeZone.getDefault());
            Date dateFormat = df.parse(date);
            return df.format(dateFormat);
            //return df.format(df.parse(date));
        } catch (ParseException e) {
            // In case of error, no date displayed
            return " ";
        }
    }

    // The dynamoDB database returns all values as Strings. This is a helper method to be called
    // when a conversion to List format is required.
    public static List<Medicine> medicineStringToList(String stringList) {
        List<Medicine> listMedicines = new ArrayList<>();
        // In case of String being null, return empty list
        if (TextUtils.isEmpty(stringList)) {
            return listMedicines;
        }

        // If String is not null, convert to a Medicine List and return
        try {
            JSONArray array = new JSONArray(stringList);
            //Add each medicine to the list
            for (int i = 0; i < array.length(); i++) {
                JSONObject element = array.getJSONObject(i);
                Medicine medicine = new Medicine(
                        element.get(MedicineListActivity.KEY_ID).toString(),
                        element.get(MedicineListActivity.KEY_NAME).toString(),
                        element.get(MedicineListActivity.KEY_TYPE).toString(),
                        element.get(MedicineListActivity.KEY_ONSETACTION).toString(),
                        element.get(MedicineListActivity.KEY_IMAGEURL).toString(),
                        element.get(MedicineListActivity.KEY_CONFLICT).toString(),
                        element.get(MedicineListActivity.KEY_DATE).toString()
                );
                listMedicines.add(medicine);
            }

        } catch (JSONException e) {
            Log.d("MediApp", "A JSONException has occurred: " + e.toString());
        }
        return listMedicines;
    }

}