///<reference path='mwg.d.ts'/>
///<reference path='reconnecting-websocket/reconnectiong-websockets.d.ts'/>

namespace org {
    namespace mwg {
        namespace plugins {
            class WSClient implements org.mwg.plugin.Storage {

                private url:string;
                private callbacks:java.util.Map<number,org.mwg.Callback>;
                private ws:ReconnectingWebSocket = null;
                private graph:org.mwg.Graph = null;

                constructor(p_url:String) {
                    this.url = p_url;
                    this.callbacks = new java.util.HashMap<number,org.mwg.Callback>();
                }

                get(keys:org.mwg.struct.Buffer, callback:org.mwg.Callback<org.mwg.struct.Buffer>):void {

                }

                put(stream:org.mwg.struct.Buffer, callback:org.mwg.Callback<boolean>):void {

                }

                remove(keys:org.mwg.struct.Buffer, callback:org.mwg.Callback<boolean>):void {

                }

                connect(p_graph:org.mwg.Graph, callback:org.mwg.Callback<boolean>):void {
                    this.graph = p_graph;
                    if (this.ws == null) {
                        this.ws = new ReconnectingWebSocket(this.url);
                        this

                    } else {
                        //do nothing
                    }
                }

                disconnect(prefix:number, callback:org.mwg.Callback<boolean>):void {
                    if(this.ws != null){
                        this.ws.close(0);
                        callback(true);
                    }
                }

                lock(callback:org.mwg.Callback<number>):void {
                    //TODO
                }

                unlock(previousLock:number, callback:org.mwg.Callback<boolean>):void {
                    //TODO
                }

            }
        }
    }
}

