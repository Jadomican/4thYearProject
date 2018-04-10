package jadomican.a4thyearproject;

/**
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 *
 * The home screen activity of the application, loaded after the initial login. Contains 'links' to
 * other main functionality of the app, such as editing user profile and searching for medicines
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBar;

public class MainActivity extends AppCompatActivity {

    Button medicineSearchButton;
    Button cameraSearchButton;
    NavigationView navigationView;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_home);

        medicineSearchButton = (Button) findViewById(R.id.medicineSearchButton);
        cameraSearchButton = (Button) findViewById(R.id.cameraSearchButton);

        // Set listener for information popup
        setTouchListener(medicineSearchButton,
                getResources().getString(R.string.medicine_search_info_title),
                getResources().getString(R.string.medicine_search_info_message));
        setTouchListener(cameraSearchButton,
                getResources().getString(R.string.camera_info_title),
                getResources().getString(R.string.camera_info_message));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        MediApp.setNavigationListener(navigationView, mDrawerLayout, R.id.nav_home, this);

        if (getIntent().getExtras() != null) {
            if (getIntent().getStringExtra("search").equals("yes")) {
                onSearchRequested();
            }
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_home);
    }

    public void goToEditProfile(View view) {
        Bundle arguments = new Bundle();
        arguments.putString(UserDetailFragment.ARG_ITEM_ID, AWSProvider.getInstance().getIdentityManager().getCachedUserID());
        Context context = view.getContext();
        Intent intent = new Intent(context, UserDetailActivity.class);
        intent.putExtras(arguments);
        context.startActivity(intent);
    }

    public void goToProfileHub(View view) {
        Context context = view.getContext();
        Intent intent = new Intent(context, ProfileMedicineListActivity.class);
        context.startActivity(intent);
    }

    public void goToSearchMedicines(View view) {
        onSearchRequested();
    }

    public void goToCameraSearch(View view) {
        Context context = view.getContext();
        Intent intent = new Intent(context, OcrCaptureActivity.class);
        context.startActivity(intent);
    }

    // Method to contextually display information about a feature when the 'i' symbol is
    // tapped. Takes a button and messages to be displayed as parameters, also passing in the alert
    // title and messages
    public void setTouchListener(final Button button, final String title, final String message) {
        // The following comment stops the IDE from warning about overriding the performClick()
        // method, an optional setOnTouchListener method:
        // noinspection AndroidLintClickableViewAccessibility
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // DrawableRight is index value 2 for the getCompoundDrawables method
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (button.getRight() - button.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle(title);
                        alertDialog.setMessage(message);
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_search:
                onSearchRequested();
                return true;
            case R.id.log_out:
                Context context = getApplicationContext();
                Intent intent = new Intent(context, AuthenticatorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Open the navigation bar when pressing the back button
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    // Add the additional action bar items based on the xml defined menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
