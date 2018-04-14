package jadomican.a4thyearproject;

/*
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.amazonaws.mobile.auth.core.IdentityManager;

/**
 * Base class with common code components.
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity {

    // The side bar (drawer), it's navigation items, and the tool bar at the top of the screen
    protected NavigationView mNavigationView;
    protected DrawerLayout mDrawerLayout;
    protected ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        // Set the drawer and navigation menu layouts
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        MediApp.setNavigationListener(mNavigationView, mDrawerLayout, getLayoutResourceId(), this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

    }

    /**
     * Open the navigation drawer when pressing the back button
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mNavigationView.setCheckedItem(getMenuItemResourceId());
    }

    // Add the additional action bar items based on the xml defined menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(getMenuResourceId(), menu);
        return super.onCreateOptionsMenu(menu);
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
                IdentityManager id = AWSProvider.getInstance().getIdentityManager();
                id.signOut();
                Context context = getApplicationContext();
                Intent intent = new Intent(context, AuthenticatorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
                finish();
                return true;
            case R.id.action_help:
                MediApp.displayDialog(this,
                        getHelpTitle(),
                        getHelpMessage());
                return true;

            case R.id.action_back:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * The device back button behaves differently depending on the context. In most pages it opens
     * the navigation menu, however on MedicineDetailsActivity for example, it returns to the search results.
     */
    public void onBackDeviceButtonPressed() {
        super.onBackPressed();
    }

    // Getters to allow subclasses to pass their Activity specific resources to this base class
    protected abstract int getLayoutResourceId();

    protected abstract int getMenuResourceId();

    protected abstract int getMenuItemResourceId();

    protected abstract String getHelpTitle();

    protected abstract String getHelpMessage();
}
