package com.naikeri.sgw.impl.chn;

import com.naikeri.sgw.api.chn.ChannelMessage;
import com.naikeri.sgw.api.network.LayerInterface;
import com.naikeri.sgw.impl.app.handler.RequestHandler;
import com.naikeri.sgw.impl.db.DataSource;
import com.naikeri.sgw.impl.db.entity.ApplicationId;
import com.naikeri.sgw.impl.db.entity.Realm;
import com.naikeri.sgw.impl.db.repository.RealmRepository;
import com.naikeri.sgw.impl.settings.ChannelSettings;
import com.naikeri.sgw.network.layers.DiameterLayer;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.Message;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.Session;
import org.jdiameter.client.api.controller.IRealm;
import org.jdiameter.client.impl.controller.RealmImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DraChannel extends ChannelHandler {

    public final static int FAILED_RESPONSE = -2;
    public final static int NETWORK_ERROR = -1;
    public final static int SUCCESSFUL_RESPONSE = 0;
    private final RealmRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(DraChannel.class);

    private DiameterLayer diameter = null;

    public DraChannel(ChannelSettings channelSetting) {
        super(channelSetting);
        repository = DataSource.initialize();
    }

    @Override
    public void channelInitialize(LayerInterface[] layerInterfaces) {
        diameter = (DiameterLayer) layerInterfaces[0];
        RequestHandler.initialize(diameter);
        if (repository == null) {
            // DataSource has already reported why; the realms configured in diameter-server.xml still apply.
            logger.info("No realm repository, so no stored realms are loaded");
            return;
        }
        try {
            List<Realm> realms = repository.findAll();
            if (realms == null || realms.isEmpty()) {
                logger.info("No stored realms to load");
                return;
            }
            diameter.addRealms(realms.stream()
                    .map(realm -> new RealmImpl(realm.getName(), realm.getDiameterApplicationId(), realm.getLocalAction(),
                            null, null, realm.getDynamic(), realm.getExpTime(), realm.getPeers()))
                    .toArray(IRealm[]::new));
            logger.info("Loaded '{}' realms from the repository", realms.size());
        } catch (Exception e) {
            logger.error("Error loading realms", e);
        }
    }

    @Override
    public LayerInterface getLayerInterface(String s) {
        return null;
    }

    @Override
    public LayerInterface getLayerInterface() {
        return diameter;
    }

    @Override
    public void receiveMessageRequest(ChannelMessage channelMessage) {
        // STEP 1: message to be received from reference point (map, cap, diameter, ...)
        logger.info("Sending message '{}' to application.", channelMessage);
        sendMessageRequest(channelMessage);
    }

    @Override
    public int sendMessageResponse(ChannelMessage channelMessage) {
        // STEP 2: message to be replied over reference point (ss7, diameter, ...)
        try {
            Message message = (Message) channelMessage.getParameter("FORWARDING");
            Boolean routeRecord = (Boolean) channelMessage.getParameter("ROUTE_RECORD");
            Session newSession = diameter.getSession(message.getSessionId());
            if (newSession != null) {
                try {
                    diameter.sendMessage(newSession, message, routeRecord);
                    /*if (endSession) {
                        newSession.release();
                    }*/
                } catch (InternalException | IllegalDiameterStateException | RouteException | OverloadException e) {
                    final int result = (e instanceof RouteException) ? NETWORK_ERROR : FAILED_RESPONSE;
                    logger.warn("Caught exception while sending response for sessionId [{}], returning {}.",
                            message.getSessionId(), result == NETWORK_ERROR ? "NETWORK_ERROR" : "FAILED_RESPONSE", e);
                    return result;
                }
            }
            logger.info("Message '{}' sent to diameter.", message.getSessionId());
        } catch (Exception e) {
            logger.error("Caught exception while sending response for transactionId [{}], returning FAILED_RESPONSE.",
                    channelMessage.getTransactionId(), e);
            return FAILED_RESPONSE;
        }

        return SUCCESSFUL_RESPONSE;
    }

    @Override
    public void onReceiveUnknownRealm(IRealm unknownRealm) {
        if (repository == null) {
            logger.debug("Realm '{}' not persisted: no realm repository", unknownRealm.getName());
            return;
        }
        try {
            Realm realm = new Realm();
            realm.setName(unknownRealm.getName());
            realm.setPeers(unknownRealm.getPeerNames());
            realm.setLocalAction(unknownRealm.getLocalAction().name());
            realm.setDynamic(unknownRealm.isDynamic());
            realm.setExpTime(unknownRealm.getExpirationTime());
            ApplicationId applicationId = new ApplicationId();
            applicationId.setVendorId(unknownRealm.getApplicationId().getVendorId());
            applicationId.setAuthApplId(unknownRealm.getApplicationId().getAuthAppId());
            applicationId.setAcctApplId(unknownRealm.getApplicationId().getAcctAppId());
            realm.setApplicationId(applicationId);
            repository.saveRealm(realm);
        } catch (Exception e) {
            logger.warn("Exception caught", e);
        }

    }

}