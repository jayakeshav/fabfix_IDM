package edu.uci.ics.jkotha.service.idm.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.jkotha.service.idm.BasicService;
import edu.uci.ics.jkotha.service.idm.logger.ServiceLogger;
import edu.uci.ics.jkotha.service.idm.models.*;
import edu.uci.ics.jkotha.service.idm.security.Crypto;
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

@Path("/user")
public class UsersPage2 {

    @Path("/updatePassword")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePassword(String jsonText){
        ServiceLogger.LOGGER.info("user/updatePassword page requested");
        ObjectMapper mapper = new ObjectMapper();
        UpdatePasswordRequestModel requestModel ;
        DefaultResponseModel responseModel = null;
        String email;
        char[] oldpword,newpword;
        try {
            requestModel = mapper.readValue(jsonText,UpdatePasswordRequestModel.class);
            email= requestModel.getEmail();
            oldpword = requestModel.getOldpword();
            newpword = requestModel.getNewpword();
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
            else if (oldpword == null | newpword== null){
                ServiceLogger.LOGGER.info("result code: "+(-12));
                responseModel = new DefaultResponseModel(-12,"Password has invalid length");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (oldpword.length==0 | newpword.length==0){
                ServiceLogger.LOGGER.info("result code: "+(-12));
                responseModel = new DefaultResponseModel(-12,"Password has invalid length");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (newpword.length>16 || newpword.length<7){
                ServiceLogger.LOGGER.info("result code: "+(12));
                responseModel = new DefaultResponseModel(12,"Password does not meet length requirements");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else if(!FunctionsRequired.isValidPassowrd(newpword)){
                ServiceLogger.LOGGER.info("result code: "+(13));
                responseModel = new DefaultResponseModel(13,"Password does not meet character requirements");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }

            String userString = "select pword,salt from users where email=?";
            PreparedStatement userStatement = BasicService.getCon().prepareStatement(userString);
            userStatement.setString(1,email);
            ResultSet rs = userStatement.executeQuery();
            if (rs.next()){
                char[] passwordDb = rs.getString("pword").toCharArray();
                byte[] salt = FunctionsRequired.toByteArray(rs.getString("salt"));
                char[] hashedPassword = FunctionsRequired.getHashedPass(Crypto.hashPassword(oldpword,salt)).toCharArray();
                if (!FunctionsRequired.isPasswordSame(hashedPassword,passwordDb)){
                    ServiceLogger.LOGGER.info("result code: "+(11));
                    responseModel = new DefaultResponseModel(11,"Passwords mismatch");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
                String updateString = "update users set pword = ? where email = ?";
                PreparedStatement updateStatement = BasicService.getCon().prepareStatement(updateString);
                updateStatement.setString(1,FunctionsRequired.getHashedPass(Crypto.hashPassword(newpword,salt)));
                updateStatement.setString(2,email);
                updateStatement.execute();
                ServiceLogger.LOGGER.info("result code: "+(150));
                responseModel = new DefaultResponseModel(150,"Password updated successfully");
                return Response.status(Response.Status.OK).entity(responseModel).build();

            }
            else {
                ServiceLogger.LOGGER.info("result code: "+(14));
                responseModel = new DefaultResponseModel(14,"User not found");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }

        }catch (IOException | SQLException e){
            ServiceLogger.LOGGER.warning(ExceptionUtils.exceptionStackTraceAsString(e));
            if (e instanceof JsonMappingException)
                responseModel = new DefaultResponseModel(-2,"JSON Mapping Exception.");
            else if(e instanceof JsonParseException)
                responseModel = new DefaultResponseModel(-3,"JSON Parse Exception.");
            else
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
        }
    }

    @Path("/create")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(String jsonText){
        ServiceLogger.LOGGER.info("user/create page requested");
        ObjectMapper mapper = new ObjectMapper();
        CreateRequestModel requestModel;
        DefaultResponseModel responseModel;
        int plevel;
        String email;
        String plevelString;
        char[] password;
        try {
            requestModel = mapper.readValue(jsonText,CreateRequestModel.class);
            email = requestModel.getEmail();
            plevelString = requestModel.getPlevel();//FunctionsRequired.getPlevel(requestModel.getPlevel());
            password = requestModel.getPassword();
            plevel = FunctionsRequired.getPlevel(plevelString);
            if (!FunctionsRequired.isValidEmail(email)) {
                ServiceLogger.LOGGER.info("result code: "+(-11));
                responseModel = new DefaultResponseModel(-11, "Email address has invalid format.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(email.length()>50){
                ServiceLogger.LOGGER.info("result code: "+(-10));
                responseModel = new DefaultResponseModel(-10,"Email address has invalid length.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(password==null){
                ServiceLogger.LOGGER.info("result code: "+(-12));
                responseModel = new DefaultResponseModel(-12,"Password has invalid length");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (password.length==0){
                ServiceLogger.LOGGER.info("result code: "+(-12));
                responseModel = new DefaultResponseModel(-12,"Password has invalid length");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(plevel>5 | plevel < 0){
                ServiceLogger.LOGGER.info("result code: "+(-14));
                responseModel = new DefaultResponseModel(-14,"privilege level out of range");
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
            else if(plevel==1){
                ServiceLogger.LOGGER.info("result code: "+(171));
                responseModel = new DefaultResponseModel(171,"Creating user with 'ROOT' privilege is not allowed.");
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
                String insert = "insert into users(email, plevel, salt, pword,status) values (?,?,?,?,1)";//status default active
                PreparedStatement insertStatement = BasicService.getCon().prepareStatement(insert);
                insertStatement.setString(1,email);
                insertStatement.setInt(2,plevel);
                insertStatement.setString(3,FunctionsRequired.getHashedPass(salt));
                insertStatement.setString(4,FunctionsRequired.getHashedPass(Crypto.hashPassword(password,salt)));
                insertStatement.execute();
                ServiceLogger.LOGGER.info("result code: "+(170));
                responseModel = new DefaultResponseModel(170,"User created");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
        }
        catch (IOException | SQLException e){
            ServiceLogger.LOGGER.warning(ExceptionUtils.exceptionStackTraceAsString(e));
            if (e instanceof JsonMappingException)
                responseModel = new DefaultResponseModel(-2,"JSON Mapping Exception.");
            else if(e instanceof JsonParseException)
                responseModel = new DefaultResponseModel(-3,"JSON Parse Exception.");
            else
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @Path("/update")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(String jsonText){
        ServiceLogger.LOGGER.info("user/update page requested");
        ObjectMapper mapper = new ObjectMapper();
        UpdateRequestModel requestModel;
        DefaultResponseModel responseModel;
        String email;
        int plevel;
        int id;
        try {
            requestModel = mapper.readValue(jsonText,UpdateRequestModel.class);
            id = requestModel.getId();
            email = requestModel.getEmail();
            plevel = FunctionsRequired.getPlevel(requestModel.getPlevel());
            if (id<=0){
                ServiceLogger.LOGGER.info("result code: "+(-15));
                responseModel = new DefaultResponseModel(-15, "User ID number of range.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(plevel<=0 | plevel>5){
                ServiceLogger.LOGGER.info("result code: "+(-14));
                responseModel = new DefaultResponseModel(-14, "Privilege level out of range");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (!FunctionsRequired.isValidEmail(email)) {
                ServiceLogger.LOGGER.info("result code: "+(-11));
                responseModel = new DefaultResponseModel(-11, "Email address has invalid format.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(email.length()>50){
                ServiceLogger.LOGGER.info("result code: "+(-10));
                responseModel = new DefaultResponseModel(-10,"Email address has invalid length.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }

            String getUserString = "select plevel,email,id from users where email = ?";
            PreparedStatement getUserStatement = BasicService.getCon().prepareStatement(getUserString);
            getUserStatement.setString(1,email);
            ResultSet rs = getUserStatement.executeQuery();
            if(rs.next()){
                if(plevel==1){
                    ServiceLogger.LOGGER.info("result code: "+(181));
                    responseModel = new DefaultResponseModel(181,"Users cannot be elevated to root privilege level.");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
                String updateString = "update users set plevel = ? where email=?";
                PreparedStatement updateStatement = BasicService.getCon().prepareStatement(updateString);
                updateStatement.setInt(1,plevel);
                updateStatement.setString(2,email);
                updateStatement.executeUpdate();
                ServiceLogger.LOGGER.info("result code: "+(180));
                responseModel = new DefaultResponseModel(180,"User updated");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else {
                ServiceLogger.LOGGER.info("result code: "+(14));
                responseModel = new DefaultResponseModel(14,"User not found");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }

        }catch (IOException | SQLException e){
            ServiceLogger.LOGGER.warning(ExceptionUtils.exceptionStackTraceAsString(e));
            if (e instanceof JsonMappingException)
                responseModel = new DefaultResponseModel(-2,"JSON Mapping Exception.");
            else if(e instanceof JsonParseException)
                responseModel = new DefaultResponseModel(-3,"JSON Parse Exception.");
            else
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
        }
    }
}
