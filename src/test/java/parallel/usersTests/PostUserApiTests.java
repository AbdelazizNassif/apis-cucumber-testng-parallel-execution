package parallel.usersTests;

import apiObjects.users.PostUserApi;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import utils.Utils;

import static filesReaders.ReadFromFiles.getJsonStringValueByKey;
import static filesReaders.ReadFromFiles.getPropertyByKey;

public class PostUserApiTests {
    volatile String userDataJsonFile = "userTestData.json" ;

    @Test
    public synchronized  void testCreateUserWithValidTokenAndValidEmail ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        PostUserApi postUserApiTests = new PostUserApi(request);
        Response response = postUserApiTests.createNewUser_validTokenAndValidEmail(
                String.format(getJsonStringValueByKey(userDataJsonFile, "email"), Utils.generateRandomString(7))
        );
        response.then().log().all()
                .statusCode(201)
                .header("Content-Type", Matchers.containsStringIgnoringCase("application/json;"))
                .body("id", Matchers.notNullValue())
                .body("name" , Matchers.equalTo(getJsonStringValueByKey(userDataJsonFile , "username")))
                .body("email", Matchers.containsStringIgnoringCase(getJsonStringValueByKey(userDataJsonFile , "emailDomain")))
                .body("gender" , Matchers.equalTo(getJsonStringValueByKey(userDataJsonFile , "maleGender")))
                .body("status" , Matchers.equalToIgnoringCase(getJsonStringValueByKey(userDataJsonFile, "activeStatus")));
    }

    @Test
    public synchronized void testCreateUser_redundantEmail()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));
        PostUserApi postUserApiTests = new PostUserApi(request);
        System.out.println("Before");
        Response creationResponse = postUserApiTests.createNewUser_validTokenAndValidEmail(
                String.format(getJsonStringValueByKey(userDataJsonFile, "email"), Utils.generateRandomString(7))
        );
        System.out.println("Before after");

        JsonPath jp = creationResponse.jsonPath();
        String email = jp.getString("email") ;
        System.out.println(email + "GGGGGGGG");

        RequestSpecification request2 = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));
        PostUserApi postUserApiTests2 = new PostUserApi(request2);

        Response response = postUserApiTests2.createNewUser_validTokenAndValidEmail(email);
        System.out.println("RESPONSE IS *****************");
        response.then().log().all()
                .statusCode(422)
                .header("Content-Type", Matchers.containsStringIgnoringCase("application/json;"))
                .body("field[0]", Matchers.equalTo("email"))
                .body("message[0]", Matchers.equalTo("has already been taken"));
    }

    @Test
    public synchronized  void testCreateUserWithMissingAuthentication ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        PostUserApi postUserApiTests = new PostUserApi(request);
        Response response = postUserApiTests.createNewUser_missingAuthenticationAndValidEmail();
        response.then()
                .statusCode(401)
                .header("Content-Type", Matchers.containsStringIgnoringCase("application/json;"))
                .body("message" , Matchers.equalTo("Authentication failed"));
    }

    @Test
    public synchronized  void testCreateUserWithInvalidToken ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        PostUserApi postUserApiTests = new PostUserApi(request);
        Response response = postUserApiTests.createNewUser_invalidTokenAndValidEmail();
        response.then()
                .statusLine(Matchers.equalTo("HTTP/1.1 401 Unauthorized"))
                .statusCode(401)
                .header("Content-Type", Matchers.containsStringIgnoringCase("application/json;"))
                .body("message" , Matchers.equalTo("Invalid token"));
    }

    @Test
    public synchronized  void testCreateUserWithInvalidEmail ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        PostUserApi postUserApiTests = new PostUserApi(request);
        Response response = postUserApiTests.createNewUser_validTokenAndInvalidEmail();

        response.then()
                .statusLine(Matchers.equalTo("HTTP/1.1 422 Unprocessable Entity"))
                .statusCode(422)
                .header("Content-Type", Matchers.containsStringIgnoringCase("application/json;"))
                .body("field[0]" , Matchers.equalTo("email"))
                .body("message[0]" , Matchers.equalTo("is invalid"));
    }

}
