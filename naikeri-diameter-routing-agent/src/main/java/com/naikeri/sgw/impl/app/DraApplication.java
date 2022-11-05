package com.naikeri.sgw.impl.app;

import com.naikeri.sgw.impl.app.handler.RequestHandler;
import com.naikeri.sgw.impl.settings.ApplicationSettings;
import com.naikeri.sgw.impl.app.util.GettingRules;
import com.naikeri.sgw.api.chn.ChannelMessage;
import com.naikeri.sgw.impl.app.model.Host;
import com.naikeri.sgw.impl.app.model.Rule;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.ResultCode;
import org.jdiameter.api.Request;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.Answer;
import org.jdiameter.api.Avp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;

import static com.naikeri.sgw.impl.chn.DraChannel.*;

public class DraApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(DraApplication.class);


    public DraApplication(ApplicationSettings applicationSettings) {
        super(applicationSettings);
    }

    @Override
    public void processMessage(ChannelMessage channelMessage) {
        String originId = channelMessage.getOriginId();
        if ("DMR".equals(originId)) {
            RequestHandler.addRequest((Request) channelMessage.getParameter("REQUEST"));
            processRequestMessage(channelMessage);
        } else if ("DMA".equals(originId)) {
            Request dma = (Request) channelMessage.getParameter("REQUEST");
            channelMessage.setParameter("REQUEST", RequestHandler.getRequest(dma.getSessionId(), dma.getEndToEndIdentifier()));
            processAnswerMessage(channelMessage);
        }
    }

    private void processRequestMessage(ChannelMessage channelMessage) {
        List<String> previousHost = new ArrayList<>();
        try {
            Request request = (Request) channelMessage.getParameter("REQUEST");
            logger.info(String.format("Processing request with sessionId '%s' using transactionId '%s' from originId '%s'",
                    request.getSessionId(), channelMessage.getTransactionId(), channelMessage.getOriginId()));

            // Create request and configure avps
            //Request msg = new MessageParser().createEmptyMessage(request.getCommandCode(), request.getApplicationId());
            Answer answer = request.createAnswer();
            answer.setRequest(true);
            answer.setProxiable(true);

            AvpSet avps = request.getAvps();
            avps.removeAvp(Avp.AUTH_APPLICATION_ID);
            avps.removeAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID);
            answer.getAvps().removeAvp(Avp.SESSION_ID);
            answer.getAvps().addAvp(avps);
            Rule rule = getRule(avps);

            if (rule != null) {
                while (true) {
                    Host host = getRoutingHost(rule, previousHost);
                    if (host == null || previousHost.contains(host.getName())) {
                        // verify if exist default rule.
                        Rule defaultRule = getDefaultRule();
                        if (defaultRule != null) {
                            previousHost.clear();
                            rule = defaultRule;
                            continue;
                        }
                        String imsi = getImsi(rule, avps);
                        logger.info(String.format("No routing host with the following data: rule '%s' imsi '%s'",
                                rule.getName(), imsi));

                        // drop-policy  // no-routing
                        processDropPolicy(channelMessage, request, "no-routing");
                        break;
                    }
                    host.incrementSentMessages();

                    if (host.getRouteRecord())
                        channelMessage.setParameter("ROUTE_RECORD", true);

                    // Realm Replacement; Host Replacement
                    if (host.getRealm() != null && !"".equals(host.getRealm().trim())) {
                        answer.getAvps().removeAvp(Avp.DESTINATION_REALM);
                        answer.getAvps().addAvp(Avp.DESTINATION_REALM, host.getRealm().getBytes());
                    }

                    if (host.getAddress() != null && !"".equals(host.getAddress().trim())) {
                        if (host.isReplaceHost()) {
                            answer.getAvps().removeAvp(Avp.DESTINATION_HOST);
                            answer.getAvps().addAvp(Avp.DESTINATION_HOST, host.getAddress().getBytes());
                        } else {
                            answer.getAvps().addAvp(999, host.getAddress().getBytes());
                        }
                    }
                    if (host.getOriginHost() != null && !"".equals(host.getOriginHost().trim())) {
                        answer.getAvps().removeAvp(Avp.ORIGIN_HOST);
                        answer.getAvps().addAvp(Avp.ORIGIN_HOST, host.getOriginHost().getBytes());
                    }
                    if (host.getOriginRealm() != null && !"".equals(host.getOriginRealm().trim())) {
                        answer.getAvps().removeAvp(Avp.ORIGIN_REALM);
                        answer.getAvps().addAvp(Avp.ORIGIN_REALM, host.getOriginRealm().getBytes());
                    }
                    if (host.getDestinationHost() != null && !"".equals(host.getDestinationHost().trim())) {
                        answer.getAvps().removeAvp(Avp.DESTINATION_HOST);
                        answer.getAvps().addAvp(Avp.DESTINATION_HOST, host.getDestinationHost().getBytes());
                    }
                    if (host.getDestinationRealm() != null && !"".equals(host.getDestinationRealm().trim())) {
                        answer.getAvps().removeAvp(Avp.DESTINATION_REALM);
                        answer.getAvps().addAvp(Avp.DESTINATION_REALM, host.getDestinationRealm().getBytes());
                    }
                    channelMessage.setParameter("FORWARDING", answer);
                    channelMessage.setParameter("END_SESSION", false);
                    int response = channelHandler.sendMessageResponse(channelMessage);
                    logger.info(String.format("Send message response result '%d' for sessionId '%s'", response, answer.getSessionId()));
                    if (response == SUCCESSFUL_RESPONSE) {
                        break;
                    } else if (rule.getFallbackPolicy().contains((response == NETWORK_ERROR ? "network" : response == FAILED_RESPONSE ? "error" : "unknown"))) {
                        // fallback-policy
                        previousHost.add(host.getName());
                    } else {
                        //no-fallback-policy
                        rule.getFallbackPolicy().forEach(value -> {
                            // TODO this should be checked on loading just once
                            if (!Arrays.asList("error", "network").contains(value)) {
                                logger.warn(String.format("The value '%s' of the fallback-policy attribute is not valid", value));
                            }
                        });
                        break;
                    }
                }
            } else {
                // drop-policy
                processDropPolicy(channelMessage, request, "no-match");
            }

        } catch (AvpDataException ex) {
            // TODO consider all the potential exception thrown or propagater over protected code - error is too generic to find something out of it
            logger.error("Error in getting AVP", ex);
        }
    }

    private void processDropPolicy(ChannelMessage channelMessage, Request request, String policy) {
        if (!GettingRules.rules().dropPolicyEnabled(policy)) {
            Answer answerPolicy = request.createAnswer(ResultCode.UNABLE_TO_COMPLY);
            channelMessage.setParameter("FORWARDING", answerPolicy);
            channelMessage.setParameter("END_SESSION", true);
            channelHandler.sendMessageResponse(channelMessage);
        } else {
            logger.warn(String.format("Omitting request message '%s' for transactionId '%s' from origin '%s' due to no matching rule found",
                    request.getSessionId(), channelMessage.getTransactionId(), channelMessage.getOriginId()));
        }
    }

    private void processAnswerMessage(ChannelMessage channelMessage) {
        Request request = null;
        Answer answer = null;

        try {
            request = (Request) channelMessage.getParameter("REQUEST");
            answer = (Answer) channelMessage.getParameter("ANSWER");
            logger.info(String.format("Processing answer with sessionId '%s' corresponding to request '%s' using transactionId '%s' from originId '%s'",
                    answer.getSessionId(), request.getSessionId(), channelMessage.getTransactionId(), channelMessage.getOriginId()));
            Answer forwardAnswer = request.createAnswer();
            if (forwardAnswer != null) {
                request.getAvps().removeAvp(Avp.AUTH_APPLICATION_ID);
                request.getAvps().removeAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID);
                forwardAnswer.getAvps().removeAvp(Avp.SESSION_ID);
                forwardAnswer.getAvps().addAvp(answer.getAvps());
                channelMessage.setParameter("ROUTE_RECORD", false);
                channelMessage.setParameter("FORWARDING", forwardAnswer);
                channelMessage.setParameter("END_SESSION", true);
                channelHandler.sendMessageResponse(channelMessage);
            } else {
                logger.error(String.format("Unable to create answer for transactionId '%s'", channelMessage.getTransactionId()));
            }
        } catch (Exception e) {
            logger.error(String.format("Exception '%s' caught while processing answer for transactionId '%s' from origin '%s', request '%s', answer '%s'",
                    e.getMessage(), channelMessage.getTransactionId(), channelMessage.getOriginId(),
                    ((request == null) ? "null" : request.getSessionId()), ((answer == null) ? "null" : answer.getSessionId())));
        }
    }

    public Rule getRule(AvpSet avps) throws AvpDataException {
        String originRealm = avps.getAvp(Avp.ORIGIN_REALM).getUTF8String();
        String originHost = avps.getAvp(Avp.ORIGIN_HOST).getUTF8String();
        String destinationRealm = avps.getAvp(Avp.DESTINATION_REALM).getUTF8String();
        Avp avpDestHost = avps.getAvp(Avp.DESTINATION_HOST);
        String destinationHost = avpDestHost != null ? avpDestHost.getUTF8String() : null;
        final String[] imsi = {""};

        Optional<Rule> ruleOptional = GettingRules
                .rules().list().stream().filter(rule -> {
                    boolean match = (rule.match().imsi().matcher((imsi[0] = getImsi(rule, avps))).matches() && rule.isEnabled()
                            && rule.match().originHost().matcher(originHost).matches() && rule.match().originRealm().matcher(originRealm).matches()
                            && rule.match().destinationRealm().matcher(destinationRealm).matches()
                            && (destinationHost == null || rule.match().destinationHost().matcher(destinationHost).matches()));
                    if (match)
                        logger.debug(String.format("Match in rule [%s] with the following data: imsi[%s] match imsi[%s] " +
                                        "originHost[%s] match host%s originRealm[%s] match realm[%s]",
                                rule.getName(), imsi[0], rule.match().imsi().toString(), originHost, rule.match().originHost().pattern(),
                                originRealm, rule.match().originRealm().pattern()));
                    return match;
                })
                .findFirst();
        if (ruleOptional.isPresent()) {
            Rule rule = ruleOptional.get();
            return rule;
        } else {
            // verify if exist default rule
            Rule defaultRule = getDefaultRule();
            if (defaultRule != null) return defaultRule;
        }
        logger.info(String.format("No matching rule for imsi '%s', originHost '%s' originRealm '%s'", imsi[0], originHost, originRealm));
        return null;
    }

    private Rule getDefaultRule() {
        Optional<Rule> defaultRule = GettingRules.rules().list().stream().filter(rule -> rule.getDefault() && rule.isEnabled()).findFirst();
        if (defaultRule.isPresent()) {
            return defaultRule.get();
        }
        return null;
    }

    private String getImsi(Rule rule, AvpSet avps) {
        Avp imsi = null;
        try {
            if (!rule.match().isSubscriptionId()) {
                imsi = avps.getAvp(Avp.TGPP_IMSI);
                if (imsi == null) {
                    logger.debug(String.format("No IMSI found in [Avp.TGPP_IMSI], rule name=[%s]", rule.getName()));
                    return "";
                }
            } else {
                if (avps.getAvps(Avp.SUBSCRIPTION_ID) != null) {
                    for (Avp avp : avps.getAvps(Avp.SUBSCRIPTION_ID).asArray()) {
                        AvpSet grouped = avp.getGrouped();
                        if (grouped.getAvp(Avp.SUBSCRIPTION_ID_TYPE).getInteger32() == 1) {
                            imsi = grouped.getAvp(Avp.SUBSCRIPTION_ID_DATA);
                            break;
                        }
                    }
                }
                if (imsi == null) {
                    logger.debug(String.format("No IMSI found in [Avp.SUBSCRIPTION_ID_DATA], rule name=[%s]", rule.getName()));
                    return "";
                }
            }
            return imsi.getUTF8String();
        } catch (Exception e) {
            logger.error("Exception caught while get Imsi in the {} rule", rule.getName(), e);
        }
        return "";
    }

    public Host getRoutingHost(Rule rule, List<String> previousHost) {
        Host host = rule.getHostByPriority(previousHost); // get first;
        if (host == null) { // find load balance
            host = rule.getHostByLoadBalance();
        }
        return host;
    }

}