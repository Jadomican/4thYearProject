package jadomican.a4thyearproject;

/**
 * Created by jadom_000 on 27/01/2018.
 */

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

/**
 * An activity representing a single User profile details screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * details are presented side-by-side with a list of items
 * in a { NoteListActivity}.
 */
public class UserDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == android.R.id.home) {
//            navigateUpTo(new Intent(this, MedicineListActivity.class));
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
}