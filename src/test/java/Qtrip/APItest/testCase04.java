package Qtrip.APItest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.UUID;

public class testCase04 {

    private static final String BASE_URL = "https://content-qtripdynamic-qa-backend.azurewebsites.net";

    @Test(groups = "API Tests")
    public void registration() {
        // Generate a unique email for each test run
        String userEmail = UUID.randomUUID() + "naga@gmail.com";
        String userPassword = "1234564";

        // Request Body
        String registrationRequestBody = "{ \"email\":\"" + userEmail + "\", \"password\":\"" + userPassword + "\", \"confirmpassword\":\"" + userPassword + "\" }";

        // API Endpoint
        String registerEndpoint = "/api/v1/register";

        // Setting up the request
        RequestSpecification http = RestAssured.given();
        http.baseUri(BASE_URL);
        http.basePath(registerEndpoint);
        http.contentType(ContentType.JSON);
        http.body(registrationRequestBody);

        // Making the POST request
        Response response = http.post();
        System.out.println(response.asPrettyString());

        // Assertion
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(response.getStatusCode(), 201, "User Registered Successfully");

        // Attempting a second registration with the same email
        Response secondRegistrationResponse = http.post();
        System.out.println(secondRegistrationResponse.asPrettyString());

        // Additional assertions for the second registration
        softAssert.assertEquals(secondRegistrationResponse.getStatusCode(), 400, "Second Registration should fail with status code 400");
        softAssert.assertTrue(secondRegistrationResponse.getBody().asString().contains("Email already exists"),
                "Response should contain 'Email already exists' message");

        // Assert all soft assertions
        softAssert.assertAll();
    }
}
