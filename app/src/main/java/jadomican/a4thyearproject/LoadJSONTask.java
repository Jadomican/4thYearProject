package jadomican.a4thyearproject;

/*
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 */

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import jadomican.a4thyearproject.data.Medicine;
import jadomican.a4thyearproject.data.MedicineResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * The AsyncTask used to retrieve the Medicines from the JSON dataset. This is done as an
 * AsyncTask as network operations should not be performed in the main UI thread in most
 * situations as it can affect performance/ response time while the UI thread waits for the
 * JSON response.
 */
public class LoadJSONTask extends AsyncTask<String, Void, MedicineResponse> {

    private ProgressDialog mDialog;
    private Listener mListener;

    public LoadJSONTask(Listener listener, MedicineListActivity activity) {

        mListener = listener;

        //Display a progress bar to the user while the dataset is loading
        mDialog = new ProgressDialog(activity);
    }

    //Utilising interfaces here to enforce use of the onLoaded() and onError() methods
    public interface Listener {

        void onLoaded(List<Medicine> medicineList);

        void onError();
    }


    /**
     * Show a progress dialog while the page loads
     */
    @Override
    protected void onPreExecute() {
        this.mDialog.setMessage(MediApp.getAppContext().getResources().getString(R.string.loading_many));
        this.mDialog.show();
    }

    /**
     * Convert the response to the format required for the calling classes
     *
     * @see MedicineResponse
     */
    @Override
    protected MedicineResponse doInBackground(String... strings) {
        try {
            String stringResponse = loadJSON(strings[0]);
            Gson gson = new Gson();
            return gson.fromJson(stringResponse, MedicineResponse.class);
        } catch (IOException e) {
            Log.d("LoadJSONTask", "An IOException has occurred" + e.getMessage());
            return null;

        } catch (JsonSyntaxException e) {
            Log.d("LoadJSONTask", "A JsonSyntaxException has occurred" + e.getMessage());
            return null;
        }
    }

    /**
     * Dismiss the dialog and load up the list, or inform the user if there is an error
     */
    @Override
    protected void onPostExecute(MedicineResponse response) {

        if (response != null) {
            mListener.onLoaded(response.getMedicinesList());
        } else {
            mListener.onError();
        }

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }


    /**
     * Set up HTTP GET url connection and load the resulting JSON response
     *
     * @param jsonURL The API invocation URL to be queried
     */
    private String loadJSON(String jsonURL) throws IOException {
        URL url = new URL(jsonURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.connect();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();

        while ((line = in.readLine()) != null) {
            response.append(line);
        }

        //Close buffered reader after use
        in.close();
        return response.toString();
    }

}
