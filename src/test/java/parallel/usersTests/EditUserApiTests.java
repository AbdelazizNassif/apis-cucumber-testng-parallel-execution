package parallel.usersTests;

import apiObjects.users.EditUserApi;
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

public class EditUserApiTests {

    ThreadLocal<String> userId = new ThreadLocal<>();
    static volatile String userDataJsonFile = "userTestData.json" ;


    @BeforeMethod
    public  synchronized  void setUpPreconditions_createNewUser ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        PostUserApi postUserApiTests = new PostUserApi(request);
        Response response = postUserApiTests.createNewUser_validTokenAndValidEmail(
                String.format(getJsonStringValueByKey(userDataJsonFile, "email"), System.currentTimeMillis())
        );
        JsonPath jp = response.jsonPath();
        userId .set(  jp.getString("id") ) ;
    }

    @Test
    public synchronized  void testEditNewlyCreatedUser_validUserIdAndValidToken ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        EditUserApi editUserApi = new EditUserApi(request);
        Response response = editUserApi.editUser_validToken(userId.get());
        response.then().log().all()
                .statusCode(200)
                .header("Content-Type", Matchers.containsStringIgnoringCase("application/json;"))
                .body("id", Matchers.notNullValue())
                .body("name" , Matchers.equalTo(getJsonStringValueByKey(userDataJsonFile , "newUsername")))
                .body("email", Matchers.containsStringIgnoringCase(getJsonStringValueByKey(userDataJsonFile , "newEmailDomain")))
                .body("status" , Matchers.equalToIgnoringCase(getJsonStringValueByKey(userDataJsonFile , "inactiveStatus")));
    }
    @Test
    public synchronized  void testEditNewlyCreatedUser_missingAuth ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        EditUserApi editUserApi = new EditUserApi(request);
        Response response = editUserApi.editUser_missingAuth(userId.get());
        response.then()
                .statusCode(404)
                .header("Content-Type", Matchers.containsStringIgnoringCase("application/json;"))
                .body("message" , Matchers.equalTo("Resource not found"))
                ;
    }
    @Test
    public synchronized  void testEditNewlyCreatedUser_invalidToken ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        EditUserApi editUserApi = new EditUserApi(request);
        Response response = editUserApi.editUser_invalidToken(userId.get());
        response.then()
                .statusCode(401)
                .header("Content-Type", Matchers.containsStringIgnoringCase("application/json;"))
                .body("message" , Matchers.equalTo("Invalid token"))
;
    }

}
