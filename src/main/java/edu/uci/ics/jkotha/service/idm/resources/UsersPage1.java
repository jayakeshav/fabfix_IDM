package edu.uci.ics.jkotha.service.idm.resources;

import edu.uci.ics.jkotha.service.idm.BasicService;
import edu.uci.ics.jkotha.service.idm.logger.ServiceLogger;
import edu.uci.ics.jkotha.service.idm.models.FunctionsRequired;
import edu.uci.ics.jkotha.service.idm.models.QueryResponseModel;
import edu.uci.ics.jkotha.service.idm.models.UserModel;
import org.glassfish.jersey.internal.util.ExceptionUtils;

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
            @QueryParam(value = "plevel") String plevel1
    ){
        ServiceLogger.LOGGER.info("user? page");
        QueryResponseModel responseModel;
        int plevel = FunctionsRequired.getPlevel(plevel1);
        if(email!=null) {
            if (id <= 0) {
                ServiceLogger.LOGGER.info("result code: "+(-15));
                responseModel = new QueryResponseModel(-15, "User ID number is out of valid range.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else if (!FunctionsRequired.isValidEmail(email)) {
                ServiceLogger.LOGGER.info("result code: "+(-11));
                responseModel = new QueryResponseModel(-11, "Email address has invalid format.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            } else if (email.length() > 50) {
                ServiceLogger.LOGGER.info("result code: "+(-10));
                responseModel = new QueryResponseModel(-10, "Email address has invalid length");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
        }
        try {
            if (id==0 & plevel==0 & email == null){
                String allString = "select * from users";
                PreparedStatement userStatement = BasicService.getCon().prepareStatement(allString);
                ResultSet rs = userStatement.executeQuery();
                if (rs.next()){
                    rs.previous();
                    UserModel[] resultArray = FunctionsRequired.toUserArray(rs);
                    ServiceLogger.LOGGER.info("result code: "+(160));
                    responseModel = new QueryResponseModel(160,"all User successfully retrieved.",resultArray);
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
            }
            String userString = "select * from users where id like ? or email like ? or plevel like ?";
            PreparedStatement userStatement = BasicService.getCon().prepareStatement(userString);
            userStatement.setInt(1,id);
            userStatement.setString(2,email);
            userStatement.setInt(3,plevel);
            //ServiceLogger.LOGGER.info(userStatement.toString());
            ResultSet rs = userStatement.executeQuery();
            if (rs.next()){
                rs.previous();
                UserModel[] resultArray = FunctionsRequired.toUserArray(rs);
                ServiceLogger.LOGGER.info("result code: "+(160));
                responseModel = new QueryResponseModel(160,"User successfully retrieved.",resultArray);
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            ServiceLogger.LOGGER.info("result code: "+(14));
            responseModel = new QueryResponseModel(14, "User not found");
            return Response.status(Response.Status.OK).entity(responseModel).build();
        }catch (SQLException e){
            ServiceLogger.LOGGER.warning("SQL exception");
            ServiceLogger.LOGGER.warning(ExceptionUtils.exceptionStackTraceAsString(e));
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
