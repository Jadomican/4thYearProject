package jadomican.a4thyearproject;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jadomican.a4thyearproject.data.Medicine;

/**
 * An activity to represent the medicine search/ list screen.
 */

public class MedicineListActivity extends AppCompatActivity
        implements LoadJSONTask.Listener, AdapterView.OnItemClickListener {

    private ListView mListView;

    //The url from which to fetch the JSON result
    public static final String URL = "https://xjahc9ekrl.execute-api.eu-west-1.amazonaws.com/dev/medicines";

    private List<HashMap<String, String>> mMedicineMapList = new ArrayList<>();
    private HashMap<String, String> mapParam = new HashMap<String, String>();

    public static final String KEY_MEDICINEMAP = "medicineMap";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_ONSETACTION = "onsetaction";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_list);

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
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

            //For each medicine, add to list
            mMedicineMapList.add(map);
        }

        Toast.makeText(this, "Found " + medicineList.size() + " results", Toast.LENGTH_SHORT).show();
        loadListView();
    }

    @Override
    public void onError() {
        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Toast.makeText(this, mMedicineMapList.get(i).get(KEY_NAME), Toast.LENGTH_LONG).show();

        Bundle arguments = new Bundle();

        arguments.putString(UserDetailFragment.ARG_ITEM_ID, AWSProvider.getInstance().getIdentityManager().getCachedUserID());

        mapParam.put(KEY_NAME, mMedicineMapList.get(i).get(KEY_NAME));
        mapParam.put(KEY_TYPE, mMedicineMapList.get(i).get(KEY_TYPE));
        mapParam.put(KEY_ONSETACTION, mMedicineMapList.get(i).get(KEY_ONSETACTION));
        mapParam.put(KEY_ID, mMedicineMapList.get(i).get(KEY_ID));

        arguments.putSerializable(KEY_MEDICINEMAP, mapParam);

        Context context = view.getContext();
        Intent intent = new Intent(context, MedicineDetailsActivity.class);
        intent.putExtras(arguments);
        context.startActivity(intent);
    }

    private void loadListView() {

        ListAdapter adapter = new SimpleAdapter(
                MedicineListActivity.this,
                mMedicineMapList,
                R.layout.list_item,
                new String[]{KEY_NAME, KEY_TYPE, KEY_ONSETACTION},
                new int[]{R.id.name, R.id.type, R.id.onsetaction});

        mListView.setAdapter(adapter);
    }

}