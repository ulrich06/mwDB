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

#Test information
##WebSocket Secure with authentification by certificate exchange
To test our wss implementation, we create keystore and truststore files for server and client following the procedure below.

**Certificates password:**
- `server.keystore`: WS-Server-Keystore-16
- wsTestKey: same as  `server.keystore` password
- `server.truststore`: WS-Server-Truststore-16
- `client.keystore`: WS-Client-Keystore-16
- wsTestKeyClt: same as `client.keystore` password
- `client.truststore`: WS-Client-Truststore-16

**Procedure of keystore/truststore creation:**
- `keytool -genkey -alias wsTestKey -keystore server.keystore`
- password: see above
- press enter until the end (except for the question Is ...?, answer by yes)
- `keytool -export -alias wsTestKey -file certfile.cert -keystore server.keystore`
- `keytool -import -alias publicWsTestKey -file certfile.cert -keystore server.truststore`
- answer by `yes` to the question `Trust this certificate? [no]:`
- `keytool -genkey -alias wsTestKeyClt -keystore client.keystore`
- password: see above
- `keytool -export -alias wsTestKeyClt -file client.cert -keystore client.keystore`
- `keytool -import -alias wsTestKeyClt -file client.cert -keystore client.truststore`
- `keytool -import -alias wsTestKeyClt -file client.cert -keystore server.truststore`
- `keytool -import -alias wsTestKey -file certfile.cert -keystore client.truststore`
- answer by `yes` to the question `Trust this certificate? [no]:`
- `rm certfile.cert`
- `rm client.cert`

At the end:
   - `server.keystore` should contain the private key of the server
   - `server.truststore` and `client.truststore` should contain the public key of the client and the server
   - `client.keystore` should contain the private key of the client