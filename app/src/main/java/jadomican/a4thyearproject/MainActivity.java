package jadomican.a4thyearproject;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
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
        Context context = view.getContext();
        Intent intent = new Intent(context, UserDetailActivity.class);
        context.startActivity(intent);
    }

    public void goToSearchMedicines(View view) {
        Context context = view.getContext();
        Intent intent = new Intent(context, MedicineListActivity.class);
        context.startActivity(intent);
    }

}
