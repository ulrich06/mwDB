package org.mwdb.debug;

import io.undertow.Undertow;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.mwdb.KCallback;
import org.mwdb.KGraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.mwdb.KCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server implements HttpHandler {

    private KGraph graph;
    private int port;
    private Undertow _server;

    public Server(KGraph graph, int port) {
        this.graph = graph;
        this.port = port;
        _server = Undertow.builder().addHttpListener(port, "0.0.0.0").setHandler(this).build();
        _server.start();
    }

    public void shutdown() {
        _server.stop();
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

                    /*

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

                    */

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


}
