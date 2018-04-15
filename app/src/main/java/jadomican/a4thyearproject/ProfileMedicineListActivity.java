package jadomican.a4thyearproject;

/*
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 */

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
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jadomican.a4thyearproject.data.Medicine;
import jadomican.a4thyearproject.data.UserDetail;
import jadomican.a4thyearproject.data.UserDetailsContentContract;

/**
 * An activity to represent the user profile's list of medicines.
 */

public class ProfileMedicineListActivity extends BaseAppCompatActivity
        implements AdapterView.OnItemClickListener {

    private UserDetail mItem;
    private Uri mItemUri;

    private ContentResolver contentResolver;

    private static final int QUERY_TOKEN = 1001;
    private static final int UPDATE_TOKEN = 1002;
    public static final String SORT_NAME = "name";
    public static final String SORT_DATE = "date";
    public static final String SORT_TYPE = "type";
    public static final String DATE_FORMAT = "dd/MMM/yyyy HH:mm z";
    public static final String DATE_FORMAT_NO_ZONE = "dd/MMM/yyyy HH:mm";
    public static final String KEY_DATE_FORMAT = "formattedDate";

    private SwipeMenuListView mListView;
    private SimpleAdapter mSimpleAdapter;
    //List of HashMaps representing the list of profile medicines to be displayed
    private List<HashMap<String, String>> mMedicineMapList = new ArrayList<>();

    /**
     * Overrides the Base class Activity and provides activity specific actions
     *
     * @see BaseAppCompatActivity#onCreate(Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListView = (SwipeMenuListView) findViewById(R.id.profile_list_view);
        mListView.setOnItemClickListener(this);
        // Perform bounce animation when closing menu
        mListView.setCloseInterpolator(new BounceInterpolator());

        contentResolver = getApplicationContext().getContentResolver();

        // Get current user details based on user ID
        String itemId = AWSProvider.getInstance().getIdentityManager().getCachedUserID();
        mItemUri = UserDetailsContentContract.UserDetails.uriBuilder(itemId);

        // Initialise and populate the Swipe Menu Creator
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // Swipe "Conflicts" item
                SwipeMenuItem moreInfoItem = new SwipeMenuItem(
                        getApplicationContext());
                // Set BG colour
                moreInfoItem.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.mediBlue)));
                moreInfoItem.setWidth(170);
                moreInfoItem.setTitle(getString(R.string.conflicts));
                moreInfoItem.setTitleSize(18);
                moreInfoItem.setTitleColor(Color.WHITE);
                // Add item to the menu
                menu.addMenuItem(moreInfoItem);
                // Create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // Set BG colour
                deleteItem.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.delete_red)));
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
                        showConflicts(mMedicineMapList, position);
                        break;
                    case 1:
                        final String medicineToDelete = mMedicineMapList.get(position).get(MedicineListActivity.KEY_NAME);
                        // Confirm if the user really wants to delete the medicine
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        mMedicineMapList.remove(position);
                                        mSimpleAdapter.notifyDataSetChanged();
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
                        builder.setMessage(getString(R.string.delete_medicine, medicineToDelete))
                                .setPositiveButton(getString(R.string.yes), dialogClickListener)
                                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                        break;
                }
                // Return false - close the menu
                return false;
            }
        });

    }

    // Return activity specific resources to the base class
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_profile_medicine_list;
    }

    @Override
    protected int getMenuItemResourceId() {
        return R.id.nav_profile_medicines;
    }

    @Override
    protected int getMenuResourceId() {
        return R.menu.bar_menu_search;
    }

    @Override
    protected String getHelpTitle() {
        return getString(R.string.bar_profile_medicines_help_title);
    }

    @Override
    protected String getHelpMessage() {
        return getString(R.string.bar_profile_medicines_help_message);
    }

    /**
     * Update the profile if a delete has been specified
     *
     * @param medicineToRemove The medicine to be removed from the profile
     */
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
                MediApp.customToast("Deleted", MediApp.KEY_POSITIVE);
            }
        };
        queryHandler.startUpdate(UPDATE_TOKEN, null, mItemUri, values, null, null);
    }

    /**
     * Populate the user object with their latest changes from the DynamoDB table
     */
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
        queryHandler.startQuery(QUERY_TOKEN, null, mItemUri, UserDetailsContentContract.UserDetails.PROJECTION_ALL, null, null, null);
    }

    /**
     * Populate and load the list displaying user medicines
     *
     * @param sortType The type of sort specified by the user, passed in to the sort method in the Medicine class
     * @see Medicine#sort(List, String)
     */
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

    /**
     * Determine whether or not the list is empty, if so display a relevant message
     */
    private void determineEmptyList(List<Medicine> list) {
        TextView noMedicines = (TextView) findViewById(R.id.no_medicines);
        if (list.size() <= 0) {
            noMedicines.setVisibility(View.VISIBLE);
        } else {
            noMedicines.setVisibility(View.GONE);
        }
    }

    /**
     * Load (or reload) the list of medicines
     */
    private void loadListView() {
        Resources res = getResources();
        String text = String.format(res.getString(R.string.date_added_alt), MedicineListActivity.KEY_ONSETACTION);

        mSimpleAdapter = new SimpleAdapter(
                ProfileMedicineListActivity.this,
                mMedicineMapList,
                R.layout.list_item_date,
                new String[]{MedicineListActivity.KEY_NAME, MedicineListActivity.KEY_TYPE, KEY_DATE_FORMAT},
                new int[]{R.id.name, R.id.type, R.id.date});
        mListView.setAdapter(mSimpleAdapter);
    }

    /**
     * Show medicine conflicts on list item click
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        showConflicts(mMedicineMapList, i);
    }


    /**
     * Determine whether (or not) there are any potential conflicts between the currently added
     * medicines in the profile
     *
     * @param medicines The currently added medicines
     * @param position  The position indicating which medicine the user has tapped on
     */
    private void showConflicts(List<HashMap<String, String>> medicines, int position) {
        String currentMedicine = medicines.get(position).get(MedicineListActivity.KEY_NAME);
        AlertDialog alertDialog = new AlertDialog.Builder(ProfileMedicineListActivity.this).create();
        alertDialog.setTitle(currentMedicine);
        StringBuilder activeConflicts = new StringBuilder("");

        for (HashMap<String, String> hashMap : medicines) {
            for (Map.Entry<String, String> entry : hashMap.entrySet()) {
                // If the selected medicine is contained in the conflicts of another medicine in the profile,
                // we determine that a conflict has occurred
                if (entry.getKey().equals(MedicineListActivity.KEY_CONFLICT) && entry.getValue().contains(currentMedicine)) {
                    activeConflicts.append(hashMap.get(MedicineListActivity.KEY_NAME)).append(" ");
                }
            }

        }
        if (!activeConflicts.toString().equals("")) {
            alertDialog.setMessage("Potential Conflict: " + activeConflicts.toString().trim().replace(" ", ", "));
        } else {
            alertDialog.setMessage("No Conflicts Detected");
        }

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    /**
     * Allow user to sort list by name, date added (most recent) and type, overriding base class
     * version
     */
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
            case R.id.action_help:
                MediApp.displayDialog(this,
                        getString(R.string.bar_profile_medicines_help_title),
                        getString(R.string.bar_profile_medicines_help_message));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sync the user and reload the list on Activity resume
     */
    @Override
    public void onResume() {
        super.onResume();
        mNavigationView.setCheckedItem(R.id.nav_profile_medicines);
        // Refresh the list on activity resume with latest details
        if (mSimpleAdapter != null) {
            mSimpleAdapter.notifyDataSetChanged();
        }
        mMedicineMapList.clear();
        syncUser();
    }
}

