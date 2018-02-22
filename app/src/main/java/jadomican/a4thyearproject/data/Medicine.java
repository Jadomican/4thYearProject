package jadomican.a4thyearproject.data;

/**
 * A model to represent a medicine object.
 */

public class Medicine {

    private String id;
    private String name;
    private String type;
    private String onsetaction;

    public String getMedicineId() {
        return id;
    }

    public void setMedicineId(String id) {
        this.id = id;
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

    public void setMedicineOnsetaction(String onsetaction) {
        this.onsetaction = onsetaction;
    }
}