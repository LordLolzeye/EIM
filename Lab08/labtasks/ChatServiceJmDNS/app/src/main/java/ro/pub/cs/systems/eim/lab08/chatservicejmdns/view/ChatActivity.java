package ro.pub.cs.systems.eim.lab08.chatservicejmdns.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import ro.pub.cs.systems.eim.lab08.chatservicejmdns.R;
import ro.pub.cs.systems.eim.lab08.chatservicejmdns.general.Constants;
import ro.pub.cs.systems.eim.lab08.chatservicejmdns.model.NetworkService;
import ro.pub.cs.systems.eim.lab08.chatservicejmdns.networkservicediscoveryoperations.NetworkServiceDiscoveryOperations;

public class ChatActivity extends AppCompatActivity {

    private NetworkServiceDiscoveryOperations networkServiceDiscoveryOperations = null;

    private boolean serviceRegistrationStatus = false;
    private boolean serviceDiscoveryStatus = false;

    private ArrayList<NetworkService> discoveredServices = null;
    private ArrayList<NetworkService> conversations = null;

    private Handler handler = null;

    private WifiManager.MulticastLock multicastLock = null;
    private WifiManager wifiManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(Constants.TAG, "onCreate() callback method was invoked!");
        setContentView(R.layout.activity_chat);

        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifiManager.createMulticastLock(Constants.TAG);
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();

        discoveredServices = new ArrayList<>();
        conversations = new ArrayList<>();

        setHandler(new Handler());

        setNetworkServiceDiscoveryOperations(new NetworkServiceDiscoveryOperations(this));
        setChatNetworkServiceFragment(new ChatNetworkServiceFragment());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(Constants.TAG, "onResume() callback method was invoked!");
        if (networkServiceDiscoveryOperations != null) {
            if (!serviceDiscoveryStatus) {
                serviceDiscoveryStatus = true;
                networkServiceDiscoveryOperations.startNetworkServiceDiscovery();
                if (getChatNetworkServiceFragment() != null) {
                    getChatNetworkServiceFragment().startServiceDiscovery();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        Log.v(Constants.TAG, "onPause() callback method was invoked!");
        if (networkServiceDiscoveryOperations != null) {
            if (serviceDiscoveryStatus) {
                serviceDiscoveryStatus = false;
                networkServiceDiscoveryOperations.stopNetworkServiceDiscovery();
                if (getChatNetworkServiceFragment() != null) {
                    getChatNetworkServiceFragment().stopServiceDiscovery();
                }
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.v(Constants.TAG, "onDestroy() callback method was invoked!");
        if (networkServiceDiscoveryOperations != null) {
            if (serviceDiscoveryStatus) {
                serviceDiscoveryStatus = false;
                networkServiceDiscoveryOperations.stopNetworkServiceDiscovery();
                if (getChatNetworkServiceFragment() != null) {
                    getChatNetworkServiceFragment().stopServiceDiscovery();
                }
            }
            if (serviceRegistrationStatus) {
                serviceDiscoveryStatus = false;
                networkServiceDiscoveryOperations.unregisterNetworkService();
                if (getChatNetworkServiceFragment() != null) {
                    getChatNetworkServiceFragment().stopServiceRegistration();
                }
            }
            networkServiceDiscoveryOperations.closeJmDNS();
        }

        if (multicastLock != null) {
            multicastLock.release();
        }

        super.onDestroy();
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setNetworkServiceDiscoveryOperations(NetworkServiceDiscoveryOperations networkServiceDiscoveryOperations) {
        this.networkServiceDiscoveryOperations = networkServiceDiscoveryOperations;
    }

    public NetworkServiceDiscoveryOperations getNetworkServiceDiscoveryOperations() {
        return networkServiceDiscoveryOperations;
    }

    public void setChatNetworkServiceFragment(ChatNetworkServiceFragment chatNetworkServiceFragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_frame_layout, chatNetworkServiceFragment, Constants.FRAGMENT_TAG);
        fragmentTransaction.commit();
    }

    public ChatNetworkServiceFragment getChatNetworkServiceFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(Constants.FRAGMENT_TAG);
        if (fragment instanceof  ChatNetworkServiceFragment) {
            return (ChatNetworkServiceFragment)fragment;
        }
        return null;
    }

    public void setServiceRegistrationStatus(boolean serviceRegistrationStatus) {
        this.serviceRegistrationStatus = serviceRegistrationStatus;
    }

    public boolean getServiceRegistrationStatus() {
        return serviceRegistrationStatus;
    }

    public void setServiceDiscoveryStatus(boolean serviceDiscoveryStatus) {
        this.serviceDiscoveryStatus = serviceDiscoveryStatus;
    }

    public boolean getServiceDiscoveryStatus() {
        return serviceDiscoveryStatus;
    }

    public void setDiscoveredServices(final ArrayList<NetworkService> discoveredServices) {
        this.discoveredServices = discoveredServices;
        handler.post(new Runnable() {
            @Override
            public void run() {
                ChatNetworkServiceFragment chatNetworkServiceFragment = getChatNetworkServiceFragment();
                if (chatNetworkServiceFragment != null && chatNetworkServiceFragment.isVisible()) {
                    chatNetworkServiceFragment.getDiscoveredServicesAdapter().setData(discoveredServices);
                    chatNetworkServiceFragment.getDiscoveredServicesAdapter().notifyDataSetChanged();
                }
            }
        });
    }

    public ArrayList<NetworkService> getDiscoveredServices() {
        return discoveredServices;
    }

    public void setConversations(final ArrayList<NetworkService> conversations) {
        this.conversations = conversations;
        handler.post(new Runnable() {
            @Override
            public void run() {
                ChatNetworkServiceFragment chatNetworkServiceFragment = getChatNetworkServiceFragment();
                if (chatNetworkServiceFragment != null && chatNetworkServiceFragment.isVisible()) {
                    chatNetworkServiceFragment.getConversationsAdapter().setData(conversations);
                    chatNetworkServiceFragment.getConversationsAdapter().notifyDataSetChanged();
                }
            }
        });
    }

    public ArrayList<NetworkService> getConversations() {
        return conversations;
    }

    public void setWifiManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }
}
