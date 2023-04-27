package parallel.usersTests;

import apiObjects.users.DeleteUserApi;
import apiObjects.users.EditUserApi;
import apiObjects.users.PostUserApi;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utils.Utils;

import static filesReaders.ReadFromFiles.getJsonStringValueByKey;
import static filesReaders.ReadFromFiles.getPropertyByKey;

public class DeleteUserApiTests {

    volatile  ThreadLocal<String> userId = new ThreadLocal<>();
    volatile String userDataJsonFile = "userTestData.json" ;

    @BeforeMethod
    public synchronized  void setUpPreconditions_createNewUser ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));
        PostUserApi postUserApiTests = new PostUserApi(request);
        Response response = postUserApiTests.createNewUser_validTokenAndValidEmail(
                String.format(getJsonStringValueByKey(userDataJsonFile, "email"), Utils.generateRandomString(7))
        );
        response.then().log().all();
        JsonPath jp = response.jsonPath();
        userId.set( jp.getString("id") ) ;
    }

    @Test
    public synchronized  void tesDeleteUser_validUserIdAndValidToken ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));
        PostUserApi postUserApiTests = new PostUserApi(request);
        Response response = postUserApiTests.createNewUser_validTokenAndValidEmail(
                String.format(getJsonStringValueByKey(userDataJsonFile, "email"), Utils.generateRandomString(7))
        );
        JsonPath jp = response.jsonPath();
        userId.set( jp.getString("id") ) ;

        RequestSpecification request2 = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        DeleteUserApi deleteUserApi2 = new DeleteUserApi(request2);
        Response response2 = deleteUserApi2.deleteUser_validToken(userId.get());
        System.out.println("Delete response is :");
        response2.then().log().all()
                .statusCode(204);
        Assert.assertEquals(response2.asString(), "", "Response is empty string");
    }
    @Test
    public synchronized  void tesDeleteUser_missingAuth ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        DeleteUserApi deleteUserApi = new DeleteUserApi(request);
        Response response = deleteUserApi.deleteUser_missingAuth(userId.get());
        response.then()
                .statusCode(404)
                .header("Content-Type", Matchers.containsStringIgnoringCase("application/json;"))
                .body("message" , Matchers.equalTo("Resource not found"));
    }
    @Test
    public synchronized  void testDeleteUser_invalidToken ()
    {
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));

        EditUserApi editUserApi = new EditUserApi(request);
        Response response = editUserApi.editUser_invalidToken(userId.get());
        response.then()
                .statusCode(401)
                .header("Content-Type", Matchers.containsStringIgnoringCase("application/json;"))
                .body("message" , Matchers.equalTo("Invalid token"));
    }

}
