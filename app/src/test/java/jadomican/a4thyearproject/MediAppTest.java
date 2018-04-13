package jadomican.a4thyearproject;

import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import jadomican.a4thyearproject.data.Medicine;
import jadomican.a4thyearproject.data.UserDetail;
import jadomican.a4thyearproject.data.UserDetailDO;
import jadomican.a4thyearproject.data.UserDetailsContentContract;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import static org.mockito.Matchers.any;

/**
 * Unit Testing file for User action related tests, including testing the methods called loading a
 * user's medicines from the backend database, conversions performed in the app, sorting mechanisms
 * which can be initiated by the user, creating a user profile and more
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class, TextUtils.class, MatrixCursor.class, Uri.class})
public class MediAppTest {

    @Mock
    private MatrixCursor myMatrixCursor;

    // Mock the TextUtils class to allow testing of the isEmpty method
    @Before
    public void setup() {
        PowerMockito.mockStatic(TextUtils.class);

        PowerMockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                return !(a != null && a.length() > 0);
            }
        });
    }

    @Test
    public void userProfileMedicines_isCorrect() throws Exception {
        Medicine m1 = new Medicine();
        m1.setMedicineName("Xanax");
        m1.setMedicineType("Anti-Depressant");
        Medicine m2 = new Medicine();
        m2.setMedicineName("Paracetamol");
        m2.setMedicineType("Pain-Relief");
        List<Medicine> medicineList = new ArrayList<>();
        medicineList.add(m1);
        medicineList.add(m2);
        UserDetailDO testUser = new UserDetailDO();
        testUser.setBio("This is my bio");
        assertThat(testUser.getBio(), is("This is my bio"));
        testUser.setAddedMedicines(medicineList);
        assertEquals(testUser.getAddedMedicines().get(0).getMedicineName(), "Xanax");
        assertEquals(testUser.getAddedMedicines().get(1).getMedicineName(), "Paracetamol");
    }

    @Test
    public void medicine_sortName_isCorrect() throws Exception {
        // Create and populate a sample list
        List<Medicine> medicineList = new ArrayList<>();
        Medicine m1 = new Medicine();
        m1.setMedicineName("Xanax");
        Medicine m2 = new Medicine();
        m2.setMedicineName("Paracetamol");
        Medicine m3 = new Medicine();
        m3.setMedicineName("Aspirin");
        medicineList.add(m1);
        medicineList.add(m2);
        medicineList.add(m3);

        // Sort the list using the 'name' sort tag, accepted as a parameter in the sort method
        List<Medicine> sortedList = new ArrayList<>(Medicine.sort(medicineList, "name"));
        // Array of names in the expected sorted order
        String[] orderedNames = {"Aspirin", "Paracetamol", "Xanax"};
        // Assert that the list is sorted correctly
        for (int i = 0; i < sortedList.size(); i++) {
            // Assertion performed for each entry in the list
            assertThat(sortedList.get(i).getMedicineName(), is(orderedNames[i]));
        }
    }

    @Test
    public void medicine_sortType_isCorrect() throws Exception {
        List<Medicine> medicineList = new ArrayList<>();
        Medicine m1 = new Medicine();
        m1.setMedicineType("Pain-Relief");
        Medicine m2 = new Medicine();
        m2.setMedicineType("Children");
        Medicine m3 = new Medicine();
        m3.setMedicineType("Anti-Inflammatory");
        medicineList.add(m1);
        medicineList.add(m2);
        medicineList.add(m3);

        // Sort the list using the 'type' sort tag, accepted as a parameter in the sort method
        List<Medicine> sortedList = new ArrayList<>(Medicine.sort(medicineList, "type"));
        // Array of types in the expected sorted order
        String[] orderedTypes = {"Anti-Inflammatory", "Children", "Pain-Relief"};
        // Assert that the list is sorted correctly
        for (int i = 0; i < sortedList.size(); i++) {
            assertThat(sortedList.get(i).getMedicineType(), is(orderedTypes[i]));
        }
    }

    @Test
    public void medicine_sortDate_isCorrect() throws Exception {
        List<Medicine> medicineList = new ArrayList<>();
        Medicine m1 = new Medicine();
        m1.setMedicineDate("12/Jun/1996 05:56 UTC");
        Medicine m2 = new Medicine();
        m2.setMedicineDate("24/Jul/1969 20:18 UTC");
        Medicine m3 = new Medicine();
        m3.setMedicineDate("12/Apr/2018 11:56 UTC");
        medicineList.add(m1);
        medicineList.add(m2);
        medicineList.add(m3);

        // Sort the list using the 'date' sort tag, accepted as a parameter in the sort method
        List<Medicine> sortedList = new ArrayList<>(Medicine.sort(medicineList, "date"));
        // Array of expected sorted dates (Most recent first!)
        String[] orderedDates = {"12/Apr/2018 11:56 UTC", "12/Jun/1996 05:56 UTC", "24/Jul/1969 20:18 UTC"};
        // Assert that the list is sorted correctly
        for (int i = 0; i < sortedList.size(); i++) {
            assertThat(sortedList.get(i).getMedicineDate(), is(orderedDates[i]));
        }
    }

    // Test for the sort method when an invalid sort type is supplied
    @Test
    public void medicine_sortNone_isCorrect() throws Exception {
        List<Medicine> medicineList = new ArrayList<>();
        Medicine m1 = new Medicine();
        m1.setMedicineName("Xanax");
        Medicine m2 = new Medicine();
        m2.setMedicineName("Paracetamol");
        Medicine m3 = new Medicine();
        m3.setMedicineName("Aspirin");
        medicineList.add(m1);
        medicineList.add(m2);
        medicineList.add(m3);

        // Call the sort method once again but pass in an 'invalud' sort type, which is not
        // recognised by the method.
        List<Medicine> sortedList = new ArrayList<>(Medicine.sort(medicineList, "INVALID"));

        // In this case we would expect the original list to be returned, unmodified
        for (int i = 0; i < sortedList.size(); i++) {
            assertThat(sortedList, is(medicineList));
        }
    }

    @Test
    public void user_Profile_toString_isCorrect() throws Exception {
        UserDetail user1 = new UserDetail(
                1,
                "id123",
                new ArrayList<Medicine>(),
                "12/Jun/96",
                "student",
                "Jason",
                "Domican");
        UserDetail user2 = new UserDetail(
                2,
                "id467",
                new ArrayList<Medicine>(),
                "01/Nov/02",
                "student",
                "Jane",
                "Doe");
        // The toString method here provies a unique string identifier for the user (assuming that
        // the id has been assigned from the backend user pool and is unique.
        String expectedString = user1.toString();
        assertThat(expectedString, is("[profile#id123] Jason Domican"));
        expectedString = user2.toString();
        assertThat(expectedString, is("[profile#id467] Jane Doe"));
    }

    // Test that the method to convert a String to a list of medicines works correctly
    @Test
    public void string_toList_isCorrect() throws Exception {
        // Mock the Log and TextUtils classes to avoid test compiler errors
        PowerMockito.mockStatic(Log.class);
        PowerMockito.mockStatic(TextUtils.class);

        // Sample string representing 2 medicines
        final String exampleStringList = "[{\"conflict\":\" \",\"date\":\"12/Apr/2018 18:23 UTC\",\"id\":\"5\",\"imageurl" +
                "\":\"https://www.maynepharma.com/media/1867/morphine_sulfate_er_15-30mg_100ct-1_web.jpg\",\"name\":\"Morphine\",\"onsetaction" +
                "\":\"20\",\"type\":\"Pain Relief\"},{\"conflict\":\"Ibuprofen, Prozac\",\"date\":\"12/Apr/2018 18:23 UTC\",\"id\":\"6\",\"imageurl" +
                "\":\"http://www.doctoralerts.com/wp-content/uploads/2016/11/Aspirin-Therapeutic-uses-Dosage-Side-Effects.jpg\",\"name\":\"Aspirin" +
                "\",\"onsetaction\":\"80\",\"type\":\"Pain Relief\"}]";

        List<Medicine> medicineList = MediApp.medicineStringToList(exampleStringList);

        assertThat(medicineList.get(0).getMedicineName(), is("Morphine"));
        assertThat(medicineList.get(1).getMedicineType(), is("Pain Relief"));
        assertThat(medicineList.size(), is(2));
    }

    @Test
    public void emptyString_toList_isCorrect() throws Exception {
        PowerMockito.mockStatic(Log.class);

        // Test in case of a null String
        final String exampleNullString = null;
        List<Medicine> medicineList = MediApp.medicineStringToList(exampleNullString);
        //Expect that an empty list is created successfully
        assertThat(medicineList.size(), is(0));
        assertThat(medicineList.isEmpty(), is(true));

        // Test in case of an empty ("") String
        final String exampleEmptyString = "";
        medicineList = MediApp.medicineStringToList(exampleEmptyString);
        //Expect that an empty list is created successfully
        assertThat(medicineList.size(), is(0));
        assertThat(medicineList.isEmpty(), is(true));

        // In case of a malformed or garbage string, expect an empty list to return
        final String malformedString = "kjfdnsja\"fdjkabfsdbjgekw\"bfsdgdjsk";
        medicineList = MediApp.medicineStringToList(malformedString);
        assertThat(medicineList.size(), is(0));
        assertThat(medicineList.isEmpty(), is(true));
    }

    // Test the fromCursor() method functions correctlt when given an empty cursor. This may arise
    // if the a user attempts to use the app who has no entry in the database
    @Test
    public void empty_fromCursor_isCorrect() throws Exception {
        PowerMockito.mockStatic(MatrixCursor.class);
        PowerMockito.mockStatic(Uri.class);

        MatrixCursor emptyCursor = mock(MatrixCursor.class);

        // Populate a sample cursor with the user profile columns
        String[] fields = UserDetailsContentContract.UserDetails.PROJECTION_ALL;
        Object[] sampleValues = new Object[fields.length];
        emptyCursor.addRow(sampleValues);

        // Invoke the fromCursor method, converting the cursor to a user profile object
        UserDetail userCreatedFromCursor = UserDetail.fromCursor(myMatrixCursor);
        assertThat(userCreatedFromCursor.getFirstName(), is(""));
        assertThat(userCreatedFromCursor.getLastName(), is(""));
        assertThat(userCreatedFromCursor.getDateOfBirth(), is(""));
        assertThat(userCreatedFromCursor.getBio(), is(""));

        // In this case an empty list of medicines should be returned, rather than a null object,
        // which could cause errors at various points in the application
        assertThat(userCreatedFromCursor.getAddedMedicines().isEmpty(), is(true));
    }

}


