package jadomican.a4thyearproject;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import jadomican.a4thyearproject.data.UserDetail;
import jadomican.a4thyearproject.data.UserDetailsContentContract;

public class MedicineDetailsActivity extends AppCompatActivity {

    private UserDetail mItem;
    private Uri itemUri;

    private ContentResolver contentResolver;


    private static final int QUERY_TOKEN = 1001;
    private static final int UPDATE_TOKEN = 1002;

    Map<String, String> medicineMap = new HashMap<String, String>();


    Handler timer = new Handler();
    Runnable timerTask = new Runnable() {
        @Override
        public void run() {
            saveData();                             // Save the data
            timer.postDelayed(timerTask, 5000);     // Every 5 seconds
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_details);

        contentResolver = getApplicationContext().getContentResolver();

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey(UserDetailFragment.ARG_ITEM_ID)) {
                String itemId = extras.getString(UserDetailFragment.ARG_ITEM_ID);
                itemUri = UserDetailsContentContract.UserDetails.uriBuilder(itemId);

                medicineMap = (HashMap<String, String>)extras.getSerializable("medicineMap");


                // Replace local cursor methods with async query handling
                AsyncQueryHandler queryHandler = new AsyncQueryHandler(contentResolver) {
                    @Override
                    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                        super.onQueryComplete(token, cookie, cursor);
                        cursor.moveToFirst();
                        mItem = UserDetail.fromCursor(cursor);

                    }
                };
                queryHandler.startQuery(QUERY_TOKEN, null, itemUri, UserDetailsContentContract.UserDetails.PROJECTION_ALL, null, null, null);

            }
        }
        // Start the timer for the delayed start
        timer.postDelayed(timerTask, 5000);

    }

    private void saveData() {
        // Save the edited text back to the item.
        boolean isUpdated = false;
        if (!mItem.getAddedMedicines().equals(medicineMap)) {
            mItem.setAddedMedicines(medicineMap);
            isUpdated = true;
        }

        if (isUpdated == true) {

            // Convert to ContentValues and store in the database.
            ContentValues values = mItem.toContentValues();

            AsyncQueryHandler queryHandler = new AsyncQueryHandler(contentResolver) {
                @Override
                protected void onInsertComplete(int token, Object cookie, Uri uri) {
                    super.onInsertComplete(token, cookie, uri);
                    Log.d("MedicineDetailsActivity", "insert completed");
                }

                @Override
                protected void onUpdateComplete(int token, Object cookie, int result) {
                    super.onUpdateComplete(token, cookie, result);
                    Log.d("MedicineDetailsActivity", "update completed");
                }
            };

            queryHandler.startUpdate(UPDATE_TOKEN, null, itemUri, values, null, null);
        }
    }

}
