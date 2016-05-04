package org.mwg.ws;

import io.undertow.connector.ByteBufferPool;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.client.WebSocketClient;
import io.undertow.websockets.core.*;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.core.CoreConstants;
import org.mwg.core.utility.Base64;
import org.mwg.plugin.Storage;
import org.mwg.struct.Buffer;
import org.mwg.struct.BufferIterator;
import org.mwg.utils.DynamicArray;
import org.mwg.utils.impl.DynamicArrayImpl;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

/**
 * A websocket client to send request on a remote storage
 */
public class WSStorageClient implements Storage {
//    private WebSocketClient.ConnectionBuilder _channelBuilder;
    private WebSocketChannel _channel;

    private final AtomicInteger _nextIdMessages;
    private final DynamicArray<Callback> _callBacks;
    private static final int MAX_CALLBACK = 1_000_000;
    private static final int INITIAL_SIZE = 16;
    private Graph _graph;

    private String _url;
    private int _port;

    public WSStorageClient(String URL, int port/*WebSocketClient.ConnectionBuilder builder*/) {
//        _channelBuilder = builder;
        _nextIdMessages = new AtomicInteger(0);
        _callBacks = new DynamicArrayImpl<>(INITIAL_SIZE);
        _url = URL;
        _port = port;
    }

    /*//todo mettre dans le connect
    public static WSStorageClient init(String URL, int port) throws IOException, URISyntaxException {
        XnioWorker _worker;
        Xnio xnio = Xnio.getInstance(io.undertow.websockets.client.WebSocketClient.class.getClassLoader());
        _worker = xnio.createWorker(OptionMap.builder()
                .set(Options.WORKER_IO_THREADS, 2)
                .set(Options.CONNECTION_HIGH_WATER, 1_000_000)
                .set(Options.CONNECTION_LOW_WATER, 1_000_000)
                .set(Options.WORKER_TASK_CORE_THREADS, 30)
                .set(Options.WORKER_TASK_MAX_THREADS, 30)
                .set(Options.TCP_NODELAY, true)
                .set(Options.CORK, true)
                .getMap());
        ByteBufferPool _buffer = new DefaultByteBufferPool(true, 1024 * 1024);
        WebSocketClient.ConnectionBuilder builder = io.undertow.websockets.client.WebSocketClient
                .connectionBuilder(_worker,_buffer,new URI("ws://" + URL + ":" + port));
        return new WSStorageClient(builder);

    }*/


    @Override
    public void get(Buffer keys, Callback<Buffer> callback) {
        if(_channel == null) {
            throw new RuntimeException("Please connect your websocket client first.");
        }
        int messageID = nextMessageID();
        keys.write(CoreConstants.BUFFER_SEP);
        keys.write(WSMessageType.RQST_GET);
        Base64.encodeIntToBuffer(messageID,keys);
        _callBacks.put(messageID,callback);
        send(keys);
    }

    @Override
    public void put(Buffer stream, Callback<Boolean> callback) {
        if(_channel == null) {
            throw new RuntimeException("Please connect your websocket client first.");
        }
        int messageID = nextMessageID();
        stream.write(CoreConstants.BUFFER_SEP);
        stream.write(WSMessageType.RQST_PUT);
        Base64.encodeIntToBuffer(messageID,stream);
        _callBacks.put(messageID,callback);
        send(stream);
    }

    @Override
    public void remove(Buffer keys, Callback<Boolean> callback) {
        if(_channel == null) {
            throw new RuntimeException("Please connect your websocket client first.");
        }
        int messageID = nextMessageID();
        keys.write(CoreConstants.BUFFER_SEP);
        keys.write(WSMessageType.RQST_REMOVE);
        Base64.encodeIntToBuffer(messageID,keys);
        _callBacks.put(messageID,callback);
        send(keys);
    }

    @Override
    public void connect(Graph graph, Callback<Short> callback) {
        if(_channel != null) {
            if(callback != null) {
                callback.on(null);
            }
        }

        try {
            XnioWorker _worker;
            Xnio xnio = Xnio.getInstance(io.undertow.websockets.client.WebSocketClient.class.getClassLoader());
            _worker = xnio.createWorker(OptionMap.builder()
                    .set(Options.WORKER_IO_THREADS, 2)
                    .set(Options.CONNECTION_HIGH_WATER, 1_000_000)
                    .set(Options.CONNECTION_LOW_WATER, 1_000_000)
                    .set(Options.WORKER_TASK_CORE_THREADS, 30)
                    .set(Options.WORKER_TASK_MAX_THREADS, 30)
                    .set(Options.TCP_NODELAY, true)
                    .set(Options.CORK, true)
                    .getMap());
            ByteBufferPool _buffer = new DefaultByteBufferPool(true, 1024 * 1024);
            WebSocketClient.ConnectionBuilder builder = io.undertow.websockets.client.WebSocketClient
                    .connectionBuilder(_worker, _buffer, new URI("ws://" + _url + ":" + _port));
            _channel = builder.connect().get();
            _channel.getReceiveSetter().set(new MessageReceiver());
            _channel.resumeReceives();
            _graph = graph;
        } catch (Exception e) {
            throw new RuntimeException("Error during connection to ws://" + _url + ":" + _port + "\n" + e.getMessage());
        }

        /*try {
            _channel = _channelBuilder.connect().get();
            _channel.getReceiveSetter().set(new MessageReceiver());
            _channel.resumeReceives();
        } catch (IOException e) {
            if(callback != null) {
                callback.on(null);
            }
        }*/
        if(callback != null) {
            Buffer buffer = _graph.newBuffer();
            buffer.write(WSMessageType.RQST_PREFIX);
            int msgID = nextMessageID();
            Base64.encodeIntToBuffer(msgID,buffer);
            _callBacks.put(msgID,callback);
            send(buffer);
        }

    }

    @Override
    public void disconnect(Short prefix, Callback<Boolean> callback) {
        //fixme maybe wait to receive all message or something like that
        try {
            _channel.sendClose();
            _channel = null;
            _nextIdMessages.set(0);
            _callBacks.clean();
            _graph = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void send(Buffer buffer){
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer.data());
        WebSockets.sendBinary(byteBuffer, _channel, new WebSocketCallback<Void>() {
            @Override
            public void complete(WebSocketChannel channel, Void context) {
            }

            @Override
            public void onError(WebSocketChannel channel, Void context, Throwable throwable) {
                System.err.println("Error while sending the buffer on channel " + channel.getUrl() + ": ");
                throwable.printStackTrace();
            }
        });
    }

    private int nextKey() {
        return _nextIdMessages.getAndUpdate(new IntUnaryOperator() {
            @Override
            public int applyAsInt(int operand) {
                if(operand == MAX_CALLBACK) {
                    return 0;
                }
                return operand + 1;
            }
        });
    }

    private int nextMessageID() {
        int nextMessageID = nextKey();
        while(!_callBacks.isEmpty(nextMessageID)){
            nextMessageID = nextKey();
        }
        return nextMessageID;
    }



    private class MessageReceiver extends AbstractReceiveListener {
        @Override
        protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
            ByteBuffer[] data = message.getData().getResource();

            for(ByteBuffer byteBuffer : data) {
                Buffer buffer = _graph.newBuffer();
                byte[] bytes = new byte[byteBuffer.limit()];
                byteBuffer.get(bytes);
                buffer.writeAll(bytes);
                bytes = null;

                Buffer wsInfo = null;
                BufferIterator it = buffer.iterator();
                while (it.hasNext()) {
                    wsInfo = it.next();
                }


                byte messageType = wsInfo.read(0);
                int msgID = Base64.decodeToIntWithBounds(wsInfo,1,wsInfo.size());

                for(int i=0;i<=wsInfo.size();i++) {
                    buffer.removeLast();
                }

                Callback callback = _callBacks.get(msgID);
                if(callback != null) { //messageType with callback
                    switch (messageType) {
                        case WSMessageType.RESP_GET: {
                            callback.on(buffer);
                            break;
                        }
                        case WSMessageType.RESP_PUT: {
                            callback.on(buffer.read(0) == 1);
                            break;
                        }
                        case WSMessageType.RESP_REMOVE: {
                            callback.on(buffer.read(0) == 1);
                            break;
                        }
                        case WSMessageType.RESP_PREFIX: {
                            int firstSep = 0;
                            while(buffer.read(firstSep) != CoreConstants.BUFFER_SEP) {
                                firstSep++;
                            }
                            short prefix = (short) Base64.decodeToIntWithBounds(buffer,0,firstSep);
                            callback.on(prefix);
                        }
                        default: {
                            System.err.println("The message " + msgID + " needs a callback but its type (" + messageType + ") is unknown");
                            buffer.free();
                        }

                    }
                } else {
                   switch (messageType) {
                       case WSMessageType.RQST_FORCE_RELOAD: {
                           _graph.reload(buffer);
                           break;
                       }
                       default: {
                           System.err.println("Either the message " + msgID + " does not need a callback but its type " +
                                   "(" + messageType + ") is unknown or the callback has not been found.");
                           buffer.free();
                       }
                   }
                }

                _callBacks.remove(msgID);

            }
        }
    }
}
