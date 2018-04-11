package jadomican.a4thyearproject;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBar;
import android.widget.TextView;

import java.text.ParseException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import jadomican.a4thyearproject.data.Medicine;

/**
 * An activity to represent the medicine search/ list screen.
 */

public class MedicineListActivity extends AppCompatActivity
        implements LoadJSONTask.Listener, AdapterView.OnItemClickListener {

    private ListView mListView;
    SimpleAdapter adapter;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private String query;
    //The url from which to invoke the API and fetch the JSON result
    public String URL = "https://xjahc9ekrl.execute-api.eu-west-1.amazonaws.com/dev/medicines";

    //List of HashMaps to represent the list of medicines to be displayed
    private List<HashMap<String, String>> mMedicineMapList = new ArrayList<>();
    private List<Medicine> medicinesResponse;

    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_ONSETACTION = "onsetaction";
    public static final String KEY_IMAGEURL = "imageurl";
    public static final String KEY_CONFLICT = "conflict";
    public static final String KEY_QUERY = "query";
    public static final String KEY_DATE = "date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_list);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        MediApp.setNavigationListener(navigationView, mDrawerLayout, -1, this);

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
        getIntentAndLoad();
    }

    private void getIntentAndLoad() {
        // Get the intent, verify that a search occurred
        String queryURL = "";
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY).trim();
            // Append the query to the URL
            queryURL = (URL + "/" + query.toLowerCase());
            Log.d("Search", "SEARCHED:" + URL);
        } else if (intent.getStringExtra(MedicineListActivity.KEY_QUERY) != null) {
            query = intent.getStringExtra(MedicineListActivity.KEY_QUERY);
            queryURL = (URL + "/" + query.toLowerCase());
        }
        // Invoke API and return results
        new LoadJSONTask(this, this).execute(queryURL);
    }

    // Allow user to re-search within search results page
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        getIntentAndLoad();
    }


    // Allow user to sort list of medicines if they choose
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
                populateAndLoadList(ProfileMedicineListActivity.SORT_NAME);
                return true;
            case R.id.action_sort_type:
                populateAndLoadList(ProfileMedicineListActivity.SORT_TYPE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //The onLoaded() method is called when the request is successful
    @Override
    public void onLoaded(List<Medicine> medicineList) {
        TextView noResults = (TextView) findViewById(R.id.no_results);
        if (medicineList.size() > 0) {
            MediApp.customToast("Found " + medicineList.size() + " results", MediApp.KEY_POSITIVE);
            noResults.setVisibility(View.GONE);
        }
        else
        {
            noResults.setVisibility(View.VISIBLE);
            noResults.setText(getString(R.string.no_results, query));
        }

        medicinesResponse = medicineList;
        populateAndLoadList(ProfileMedicineListActivity.SORT_NAME);
    }

    // Populate and load the list displaying medicines
    private void populateAndLoadList(String sortType) {
        mMedicineMapList.clear();
        try {
            medicinesResponse = (Medicine.sort(medicinesResponse, sortType));
        } catch (ParseException e) {
            Log.d("ProfileMedicineListAct", "A Parse Exception has occurred: " + e.getMessage());
        }

        for (Medicine medicine : medicinesResponse) {
            HashMap<String, String> map = new HashMap<>();
            map.put(MedicineListActivity.KEY_ID, medicine.getMedicineId());
            map.put(MedicineListActivity.KEY_NAME, medicine.getMedicineName());
            map.put(MedicineListActivity.KEY_TYPE, medicine.getMedicineType());
            map.put(MedicineListActivity.KEY_ONSETACTION, medicine.getMedicineOnsetAction());
            map.put(MedicineListActivity.KEY_IMAGEURL, medicine.getMedicineImageUrl());
            map.put(MedicineListActivity.KEY_CONFLICT, medicine.getMedicineConflict());
            //For each medicine, add to list
            mMedicineMapList.add(map);
        }
        loadListView();
    }

    // Called if there is an error when trying to query the API. For example, this could arise from
    // a malformed URL which is not accepted by the API
    @Override
    public void onError() {
        MediApp.customToast("No medicines found", MediApp.KEY_NEGATIVE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Bundle arguments = new Bundle();

        arguments.putString(UserDetailFragment.ARG_ITEM_ID, AWSProvider.getInstance().getIdentityManager().getCachedUserID());

        arguments.putString(KEY_NAME, mMedicineMapList.get(i).get(KEY_NAME));
        arguments.putString(KEY_TYPE, mMedicineMapList.get(i).get(KEY_TYPE));
        arguments.putString(KEY_ONSETACTION, mMedicineMapList.get(i).get(KEY_ONSETACTION));
        arguments.putString(KEY_ID, mMedicineMapList.get(i).get(KEY_ID));
        arguments.putString(KEY_IMAGEURL, mMedicineMapList.get(i).get(KEY_IMAGEURL));
        arguments.putString(KEY_CONFLICT, mMedicineMapList.get(i).get(KEY_CONFLICT));

        Context context = view.getContext();
        Intent intent = new Intent(context, MedicineDetailsActivity.class);
        intent.putExtras(arguments);
        context.startActivity(intent);
    }

    private void loadListView() {

        //The adapter which lists the medicines on the screen
        adapter = new SimpleAdapter(
                MedicineListActivity.this,
                mMedicineMapList,
                R.layout.list_item,
                new String[]{KEY_NAME, KEY_TYPE},
                new int[]{R.id.name, R.id.type});
        mListView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
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
        inflater.inflate(R.menu.bar_menu_search_no_date, menu);
        return super.onCreateOptionsMenu(menu);
    }

}