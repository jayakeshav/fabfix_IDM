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
import org.glassfish.jersey.internal.util.ExceptionUtils;

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
                ServiceLogger.LOGGER.info("result code: "+(-11));
                responseModel = new DefaultResponseModel(-11, "Email address has invalid format.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(email.length()>50){
                ServiceLogger.LOGGER.info("result code: "+(-10));
                responseModel = new DefaultResponseModel(-10,"Email address has invalid length");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (password == null){
                ServiceLogger.LOGGER.info("result code: "+(-12));
                responseModel = new DefaultResponseModel(-12,"Password has invalid length.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (password.length==0){
                ServiceLogger.LOGGER.info("result code: "+(-12));
                responseModel = new DefaultResponseModel(-12,"Password has invalid length.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(password.length>16 || password.length<7){
                ServiceLogger.LOGGER.info("result code: "+(12));
                responseModel = new DefaultResponseModel(12,"Password does not meet length requirements");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else if(!FunctionsRequired.isValidPassowrd(password)){
                ServiceLogger.LOGGER.info("result code: "+(13));
                responseModel = new DefaultResponseModel(13,"Password does not meet character requirements");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            String statement = "select count(*) as result from users where email = ?";
            PreparedStatement inputStatement = BasicService.getCon().prepareStatement(statement);
            inputStatement.setString(1,email);
            ResultSet rs = inputStatement.executeQuery();
            if (rs.next()){
                int result = rs.getInt("result");
                if(result==1){
                    ServiceLogger.LOGGER.info("result code: "+(16));
                    responseModel = new DefaultResponseModel(16,"Email already in use");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
                byte[] salt = Crypto.genSalt();
                String insert = "insert into users(email, plevel, salt, pword,status) values (?,?,?,?,1)";//1=active user
                PreparedStatement insertStatement = BasicService.getCon().prepareStatement(insert);
                insertStatement.setString(1,email);
                insertStatement.setInt(2,userPrivilage);
                insertStatement.setString(3,FunctionsRequired.getHashedPass(salt));
                insertStatement.setString(4,FunctionsRequired.getHashedPass(Crypto.hashPassword(password,salt)));
                insertStatement.execute();
                ServiceLogger.LOGGER.info("result code: "+(110));
                responseModel = new DefaultResponseModel(110,"User registered successfully");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
        }catch (IOException | SQLException e){
            ServiceLogger.LOGGER.warning(ExceptionUtils.exceptionStackTraceAsString(e));
            if (e instanceof JsonMappingException){
                ServiceLogger.LOGGER.info("result code: "+(-2));
                responseModel = new DefaultResponseModel(-2,"JSON Mapping Exception.");
            }
            else if(e instanceof JsonParseException){
                ServiceLogger.LOGGER.info("result code: "+(-3));
                responseModel = new DefaultResponseModel(-3,"JSON Parse Exception.");
            }
            else {
                ServiceLogger.LOGGER.info("result code: "+(-1));
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
