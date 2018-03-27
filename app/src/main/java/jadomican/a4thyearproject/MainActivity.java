package jadomican.a4thyearproject;

/**
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 *
 * The home screen activity of the application, loaded after the initial login. Contains 'links' to
 * other main functionality of the app, such as editing user profile and searching for medicines
 */

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goToEditProfile(View view) {
        Bundle arguments = new Bundle();
        arguments.putString(UserDetailFragment.ARG_ITEM_ID, AWSProvider.getInstance().getIdentityManager().getCachedUserID());
        Context context = view.getContext();
        Intent intent = new Intent(context, UserDetailActivity.class);
        intent.putExtras(arguments);
        context.startActivity(intent);
    }

    public void goToSearchMedicines(View view) {
        Context context = view.getContext();
        Intent intent = new Intent(context, MedicineListActivity.class);
        context.startActivity(intent);
    }

    public void goToCameraSearch(View view) {
        Context context = view.getContext();
        Intent intent = new Intent(context, OcrCaptureActivity.class);
        context.startActivity(intent);
    }

}
