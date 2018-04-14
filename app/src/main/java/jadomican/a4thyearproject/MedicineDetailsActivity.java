package jadomican.a4thyearproject;

/*
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 */

import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import jadomican.a4thyearproject.data.Medicine;
import jadomican.a4thyearproject.data.UserDetail;
import jadomican.a4thyearproject.data.UserDetailsContentContract;

/**
 * Activity representing a medicine information page. From here a user can add a Medicine to their
 * profile
 */
public class MedicineDetailsActivity extends BaseAppCompatActivity {

    private UserDetail mItem;
    private Uri itemUri;
    private ContentResolver contentResolver;

    private static final int QUERY_TOKEN = 1001;
    private static final int UPDATE_TOKEN = 1002;
    // The timezone stored in the database is always the same, regardless of where the user of the
    // app is located. This helps prevent ParseException occurring due to timezone differences
    public static final String COMMON_TIMEZONE = "UTC";

    // A TextToSpeech engine for speaking a String value.
    private TextToSpeech tts;

    // The medicine this page is representing
    private Medicine medicine;
    private TextView mMedicineNameDisplay;
    private Button mAddMedicineButton;
    private ProgressDialog mProgressDialog;

    /**
     * Override the base class onCreate method with Activity specific code
     *
     * @see BaseAppCompatActivity#onCreate(Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = new ProgressDialog(this);
        contentResolver = getApplicationContext().getContentResolver();

        // Extract the user ID, used in creating the URI for database queries
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(UserDetailFragment.ARG_ITEM_ID)) {
            String itemId = AWSProvider.getInstance().getIdentityManager().getCachedUserID();
            itemUri = UserDetailsContentContract.UserDetails.uriBuilder(itemId);

            //Populate the Medicine object based on the choice made by the user from the list
            medicine = new Medicine(extras.getString(
                    MedicineListActivity.KEY_ID),
                    extras.getString(MedicineListActivity.KEY_NAME),
                    extras.getString(MedicineListActivity.KEY_TYPE),
                    extras.getString(MedicineListActivity.KEY_ONSETACTION),
                    extras.getString(MedicineListActivity.KEY_IMAGEURL),
                    extras.getString(MedicineListActivity.KEY_CONFLICT)
            );
            actionBar.setTitle(getString(R.string.medicine_name, medicine.getMedicineName()));
            syncUser();
            setImage();
        }

        mAddMedicineButton = (Button) findViewById(R.id.addMedicineButton);
        mAddMedicineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData(v);
            }
        });
        mMedicineNameDisplay = (TextView) findViewById(R.id.medicineNameDisplay);

        setTouchListener(mMedicineNameDisplay, medicine.getMedicineName());

        // Set up the Text to Speech listener
        TextToSpeech.OnInitListener listener =
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Log.d("TTS", "Text to speech engine started successfully.");
                            tts.setLanguage(Locale.getDefault());
                        } else {
                            Log.d("TTS", "Error starting the text to speech engine.");
                        }
                    }
                };
        tts = new TextToSpeech(this.getApplicationContext(), listener);
    }

    // Pass Activity-specific resources to the base class
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_medicine_details;
    }

    // No menu item for medicine search detail page
    @Override
    protected int getMenuItemResourceId() {
        return -1;
    }

    @Override
    protected int getMenuResourceId() {
        return R.menu.bar_menu_back;
    }

    @Override
    protected String getHelpTitle() {
        return getString(R.string.bar_medicine_info_help_title, medicine.getMedicineName());
    }

    @Override
    protected String getHelpMessage() {
        return getString(R.string.bar_medicine_info_help_message, medicine.getMedicineName());
    }

    /**
     * Download and show the image content associated with the medicine
     */
    private class ImageDownload extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public ImageDownload(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog.setMessage(getResources().getString(R.string.loading));
            mProgressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... url) {
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(url[0]).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("MedicineDetailsActivity", e.getMessage());
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mAddMedicineButton.setVisibility(View.VISIBLE);
            mMedicineNameDisplay.setVisibility(View.VISIBLE);
            bmImage.setImageBitmap(result);
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    /**
     * Set up the ImageView to display the downloaded image
     */
    private void setImage() {
        final ImageView imageView = (ImageView) findViewById(R.id.imageDisplay);
        ImageDownload imgDownload = new ImageDownload(imageView);
        imgDownload.execute(medicine.getMedicineImageUrl());
    }

    /**
     * Set up text and button content. Inform user if there are any potential conflicts between the
     * medicine in view and those in the user's profile
     */
    private void setAssets() {
        mMedicineNameDisplay.setText(medicine.getMedicineName());
        String buttonText = getString(R.string.button_add_medicine);
        boolean alreadyAdded = false;
        for (Medicine addedMedicine : mItem.getAddedMedicines()) {

            // If the medicine already exists, inform the user
            if (addedMedicine.getMedicineName().equals(medicine.getMedicineName())) {
                buttonText = getString(R.string.medicine_added, medicine.getMedicineName());
                //getColor is deprecated as of API 23, use ContextCompat instead
                mAddMedicineButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.mediBlue));
                alreadyAdded = true;
            } else if (addedMedicine.getMedicineConflict().contains(medicine.getMedicineName()) && !alreadyAdded) {
                buttonText = getString(R.string.button_medicine_conflict);
                //getColor is deprecated as of API 23, use ContextCompat instead
                mAddMedicineButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.danger));
            }
        }
        mAddMedicineButton.setText(buttonText);
    }

    /**
     * Save the new UserDetail object to the database with the new medicine stored
     */
    public void saveData(View view) {
        // Save the edited medicines back to the profile.
        boolean isUpdated = true;
        List<Medicine> updatedList = new ArrayList<>(mItem.getAddedMedicines());

        //Check if the medicine already exists in the user's profile
        for (Medicine addedMedicine : updatedList) {

            if (addedMedicine.getMedicineName().equals(medicine.getMedicineName())) {
                MediApp.customToast(addedMedicine.getMedicineName() + " has already been added!", MediApp.KEY_NEGATIVE);
                isUpdated = false;
            }
        }

        if (isUpdated) {
            // Set the date that the medicine is added
            SimpleDateFormat df = new SimpleDateFormat(ProfileMedicineListActivity.DATE_FORMAT);
            df.setTimeZone(TimeZone.getTimeZone(COMMON_TIMEZONE));

            Date now = Calendar.getInstance().getTime();
            medicine.setMedicineDate(df.format(now));
            updatedList.add(medicine);

            mItem.setAddedMedicines(updatedList);
            // Convert to ContentValues and store in the database.
            ContentValues values = mItem.toContentValues();

            AsyncQueryHandler queryHandler = new AsyncQueryHandler(contentResolver) {
                @Override
                protected void onInsertComplete(int token, Object cookie, Uri uri) {
                    super.onInsertComplete(token, cookie, uri);
                    MediApp.customToast("Medicine Added", MediApp.KEY_POSITIVE);
                    Log.d("MedicineDetailsActivity", "insert completed");
                }

                @Override
                protected void onUpdateComplete(int token, Object cookie, int result) {
                    super.onUpdateComplete(token, cookie, result);
                    MediApp.customToast("Medicine Added", MediApp.KEY_POSITIVE);
                    Log.d("MedicineDetailsActivity", "update completed");
                }
            };
            queryHandler.startUpdate(UPDATE_TOKEN, null, itemUri, values, null, null);
        }
        setAssets();
    }

    /**
     * Sync user on resume of Activity to maintain consistency with the backend database
     */
    @Override
    public void onResume() {
        super.onResume();
        syncUser();
    }

    /**
     * Populate the user object with their latest changes from the DynamoDB table
     */
    private void syncUser() {
        contentResolver = getApplicationContext().getContentResolver();
        // Asynchronously query the backend database
        AsyncQueryHandler queryHandler = new AsyncQueryHandler(contentResolver) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                super.onQueryComplete(token, cookie, cursor);

                cursor.moveToFirst();
                mItem = UserDetail.fromCursor(cursor);
                setAssets();
            }
        };
        queryHandler.startQuery(QUERY_TOKEN, null, itemUri, UserDetailsContentContract.UserDetails.PROJECTION_ALL, null, null, null);
    }


    /**
     * Set a listener for the text-to-speech icon
     */
    public void setTouchListener(final TextView textView, final String wordsToSay) {
        // noinspection AndroidLintClickableViewAccessibility
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // DrawableRight is index value 2 for the getCompoundDrawables method
                final int DRAWABLE_RIGHT = 2;

                // If user presses down (taps) on icon
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (event.getRawX() >= (mMedicineNameDisplay.getRight() - mMedicineNameDisplay.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (wordsToSay != null) {
                            tts.speak(wordsToSay, TextToSpeech.QUEUE_ADD, null, "DEFAULT");
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * On the medicine details screen the back button returns to the search results, rather than opening the navigation drawer
     */
    @Override
    public void onBackPressed() {
        super.onBackDeviceButtonPressed();
    }

}
