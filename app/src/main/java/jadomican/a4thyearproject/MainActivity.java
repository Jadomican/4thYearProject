package jadomican.a4thyearproject;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton editDetailsButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the floating action button
        editDetailsButton = (FloatingActionButton) findViewById(R.id.editDetailsButton);
        editDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, UserDetailActivity.class);
                    context.startActivity(intent);
            }

        });



    }
}
