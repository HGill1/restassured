import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class Sprint4SearchCaseTest {

    private final static String pccEndpointChargingCase = "pcc-cst-write/rest/v1/cst/charging-case";
    private final static String pccEndpointReviewComplete = "pcc-cst-write/rest/v1/cst/complete-review";
    private final static String pccEndpointAuthorisedCharge = "pcc-cst-write/rest/v1/cst/auth-charge";
    private final static String pccEndpointPutCharges = "pcc-cst-write/rest/v1/cst/put-charges";

    @BeforeClass
    public static void setup() throws IOException {
        RestAssured.baseURI = "http://83.151.216.178/";
        RestAssured.basePath = "cppdpp/dpp-cst-read/rest/1.0/";

        Class clazz = Sprint4SearchCaseTest.class;
        String chargingCase = IOUtils.toString(clazz.getClassLoader().getResourceAsStream("charging-case.json"), "UTF-8");
        String reviewComplete = IOUtils.toString(clazz.getClassLoader().getResourceAsStream("review-complete.json"), "UTF-8");
        String authorisedCharge = IOUtils.toString(clazz.getClassLoader().getResourceAsStream("authorised-charge.json"), "UTF-8");
        String putCharges = IOUtils.toString(clazz.getClassLoader().getResourceAsStream("put-charges.json"), "UTF-8");

        pccPostURL(pccEndpointChargingCase, chargingCase);
        pccPostURL(pccEndpointReviewComplete, reviewComplete);
        pccPostURL(pccEndpointAuthorisedCharge, authorisedCharge);
        pccPostURL(pccEndpointPutCharges, putCharges);
    }

    /**
     * Fire the thing in PCC that causes the information in DP and OCP to appear.
     * @param endpoint
     * @param json
     */
    private static void pccPostURL(String endpoint, String json) {
        Response response = given()
                .contentType("application/json").
                        body(json).
                        when().
                        post(endpoint);

        if(response.getStatusCode() != 200) {
            throw new RuntimeException(
                    "Response code was not 200 it was: " + response.getStatusCode() +
                    " and the URL we requested was: " + RestAssured.baseURI + RestAssured.basePath + endpoint);
        }
    }

    /**
     * This is for DP
     * Pre-requisite is that PCC has pushed case into DP
     */
    @Test
    public void testWhenSearchingForChargingCaseTheCorrectCaseIsReturned() {
        String dpEndpoint = "cst/case/search/urn/11PP0000010/forename/John/lastName/Doe/dob/1979-01-01";
        Response response = get(dpEndpoint);
        JSONArray searchResults = new JSONArray(response.asString());
        JSONObject searchResult = searchResults.getJSONObject(0);
        assertThat("DP - Response code is not 200", 200, equalTo(response.getStatusCode()));
        assertThat("DP - We got more than one result", 1, equalTo(searchResults.length()));
        assertThat("DP - Incorrect Forename is displayed", "John", equalTo(searchResult.getString("foreName")));
        assertThat("DP - Incorrect Lastname is displayed", "Doe", equalTo(searchResult.getString("lastName")));
        assertThat("DP - Incorrect URN is displayed", "11PP0000010", equalTo(searchResult.getString("urn")));
        assertThat("DP - Incorrect Date of Birth is displayed", "1979-01-01", equalTo(searchResult.getString("dateOfBirth")));
    }

    /**
     * This is for OCP
     * Pre-requisite is that PCC has pushed case into OCP
     */
    @Test
    public void testGetSpecificCaseSummaryDetails() {
        String ocpEndpoint = "routing-cases/1285?details";
        Response response = get(ocpEndpoint);
        JSONArray searchResults = new JSONArray(response.asString());
        JSONObject searchResult = searchResults.getJSONObject(0);
        assertThat("OCP - Response code is not 200", 200, equalTo(response.getStatusCode()));
        assertThat("OCP - We got more than one result", 1, equalTo(searchResults.length()));
        assertThat("OCP - Incorrect Forename is displayed", "John", equalTo(searchResult.getString("foreName")));
        assertThat("OCP - Incorrect Lastname is displayed", "Doe", equalTo(searchResult.getString("lastName")));
        assertThat("OCP - Incorrect URN is displayed", "11PP0000010", equalTo(searchResult.getString("urn")));
        assertThat("OCP - Incorrect Date of Birth is displayed", "1979-01-01", equalTo(searchResult.getString("dateOfBirth")));
        assertThat("OCP - Incorrect offence description", "Common assault on", equalTo(searchResult.getString("offenceDescription")));
    }
}