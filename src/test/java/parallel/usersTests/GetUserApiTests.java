package parallel.usersTests;

import apiObjects.users.GetUserApi;
import apiObjects.users.PostUserApi;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

import static filesReaders.ReadFromFiles.getJsonStringValueByKey;
import static filesReaders.ReadFromFiles.getPropertyByKey;

public class GetUserApiTests {

    volatile ThreadLocal<String> userId = new ThreadLocal<>();
    volatile String userDataJsonFile = "userTestData.json" ;

    @BeforeMethod
    public synchronized  void setUpPreconditions_createNewUser ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        PostUserApi postUserApiTests = new PostUserApi(request);
        Response response = postUserApiTests.createNewUser_validTokenAndValidEmail(
                String.format(getJsonStringValueByKey(userDataJsonFile, "email"), System.currentTimeMillis())
        );
        JsonPath jp = response.jsonPath();
        userId.set(  jp.getString("id") ) ;
    }

    @Test
    public synchronized  void testGetNewlyCreatedUser_validUserIdAndValidToken ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        GetUserApi getUserApi = new GetUserApi(request);
        Response response = getUserApi.getNewlyCreatedUser_validToken(userId.get());
        response.then()
                .statusCode(200)
                .header("Content-Type", Matchers.containsStringIgnoringCase("application/json;"))
                .body("id", Matchers.notNullValue())
                .body("name" , Matchers.equalTo(getJsonStringValueByKey(userDataJsonFile , "username")))
                .body("email", Matchers.containsStringIgnoringCase(getJsonStringValueByKey(userDataJsonFile , "emailDomain")))
                .body("gender" , Matchers.equalTo(getJsonStringValueByKey(userDataJsonFile , "maleGender")))
                .body("status" , Matchers.equalToIgnoringCase(getJsonStringValueByKey(userDataJsonFile, "activeStatus")));
    }

    @Test
    public synchronized  void testGetNewlyCreatedUser_invalidUserId ()
    {
        String invalidUserId = new Date().getTime() + "" ;
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        GetUserApi getUserApi = new GetUserApi(request);
        Response response = getUserApi.getNewlyCreatedUser_validToken(invalidUserId);
        response.then()
                .statusCode(404)
                .header("Content-Type", Matchers.containsStringIgnoringCase("application/json;"))
                .body("message" , Matchers.equalTo("Resource not found"));
    }

    @Test
    public synchronized  void testGetNewlyCreatedUser_missingAuthHeader ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        GetUserApi getUserApi = new GetUserApi(request);
        Response response = getUserApi.getNewlyCreatedUser_missingTokenHeader(userId.get());
        response.then()
                .statusCode(404)
                .header("Content-Type", Matchers.containsStringIgnoringCase("application/json;"))
                .body("message" , Matchers.equalTo("Resource not found"));
    }

    @Test
    public synchronized  void testGetNewlyCreatedUser_invalidToken ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        GetUserApi getUserApi = new GetUserApi(request);
        Response response = getUserApi.getNewlyCreatedUser_invalidTokenAndValidUserId(userId.get());
        response.then()
                .statusLine(Matchers.equalTo("HTTP/1.1 401 Unauthorized"))
                .statusCode(401)
                .header("Content-Type", Matchers.containsStringIgnoringCase("application/json;"))
                .body("message" , Matchers.equalTo("Invalid token"));
    }
}
