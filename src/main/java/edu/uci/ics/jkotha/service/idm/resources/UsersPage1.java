package edu.uci.ics.jkotha.service.idm.resources;

import edu.uci.ics.jkotha.service.idm.BasicService;
import edu.uci.ics.jkotha.service.idm.logger.ServiceLogger;
import edu.uci.ics.jkotha.service.idm.models.FunctionsRequired;
import edu.uci.ics.jkotha.service.idm.models.QueryResponseModel;
import edu.uci.ics.jkotha.service.idm.models.UserModel;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("/user")
public class UsersPage1 {

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response userCred(
            @QueryParam(value = "id") int id,
            @QueryParam(value = "email") String email,
            @QueryParam(value = "plevel") int plevel
    ){
        ServiceLogger.LOGGER.info("user? page");
        QueryResponseModel responseModel;

        if(email!=null) {
            if (id < 0) {
                responseModel = new QueryResponseModel(-15, "User ID number is out of valid range.", null);
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else if (!FunctionsRequired.isValidEmail(email)) {
                responseModel = new QueryResponseModel(-11, "Email address has invalid format.", null);
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else if (email.length() > 50) {
                responseModel = new QueryResponseModel(-10, "Email address is too long.", null);
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
        }
        try {
            if (id==0 & plevel==0){
                String allString = "select * from users";
                PreparedStatement userStatement = BasicService.getCon().prepareStatement(allString);
                ResultSet rs = userStatement.executeQuery();
                if (rs.next()){
                    rs.previous();
                    UserModel[] resultArray = FunctionsRequired.toUserArray(rs);
                    responseModel = new QueryResponseModel(160,"all User successfully retrieved.",resultArray);
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
            }
            String userString = "select * from users where id like ? or email like ? or plevel like ?";
            PreparedStatement userStatement = BasicService.getCon().prepareStatement(userString);
            userStatement.setInt(1,id);
            userStatement.setString(2,""+email);
            userStatement.setInt(3,plevel);
            //ServiceLogger.LOGGER.info(userStatement.toString());
            ResultSet rs = userStatement.executeQuery();
            if (rs.next()){
                rs.previous();
                UserModel[] resultArray = FunctionsRequired.toUserArray(rs);
                responseModel = new QueryResponseModel(160,"User successfully retrieved.",resultArray);
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            responseModel = new QueryResponseModel(14, "User not found", null);
            return Response.status(Response.Status.OK).entity(responseModel).build();
        }catch (SQLException e){
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("SQL exception");
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
