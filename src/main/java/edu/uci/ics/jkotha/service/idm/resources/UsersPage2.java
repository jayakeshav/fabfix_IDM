package edu.uci.ics.jkotha.service.idm.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.jkotha.service.idm.BasicService;
import edu.uci.ics.jkotha.service.idm.logger.ServiceLogger;
import edu.uci.ics.jkotha.service.idm.models.*;
import edu.uci.ics.jkotha.service.idm.security.Crypto;

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
                responseModel = new DefaultResponseModel(-11, "Email address has invalid format.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(email.length()>50){
                responseModel = new DefaultResponseModel(-10,"Email address is too long.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (oldpword.length==0 | newpword.length==0){
                responseModel = new DefaultResponseModel(-12,"Password has invalid length");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (newpword.length>16 || newpword.length<7){
                responseModel = new DefaultResponseModel(12,"Password does not meet length requirements");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else if(!FunctionsRequired.isValidPassowrd(newpword)){
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
                    responseModel = new DefaultResponseModel(11,"Passwords mismatch");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
                String updateString = "update users set pword = ? where email = ?";
                PreparedStatement updateStatement = BasicService.getCon().prepareStatement(updateString);
                updateStatement.setString(1,FunctionsRequired.getHashedPass(Crypto.hashPassword(newpword,salt)));
                updateStatement.setString(2,email);
                updateStatement.execute();
                responseModel = new DefaultResponseModel(150,"Password updated successfully");
                return Response.status(Response.Status.OK).entity(responseModel).build();

            }
            else {
                responseModel = new DefaultResponseModel(14,"User not found");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }

        }catch (IOException e){
            e.printStackTrace();
            if(e instanceof JsonMappingException)
                responseModel = new DefaultResponseModel(-2,"JSON Parse Exception.");
            else if(e instanceof JsonParseException)
                responseModel = new DefaultResponseModel(-3,"JSON Mapping Exception.");
            else
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
        }catch (SQLException e){}
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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
        String email;
        int plevel;
        char[] password;
        try {
            requestModel = mapper.readValue(jsonText,CreateRequestModel.class);
            email = requestModel.getEmail();
            plevel = requestModel.getPlevel();//FunctionsRequired.getPlevel(requestModel.getPlevel());
            password = requestModel.getPassword();
            if (!FunctionsRequired.isValidEmail(email)) {
                responseModel = new DefaultResponseModel(-11, "Email address has invalid format.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(email.length()>50){
                responseModel = new DefaultResponseModel(-10,"Email address has invalid length.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (password.length==0){
                responseModel = new DefaultResponseModel(-12,"Password has invalid length");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(plevel>5 | plevel < 0){
                ServiceLogger.LOGGER.info(""+plevel);
                responseModel = new DefaultResponseModel(-14,"privilege level out of range");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(password.length>16 || password.length<7){
                responseModel = new DefaultResponseModel(12,"Password does not meet length requirements");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else if(!FunctionsRequired.isValidPassowrd(password)){
                responseModel = new DefaultResponseModel(13,"Password does not meet character requirements");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            else if(plevel==1){
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
                    responseModel = new DefaultResponseModel(16,"Email already in use");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
                byte[] salt = Crypto.genSalt();
                String insert = "insert into users(email, plevel, salt, pword) values (?,?,?,?)";
                PreparedStatement insertStatement = BasicService.getCon().prepareStatement(insert);
                insertStatement.setString(1,email);
                insertStatement.setInt(2,plevel);
                insertStatement.setString(3,FunctionsRequired.getHashedPass(salt));
                insertStatement.setString(4,FunctionsRequired.getHashedPass(Crypto.hashPassword(password,salt)));
                insertStatement.execute();
                responseModel = new DefaultResponseModel(170,"User created");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
        }
        catch (IOException | SQLException e){
            e.printStackTrace();
            if (e instanceof JsonMappingException)
                responseModel = new DefaultResponseModel(-2,"JSON Parse Exception.");
            else if(e instanceof JsonParseException)
                responseModel = new DefaultResponseModel(-3,"JSON Mapping Exception.");
            else
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

//    @Path("/update")
//    @POST
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response update(String jsonText){
//        ServiceLogger.LOGGER.info("user/update page requested");
//        ObjectMapper mapper = new ObjectMapper();
//        UpdateRequestModel requestModel;
//        DefaultResponseModel responseModel;
//        String email;
//        int plevel;
//        int id;
//        try {
//            requestModel = mapper.readValue(jsonText,UpdateRequestModel.class);
//            id = requestModel.getId();
//            email = requestModel.getEmail();
//            plevel = FunctionsRequired.getPlevel(requestModel.getPlevel());
//
//        }catch (IOException e){
//        }
//
//    }
}