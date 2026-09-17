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

    /**
     * Carries the address of the host this agent routed to, added to the answer when the rule's host is not
     * replacing Destination-Host. RFC 6733 section 11.1.1 reserves 16777214 and 16777215 for experimental
     * use, so no future allocation can collide with it; code 999, used before, sits in the IETF-managed
     * range and could be assigned to something else. Move this to a vendor-specific code once Naikeri has
     * an SMI Private Enterprise Number. The AVP is added with the M bit clear, so a peer that does not know
     * it ignores it rather than rejecting the answer.
     */
    private static final int NAIKERI_ROUTED_HOST = 16777214;


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
        } else {
            logger.warn("Discarding message with transactionId '{}': unexpected originId '{}', expected DMR or DMA",
                    channelMessage.getTransactionId(), originId);
        }
    }

    private void processRequestMessage(ChannelMessage channelMessage) {
        List<String> previousHost = new ArrayList<>();
        try {
            Request request = (Request) channelMessage.getParameter("REQUEST");
            logger.info("Processing request with sessionId '{}' using transactionId '{}' from originId '{}'",
                    request.getSessionId(), channelMessage.getTransactionId(), channelMessage.getOriginId());

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
                        logger.info("No routing host with the following data: rule '{}' imsi '{}'",
                                rule.getName(), imsi);

                        // drop-policy  // no-routing
                        processDropPolicy(channelMessage, request, "no-routing");
                        break;
                    }
                    host.incrementSentMessages();

                    if (host.getRouteRecord())
                        channelMessage.setParameter("ROUTE_RECORD", true);

                    // Realm Replacement; Host Replacement
                    if (host.getRealm() != null && !host.getRealm().trim().isEmpty()) {
                        answer.getAvps().removeAvp(Avp.DESTINATION_REALM);
                        answer.getAvps().addAvp(Avp.DESTINATION_REALM, host.getRealm().getBytes());
                    }

                    if (host.getAddress() != null && !host.getAddress().trim().isEmpty()) {
                        if (host.isReplaceHost()) {
                            answer.getAvps().removeAvp(Avp.DESTINATION_HOST);
                            answer.getAvps().addAvp(Avp.DESTINATION_HOST, host.getAddress().getBytes());
                        } else {
                            answer.getAvps().addAvp(NAIKERI_ROUTED_HOST, host.getAddress().getBytes());
                        }
                    }
                    if (host.getOriginHost() != null && !host.getOriginHost().trim().isEmpty()) {
                        answer.getAvps().removeAvp(Avp.ORIGIN_HOST);
                        answer.getAvps().addAvp(Avp.ORIGIN_HOST, host.getOriginHost().getBytes());
                    }
                    if (host.getOriginRealm() != null && !host.getOriginRealm().trim().isEmpty()) {
                        answer.getAvps().removeAvp(Avp.ORIGIN_REALM);
                        answer.getAvps().addAvp(Avp.ORIGIN_REALM, host.getOriginRealm().getBytes());
                    }
                    if (host.getDestinationHost() != null && !host.getDestinationHost().trim().isEmpty()) {
                        answer.getAvps().removeAvp(Avp.DESTINATION_HOST);
                        answer.getAvps().addAvp(Avp.DESTINATION_HOST, host.getDestinationHost().getBytes());
                    }
                    if (host.getDestinationRealm() != null && !host.getDestinationRealm().trim().isEmpty()) {
                        answer.getAvps().removeAvp(Avp.DESTINATION_REALM);
                        answer.getAvps().addAvp(Avp.DESTINATION_REALM, host.getDestinationRealm().getBytes());
                    }
                    channelMessage.setParameter("FORWARDING", answer);
                    channelMessage.setParameter("END_SESSION", false);
                    int response = channelHandler.sendMessageResponse(channelMessage);
                    logger.info("Send message response result '{}' for sessionId '{}'", response, answer.getSessionId());
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
                                logger.warn("The value '{}' of the fallback-policy attribute is not valid", value);
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
            logger.warn("Omitting request message '{}' for transactionId '{}' from origin '{}' due to no matching rule found",
                    request.getSessionId(), channelMessage.getTransactionId(), channelMessage.getOriginId());
        }
    }

    private void processAnswerMessage(ChannelMessage channelMessage) {
        Request request = null;
        Answer answer = null;

        try {
            request = (Request) channelMessage.getParameter("REQUEST");
            answer = (Answer) channelMessage.getParameter("ANSWER");
            logger.info("Processing answer with sessionId '{}' corresponding to request '{}' using transactionId '{}' from originId '{}'",
                    answer.getSessionId(), request.getSessionId(), channelMessage.getTransactionId(), channelMessage.getOriginId());
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
                logger.error("Unable to create answer for transactionId '{}'", channelMessage.getTransactionId());
            }
        } catch (Exception e) {
            logger.error("Caught exception while processing answer for transactionId '{}' from origin '{}', request '{}', answer '{}'",
                    channelMessage.getTransactionId(), channelMessage.getOriginId(),
                    ((request == null) ? "null" : request.getSessionId()), ((answer == null) ? "null" : answer.getSessionId()), e);
        }
    }

    public Rule getRule(AvpSet avps) throws AvpDataException {
        String originRealm = avps.getAvp(Avp.ORIGIN_REALM).getUTF8String();
        String originHost = avps.getAvp(Avp.ORIGIN_HOST).getUTF8String();
        String destinationRealm = avps.getAvp(Avp.DESTINATION_REALM).getUTF8String();
        Avp avpDestHost = avps.getAvp(Avp.DESTINATION_HOST);
        String destinationHost = avpDestHost != null ? avpDestHost.getUTF8String() : null;
        // Which AVP carries the IMSI depends on the rule, but the message does not: extract both once here
        // rather than parsing the AVP set again for every rule.
        final String imsiFromImsiAvp = getImsiFromImsiAvp(avps);
        final String imsiFromSubscriptionId = getImsiFromSubscriptionId(avps);

        Optional<Rule> ruleOptional = GettingRules
                .rules().list().stream().filter(rule -> {
                    String imsi = rule.match().isSubscriptionId() ? imsiFromSubscriptionId : imsiFromImsiAvp;
                    boolean match = (rule.isEnabled() && rule.match().imsi().matcher(imsi).matches()
                            && rule.match().originHost().matcher(originHost).matches() && rule.match().originRealm().matcher(originRealm).matches()
                            && rule.match().destinationRealm().matcher(destinationRealm).matches()
                            && (destinationHost == null || rule.match().destinationHost().matcher(destinationHost).matches()));
                    if (match)
                        logger.debug("Match in rule [{}] with the following data: imsi[{}] match imsi[{}] " +
                                        "originHost[{}] match host[{}] originRealm[{}] match realm[{}]",
                                rule.getName(), imsi, rule.match().imsi(), originHost, rule.match().originHost().pattern(),
                                originRealm, rule.match().originRealm().pattern());
                    return match;
                })
                .findFirst();
        if (ruleOptional.isPresent()) {
            return ruleOptional.get();
        } else {
            // verify if exist default rule
            Rule defaultRule = getDefaultRule();
            if (defaultRule != null) return defaultRule;
        }
        logger.info("No matching rule for imsi '{}' (Subscription-Id '{}'), originHost '{}' originRealm '{}'",
                imsiFromImsiAvp, imsiFromSubscriptionId, originHost, originRealm);
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
        return rule.match().isSubscriptionId() ? getImsiFromSubscriptionId(avps) : getImsiFromImsiAvp(avps);
    }

    private String getImsiFromImsiAvp(AvpSet avps) {
        try {
            Avp imsi = avps.getAvp(Avp.TGPP_IMSI);
            if (imsi == null) {
                logger.debug("No IMSI found in [Avp.TGPP_IMSI]");
                return "";
            }
            return imsi.getUTF8String();
        } catch (Exception e) {
            logger.error("Exception caught while reading the IMSI from [Avp.TGPP_IMSI]", e);
            return "";
        }
    }

    private String getImsiFromSubscriptionId(AvpSet avps) {
        try {
            Avp imsi = null;
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
                logger.debug("No IMSI found in [Avp.SUBSCRIPTION_ID_DATA]");
                return "";
            }
            return imsi.getUTF8String();
        } catch (Exception e) {
            logger.error("Exception caught while reading the IMSI from [Avp.SUBSCRIPTION_ID_DATA]", e);
            return "";
        }
    }

    public Host getRoutingHost(Rule rule, List<String> previousHost) {
        Host host = rule.getHostByPriority(previousHost); // get first;
        if (host == null) { // find load balance
            host = rule.getHostByLoadBalance();
        }
        return host;
    }

}