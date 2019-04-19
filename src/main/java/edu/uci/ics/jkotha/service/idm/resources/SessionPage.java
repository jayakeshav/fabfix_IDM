package edu.uci.ics.jkotha.service.idm.resources;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.jkotha.service.idm.BasicService;
import edu.uci.ics.jkotha.service.idm.logger.ServiceLogger;
import edu.uci.ics.jkotha.service.idm.models.DefaultResponseModel;
import edu.uci.ics.jkotha.service.idm.models.FunctionsRequired;
import edu.uci.ics.jkotha.service.idm.models.SessionResponseModel;
import edu.uci.ics.jkotha.service.idm.models.SessionsRequestModel;
import edu.uci.ics.jkotha.service.idm.security.Session;
import edu.uci.ics.jkotha.service.idm.security.Token;

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

@Path("session")
public class SessionPage {
    public static final int ACTIVE = 1;
    public static final int CLOSED = 2;
    public static final int EXPIRED = 3;
    public static final int REVOKED = 4;


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response session(String jsonText){
        ServiceLogger.LOGGER.info("session page requested");
        ObjectMapper mapper = new ObjectMapper();
        SessionResponseModel responseModel=null;
        SessionsRequestModel requestModel;
        try {
            requestModel = mapper.readValue(jsonText, SessionsRequestModel.class);
            String email = requestModel.getEmail();
            String sessionID = requestModel.getSessionID();
            if (!FunctionsRequired.isValidEmail(email)) {
                responseModel = new SessionResponseModel(-11, "Email address has invalid format.",null);
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(email.length()>50){
                responseModel = new SessionResponseModel(-10,"Email address is too long.",null);
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if(sessionID.length()>128){
                responseModel = new SessionResponseModel(-13,"Token has invalid length.",null);
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            String statement = "select * from sessions where sessionID = ?";
            PreparedStatement inputStatement = BasicService.getCon().prepareStatement(statement);
            inputStatement.setString(1,sessionID);
            ResultSet rs = inputStatement.executeQuery();
            if(rs.next()){
                Session session = Session.rebuildSession(rs.getString("email"),
                        Token.rebuildToken(rs.getString("sessionID")),
                        rs.getTimestamp("timeCreated"),
                        rs.getTimestamp("lastUsed"),
                        rs.getTimestamp("exprTime"));
                if(rs.getInt("status")==CLOSED){
                    responseModel = new SessionResponseModel(132,"Session is closed",sessionID);
                }
                switch (session.getSessionStatus()){
                    case ACTIVE:
                        responseModel = new SessionResponseModel(130,"Session is active",sessionID);
                        break;
                    case EXPIRED:
                        responseModel = new SessionResponseModel(131,"Session is expired",sessionID);
                        break;
                    case REVOKED:
                        {   if (session.needToCreateNewSession()){
                                Session  session1 = Session.createSession(email);
                                String sessionString = "insert into sessions" +
                                        "(email, sessionID, status, timeCreated, lastUsed, exprTime) " +
                                        "values (?,?,1,?,?,?)";
                                PreparedStatement sessionStatement = BasicService.getCon().prepareStatement(sessionString);
                                sessionStatement.setString(1,email);
                                sessionStatement.setString(2,session1.getSessionID().toString());
                                sessionStatement.setTimestamp(3,session1.getTimeCreated());
                                sessionStatement.setTimestamp(4,session1.getLastUsed());
                                sessionStatement.setTimestamp(5,session1.getExprTime());
                                sessionStatement.execute();
                                responseModel = new SessionResponseModel(133,"Session is revoked",session1.getSessionID().toString());
                            }
                            else
                                {
                                    responseModel = new SessionResponseModel(133,"Session is revoked",sessionID);
                                }
                            break;
                        }
                }
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }

        }catch (IOException | SQLException excep){
            excep.printStackTrace();
            if (excep instanceof JsonMappingException)
            responseModel = new SessionResponseModel(-2,"JSON Parse Exception.",null);
            else if(excep instanceof JsonParseException)
                responseModel = new SessionResponseModel(-3,"JSON Mapping Exception.",null);
            else
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
