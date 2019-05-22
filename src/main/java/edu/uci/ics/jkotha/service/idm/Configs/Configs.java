package edu.uci.ics.jkotha.service.idm.Configs;


import edu.uci.ics.jkotha.service.idm.logger.ServiceLogger;
import edu.uci.ics.jkotha.service.idm.models.ConfigsModel;
import edu.uci.ics.jkotha.service.idm.models.DefaultResponseModel;


public class Configs {

    // Default service configs
    private final String DEFAULT_SCHEME = "http://";
    private final String DEFAULT_HOSTNAME = "0.0.0.0";
    private final int DEFAULT_PORT = 6243;
    private final String DEFAULT_PATH = "/api/idm";
    // Default logger configs
    private final String DEFAULT_OUTPUTDIR = "./logs/";
    private final String DEFAULT_OUTPUTFILE = "basicService.log";

    private final String DEFAULT_DBUSERNAME = "testuser";
    private final String DEFAULT_DBPASSWORD = "testuser";
    private final String DEFAULT_DBHOSTNAME = "localhost";
    private final int DEFAULT_DBPORT = 3306;
    private final String DEFAULT_DBDRIVER = "com.mysql.cj.jdbc.Driver";
    private final String DEFAULT_DBNAME = "idm";
    private final String DEFAULT_DBSETTINGS = "?autoReconnect=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=PST";

    private final long DEFAULT_SESSION_TIMEOUT = 600000; // 10-minute timeout
    private final long DEFAULT_TOKEN_EXPR = 1800000;

    private String scheme;
    private String hostName;
    private int port;
    private String path;

    private String outputDir;
    private String outputFile;

    private String dbUsername;
    private String dbPassword;
    private String dbHostname;
    private int dbPort;
    private String dbDriver;
    private String dbName;
    private String dbSettings;

    private long session_timeout;
    private long token_expr;

    public Configs() {
        //basic configs
        scheme = DEFAULT_SCHEME;
        hostName = DEFAULT_HOSTNAME;
        port = DEFAULT_PORT;
        path = DEFAULT_PATH;
        //log-file configs
        outputDir = DEFAULT_OUTPUTDIR;
        outputFile = DEFAULT_OUTPUTFILE;
        //database configurations
        dbUsername = DEFAULT_DBUSERNAME;
        dbPassword = DEFAULT_DBPASSWORD;
        dbHostname = DEFAULT_DBHOSTNAME;
        dbPort = DEFAULT_DBPORT;
        dbDriver = DEFAULT_DBDRIVER;
        dbName = DEFAULT_DBNAME;
        dbSettings = DEFAULT_DBSETTINGS;

        session_timeout = DEFAULT_SESSION_TIMEOUT;
        token_expr = DEFAULT_TOKEN_EXPR;
    }

    public Configs(ConfigsModel cm) throws NullPointerException {
        if (cm == null) {
            throw new NullPointerException("Unable to create Configs from ConfigsModel.");
        } else {
            // Set service configs
            scheme = cm.getServiceConfig().get("scheme");
            if (scheme == null) {
                scheme = DEFAULT_SCHEME;
                System.err.println("Scheme not found in configuration file. Using default.");
            } else {
                System.err.println("Scheme: " + scheme);
            }

            hostName = cm.getServiceConfig().get("hostName");
            if (hostName == null) {
                hostName = DEFAULT_HOSTNAME;
                System.err.println("Hostname not found in configuration file. Using default.");
            } else {
                System.err.println("Hostname: " + hostName);
            }

            port = Integer.parseInt(cm.getServiceConfig().get("port"));
            if (port == 0) {
                port = DEFAULT_PORT;
                System.err.println("Port not found in configuration file. Using default.");
            } else if (port < 1024 || port > 65536) {
                port = DEFAULT_PORT;
                System.err.println("Port is not within valid range. Using default.");
            } else {
                System.err.println("Port: " + port);
            }

            path = cm.getServiceConfig().get("path");
            if (path == null) {
                path = DEFAULT_PATH;
                System.err.println("Path not found in configuration file. Using default.");
            } else {
                System.err.println("Path: " + path);
            }

            // Set logger configs
            outputDir = cm.getLoggerConfig().get("outputDir");
            if (outputDir == null) {
                outputDir = DEFAULT_OUTPUTDIR;
                System.err.println("Logging output directory not found in configuration file. Using default.");
            } else {
                System.err.println("Logging output directory: " + outputDir);
            }

            outputFile = cm.getLoggerConfig().get("outputFile");
            if (outputFile == null) {
                outputFile = DEFAULT_OUTPUTFILE;
                System.err.println("Logging output file not found in configuration file. Using default.");
            } else {
                System.err.println("Logging output file: " + outputFile);
            }
            //set database configs
            dbUsername = cm.getDatabaseConfig().get("dbUsername");
            if (dbUsername == null) {
                dbUsername = DEFAULT_DBUSERNAME;
                System.err.println("Database username not found in configuration file. Using default.");
            } else {
                System.err.println("Database user name: " + dbUsername);
            }

            dbPassword = cm.getDatabaseConfig().get("dbPassword");
            if (dbPassword == null) {
                dbPassword = DEFAULT_DBPASSWORD;
                System.err.println("Database Password not found in configuration file. Using default.");
            } else {
                System.err.println("Database Password: " + dbPassword);
            }

            dbPort = Integer.parseInt(cm.getDatabaseConfig().get("dbPort"));
            if (dbPort == 0) {
                dbPort = DEFAULT_DBPORT;
                System.err.println("Database Port not found in configuration file. Using default.");
            } else if (dbPort < 1024 || dbPort > 65536) {
                dbPort = DEFAULT_DBPORT;
                System.err.println("Database Port is not within valid range. Using default.");
            } else {
                System.err.println("Database Port: " + dbPort);
            }

            dbHostname = cm.getDatabaseConfig().get("dbHostname");
            if (dbHostname == null) {
                dbHostname = DEFAULT_DBHOSTNAME;
                System.err.println("Database hostname not found in configuration file. Using default.");
            } else {
                System.err.println("Database Hostname: " + dbHostname);
            }

            dbDriver = cm.getDatabaseConfig().get("dbDriver");
            if (dbDriver == null) {
                dbDriver = DEFAULT_DBDRIVER;
                System.err.println("Database Driver not found in configuration file. Using default.");
            } else {
                System.err.println("Database Driver " + dbDriver);
            }

            dbName = cm.getDatabaseConfig().get("dbName");
            if (dbName == null) {
                dbName = DEFAULT_DBNAME;
                System.err.println("Database Name not found in configuration file. Using default.");
            } else {
                System.err.println("Database Name: " + dbName);
            }

            dbSettings = cm.getDatabaseConfig().get("dbSettings");
            if (dbSettings == null) {
                dbSettings = DEFAULT_DBSETTINGS;
                System.err.println("Database settings not found in configuration file. Using default.");
            } else {
                System.err.println("Database Settings: " + dbSettings);
            }

            session_timeout = Long.parseLong(cm.getSessionConfig().get("timeout"));
            if (session_timeout == 0) {
                session_timeout = DEFAULT_SESSION_TIMEOUT;
                System.err.println("Session timeout not found in configuration file. Using default.");
            } else {
                System.err.println("Session Time out: " + session_timeout);
            }

            token_expr = Long.parseLong(cm.getSessionConfig().get("expiration"));
            if (token_expr == 0) {
                token_expr = DEFAULT_TOKEN_EXPR;
                System.err.println("Token expiration not found in configuration file. Using default.");
            } else {
                System.err.println("Token Expiration: " + token_expr);
            }
        }
    }

    public void currentConfigs() {
        ServiceLogger.LOGGER.config("Scheme: " + scheme);
        ServiceLogger.LOGGER.config("Hostname: " + hostName);
        ServiceLogger.LOGGER.config("Port: " + port);
        ServiceLogger.LOGGER.config("Path: " + path);
        ServiceLogger.LOGGER.config("Logger output directory: " + outputDir);
        ServiceLogger.LOGGER.config("Logger output file: " + outputFile);
        ServiceLogger.LOGGER.config("Database User Name:" + dbUsername);
        ServiceLogger.LOGGER.config("Database Password: Password form config file");
        ServiceLogger.LOGGER.config("Database Host Name:" + dbHostname);
        ServiceLogger.LOGGER.config("Database port:" + dbPort);
        ServiceLogger.LOGGER.config("Database driver:" + dbDriver);
        ServiceLogger.LOGGER.config("Database Name:" + dbName);
        ServiceLogger.LOGGER.config("Database Settings:" + dbSettings);
        ServiceLogger.LOGGER.config("Session Time out: " + session_timeout);
        ServiceLogger.LOGGER.config("Token Expiration: " + token_expr);
    }

    public String getScheme() {
        return scheme;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbHostname() {
        return dbHostname;
    }

    public int getDbPort() {
        return dbPort;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbSettings() {
        return dbSettings;
    }

    public long getSession_timeout() {
        return session_timeout;
    }

    public long getToken_expr() {
        return token_expr;
    }
}

