package ro.pub.cs.systems.eim.lab08.chatservicejmdns.networkservicediscoveryoperations;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import ro.pub.cs.systems.eim.lab08.chatservicejmdns.general.Constants;
import ro.pub.cs.systems.eim.lab08.chatservicejmdns.general.Utilities;
import ro.pub.cs.systems.eim.lab08.chatservicejmdns.model.NetworkService;
import ro.pub.cs.systems.eim.lab08.chatservicejmdns.view.ChatActivity;

public class NetworkServiceDiscoveryOperations {

    private Context context = null;
    private ChatActivity chatActivity = null;

    private String serviceName = null;

    private ChatServer chatServer = null;
    private List<ChatClient> communicationToServers = null;
    private List<ChatClient> communicationFromClients = null;

    private JmDNS jmDNS = null;
    private ServiceListener serviceListener = null;

    public NetworkServiceDiscoveryOperations(final Context context) {

        this.context = context;
        this.chatActivity = (ChatActivity)context;

        this.communicationToServers = new ArrayList<>();
        this.communicationFromClients = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    WifiManager wifiManager = ((ChatActivity)context).getWifiManager();
                    InetAddress address = InetAddress.getByAddress(
                            ByteBuffer.allocate(4).putInt(Integer.reverseBytes(wifiManager.getConnectionInfo().getIpAddress())).array()
                    );
                    String name = address.getHostName();
                    Log.i(Constants.TAG, "address = " + address + " name = " + name);
                    jmDNS = JmDNS.create(address, name);
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
           }
        }).start();

        serviceListener = new ServiceListener() {
            @Override
            public void serviceAdded(ServiceEvent serviceEvent) {
                Log.i(Constants.TAG, "Service found: " + serviceEvent);
                if (!serviceEvent.getType().equals(Constants.SERVICE_TYPE)) {
                    Log.i(Constants.TAG, "Unknown service type: " + serviceEvent.getType());
                } else if (serviceEvent.getName().equals(serviceName)) {
                    Log.i(Constants.TAG, "The service running on the same machine has been discovered: " + serviceName);
                } else if (serviceEvent.getName().contains(Constants.SERVICE_NAME)) {
                    Log.i(Constants.TAG, "The service name should be resolved now: " + serviceName);
                    jmDNS.requestServiceInfo(serviceEvent.getType(), serviceEvent.getName());
                }
            }

            @Override
            public void serviceRemoved(final ServiceEvent serviceEvent) {
                Log.i(Constants.TAG, "Service lost: " + serviceEvent);

                ServiceInfo serviceInfo = serviceEvent.getInfo();
                if (serviceInfo == null) {
                    Log.e(Constants.TAG, "Service info for service is null!");
                    return;
                }

                String[] hosts = serviceInfo.getHostAddresses();
                String host = null;
                if (hosts.length != 0) {
                    host = hosts[0];
                    if (host.startsWith("/")) {
                        host = host.substring(1);
                    }
                }

                final String finalizedHost = host;
                final int finalizedPort = serviceInfo.getPort();

                Handler handler = chatActivity.getHandler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<NetworkService> discoveredServices = chatActivity.getDiscoveredServices();
                        NetworkService networkService = new NetworkService(serviceEvent.getName(), finalizedHost, finalizedPort, -1);
                        if (discoveredServices.contains(networkService)) {
                            int position = discoveredServices.indexOf(networkService);
                            discoveredServices.remove(position);
                            communicationToServers.remove(position);
                            chatActivity.setDiscoveredServices(discoveredServices);
                        }
                    }
                });
            }

            @Override
            public void serviceResolved(ServiceEvent serviceEvent) {
                Log.i(Constants.TAG, "Resolve succeeded: " + serviceEvent);

                if (serviceEvent.getName().equals(serviceName)) {
                    Log.i(Constants.TAG, "The service running on the same machine has been discovered.");
                    return;
                }

                ServiceInfo serviceInfo = serviceEvent.getInfo();
                if (serviceInfo == null) {
                    Log.e(Constants.TAG, "Service info for service is null!");
                    return;
                }

                String[] hosts = serviceInfo.getHostAddresses();
                String host = null;
                if (hosts.length == 0) {
                    Log.e(Constants.TAG, "No host addresses returned for the service!");
                    return;
                }
                host = hosts[0];
                if (host.startsWith("/")) {
                    host = host.substring(1);
                }
                int port = serviceInfo.getPort();

                ArrayList<NetworkService> discoveredServices = chatActivity.getDiscoveredServices();
                NetworkService networkService = new NetworkService(serviceEvent.getName(), host, port, Constants.CONVERSATION_TO_SERVER);
                if (!discoveredServices.contains(networkService)) {
                    ChatClient chatClient = new ChatClient(null, host, port);
                    if (chatClient.getSocket() != null) {
                        communicationToServers.add(chatClient);
                        discoveredServices.add(networkService);
                        chatActivity.setDiscoveredServices(discoveredServices);
                    }
                }

                Log.i(Constants.TAG, "A service has been discovered on " + host + ":" + port);
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

        ServiceInfo serviceInfo = ServiceInfo.create(
                Constants.SERVICE_TYPE,
                Constants.SERVICE_NAME + Utilities.generateIdentifier(Constants.IDENTIFIER_LENGTH),
                port,
                Constants.SERVICE_DESCRIPTION
        );

        if (jmDNS != null && serviceInfo != null) {
            serviceName = serviceInfo.getName();
            jmDNS.registerService(serviceInfo);
        }
    }

    public void unregisterNetworkService() {
        Log.v(Constants.TAG, "Unregister network service");
        if (jmDNS != null) {
            jmDNS.unregisterAllServices();
        }
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
        if (jmDNS != null && serviceListener != null) {
            jmDNS.addServiceListener(Constants.SERVICE_TYPE, serviceListener);
        }
    }

    public void stopNetworkServiceDiscovery() {
        Log.v(Constants.TAG, "Stop network service discovery");
        if (jmDNS != null && serviceListener != null) {
            jmDNS.removeServiceListener(Constants.SERVICE_TYPE, serviceListener);
        }
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

    public void closeJmDNS() {
        try {
            if (jmDNS != null) {
                jmDNS.close();
                jmDNS = null;
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }

}
