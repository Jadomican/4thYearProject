package jadomican.a4thyearproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import jadomican.a4thyearproject.data.UserDetail;

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

    // Re-use this code wherever a toast is to be called
    public static void customToast(String message) {
        Toast toast = Toast.makeText(getAppContext(), message, Toast.LENGTH_LONG);
        View view = toast.getView();
        // Set toast BG colour
        view.getBackground().setColorFilter(Color.rgb(0, 153, 255), PorterDuff.Mode.SRC_IN);
        TextView toastMessage = (TextView) toast.getView().findViewById(android.R.id.message);
        toastMessage.setTextColor(Color.WHITE);
        toast.show();
    }

    // Re-usable code to set the listener for clicking a navigation menu item. This method is called
    // in all activities use the menu navigation bar as opposed to implementing this code in each activity
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



}