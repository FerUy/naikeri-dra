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
import java.util.stream.Collectors;

public class DraChannel extends ChannelHandler {

    public final static int FAILED_RESPONSE = -2;
    public final static int NETWORK_ERROR = -1;
    public final static int SUCCESSFUL_RESPONSE = 0;
    private RealmRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(DraChannel.class);

    DiameterLayer diameter = null;

    public DraChannel(ChannelSettings channelSetting) {
        super(channelSetting);
        repository = DataSource.initialize();
    }

    @Override
    public void channelInitialize(LayerInterface[] layerInterfaces) {
        diameter = (DiameterLayer) layerInterfaces[0];
        RequestHandler.initialize(diameter);
        try {
            List<Realm> realms = repository.findAll();

            diameter.addRealms(realms.stream()
                    .map(realm -> new RealmImpl(realm.getName(), realm.getDiameterApplicationId(), realm.getLocalAction(),
                            null, null, realm.getDynamic(), realm.getExpTime(), realm.getPeers()))
                    .collect(Collectors.toList()).toArray(new IRealm[]{}));
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
        logger.info("Sending message '" + channelMessage.toString() + "' to application.");
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
                    logger.warn(String.format("Caught exception '%s' while sending response for sessionId [%s], returning %s.",
                            e.getMessage(), message.getSessionId(), result == NETWORK_ERROR ? "NETWORK_ERROR" : "FAILED_RESPONSE"), e);
                    return result;
                }
            }
            logger.info(String.format("Message '%s' sent to diameter.", message.getSessionId()));
        } catch (Exception e) {
            logger.error(String.format("Caught exception '%s' while sending response for transactionId [%s], returning FAILED_RESPONSE.",
                    e.getMessage(), channelMessage.getTransactionId()));
            return FAILED_RESPONSE;
        }

        return SUCCESSFUL_RESPONSE;
    }

    @Override
    public void onReceiveUnknownRealm(IRealm unknownRealm) {

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