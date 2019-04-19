package edu.uci.ics.jkotha.service.idm.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.jkotha.service.idm.BasicService;
import edu.uci.ics.jkotha.service.idm.logger.ServiceLogger;
import edu.uci.ics.jkotha.service.idm.models.DefaultResponseModel;
import edu.uci.ics.jkotha.service.idm.models.FunctionsRequired;
import edu.uci.ics.jkotha.service.idm.models.RegisterRequestModel;
import edu.uci.ics.jkotha.service.idm.security.Crypto;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("register")
public class RegisterPage {
    private static final int userPrivilage = 5;//from database

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(String jsonText){
        ServiceLogger.LOGGER.info("register page requested");
        ObjectMapper mapper = new ObjectMapper();
        RegisterRequestModel requestModel ;
        DefaultResponseModel responseModel = null;

        try {
            requestModel = mapper.readValue(jsonText,RegisterRequestModel.class);
            String email = requestModel.getEmail();
            char[] password = requestModel.getPassword();
            if (!FunctionsRequired.isValidEmail(email)) {
                responseModel = new DefaultResponseModel(-11, "Email address has invalid format.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(email.length()>50){
                responseModel = new DefaultResponseModel(-10,"Email address is too long.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (password.length==0){
                responseModel = new DefaultResponseModel(-12,"Password has invalid length (cannot be empty/null)");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(password.length>16 || password.length<7){
                responseModel = new DefaultResponseModel(12,"Password does not meet length requirements");
                return Response.status(Response.Status.UNAUTHORIZED).entity(responseModel).build();
            }
            else if(!FunctionsRequired.isValidPassowrd(password)){
                responseModel = new DefaultResponseModel(13,"Password does not meet character requirements");
                return Response.status(Response.Status.UNAUTHORIZED).entity(responseModel).build();
            }
            String statement = "select count(*) as result from users where email = ?";
            PreparedStatement inputStatement = BasicService.getCon().prepareStatement(statement);
            inputStatement.setString(1,email);
            ResultSet rs = inputStatement.executeQuery();
            if (rs.next()){
                int result = rs.getInt("result");
                if(result==1){
                    responseModel = new DefaultResponseModel(16,"Email already in use");
                    return Response.status(Response.Status.UNAUTHORIZED).entity(responseModel).build();
                }
                byte[] salt = Crypto.genSalt();
                String insert = "insert into users(email, plevel, salt, pword) values (?,?,?,?)";
                PreparedStatement insertStatement = BasicService.getCon().prepareStatement(insert);
                insertStatement.setString(1,email);
                insertStatement.setInt(2,userPrivilage);
                insertStatement.setString(3,FunctionsRequired.getHashedPass(salt));
                insertStatement.setString(4,FunctionsRequired.getHashedPass(Crypto.hashPassword(password,salt)));
                insertStatement.execute();
                responseModel = new DefaultResponseModel(110,"User registered successfully");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
        }catch (IOException  | SQLException e){
            e.printStackTrace();
            if(e instanceof JsonMappingException)
                responseModel = new DefaultResponseModel(-2,"JSON Parse Exception.");
            else if(e instanceof JsonParseException)
                responseModel = new DefaultResponseModel(-3,"JSON Mapping Exception.");
            else{
                ServiceLogger.LOGGER.warning("SQLException thrown.");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
