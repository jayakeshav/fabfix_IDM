package edu.uci.ics.jkotha.service.idm.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class ServiceLogger {
    public static final Logger LOGGER = Logger.getLogger(ServiceLogger.class.getName());
    public static FileHandler fileHandler;
    public static Formatter formatter;

    public static void initLogger(String outputDir, String outputFile) throws IOException {
        System.err.println("Logger Starting");

        LOGGER.getParent().removeHandler(LOGGER.getParent().getHandlers()[0]);

        File logDir = new File(outputDir);
        if(!(logDir.exists())){
            logDir.mkdir();
        }

        fileHandler = new FileHandler(outputDir+outputFile);

        formatter= new ServiceFormatter();

        LOGGER.addHandler(fileHandler);

        fileHandler.setFormatter(formatter);

        ConsoleHandler consoleHandler =new ConsoleHandler();

        consoleHandler.setLevel(Level.CONFIG);

        LOGGER.addHandler(consoleHandler);

        consoleHandler.setFormatter(formatter);

        fileHandler.setLevel(Level.ALL);

        LOGGER.setLevel(Level.ALL);

        LOGGER.config("Logging Initialised");
    }
}
