package com.naikeri.sgw.impl.app.util;

import com.naikeri.sgw.helpers.SgwResource;
import com.naikeri.sgw.impl.app.model.SignalingGatewayRules;
import com.naikeri.sgw.impl.app.model.Rules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CoderResult;
import java.nio.file.*;

/**
 * @author joram
 */
public class GettingRules extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(GettingRules.class);
    private static final String FILE_NAME = "naikeri-signaling-gateway.xml";
    private static final Integer APPLICATION_COUNT = 0;
    private static SignalingGatewayRules sgRules;
    private static Unmarshaller unmarshaller;
    private static Rules rules = null;

    private static GettingRules instance = null;

    private static GettingRules getInstance() {
        if (instance == null) {
            instance = new GettingRules();
        }
        return instance;
    }

    /**
     * Create the JAXBContext instance
     */
    public static GettingRules initialize() {

        JAXBContext jaxbContext;
        GettingRules.getInstance();
        try {
            logger.info("Creating  JAXBContextInstance");
            jaxbContext = JAXBContext.newInstance(com.naikeri.sgw.impl.app.model.SignalingGatewayRules.class);
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            logger.error("Exception caught while initialize GettingRules! ", e);
        }
        loadXml();
        return instance;
    }

    /**
     * Invoke this constructor when you need to synchronize xml data
     */
    public static void loadXml() {
        logger.info("Loading new rule changes ...");
        InputStream xml = new SgwResource(FILE_NAME).getAsStream();
        try {
            sgRules = (com.naikeri.sgw.impl.app.model.SignalingGatewayRules) unmarshaller.unmarshal(xml);
            rules = sgRules.getApplications()
                    .get(APPLICATION_COUNT)
                    .getRules();
        } catch (Exception e) {
            logger.error("Error loading rules: " + e.getMessage());
            Runtime.getRuntime().halt(1);
        }

    }

    /**
     * @return returns the list of rules defined in the xml
     */
    public static Rules rules() {
        if (rules == null) {
            logger.warn("Please initialize GettingRules in your main");
            initialize();
        }
        return rules;
    }

    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(getFilePath(FILE_NAME));
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    watchService.close();
                } catch (IOException e) {
                    logger.error("WatchService error: " + e.getMessage());
                }
            }));

            WatchKey key;
            while (true) {
                key = watchService.take();
                Thread.sleep(50);
                if (key.pollEvents().stream().anyMatch(event -> event.kind() != CoderResult.OVERFLOW && event.context().toString().equals(FILE_NAME))) {
                    loadXml();
                }
                boolean reset = key.reset();
                if (!reset) {
                    logger.warn("Could not reset the watch key.");
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.error("WatchService error: " + e.getMessage());
        }
    }

    private String getFilePath(String filename) {
        boolean isConfigPath = System.getProperties().containsKey("mainConfig.path");
        String path = (isConfigPath ? System.getProperty("mainConfig.path") :
                System.getProperty("user.dir"));

        File file = new File(path + "/" + filename);
        if (file.exists()) {
            return path;
        } else {
            return this.getClass().getClassLoader().getResource(filename).getPath();
        }
    }
}
