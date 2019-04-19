package edu.uci.ics.jkotha.service.idm.resources;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.jkotha.service.idm.BasicService;
import edu.uci.ics.jkotha.service.idm.logger.ServiceLogger;
import edu.uci.ics.jkotha.service.idm.models.DefaultResponseModel;
import edu.uci.ics.jkotha.service.idm.models.FunctionsRequired;
import edu.uci.ics.jkotha.service.idm.models.PrivilegeRequestModel;

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

@Path("/privilege")
public class PrivilegePage {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response privilege(String jsonText){
        ServiceLogger.LOGGER.info("privilege page requested");
        ObjectMapper mapper = new ObjectMapper();
        DefaultResponseModel responseModel=null;
        PrivilegeRequestModel requestModel;
        try {
            requestModel = mapper.readValue(jsonText, PrivilegeRequestModel.class);
            String email = requestModel.getEmail();
            int plevel = requestModel.getPlevel();
            if(plevel<=0 | plevel>5){
                responseModel = new DefaultResponseModel(-14,"Privilege level out of valid range");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (!FunctionsRequired.isValidEmail(email)) {
                responseModel = new DefaultResponseModel(-11, "Email address has invalid format.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(email.length()>50){
                responseModel = new DefaultResponseModel(-10,"Email address is too long.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            String privilegeString = "select plevel from users where email=?";
            PreparedStatement privilegeStatement = BasicService.getCon().prepareStatement(privilegeString);
            privilegeStatement.setString(1,email);
            ResultSet rs = privilegeStatement.executeQuery();
            if(rs.next()){
                int plevelDB = rs.getInt("plevel");
                if(plevelDB<=plevel){
                    responseModel = new DefaultResponseModel(140,"User has sufficient privilege level.");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
                else{
                    responseModel = new DefaultResponseModel(141,"User has insufficient privilege level.");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
            }
            responseModel = new DefaultResponseModel(14,"User not found.");
            return Response.status(Response.Status.UNAUTHORIZED).entity(responseModel).build();

        }catch (IOException | SQLException e){
            if (e instanceof JsonMappingException)
                responseModel = new DefaultResponseModel(-2,"JSON Parse Exception.");
            else if(e instanceof JsonParseException)
                responseModel = new DefaultResponseModel(-3,"JSON Mapping Exception.");
            else
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
        }
    }


}
