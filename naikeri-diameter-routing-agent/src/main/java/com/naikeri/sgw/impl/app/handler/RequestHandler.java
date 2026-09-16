package com.naikeri.sgw.impl.app.handler;

import com.naikeri.sgw.network.layers.DiameterLayer;
import org.jdiameter.api.Request;
import org.jdiameter.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;

public class RequestHandler { // extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static final Long EXPIRE_AFTER = 30000L;
    public static final Map<String, Map<String, Object>> requests = Collections.synchronizedMap(new HashMap<>());

    private static RequestHandler instance = null;
    private static DiameterLayer diameter = null;

    private static synchronized void getInstance(DiameterLayer diameterLayer) {
        if (instance == null) {
            diameter = diameterLayer;
            instance = new RequestHandler();
        }
    }

    public static void initialize(DiameterLayer diameterLayer) {
        RequestHandler.getInstance(diameterLayer);
    }

    public RequestHandler() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    logger.debug("Executing task at [{}]", new Date());
                    int sessionLength = requests.size();
                    if (removeIf(f -> ((long) f.getValue().get("dateExpire")) < System.currentTimeMillis())) {
                        logger.info("Deleting '{}' out of '{}' sessions", (sessionLength - requests.size()), sessionLength);
                    }
                } catch (Exception e) {
                    logger.error("Exception caught while aging sessions! ", e);
                }
            }
        }, 0, 60000);
    }

    private boolean removeIf(Predicate<? super Map.Entry<String, Map<String, Object>>> filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        synchronized (requests) {
            final Iterator<Map.Entry<String, Map<String, Object>>> each = requests.entrySet().iterator();
            while (each.hasNext()) {
                final Map.Entry<String, Map<String, Object>> item = each.next();
                if (filter.test(item)) {
                    each.remove();
                    try {
                        Session session = diameter.getSession(((Request) item.getValue().get("request")).getSessionId());
                        session.release();
                    } catch (Exception e) {
                        logger.error("Exception caught while sessions release! ", e);
                    }
                    removed = true;
                }
            }
        }
        return removed;
    }

    public static void addRequest(Request request) {
        String key = getKey(request.getSessionId(), request.getEndToEndIdentifier());
        synchronized (requests) {
            if (!requests.containsKey(key)) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("request", request);
                entry.put("dateExpire", System.currentTimeMillis() + EXPIRE_AFTER);
                requests.put(key, entry);
            }
        }
    }

    /**
     * Takes the pending request out of the cache. Each request is looked up once, when its answer arrives, so
     * removing it here lets the cache follow the transactions in flight; the aging sweep is then left to catch
     * only requests that never received an answer. A duplicate answer therefore finds nothing and is dropped.
     */
    public static Request getRequest(String sessionId, long endToEndIdentifier) {
        Request request = null;
        String key = getKey(sessionId, endToEndIdentifier);
        Map<String, Object> entry = requests.remove(key);
        if (entry != null) {
            request = (Request) entry.get("request");
            if (request == null) {
                logger.error("Recovered null value for sessionId '{}' from request cache", sessionId);
            }
        } else {
            logger.error("Unable to find sessionId '{}' in request cache!", sessionId);
        }

        return request;
    }

    private static String getKey(String sessionId, Long endToEndIdentifier) {
        return String.format("%s:%s", sessionId, endToEndIdentifier);
    }
}