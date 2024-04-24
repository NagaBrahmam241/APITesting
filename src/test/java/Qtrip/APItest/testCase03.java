package Qtrip.APItest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
//import org.json.JSONObject;
//import org.openqa.selenium.json.Json;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.UUID;

public class testCase03 {
    private static final String BASE_URL = "https://content-qtripdynamic-qa-backend.azurewebsites.net";
    private String userId;
    private String authToken;

    @BeforeClass
    public void setUp() {
        // Step 1: Create a new user using API and login
        Map<String, String> userCredentials = createUserAndLogin();
        userId = userCredentials.get("userId");
        authToken = userCredentials.get("authToken");
    }

    @Test
    public void testBookingAPI() {
        // Step 2: Perform a booking using a POST call
        Response bookingResponse = makeBooking(userId, authToken);

        // Step 3: Ensure that the booking goes fine
        // Assertion for status code
        Assert.assertEquals(bookingResponse.getStatusCode(), 200, "Done booking");

        // Step 4: Perform a GET Reservations call for the user
        Response getReservationsResponse = getReservations(userId, authToken);

        // Assertion for successful booking listed in reservations
        Assert.assertTrue(checkIfBookingIsListed(getReservationsResponse, bookingResponse.jsonPath().getString("id")), "Booking  found in reservations");
    }

    private Map<String, String> createUserAndLogin() {
        // Implement logic to create a new user using API and login

        String userEmail = UUID.randomUUID()+"testuser@gmail.com";
        String userPassword = "ufgssssfj";

        // Registration
        RequestSpecification registrationRequest = RestAssured.given();
        registrationRequest.baseUri(BASE_URL);
        registrationRequest.basePath("/api/v1/register");
        registrationRequest.contentType(ContentType.JSON);
        registrationRequest.body("{ \"email\":\"" + userEmail + "\", \"password\":\"" + userPassword + "\", \"confirmpassword\":\"" + userPassword + "\" }");

        Response registrationResponse = registrationRequest.post();
        System.out.println(registrationResponse.asPrettyString());

        Assert.assertEquals(registrationResponse.getStatusCode(), 201, "User registered successfully");

        // Login
        String loginRequestBody = "{ \"email\":\"" + userEmail + "\", \"password\":\"" + userPassword + "\" }";
        String loginEndpoint = "/api/v1/login";

        RequestSpecification loginRequest = RestAssured.given();
        loginRequest.baseUri(BASE_URL);
        loginRequest.basePath(loginEndpoint);
        loginRequest.contentType(ContentType.JSON);
        loginRequest.body(loginRequestBody);

        Response loginResponse = loginRequest.post();
        System.out.println("Login Response: " + loginResponse.asPrettyString());
        //System.out.println("Status Code : " + loginResponse.getStatusCode());

        Assert.assertEquals(loginResponse.getStatusCode(), 201, "User Login Successful");

        String authToken = loginResponse.jsonPath().getString("data.token");
        //System.out.println("Auth Token: " + authToken);
        String userId = loginResponse.jsonPath().getString("data.id");
        //System.out.println("Id of the user : " + userId);

        return Map.of("userId", userId, "authToken", authToken);
    }

    private Response makeBooking(String userId, String authToken) {

        RequestSpecification bookingRequest = RestAssured.given();
        bookingRequest.baseUri(BASE_URL);
        bookingRequest.basePath("/api/v1/reservations/new");
        bookingRequest.contentType(ContentType.JSON);
        bookingRequest.header("Authorization", "Bearer " + authToken);

        // Truncate username before @ symbol
        String truncatedUsername = userId.split("@")[0];

        bookingRequest.body("{ \"userId\":\"" + userId + "\", \"name\":\"" + truncatedUsername + "\", \"date\":\"2024-09-09\", \"person\":\"1\", \"adventure\":\"2447910730\" }");

        Response bookingResponse = bookingRequest.post();
        System.out.println("Booking Response: " + bookingResponse.asPrettyString());



        return bookingRequest.post();
    }

    private Response getReservations(String userId, String authToken) {

        RequestSpecification getReservationsRequest = RestAssured.given();
        getReservationsRequest.baseUri(BASE_URL);
        getReservationsRequest.basePath("/api/v1/reservations/");
        getReservationsRequest.queryParam("id", userId);
        getReservationsRequest.header("Authorization", "Bearer " + authToken);

        Response response = getReservationsRequest.get();
        System.out.println("GET Reservations Response:");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: \n" + response.asPrettyString());

        return response;
    }


    private boolean checkIfBookingIsListed(Response getReservationsResponse, String bookingId) {

        String reservations = getReservationsResponse.getBody().asString();
        //System.out.println(getReservationsResponse.getBody().asPrettyString());
        return reservations.contains("id");
    }
}
