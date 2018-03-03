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
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jadomican.a4thyearproject.data.Medicine;
import jadomican.a4thyearproject.data.UserDetail;
import jadomican.a4thyearproject.data.UserDetailsContentContract;

public class MedicineDetailsActivity extends AppCompatActivity {

    private UserDetail mItem;
    private Uri itemUri;

    private ContentResolver contentResolver;

    private static final int QUERY_TOKEN = 1001;
    private static final int UPDATE_TOKEN = 1002;

    Medicine medicine = new Medicine();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_details);

        contentResolver = getApplicationContext().getContentResolver();

            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey(UserDetailFragment.ARG_ITEM_ID)) {
                String itemId = AWSProvider.getInstance().getIdentityManager().getCachedUserID();
                itemUri = UserDetailsContentContract.UserDetails.uriBuilder(itemId);

                //Populate the Medicine object based on the choice made by the user from the list
                medicine.setMedicineName(extras.getString(MedicineListActivity.KEY_NAME));
                medicine.setMedicineType(extras.getString(MedicineListActivity.KEY_TYPE));
                medicine.setMedicineOnsetAction(extras.getString(MedicineListActivity.KEY_ONSETACTION));
                medicine.setMedicineId(extras.getString(MedicineListActivity.KEY_ID));
                syncUser();
            }
    }

    public void saveData(View view) {
        // Save the edited medicines back to the profile.
        boolean isUpdated = true;
        List<Medicine> updatedList = new ArrayList<>(mItem.getAddedMedicines());

        //Check if the medicine already exists in the user's profile
        for (Medicine addedMedicine : updatedList) {

            if (addedMedicine.getMedicineName().equals(medicine.getMedicineName()))
            {
                Toast.makeText(this, addedMedicine.getMedicineName() + " has already been added!", Toast.LENGTH_SHORT).show();
                isUpdated = false;
            }
        }

        if (isUpdated) {
            updatedList.add(medicine);

            mItem.setAddedMedicines(updatedList);
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


    @Override
    public void onResume() {
        super.onResume();
        syncUser();
    }

    //Populate the user object with their latest changes from the DynamoDB table
    private void syncUser()
    {
        contentResolver = getApplicationContext().getContentResolver();
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
