package jadomican.a4thyearproject.data;

/*
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 */

import java.util.ArrayList;
import java.util.List;

/**
 * A class to model the JSON object medicine response from an API query
 */
public class MedicineResponse {

    private List<Medicine> medicine = new ArrayList<Medicine>();

    public List<Medicine> getMedicinesList()
    {
        return medicine;
    }

}
