package edu.uci.ics.jkotha.service.idm.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.jkotha.service.idm.BasicService;
import edu.uci.ics.jkotha.service.idm.logger.ServiceLogger;
import edu.uci.ics.jkotha.service.idm.models.FunctionsRequired;
import edu.uci.ics.jkotha.service.idm.models.LoginRequestModel;
import edu.uci.ics.jkotha.service.idm.models.LoginResponseModel;
import edu.uci.ics.jkotha.service.idm.security.Crypto;
import edu.uci.ics.jkotha.service.idm.security.Session;
import org.glassfish.jersey.internal.util.ExceptionUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("login")
public class LoginPage {
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(String jsonText) {
        ServiceLogger.LOGGER.info("login page requested");
        ObjectMapper mapper = new ObjectMapper();
        LoginRequestModel requestModel;
        LoginResponseModel responseModel = null;
        try {
            requestModel = mapper.readValue(jsonText, LoginRequestModel.class);
            String email = requestModel.getEmail();
            char[] password = requestModel.getPassword();
            if (!FunctionsRequired.isValidEmail(email)) {
                ServiceLogger.LOGGER.info("result code: "+(-11));
                responseModel = new LoginResponseModel(-11, "Email address has invalid format.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else if (email.length() > 50) {
                ServiceLogger.LOGGER.info("result code: "+(-10));
                responseModel = new LoginResponseModel(-10, "Email address has invalid length");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (password==null){
                ServiceLogger.LOGGER.info("result code: "+(-12));
                responseModel = new LoginResponseModel(-12, "Password has invalid length (cannot be empty/null)");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (password.length == 0) {
                ServiceLogger.LOGGER.info("result code: "+(-12));
                responseModel = new LoginResponseModel(-12, "Password has invalid length (cannot be empty/null)");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(password.length>16 || password.length<7){
                ServiceLogger.LOGGER.info("result code: "+(12));
                responseModel = new LoginResponseModel(12,"Password Password does not meet length requirements.");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            String statement = "select pword,salt,status from users where email = ?";
            PreparedStatement inputStatement = BasicService.getCon().prepareStatement(statement);
            inputStatement.setString(1, requestModel.getEmail());
            ResultSet rs = inputStatement.executeQuery();
            if (rs.next()) {
                int status = rs.getInt("status");
                if (status == 1) {
                    //update any previous sessions to revoked
                    String updateString = "update sessions set status = 4 where email = ? and status=1";
                    PreparedStatement updateStatement = BasicService.getCon().prepareStatement(updateString);
                    updateStatement.setString(1, email);
                    int updates =updateStatement.executeUpdate();
                    ServiceLogger.LOGGER.info("sessions revoked for email:"+email+" is "+updates);

                    //create new session
                    char[] passwordDb = rs.getString("pword").toCharArray();
                    byte[] salt = FunctionsRequired.toByteArray(rs.getString("salt"));
                    char[] hashedPassword;
                    hashedPassword = FunctionsRequired.getHashedPass(Crypto.hashPassword(password, salt)).toCharArray();
                    boolean result = FunctionsRequired.isPasswordSame(hashedPassword, passwordDb);
                    String string = new String(passwordDb);
                    if (!result) {
                        ServiceLogger.LOGGER.info("result code: "+(11));
                        responseModel = new LoginResponseModel(11, "Passwords do not match");
                        return Response.status(Response.Status.OK).entity(responseModel).build();
                    }
                    Session session = Session.createSession(email);
                    String sessionString = "insert into sessions(email, sessionID, status, timeCreated, lastUsed, exprTime) " +
                            "values (?,?,1,?,?,?)";
                    PreparedStatement sessionStatement = BasicService.getCon().prepareStatement(sessionString);
                    sessionStatement.setString(1, email);
                    sessionStatement.setString(2, session.getSessionID().toString());
                    sessionStatement.setTimestamp(3, session.getTimeCreated());
                    sessionStatement.setTimestamp(4, session.getLastUsed());
                    sessionStatement.setTimestamp(5, session.getExprTime());
                    sessionStatement.execute();
                    ServiceLogger.LOGGER.info("result code: "+(120));
                    responseModel = new LoginResponseModel(120, "User logged in successfully", session.getSessionID().toString());
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
            }
            ServiceLogger.LOGGER.info("result code: "+(14));
            responseModel = new LoginResponseModel(14, "User not found.");
            return Response.status(Response.Status.OK).entity(responseModel).build();
        } catch (IOException | SQLException e){
            ServiceLogger.LOGGER.warning(ExceptionUtils.exceptionStackTraceAsString(e));
            if (e instanceof JsonMappingException){
                ServiceLogger.LOGGER.info("result code: "+(-2));
                responseModel = new LoginResponseModel(-2,"JSON Mapping Exception.");
            }
            else if(e instanceof JsonParseException) {
                ServiceLogger.LOGGER.info("result code: " + (-3));
                responseModel = new LoginResponseModel(-3, "JSON Parse Exception.");
            }
            else{
                ServiceLogger.LOGGER.info("result code: "+(-1));
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
        }
    }
}