Websocket Plugin - NOT STABLE
---

This plugin allow to synchronize two models with websocket communication.


# Architecture information 
There is two parts: 
    - `WSStorageWrapper` which plays the server role
    - `WSStorageClient` that plays the client role

`WSStorageWrapper` is a wrapper of Storage (like LevelDB, RocksDB). So all the get/put are made on the database.
`WSStorageClient` behaves (from a feature point of view) like a storage. Except that, for each request (get, put, etc), it sends a message to the server.

We currently use [Undertow](http://undertow.io/) as websocket server and client. 

## Message format
Each request is divided in two part: the information relative to the request (e.g. keys of node to get) and the information relation to the message it self (type of message, id), referenced as wbsocket infnrmation - os wsInfo. The message are binaries messages.

`wsInfo` are always added by `WSStorageClient` at the end of the message before request sending, and removing when the response is received.
'wsINfo' follow this scheme:
    - one separator byte
    - one byte for the message type (if it a request, or an answer, which kind of request, etc). See `org.mwg.ws.WSMessageType` for the complete list
    - some byte for the message ID, that are a Base64 encoded integer.
    
To sum-up, the message is composed as follow
    - some bytes that represents the data to process by MWDB
    - one separator byte
    - one byte for the type of message
    - some bytes for the message id



#How to
See this [example](https://github.com/kevoree-modeling/mwDB/blob/master/plugins/websocket/src/test/java/org/mwg/ws/Test.java) for a basic example

#What is possible currently 
**Last update on 04 May 2016**

Currently the plugin allow to have one machine that produce data and another one that consume these data.

#To do
**Last update on 04 May 2016**
- [ ] Secure websocket (info: we plan to use [ACME](https://tools.ietf.org/html/draft-ietf-acme-acme-01) protocol, maybe using [ACME4J](https://github.com/shred/acme4j)
- [ ] Add a strategy to Manage re-connection of a client
- [ ] Add a strategy to notify client on modification
- [ ] Force the client to reload some data from the server
- [ ] When the client put data on the storage, the server should access these data, not only on cache miss



