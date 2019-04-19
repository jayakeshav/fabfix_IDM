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
    public Response login(String jsonText){
        ServiceLogger.LOGGER.info("login page requested");
        ObjectMapper mapper = new ObjectMapper();
        LoginRequestModel requestModel ;
        LoginResponseModel responseModel = null;
        try {
            requestModel = mapper.readValue(jsonText,LoginRequestModel.class);
            String email = requestModel.getEmail();
            char[] password = requestModel.getPassword();
            if (!FunctionsRequired.isValidEmail(email)) {
                responseModel = new LoginResponseModel(-11, "Email address has invalid format.",null);
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(email.length()>50){
                responseModel = new LoginResponseModel(-10,"Email address is too long.",null);
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (password.length==0){
                responseModel = new LoginResponseModel(-12,"Password has invalid length (cannot be empty/null)",null);
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            String statement = "select pword,salt from users where email = ?";
            PreparedStatement inputStatement = BasicService.getCon().prepareStatement(statement);
            inputStatement.setString(1,requestModel.getEmail());
            ResultSet rs = inputStatement.executeQuery();
            if(rs.next()){
                char[] passwordDb = rs.getString("pword").toCharArray();
                byte[] salt = FunctionsRequired.toByteArray(rs.getString("salt"));
                char[] hashedPassword ;
                hashedPassword = FunctionsRequired.getHashedPass(Crypto.hashPassword(password,salt)).toCharArray();
                boolean result = FunctionsRequired.isPasswordSame(hashedPassword,passwordDb);
                String string = new String(passwordDb);
                if (!result){
                    responseModel = new LoginResponseModel(11,"Passwords do not match",null);
                    return Response.status(Response.Status.UNAUTHORIZED).entity(responseModel).build();
                }
                Session session = Session.createSession(email);
                String sessionString = "insert into sessions(email, sessionID, status, timeCreated, lastUsed, exprTime) " +
                        "values (?,?,1,?,?,?)";
                PreparedStatement sessionStatement = BasicService.getCon().prepareStatement(sessionString);
                sessionStatement.setString(1,email);
                sessionStatement.setString(2,session.getSessionID().toString());
                sessionStatement.setTimestamp(3,session.getTimeCreated());
                sessionStatement.setTimestamp(4,session.getLastUsed());
                sessionStatement.setTimestamp(5,session.getExprTime());
                sessionStatement.execute();
                responseModel = new LoginResponseModel(120,"User logged in successfully",session.getSessionID().toString());
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            responseModel = new LoginResponseModel(14,"User not found.",null);
            return Response.status(Response.Status.OK).entity(responseModel).build();
        }catch (IOException | SQLException e){
            e.printStackTrace();
            if(e instanceof JsonMappingException)
                responseModel = new LoginResponseModel(-2,"JSON Parse Exception.",null);
            else if(e instanceof JsonParseException)
                responseModel = new LoginResponseModel(-3,"JSON Mapping Exception.",null);
            else
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
        }
    }
}