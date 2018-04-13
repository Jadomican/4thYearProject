package jadomican.a4thyearproject;

/**
 * Created by jadom_000 on 27/01/2018.
 */

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * An activity representing a single edit profile details screen.
 */
public class UserDetailActivity extends AppCompatActivity {

    NavigationView navigationView;
    private DrawerLayout mDrawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_edit_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        MediApp.setNavigationListener(navigationView, mDrawerLayout, R.id.nav_edit_profile, this);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey(UserDetailFragment.ARG_ITEM_ID)) {
                String userDetailId = extras.getString(UserDetailFragment.ARG_ITEM_ID);
                arguments.putString(UserDetailFragment.ARG_ITEM_ID, userDetailId);
            }
            UserDetailFragment fragment = new UserDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.user_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_edit_profile);
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
            case R.id.action_help:
                MediApp.displayDialog(this,
                        getString(R.string.bar_edit_profile_help_title),
                        getString(R.string.bar_edit_profile_help_message));
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

    // Hide on-screen keyboard when losing EditText focus, ensuring that the Side Bar (if used)
    // does not become obstructed
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }



}