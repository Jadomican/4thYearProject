package jadomican.a4thyearproject.data;

/*
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 */

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBDocument;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import jadomican.a4thyearproject.ProfileMedicineListActivity;

/**
 * A model to represent a medicine object.
 */
@DynamoDBDocument
public class Medicine implements Serializable {

    private String id;
    private String name;
    private String type;
    private String onsetaction;
    private String imageurl;
    private String conflict;
    private String date;
    private String description;

    /**
     * Default constructor for a Medicine object
     */
    public Medicine() {
    }

    /**
     * Constructor to initialise a Medicine object with values
     */
    public Medicine(String id, String name, String type, String onsetaction, String imageurl, String conflict, String date) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.onsetaction = onsetaction;
        this.imageurl = imageurl;
        this.conflict = conflict;
        this.date = date;
    }

    /**
     * Overloaded constructor with no date parameter
     */
    public Medicine(String id, String name, String type, String onsetaction, String imageurl, String conflict) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.onsetaction = onsetaction;
        this.imageurl = imageurl;
        this.conflict = conflict;
    }

    public String getMedicineDate() {
        return date;
    }

    public void setMedicineDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMedicineId() {
        return id;
    }

    public void setMedicineId(String id) {
        this.id = id;
    }

    public String getMedicineImageUrl() {
        return imageurl;
    }

    public void setMedicineImageUrl(String url) {
        this.imageurl = url;
    }

    public String getMedicineName() {
        return name;
    }

    public void setMedicineName(String name) {
        this.name = name;
    }

    public String getMedicineType() {
        return type;
    }

    public void setMedicineType(String type) {
        this.type = type;
    }

    public String getMedicineOnsetAction() {
        return onsetaction;
    }

    public void setMedicineOnsetAction(String onsetaction) {
        this.onsetaction = onsetaction;
    }

    public String getMedicineConflict() {
        return conflict;
    }


    public void setMedicineConflict(String conflict) {
        this.conflict = conflict;
    }

    /**
     * Method to allow various sorting algorithms on a list of medicines
     *
     * @param list     The list to be sorted
     * @param sortType The sorting mechanism to be performed
     */
    public static List<Medicine> sort(List<Medicine> list, String sortType) throws ParseException {
        switch (sortType) {
            case ProfileMedicineListActivity.SORT_NAME:
                Comparator<Medicine> nameOrder = new Comparator<Medicine>() {
                    public int compare(Medicine m1, Medicine m2) {
                        return m1.name.compareTo(m2.name);
                    }
                };
                Collections.sort(list, nameOrder);
                break;
            case ProfileMedicineListActivity.SORT_TYPE:
                Comparator<Medicine> typeOrder = new Comparator<Medicine>() {
                    public int compare(Medicine m1, Medicine m2) {
                        return m1.type.compareTo(m2.type);
                    }
                };
                Collections.sort(list, typeOrder);
                break;
            case ProfileMedicineListActivity.SORT_DATE:
                Collections.sort(list, new Comparator<Medicine>() {
                    @Override
                    public int compare(Medicine m1, Medicine m2) {
                        try {
                            // All dates are stored in the DB in UTC, maintaining compatibility
                            DateFormat f = new SimpleDateFormat(ProfileMedicineListActivity.DATE_FORMAT);
                            f.setTimeZone(TimeZone.getTimeZone("UTC"));
                            return f.parse(m2.getMedicineDate()).compareTo(f.parse(m1.getMedicineDate()));
                        } catch (ParseException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                });
                break;
        }
        return list;
    }

}