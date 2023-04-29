package stepDefinition;

import apiObjects.users.DeleteUserApi;
import apiObjects.users.PostUserApi;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.Assert;
import utils.Utils;

import static filesReaders.ReadFromFiles.getJsonStringValueByKey;
import static filesReaders.ReadFromFiles.getPropertyByKey;

public class DeleteUserStepDefs {
    String userId ;
    static volatile String userDataJsonFile = "userTestData.json" ;
    RequestSpecification request ;
    DeleteUserApi deleteUserApi ;
    Response response ;

    @Before
    public void beforeAnnotation ()
    {
        System.out.println("I am Before Annotation");
    }
    @After
    public void afterAnnotation ()
    {
        System.out.println("I am After Annotation");
        request = null ;
        deleteUserApi = null ;
        response = null ;
    }
    @Given("a new user is just created to be deleted")
    public void a_new_user_is_just_created_to_be_deleted() {
        // Write code here that turns the phrase above into concrete actions
        RequestSpecification request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"));
        PostUserApi postUserApiTests = new PostUserApi(request);
        Response response = postUserApiTests.createNewUser_validTokenAndValidEmail(
                String.format(getJsonStringValueByKey(userDataJsonFile, "email"), Utils.generateRandomString(5))
        );
        response.then().log().all();
        JsonPath jp = response.jsonPath();
        userId =  jp.getString("id")  ;
    }

    @And("I am a logged in system user, who have permission to delete existing user")
    public void i_have_valid_authentication_token_for_deleting_existing_user() {
        // Write code here that turns the phrase above into concrete actions
        request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL"))  ;

        deleteUserApi = new DeleteUserApi(request);

    }

    @When("I delete existing user")
    public void i_delete_existing_user() {
        // Write code here that turns the phrase above into concrete actions
        response=  deleteUserApi.deleteUser_validToken(userId);
    }

    @Then("The user should be removed from the system")
    public void the_user_should_be_removed_from_the_system() {
        // Write code here that turns the phrase above into concrete actions
        System.out.println("Delete response is :");
        response.then().log().all()
                .statusCode(204);
        Assert.assertEquals( "Response should be empty", "", response.asString());
    }

    @And("I am not logged in for deleting existing user")
    public void i_did_not_add_authentication_token_for_deleting_existing_user() {
        // Write code here that turns the phrase above into concrete actions
        request = RestAssured.given()
                .baseUri(getPropertyByKey("environment.properties", "APP_URL")) ;

        deleteUserApi =new DeleteUserApi(request ) ;
    }

    @When("I delete existing while being non-authorized to delete")
    public void i_delete_existing_while_being_unauthenticated() {
        // Write code here that turns the phrase above into concrete actions
        response = deleteUserApi.deleteUser_missingAuth(userId ) ;
    }

    @Then("I should receive response that {string} as protection for our users")
    public void i_should_receive_response_that_authentication_is_required_for_deleting_users(String errMessage) {
        // Write code here that turns the phrase above into concrete actions
        response.then()
                .statusCode(404)
                .header("Content-Type", Matchers.containsStringIgnoringCase("application/json;"))
                .body("message" , Matchers.equalTo(errMessage));
    }




}
