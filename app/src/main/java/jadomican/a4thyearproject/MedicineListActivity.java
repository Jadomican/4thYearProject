package jadomican.a4thyearproject;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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

    //The url from which to invoke the API and fetch the JSON result
    public String URL = "https://xjahc9ekrl.execute-api.eu-west-1.amazonaws.com/dev/medicines";

    //List of HashMaps to represent the list of medicines to be displayed
    private List<HashMap<String, String>> mMedicineMapList = new ArrayList<>();

    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_ONSETACTION = "onsetaction";
    public static final String KEY_IMAGEURL = "imageurl";
    public static final String KEY_CONFLICT = "conflict";
    public static final String KEY_QUERY = "query";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_list);

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);

        // Get the intent, verify that a search occurred
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // Append the query to the URL
            URL += "/" + intent.getStringExtra(SearchManager.QUERY);
            Log.d("Search", "SEARCHED:" + URL);
        }
        else if (intent.getStringExtra(MedicineListActivity.KEY_QUERY) != null) {
            URL += "/" + intent.getStringExtra(MedicineListActivity.KEY_QUERY);
        }




        // Invoke API and return results
        new LoadJSONTask(this,this).execute(URL);
    }

    //The onLoaded() method is called when the request is successful
    @Override
    public void onLoaded(List<Medicine> medicineList) {

        for (Medicine medicine : medicineList) {
            HashMap<String, String> map = new HashMap<>();
            map.put(KEY_ID, medicine.getMedicineId());
            map.put(KEY_NAME, medicine.getMedicineName());
            map.put(KEY_TYPE, medicine.getMedicineType());
            map.put(KEY_ONSETACTION, medicine.getMedicineOnsetAction());
            map.put(KEY_IMAGEURL, medicine.getMedicineImageUrl());
            map.put(KEY_CONFLICT, medicine.getMedicineConflict());

            //For each medicine, add to list
            mMedicineMapList.add(map);
        }

        MediApp.customToast("Found " + medicineList.size() + " results");
        loadListView();
    }

    @Override
    public void onError() {
        MediApp.customToast("Something went wrong");
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
        ListAdapter adapter = new SimpleAdapter(
                MedicineListActivity.this,
                mMedicineMapList,
                R.layout.list_item,
                new String[]{KEY_NAME, KEY_TYPE, KEY_ONSETACTION},
                new int[]{R.id.name, R.id.type, R.id.onsetaction});
        mListView.setAdapter(adapter);
    }

}