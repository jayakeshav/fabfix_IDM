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
import org.checkerframework.checker.units.qual.Time;
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
import java.sql.Timestamp;

@Path("session")
public class SessionPage {
    public static final int ACTIVE = 1;
    public static final int CLOSED = 2;
    public static final int EXPIRED = 3;
    public static final int REVOKED = 4;


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response session(String jsonText) {
        ServiceLogger.LOGGER.info("session page requested");
        ObjectMapper mapper = new ObjectMapper();
        SessionResponseModel responseModel = null;
        SessionsRequestModel requestModel;
        try {
            requestModel = mapper.readValue(jsonText, SessionsRequestModel.class);
            String email = requestModel.getEmail();
            String sessionID = requestModel.getSessionID();
            if (!FunctionsRequired.isValidEmail(email)) {
                ServiceLogger.LOGGER.info("result code: " + (-11));
                responseModel = new SessionResponseModel(-11, "Email address has invalid format.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (email.length() > 50) {
                ServiceLogger.LOGGER.info("result code: " + (-10));
                responseModel = new SessionResponseModel(-10, "Email address has invalid length");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }
            else if (sessionID.length() > 128) {
                ServiceLogger.LOGGER.info("result code: " + (-13));
                responseModel = new SessionResponseModel(-13, "Token has invalid length.");
                return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
            }

            String statement = "select * from sessions where sessionID = ?";
            PreparedStatement inputStatement = BasicService.getCon().prepareStatement(statement);
            inputStatement.setString(1, sessionID);
            ResultSet rs = inputStatement.executeQuery();

            if (rs.next()) {
                String emailDB = rs.getString("email");
                if (!emailDB.equals(email)) {
                    ServiceLogger.LOGGER.info("result code: " + (14));
                    responseModel = new SessionResponseModel(14, "User not found");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }

                int sessionStatus = rs.getInt("status");
                if (sessionStatus == ACTIVE) {
                    //rebuild session
                    Session session = Session.rebuildSession(rs.getString("email"),
                            Token.rebuildToken(rs.getString("sessionID")),
                            rs.getTimestamp("timeCreated"),
                            rs.getTimestamp("lastUsed"),
                            rs.getTimestamp("exprTime"));
                    //update last used
                    String updateString = "update sessions set lastUsed = ? where sessionID=?";
                    PreparedStatement updateStatement = BasicService.getCon().prepareStatement(updateString);
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    updateStatement.setTimestamp(1, now);
                    updateStatement.setString(2, sessionID);
                    updateStatement.execute();
                    //update status for session if any
                    String updateStatusString = "update sessions set status = ? where sessionID=?";
                    PreparedStatement updateStatusStatement = BasicService.getCon().prepareStatement(updateStatusString);
                    updateStatusStatement.setString(2, sessionID);
                    // fins the current session status
                    switch (session.getSessionStatus()) {
                        case ACTIVE:
                            if (session.needToCreateNewSession()) {
                                updateStatusStatement.setInt(1, 4);
                                updateStatusStatement.executeUpdate();
                                ServiceLogger.LOGGER.info("result code: " + (130) + " but a new session is created");
                                Session session1 = Session.createSession(email);
                                String sessionString = "insert into sessions" +
                                        "(email, sessionID, status, timeCreated, lastUsed, exprTime) " +
                                        "values (?,?,1,?,?,?)";
                                PreparedStatement sessionStatement = BasicService.getCon().prepareStatement(sessionString);
                                sessionStatement.setString(1, email);
                                sessionStatement.setString(2, session1.getSessionID().toString());
                                sessionStatement.setTimestamp(3, session1.getTimeCreated());
                                sessionStatement.setTimestamp(4, session1.getLastUsed());
                                sessionStatement.setTimestamp(5, session1.getExprTime());
                                sessionStatement.execute();
                                responseModel = new SessionResponseModel(130, "Session is active", session1.getSessionID().toString());
                            }
                            else {
                                ServiceLogger.LOGGER.info("result code: " + (130));
                                responseModel = new SessionResponseModel(130, "Session is active", sessionID);
                            }
                            break;
                        case EXPIRED:
                            updateStatusStatement.setInt(1, 3);
                            updateStatusStatement.executeUpdate();
                            ServiceLogger.LOGGER.info("result code: " + (131));
                            responseModel = new SessionResponseModel(131, "Session is expired");
                            break;
                        case REVOKED: {
                            updateStatusStatement.setInt(1, 4);
                            updateStatusStatement.executeUpdate();
                            ServiceLogger.LOGGER.info("result code: " + (133));
                            responseModel = new SessionResponseModel(133, "Session is revoked");
                            break;
                        }
                    }
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
                else if (sessionStatus == CLOSED) {
                    ServiceLogger.LOGGER.info("result code: " + (132));
                    responseModel = new SessionResponseModel(132, "Session is closed");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
                else if (sessionStatus == EXPIRED){
                    ServiceLogger.LOGGER.info("result code: " + (131));
                    responseModel = new SessionResponseModel(131, "Session is expired");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
                else if (sessionStatus == REVOKED){
                    ServiceLogger.LOGGER.info("result code: " + (133));
                    responseModel = new SessionResponseModel(133, "Session is revoked");
                    return Response.status(Response.Status.OK).entity(responseModel).build();
                }
            }
            else {
                ServiceLogger.LOGGER.info("result code: " + (134));
                responseModel = new SessionResponseModel(134, "Session not found");
                return Response.status(Response.Status.OK).entity(responseModel).build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (IOException | SQLException e) {
            ServiceLogger.LOGGER.warning(ExceptionUtils.exceptionStackTraceAsString(e));
            if (e instanceof JsonMappingException)
                responseModel = new SessionResponseModel(-2, "JSON Mapping Exception.");
            else if (e instanceof JsonParseException)
                responseModel = new SessionResponseModel(-3, "JSON Parse Exception.");
            else
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            return Response.status(Response.Status.BAD_REQUEST).entity(responseModel).build();
        }
    }
}
