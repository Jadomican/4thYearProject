package jadomican.a4thyearproject;

/*
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 */

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.GravityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import jadomican.a4thyearproject.data.Medicine;

/**
 * An activity to represent the medicine search/ list screen. This Activity is listed in the manifest
 * as Searchable, meaning that search terms can be delivered here via calling onSearchRequested()
 */

public class MedicineListActivity extends BaseAppCompatActivity
        implements LoadJSONTask.Listener, AdapterView.OnItemClickListener {

    private ListView mListView;
    private SimpleAdapter mSimpleAdapter;
    private String mSearchQery;

    /**
     * The url from which to invoke the API and fetch the JSON result
     */
    private final String URL = "https://xjahc9ekrl.execute-api.eu-west-1.amazonaws.com/dev/medicines";

    /**
     * List of HashMaps to represent the list of medicines to be displayed by the SimpleAdapter
     */
    private List<HashMap<String, String>> mMedicineMapList = new ArrayList<>();

    /**
     * List of medicine objects to model the API query response
     */
    private List<Medicine> mMedicinesResponse;

    // Keys to identify various fields related to a particular medicine
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_ONSETACTION = "onsetaction";
    public static final String KEY_IMAGEURL = "imageurl";
    public static final String KEY_CONFLICT = "conflict";
    public static final String KEY_QUERY = "query";
    public static final String KEY_DATE = "date";

    /**
     * Overrides the BaseAppCompatActivity onCreate method with Activity-specific tasks
     *
     * @see BaseAppCompatActivity#onCreate(Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
        getIntentAndLoad();
    }


    // Return unique Activity resources to the super class for initialisation
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_medicine_list;
    }

    // No menu item for search results page
    @Override
    protected int getMenuItemResourceId() {
        return -1;
    }

    @Override
    protected int getMenuResourceId() {
        return R.menu.bar_menu_search_no_date;
    }

    @Override
    protected String getHelpTitle() {
        return getString(R.string.bar_medicine_search_help_title);
    }

    @Override
    protected String getHelpMessage() {
        return getString(R.string.bar_medicine_search_help_message);
    }

    /**
     * Extract Intent extras to determine where the search query came from - either the search bar
     * at top top of every screen or the Camera Search. Then manipulate the API query URL request
     * base on this search term
     */
    private void getIntentAndLoad() {
        // Get the intent, verify that a search occurred
        String queryURL = "";
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mSearchQery = intent.getStringExtra(SearchManager.QUERY).trim();
            // Append the query to the URL
            queryURL = (URL + "/" + mSearchQery.toLowerCase());
            Log.d("Search", "SEARCHED:" + URL);
        } else if (intent.getStringExtra(MedicineListActivity.KEY_QUERY) != null) {
            mSearchQery = intent.getStringExtra(MedicineListActivity.KEY_QUERY);
            queryURL = (URL + "/" + mSearchQery.toLowerCase());
        }
        // Invoke API and return results
        new LoadJSONTask(this, this).execute(queryURL);
    }

    /**
     * Allow user to re-search within search results page
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (mSimpleAdapter != null) {
            mSimpleAdapter.notifyDataSetChanged();
        }
        getIntentAndLoad();
    }

    /**
     * Additional options to allow user to sort list of medicines if they choose
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
                populateAndLoadList(ProfileMedicineListActivity.SORT_NAME);
                return true;
            case R.id.action_sort_type:
                populateAndLoadList(ProfileMedicineListActivity.SORT_TYPE);
                return true;
            case R.id.action_help:
                MediApp.displayDialog(this,
                        getString(R.string.bar_medicine_search_help_title),
                        getString(R.string.bar_medicine_search_help_message));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * onLoaded method called when the search request is successful. Alerts the user of the search
     * results and kicks off the list population
     *
     * @param medicineList the modelled response returned by the API
     */
    @Override
    public void onLoaded(List<Medicine> medicineList) {
        TextView noResults = (TextView) findViewById(R.id.no_results);
        if (medicineList.size() > 0) {
            MediApp.customToast("Found " + medicineList.size() + " results", MediApp.KEY_POSITIVE);
            noResults.setVisibility(View.GONE);
        } else {
            noResults.setVisibility(View.VISIBLE);
            noResults.setText(getString(R.string.no_results, mSearchQery));
        }

        mMedicinesResponse = medicineList;
        populateAndLoadList(ProfileMedicineListActivity.SORT_NAME);
    }

    /**
     * Populates and loads the list displaying medicines
     *
     * @param sortType specifies a sort tag that can be specified by the user. Allows sorting by name or type
     */
    private void populateAndLoadList(String sortType) {
        mMedicineMapList.clear();
        try {
            mMedicinesResponse = (Medicine.sort(mMedicinesResponse, sortType));
        } catch (ParseException e) {
            Log.d("ProfileMedicineListAct", "A Parse Exception has occurred: " + e.getMessage());
        }

        for (Medicine medicine : mMedicinesResponse) {
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

    /**
     * Called if there is an error when trying to query the API. For example, this could arise from
     * a malformed URL which is not accepted by the API
     */
    @Override
    public void onError() {
        MediApp.customToast("No medicines found", MediApp.KEY_NEGATIVE);
    }

    /**
     * Handle user's clicking on a list item. Pass the selected Medicine's information into a Bundle
     * to be handled in {@link MedicineDetailsActivity MedicineDetailsActivity}
     */
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

    /**
     * Initialise the adapter which lists the medicines on the screen. Called whenever there is a
     * change to the list, such as sorting
     */
    private void loadListView() {
        mSimpleAdapter = new SimpleAdapter(
                MedicineListActivity.this,
                mMedicineMapList,
                R.layout.list_item,
                new String[]{KEY_NAME, KEY_TYPE},
                new int[]{R.id.name, R.id.type});
        mListView.setAdapter(mSimpleAdapter);
    }
}