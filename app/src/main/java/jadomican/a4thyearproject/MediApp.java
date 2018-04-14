package jadomican.a4thyearproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
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

/*
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 */

/**
 * Class to store commonly used utility functions accessible by all Activities - even those that
 * don't extend from the {@link BaseAppCompatActivity Base class}
 */
public class MediApp extends Application {

    public static final String KEY_NEGATIVE = "negative";
    public static final String KEY_POSITIVE = "positive";
    private static Context context;

    /**
     * Method to allow retrieval of app context
     *
     * @see MediApp#getAppContext()
     */
    public void onCreate() {
        super.onCreate();
        MediApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MediApp.context;
    }

    /**
     * Re-use this code wherever a toast is to be called
     */
    public static void customToast(String message, String type) {
        Toast toast = Toast.makeText(getAppContext(), message, Toast.LENGTH_LONG);
        View view = toast.getView();
        // Set toast BG colour
        switch (type) {
            case KEY_NEGATIVE:
                view.getBackground().setColorFilter(ContextCompat.getColor(getAppContext(), R.color.danger_alt), PorterDuff.Mode.SRC_IN);
                break;
            case KEY_POSITIVE:
                view.getBackground().setColorFilter(ContextCompat.getColor(getAppContext(), R.color.positive), PorterDuff.Mode.SRC_IN);
                break;
        }
        TextView toastMessage = (TextView) toast.getView().findViewById(android.R.id.message);
        toastMessage.setTextColor(Color.WHITE);
        toast.show();
    }

    /**
     * Re-usable code to set the listener for clicking a navigation menu item. This method is called
     * in all activities that use the menu navigation bar as opposed to implementing this code in each activity
     *
     * @param navigationView    The navigation menu inside the drawer
     * @param drawerLayout      The drawer (side bar) object
     * @param currentMenuItemId The menu item to be highlighted. Not used for certain Activities including {@link OcrCaptureActivity OcrCaptureActivity}
     * @param act               The calling activity
     */
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
                                case R.id.log_out:
                                    intent = new Intent(context, AuthenticatorActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    context.startActivity(intent);
                                    act.finish();
                                    break;
                            }
                            act.startActivityIfNeeded(intent, 0);
                            return true;
                        }
                    }
                });
    }

    static DateFormat df = new SimpleDateFormat(ProfileMedicineListActivity.DATE_FORMAT_NO_ZONE);

    /**
     * Return a properly formatted date based on the user's device settings
     *
     * @param date The date to be parsed into the user's local format
     * @return
     */
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


    /**
     * The dynamoDB database returns all values as Strings. This is a helper method to be called
     * when a conversion to List format is required.
     *
     * @param stringList The string version of a list of Medicines
     */
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

    /**
     * Determine whether or not the device is connected to the internet
     */
    public static boolean isConnectedToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    /**
     * Display a custom dialog anywhere it's required
     *
     * @param activity The calling activity
     * @param title    The dialog title
     * @param message  The dialog message
     */
    public static void displayDialog(Activity activity, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

}