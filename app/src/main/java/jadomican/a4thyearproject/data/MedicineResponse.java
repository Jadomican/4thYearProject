package jadomican.a4thyearproject.data;

/**
 * A class to model the JSON object medicine response
 */

import java.util.ArrayList;
import java.util.List;

public class MedicineResponse {

    private List<Medicine> medicine = new ArrayList<Medicine>();

    public List<Medicine> getMedicinesList()
    {
        return medicine;
    }

}
