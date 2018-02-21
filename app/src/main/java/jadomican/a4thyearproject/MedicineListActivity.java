package jadomican.a4thyearproject;

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

import jadomican.a4thyearproject.data.Medicine;

/**
 * An activity to represent the medicine search/ list screen.
 */

public class MedicineListActivity extends AppCompatActivity
        implements LoadJSONTask.Listener, AdapterView.OnItemClickListener {

    private ListView mListView;

    //The url from which to fetch the JSON result
    public static final String URL = "http://s3-eu-west-1.amazonaws.com/projectmedicalapp/medicine.json";

    private List<HashMap<String, String>> mMedicineMapList = new ArrayList<>();

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";
    private static final String KEY_ONSETACTION = "onsetaction";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_list);

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
        new LoadJSONTask(this).execute(URL);
    }

    //The onLoaded() method is called when the request is successful
    @Override
    public void onLoaded(List<Medicine> medicineList) {

        for (Medicine medicine : medicineList) {
            HashMap<String, String> map = new HashMap<>();
            map.put(KEY_NAME, medicine.getMedicineName());
            map.put(KEY_TYPE, medicine.getMedicineType());
            map.put(KEY_ONSETACTION, medicine.getMedicineOnsetAction());

            // For each medicine, add to list
            mMedicineMapList.add(map);
        }

        Toast.makeText(this, "Found " + medicineList.size() + " results", Toast.LENGTH_SHORT).show();
        loadListView();
    }

    @Override
    public void onError() {
        Toast.makeText(this, "There has been an error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Toast.makeText(this,
                mMedicineMapList.get(i).get(KEY_NAME), Toast.LENGTH_LONG).show();
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