package jadomican.a4thyearproject;

/*
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


/**
 * The home screen activity of the application, loaded after the initial login. Contains 'links' to
 * other main functionality of the app, such as editing user profile and searching for medicines
 */
public class MainActivity extends BaseAppCompatActivity {

    /**
     * Override the onCreate method in BaseAppCompatActivity with Activity-specific code
     *
     * @see BaseAppCompatActivity#onCreate(Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button medicineSearchButton = (Button) findViewById(R.id.medicineSearchButton);
        Button cameraSearchButton = (Button) findViewById(R.id.cameraSearchButton);
        // Set listener for button information popup
        setButtonTouchListener(medicineSearchButton,
                getResources().getString(R.string.medicine_search_info_title),
                getResources().getString(R.string.medicine_search_info_message));
        setButtonTouchListener(cameraSearchButton,
                getResources().getString(R.string.camera_info_title),
                getResources().getString(R.string.camera_info_message));
    }

    // Pass Activity Resources to the Base class for initialisation
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getMenuItemResourceId() {
        return R.id.nav_home;
    }

    @Override
    protected int getMenuResourceId() {
        return R.menu.bar_menu;
    }

    @Override
    protected String getHelpTitle() {
        return getString(R.string.bar_main_help_title);
    }

    @Override
    protected String getHelpMessage() {
        return getString(R.string.bar_main_help_message);
    }


    /**
     * Button to go to the user's list of profiles
     */
    public void goToProfileHub(View view) {
        Context context = view.getContext();
        Intent intent = new Intent(context, ProfileMedicineListActivity.class);
        startActivityIfNeeded(intent, 0);
    }

    /**
     * Initiate a search. Upon activation the search bar appears at the top of the screen
     */
    public void goToSearchMedicines(View view) {
        onSearchRequested();
    }

    /**
     * Proceed to the Camera Search activity
     */
    public void goToCameraSearch(View view) {
        Context context = view.getContext();
        Intent intent = new Intent(context, OcrCaptureActivity.class);
        startActivityIfNeeded(intent, 0);
    }

    /**
     * Method to contextually display information about a feature when the 'i' symbol is
     * tapped. Takes a button and messages to be displayed as parameters, also passing in the alert
     * title and messages
     *
     * @param button  The button for which the information icon will appear
     * @param title   The title of the dialog that appears on touch
     * @param message The message of the dialog that appears on touch
     */
    public void setButtonTouchListener(final Button button, final String title, final String message) {
        // The following comment stops the IDE from warning about overriding the performClick()
        // method, an optional setOnTouchListener method:
        // noinspection AndroidLintClickableViewAccessibility
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // DrawableRight is index value 2 for the getCompoundDrawables method
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (button.getRight() - button.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle(title);
                        alertDialog.setMessage(message);
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                        return true;
                    }
                }
                return false;
            }
        });
    }
}
