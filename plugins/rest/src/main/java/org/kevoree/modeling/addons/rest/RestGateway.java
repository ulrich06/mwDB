package org.kevoree.modeling.addons.rest;

import io.undertow.Undertow;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KConfig;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.memory.chunk.KStringMap;
import org.kevoree.modeling.memory.chunk.KStringMapCallBack;
import org.kevoree.modeling.memory.chunk.impl.ArrayStringMap;
import org.kevoree.modeling.traversal.query.impl.QueryEngine;
import org.kevoree.modeling.util.PrimitiveHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RestGateway implements HttpHandler {

    private int _port;
    private Undertow _server;
    private KModel _model;

    public RestGateway(KModel p_model, int p_port) {
        this._port = p_port;
        this._model = p_model;
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        String rawPath = httpServerExchange.getRelativePath();
        if (rawPath.startsWith("/")) {
            rawPath = rawPath.substring(1);
        }
        String[] parts = rawPath.split("/");
        long universe;
        long time;
        if (parts.length >= 3) {
            try {
                universe = Long.parseLong(parts[0]);
                time = Long.parseLong(parts[1]);
                StringBuilder concatQuery = new StringBuilder();
                for (int i = 2; i < parts.length; i++) {
                    if (concatQuery.length() > 0) {
                        concatQuery.append(" | ");
                    }
                    concatQuery.append(parts[i]);
                }

                if (httpServerExchange.getRequestMethod().equalToString("GET")) {
                    httpServerExchange.dispatch();
                    _model.universe(universe).time(time).select(concatQuery.toString(), new KCallback<Object[]>() {
                        @Override
                        public void on(Object[] objects) {
                            httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                            Sender sender = httpServerExchange.getResponseSender();
                            StringBuilder builder = new StringBuilder();
                            builder.append("[\n");
                            for (int i = 0; i < objects.length; i++) {
                                if (i != 0) {
                                    builder.append(",\n");
                                }
                                builder.append(((KObject) objects[i]).toString());
                            }
                            builder.append("\n]\n");
                            sender.send(builder.toString());
                            httpServerExchange.endExchange();
                        }
                    });
                } else {
                    //avoid blocking the main HTTP server thread
                    if (httpServerExchange.isInIoThread()) {
                        httpServerExchange.dispatch(this);
                        return;
                    }
                    //read the post payload
                    BufferedReader reader = null;
                    StringBuilder builder = new StringBuilder();
                    try {
                        httpServerExchange.startBlocking();
                        reader = new BufferedReader(new InputStreamReader(httpServerExchange.getInputStream()));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    final String body = builder.toString();
                    //free the HTTP server
                    httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "test/plain");
                    httpServerExchange.getResponseSender().send(body.length() + "");
                    httpServerExchange.endExchange();

                    KStringMap<String> params = new ArrayStringMap<String>(KConfig.CACHE_INIT_SIZE, KConfig.CACHE_LOAD_FACTOR);
                    int iParam = 0;
                    int lastStart = iParam;
                    while (iParam < body.length()) {
                        if (body.charAt(iParam) == QueryEngine.VALS_SEP) {
                            String p = body.substring(lastStart, iParam).trim();
                            if (!PrimitiveHelper.equals(p, "")) {
                                String[] pArray = p.split(QueryEngine.VAL_SEP);
                                if (pArray.length > 1) {
                                    params.put(pArray[0].trim(), pArray[1].trim());
                                }
                            }
                            lastStart = iParam + 1;
                        }
                        iParam = iParam + 1;
                    }
                    String lastParam = body.substring(lastStart, iParam).trim();
                    if (!PrimitiveHelper.equals(lastParam, "")) {
                        String[] pArray = lastParam.split(QueryEngine.VAL_SEP);
                        if (pArray.length > 1) {
                            params.put(pArray[0].trim(), pArray[1].trim());
                        }
                    }
                    _model.universe(universe).time(time).select(concatQuery.toString(), new KCallback<Object[]>() {
                        @Override
                        public void on(Object[] objects) {
                            for (int i = 0; i < objects.length; i++) {
                                if (objects[i] instanceof KObject) {
                                    KObject loopObj = (KObject) objects[i];
                                    params.each(new KStringMapCallBack<String>() {
                                        @Override
                                        public void on(String key, String value) {
                                            loopObj.setByName(key, value);
                                        }
                                    });
                                }
                            }
                        }
                    });

                }
                
            } catch (Exception e) {
                httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                httpServerExchange.getResponseSender().send("Bad API usage: " + e.getMessage());
            }
        } else {
            httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            httpServerExchange.getResponseSender().send("Bad URL format");
        }
    }

    public static RestGateway expose(KModel model, int port) {
        RestGateway newgateway = new RestGateway(model, port);
        return newgateway;
    }

    public void start() {
        _server = Undertow.builder().addHttpListener(_port, "0.0.0.0").setHandler(this).build();
        _server.start();
    }

}
