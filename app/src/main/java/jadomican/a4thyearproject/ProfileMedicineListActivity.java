package jadomican.a4thyearproject;

import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBar;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jadomican.a4thyearproject.data.Medicine;
import jadomican.a4thyearproject.data.UserDetail;
import jadomican.a4thyearproject.data.UserDetailsContentContract;

/**
 * An activity to represent the user profile's list of medicines.
 */

public class ProfileMedicineListActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    private UserDetail mItem;
    private Uri itemUri;
    private DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    private TextView onsetActionView;

    private ContentResolver contentResolver;

    private static final int QUERY_TOKEN = 1001;
    private static final int UPDATE_TOKEN = 1002;
    public static final String SORT_NAME = "name";
    public static final String SORT_DATE = "date";
    public static final String SORT_TYPE = "type";
    public static final String DATE_FORMAT = "dd/MMM/yyyy HH:mm z";
    public static final String KEY_DATE_FORMAT = "formattedDate";

    //private ListView mListView;
    SwipeMenuListView mListView;
    SimpleAdapter adapter;
    //List of HashMaps representing the list of profile medicines to be displayed
    private List<HashMap<String, String>> mMedicineMapList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_medicine_list);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView.setCheckedItem(R.id.nav_profile_medicines);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        MediApp.setNavigationListener(navigationView, mDrawerLayout, R.id.nav_profile_medicines, this);

        mListView = (SwipeMenuListView) findViewById(R.id.profile_list_view);
        mListView.setOnItemClickListener(this);
        // Perform bounce animation when closing menu
        mListView.setCloseInterpolator(new BounceInterpolator());

        contentResolver = getApplicationContext().getContentResolver();

        // Get current user details based on user ID
        String itemId = AWSProvider.getInstance().getIdentityManager().getCachedUserID();
        itemUri = UserDetailsContentContract.UserDetails.uriBuilder(itemId);

        // Initialise and populate the Swipe Menu Creator
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // Swipe "More" item
                SwipeMenuItem moreInfoItem = new SwipeMenuItem(
                        getApplicationContext());
                // Set BG colour
                moreInfoItem.setBackground(new ColorDrawable(Color.rgb(0x00, 0x8a,
                        0xe6)));
                moreInfoItem.setWidth(170);
                moreInfoItem.setTitle("More");
                moreInfoItem.setTitleSize(18);
                moreInfoItem.setTitleColor(Color.WHITE);
                // Add item to the menu
                menu.addMenuItem(moreInfoItem);
                // Create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // Set BG colour
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                deleteItem.setWidth(170);
                // Add the Delete icon to the menu
                deleteItem.setIcon(R.drawable.ic_delete);
                // Add item to the menu
                menu.addMenuItem(deleteItem);
            }
        };

        mListView.setMenuCreator(creator);

        // Listener for user clicking on a swipe item
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                // index is the position of the swipe menu item chosen by user
                switch (index) {
                    case 0:
                        Log.d("swipe", "onMenuItemClick: clicked item " + position);
                        break;
                    case 1:
                        Log.d("swipe", "onMenuItemClick: clicked item " + mMedicineMapList.get(position).get(MedicineListActivity.KEY_NAME));

                        final String medicineToDelete = mMedicineMapList.get(position).get(MedicineListActivity.KEY_NAME);
                        // Confirm if the user really wants to delete the medicine
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        mMedicineMapList.remove(position);
                                        adapter.notifyDataSetChanged();
                                        loadListView();
                                        deleteFromProfile(medicineToDelete);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileMedicineListActivity.this);
                        builder.setMessage("Delete " + medicineToDelete + "?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                        break;
                }
                // Return false - close the menu
                return false;
            }
        });


    }

    // Update the profile if a delete has been specified
    private void deleteFromProfile(String medicineToRemove) {
        // Remove the specified medicine from the list
        List<Medicine> updatedList = new ArrayList<>(mItem.getAddedMedicines());
        Medicine toRemove = new Medicine();
        for (Medicine medicine : updatedList) {
            if (medicine.getMedicineName().equalsIgnoreCase(medicineToRemove)) {
                toRemove = medicine;
            }
        }
        updatedList.remove(toRemove);
        determineEmptyList(updatedList);
        mItem.setAddedMedicines(updatedList);
        // Convert to ContentValues and store in the database.
        ContentValues values = mItem.toContentValues();

        AsyncQueryHandler queryHandler = new AsyncQueryHandler(contentResolver) {
            @Override
            protected void onUpdateComplete(int token, Object cookie, int result) {
                super.onUpdateComplete(token, cookie, result);
                Log.d("MedicineDetailsActivity", "update completed");
                MediApp.customToast("Deleted");
            }
        };
        queryHandler.startUpdate(UPDATE_TOKEN, null, itemUri, values, null, null);
    }

    //Populate the user object with their latest changes from the DynamoDB table
    private void syncUser() {
        contentResolver = getApplicationContext().getContentResolver();
        // Replace local cursor methods with async query handling
        AsyncQueryHandler queryHandler = new AsyncQueryHandler(contentResolver) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                super.onQueryComplete(token, cookie, cursor);

                cursor.moveToFirst();
                mItem = UserDetail.fromCursor(cursor);
                populateAndLoadList(SORT_DATE);
            }
        };
        queryHandler.startQuery(QUERY_TOKEN, null, itemUri, UserDetailsContentContract.UserDetails.PROJECTION_ALL, null, null, null);
    }

    // Populate and load the list displaying user medicines
    private void populateAndLoadList(String sortType) {
        mMedicineMapList.clear();
        List<Medicine> addedMedicines = new ArrayList<>(mItem.getAddedMedicines());
        determineEmptyList(addedMedicines);
        try {
            addedMedicines = new ArrayList<>(Medicine.sort(mItem.getAddedMedicines(), sortType));
        } catch (ParseException e) {
            Log.d("ProfileMedicineListAct", "A Parse Exception has occurred: " + e.getMessage());
        }

        for (Medicine medicine : addedMedicines) {
            HashMap<String, String> map = new HashMap<>();
            map.put(MedicineListActivity.KEY_ID, medicine.getMedicineId());
            map.put(MedicineListActivity.KEY_NAME, medicine.getMedicineName());
            map.put(MedicineListActivity.KEY_TYPE, medicine.getMedicineType());
            map.put(MedicineListActivity.KEY_ONSETACTION, medicine.getMedicineOnsetAction());
            map.put(MedicineListActivity.KEY_IMAGEURL, medicine.getMedicineImageUrl());
            map.put(MedicineListActivity.KEY_CONFLICT, medicine.getMedicineConflict());
            map.put(MedicineListActivity.KEY_DATE, medicine.getMedicineDate());
            map.put(KEY_DATE_FORMAT, MediApp.getFormattedDate(medicine.getMedicineDate()));

            //For each medicine, add to list
            mMedicineMapList.add(map);
        }
        Log.d("ProfileMeds", "Size" + Integer.toString(mMedicineMapList.size()));
        loadListView();
    }

    private void determineEmptyList(List<Medicine> list) {
        TextView noMedicines = (TextView) findViewById(R.id.no_medicines);
        if (list.size() <= 0) {
            noMedicines.setVisibility(View.VISIBLE);
        } else {
            noMedicines.setVisibility(View.GONE);
        }
    }

    // Load (or reload) the list of medicines
    private void loadListView() {
        //The adapter which lists the medicines on the screen

        Resources res = getResources();
        String text = String.format(res.getString(R.string.date_added_alt), MedicineListActivity.KEY_ONSETACTION);

        adapter = new SimpleAdapter(
                ProfileMedicineListActivity.this,
                mMedicineMapList,
                R.layout.list_item_date,
                new String[]{MedicineListActivity.KEY_NAME, MedicineListActivity.KEY_TYPE, KEY_DATE_FORMAT},
                new int[]{R.id.name, R.id.type, R.id.date});
        mListView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Bundle arguments = new Bundle();

        arguments.putString(UserDetailFragment.ARG_ITEM_ID, AWSProvider.getInstance().getIdentityManager().getCachedUserID());

        arguments.putString(MedicineListActivity.KEY_NAME, mMedicineMapList.get(i).get(MedicineListActivity.KEY_NAME));
        arguments.putString(MedicineListActivity.KEY_TYPE, mMedicineMapList.get(i).get(MedicineListActivity.KEY_TYPE));
        arguments.putString(MedicineListActivity.KEY_ONSETACTION, mMedicineMapList.get(i).get(MedicineListActivity.KEY_ONSETACTION));
        arguments.putString(MedicineListActivity.KEY_ID, mMedicineMapList.get(i).get(MedicineListActivity.KEY_ID));
        arguments.putString(MedicineListActivity.KEY_IMAGEURL, mMedicineMapList.get(i).get(MedicineListActivity.KEY_IMAGEURL));
        arguments.putString(MedicineListActivity.KEY_CONFLICT, mMedicineMapList.get(i).get(MedicineListActivity.KEY_CONFLICT));

        Log.d("profile", "touched");
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
            case R.id.action_sort_name:
                populateAndLoadList(SORT_NAME);
                return true;
            case R.id.action_sort_date:
                populateAndLoadList(SORT_DATE);
                return true;
            case R.id.action_sort_type:
                populateAndLoadList(SORT_TYPE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_profile_medicines);
        // Refresh the list on activity resume with latest details
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        mMedicineMapList.clear();
        syncUser();
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

    // Add the addition action bar items based on the xml defined menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bar_menu_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

}