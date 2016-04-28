package org.mwg;

import io.undertow.Undertow;
import io.undertow.connector.ByteBufferPool;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.client.*;
import io.undertow.websockets.client.WebSocketClient;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.plugin.Storage;
import org.mwg.struct.Buffer;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.io.IOException;
import java.net.URI;

/**
 * Wrapper storage
 */
public class WebSocketServer implements Storage, WebSocketConnectionCallback, HttpHandler{
    private Storage _wrapped;
    private Undertow _server;



    public WebSocketServer(Storage _wrapped, int port) {
        this._wrapped = _wrapped;
        _server = Undertow.builder().addHttpListener(port,"0.0.0.0").setHandler(this).build();
    }

    @Override
    public void get(Buffer[] keys, Callback<Buffer[]> callback) {
        this._wrapped.get(keys,callback);
    }

    @Override
    public void put(Buffer[] keys, Buffer[] values, Callback<Boolean> callback) {
        this._wrapped.put(keys,values,callback);
    }

    @Override
    public void remove(Buffer[] keys, Callback<Boolean> callback) {
        this._wrapped.remove(keys,callback);
    }

    @Override
    public void connect(Graph graph, Callback<Short> callback) {
        this._wrapped.connect(graph, new Callback<Short>() {
            @Override
            public void on(Short prefix) {
                _server.start();

                if (PrimitiveHelper.isDefined(callback)) {
                    callback.on(prefix);
                }

            }
        });
    }

    @Override
    public void disconnect(Short prefix, Callback<Boolean> callback) {
        this._wrapped.disconnect(prefix, new Callback<Boolean>() {
            @Override
            public void on(Boolean succeed) {
                _server.stop();
                if (PrimitiveHelper.isDefined(callback)) {
                    callback.on(succeed);
                }
            }
        });
    }

    @Override
    public void onConnect(WebSocketHttpExchange webSocketHttpExchange, WebSocketChannel webSocketChannel) {
            webSocketChannel.getReceiveSetter().set(new AbstractReceiveListener() {
                @Override
                protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
                    super.onFullBinaryMessage(channel, message);
                }

                @Override
                protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
                    String data = message.getData();
                    System.out.println(data + "[" + message + "]");
                }
            });
        webSocketChannel.resumeReceives();
    }


    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {

    }

    public static void main(String[] args) throws Exception {
        WebSocketServer server = new WebSocketServer(null,8080);

        XnioWorker _worker;
        Xnio xnio = Xnio.getInstance(io.undertow.websockets.client.WebSocketClient.class.getClassLoader());
        _worker = xnio.createWorker(OptionMap.builder()
                .set(Options.WORKER_IO_THREADS, 2)
                .set(Options.CONNECTION_HIGH_WATER, 1000000)
                .set(Options.CONNECTION_LOW_WATER, 1000000)
                .set(Options.WORKER_TASK_CORE_THREADS, 30)
                .set(Options.WORKER_TASK_MAX_THREADS, 30)
                .set(Options.TCP_NODELAY, true)
                .set(Options.CORK, true)
                .getMap());
        ByteBufferPool _buffer = new DefaultByteBufferPool(true, 1024 * 1024);
        WebSocketChannel _webSocketChannel = WebSocketClient.connectionBuilder(_worker,_buffer,new URI("wss://0.0.0.0:8080")).connect();


    }
}
