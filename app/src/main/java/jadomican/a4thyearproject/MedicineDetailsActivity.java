package jadomican.a4thyearproject;

import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jadomican.a4thyearproject.data.Medicine;
import jadomican.a4thyearproject.data.UserDetail;
import jadomican.a4thyearproject.data.UserDetailsContentContract;

public class MedicineDetailsActivity extends AppCompatActivity {

    private UserDetail mItem;
    private Uri itemUri;

    private ContentResolver contentResolver;

    private static final int QUERY_TOKEN = 1001;
    private static final int UPDATE_TOKEN = 1002;

    // A TextToSpeech engine for speaking a String value.
    private TextToSpeech tts;

    Medicine medicine = new Medicine();

    TextView medicineNameDisplay;
    Button addMedicineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_details);

        addMedicineButton = (Button) findViewById(R.id.addMedicineButton);
        addMedicineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData(v);
            }
        });
        medicineNameDisplay = (TextView) findViewById(R.id.medicineNameDisplay);

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
            medicine.setMedicineImageUrl(extras.getString(MedicineListActivity.KEY_IMAGEURL));
            medicine.setMedicineConflict(extras.getString(MedicineListActivity.KEY_CONFLICT));
            syncUser();
            setImage();
        }

        setTouchListener(medicineNameDisplay, medicine.getMedicineName());

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

    // Download the image belonging to the medicine
    private class ImageDownload extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public ImageDownload(ImageView bmImage) {
            this.bmImage = bmImage;
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
            bmImage.setImageBitmap(result);
        }
    }

    private void setImage() {
        final ImageView imageView = (ImageView) findViewById(R.id.imageDisplay);
        ImageDownload imgDownload = new ImageDownload(imageView);
        imgDownload.execute(medicine.getMedicineImageUrl());
    }

    // Alert user of medicine conflict
    private void setAssets() {
        medicineNameDisplay.setText(medicine.getMedicineName());
        String buttonText = getString(R.string.button_add_medicine);
        for (Medicine addedMedicine : mItem.getAddedMedicines()) {
            if (addedMedicine.getMedicineConflict().equals(medicine.getMedicineName()))
            {
                buttonText = getString(R.string.button_medicine_conflict);
                //getColor is deprecated as of API 23, use ContextCompat instead
                addMedicineButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.danger));
                break;
            }
        }
        addMedicineButton.setText(buttonText);
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
                setAssets();
            }
        };
        queryHandler.startQuery(QUERY_TOKEN, null, itemUri, UserDetailsContentContract.UserDetails.PROJECTION_ALL, null, null, null);
    }

    // Set a listener for the text-to-speech icon
    public void setTouchListener(final TextView textView, final String wordsToSay) {
        // noinspection AndroidLintClickableViewAccessibility
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // DrawableRight is index value 2 for the getCompoundDrawables method
                final int DRAWABLE_RIGHT = 2;

                // If user presses down (taps) on icon
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (event.getRawX() >= (medicineNameDisplay.getRight() - medicineNameDisplay.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (wordsToSay != null)
                        {
                            tts.speak(wordsToSay, TextToSpeech.QUEUE_ADD, null, "DEFAULT");
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

}
