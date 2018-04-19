package ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.networkservicediscoveryoperations;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.util.Log;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.general.Constants;
import ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.general.Utilities;
import ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.model.NetworkService;
import ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.view.ChatActivity;

public class NetworkServiceDiscoveryOperations {

    private Context context = null;
    private ChatActivity chatActivity = null;

    private String serviceName = null;

    private ChatServer chatServer = null;
    private List<ChatClient> communicationToServers = null;
    private List<ChatClient> communicationFromClients = null;

    private NsdManager nsdManager = null;
    private NsdManager.ResolveListener resolveListener = null;
    private NsdManager.DiscoveryListener discoveryListener = null;
    private NsdManager.RegistrationListener registrationListener = null;

    public NetworkServiceDiscoveryOperations(final Context context) {

        this.context = context;
        this.chatActivity = (ChatActivity)context;
        this.communicationToServers = new ArrayList<>();
        this.communicationFromClients = new ArrayList<>();

        nsdManager = (NsdManager)context.getSystemService(Context.NSD_SERVICE);

        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int errorCode) {
                Log.e(Constants.TAG, "Resolve failed: " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
                Log.i(Constants.TAG, "Resolve succeeded: " + nsdServiceInfo);

                if (nsdServiceInfo.getServiceName().equals(serviceName)) {
                    Log.i(Constants.TAG, "The service running on the same machine has been discovered");
                    return;
                }

                String host = nsdServiceInfo.getHost().toString();
                if (host.startsWith("/")) {
                    host = host.substring(1);
                }

                int port = nsdServiceInfo.getPort();
                ArrayList<NetworkService> discoveredServices = chatActivity.getDiscoveredServices();
                NetworkService networkService = new NetworkService(nsdServiceInfo.getServiceName(), host, port, Constants.CONVERSATION_TO_SERVER);
                if (!discoveredServices.contains(networkService)) {
                    ChatClient chatClient = new ChatClient(null, host, port);
                    if (chatClient.getSocket() != null) {
                        communicationToServers.add(chatClient);
                        discoveredServices.add(networkService);
                        chatActivity.setDiscoveredServices(discoveredServices);
                    }
                }

                Log.i(Constants.TAG, "A services has been discovered on " + host + ":" + port);

            }
        };

        discoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(Constants.TAG, "Service discovery start failed! Error code: " + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(Constants.TAG, "Service discovery stop failed! Error code: " + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.i(Constants.TAG, "Service discovery started: " + serviceType);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(Constants.TAG, "Service discovery stopped: " + serviceType);
            }

            @Override
            public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
                Log.i(Constants.TAG, "Service found: " + nsdServiceInfo);
                if (!nsdServiceInfo.getServiceType().equals(Constants.SERVICE_TYPE)) {
                    Log.i(Constants.TAG, "Unknown service type: " + nsdServiceInfo.getServiceType());
                } else if (nsdServiceInfo.getServiceName().equals(serviceName)) {
                    Log.i(Constants.TAG, "The service running on the same machine has been discovered:" + serviceName);
                } else if (nsdServiceInfo.getServiceName().contains(Constants.SERVICE_NAME)) {
                    nsdManager.resolveService(nsdServiceInfo, resolveListener);
                }
            }

            @Override
            public void onServiceLost(final NsdServiceInfo nsdServiceInfo) {
                Log.i(Constants.TAG, "Service lost: " + nsdServiceInfo);

                Handler handler = chatActivity.getHandler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<NetworkService> discoveredServices = chatActivity.getDiscoveredServices();
                        NetworkService networkService = new NetworkService(nsdServiceInfo.getServiceName(), nsdServiceInfo.getHost() != null ? nsdServiceInfo.getHost().toString() : null, nsdServiceInfo.getPort(), -1);
                        if (discoveredServices.contains(networkService)) {
                            int position = discoveredServices.indexOf(networkService);
                            discoveredServices.remove(position);
                            communicationToServers.remove(position);
                            chatActivity.setDiscoveredServices(discoveredServices);
                        }

                        Log.d(Constants.TAG, "serviceName = " + serviceName + " nsdServiceInfo.getServiceName() = " + nsdServiceInfo.getServiceName());
                   }
                });
            }
        };

        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
                serviceName = nsdServiceInfo.getServiceName();
                Log.i(Constants.TAG, "The service was registered: " + nsdServiceInfo);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int errorCode) {
                Log.e(Constants.TAG, "An exception occurred while registering the service: " + errorCode);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
                Log.i(Constants.TAG, "The service was unregistered: " + nsdServiceInfo);
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int errorCode) {
                Log.e(Constants.TAG, "An exception occurred while unregistering the service: " + errorCode);
            }

        };

    }

    public void registerNetworkService(int port) throws Exception {
        Log.v(Constants.TAG, "Register network service on port " + port);
        chatServer = new ChatServer(this, port);
        ServerSocket serverSocket = chatServer.getServerSocket();
        if (serverSocket == null) {
            throw new Exception("Could not get server socket");
        }
        chatServer.start();

        NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();
        nsdServiceInfo.setServiceName(Constants.SERVICE_NAME + Utilities.generateIdentifier(Constants.IDENTIFIER_LENGTH));
        nsdServiceInfo.setServiceType(Constants.SERVICE_TYPE);
        nsdServiceInfo.setHost(serverSocket.getInetAddress());
        nsdServiceInfo.setPort(serverSocket.getLocalPort());

        nsdManager.registerService(nsdServiceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }

    public void unregisterNetworkService() {
        Log.v(Constants.TAG, "Unregister network service");
        nsdManager.unregisterService(registrationListener);
        for (ChatClient communicationFromClient: communicationFromClients) {
            communicationFromClient.stopThreads();
        }
        communicationFromClients.clear();
        chatServer.stopThread();
        ArrayList<NetworkService> conversations = chatActivity.getConversations();
        conversations.clear();
        chatActivity.setConversations(conversations);
    }

    public void startNetworkServiceDiscovery() {
        Log.v(Constants.TAG, "Start network service discovery");
        nsdManager.discoverServices(Constants.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void stopNetworkServiceDiscovery() {
        Log.v(Constants.TAG, "Stop network service discovery");
        nsdManager.stopServiceDiscovery(discoveryListener);
        ArrayList<NetworkService> discoveredServices = chatActivity.getDiscoveredServices();
        discoveredServices.clear();
        chatActivity.setDiscoveredServices(discoveredServices);
        for (ChatClient communicationToServer: communicationToServers) {
            communicationToServer.stopThreads();
        }
        communicationToServers.clear();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<ChatClient> getCommunicationToServers() {
        return communicationToServers;
    }

    public void setCommunicationToServers(List<ChatClient> communicationToServers) {
        this.communicationToServers = communicationToServers;
    }

    public List<ChatClient> getCommunicationFromClients() {
        return communicationFromClients;
    }

    public void setCommunicationFromClients(List<ChatClient> communicationFromClients) {
        this.communicationFromClients = communicationFromClients;
        ArrayList<NetworkService> conversations = new ArrayList<>();
        for (ChatClient communicationFromClient: communicationFromClients) {
            NetworkService conversation = new NetworkService(
                    null,
                    communicationFromClient.getSocket().getInetAddress().toString(),
                    communicationFromClient.getSocket().getLocalPort(),
                    Constants.CONVERSATION_FROM_CLIENT
            );
            conversations.add(conversation);
        }
        chatActivity.setConversations(conversations);
    }

}
