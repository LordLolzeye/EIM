package ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.controller;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.R;
import ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.general.Constants;
import ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.model.NetworkService;
import ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.view.ChatActivity;
import ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.view.ChatConversationFragment;

public class NetworkServiceAdapter extends BaseAdapter {

    private Context context = null;
    private LayoutInflater layoutInflater = null;

    private ArrayList<NetworkService> data = null;

    private static class NetworkServiceViewHolder {
        private TextView networkServiceNameTextView;
        private Button networkServiceConnectButton;
    }

    private class NetworkServiceConnectButtonClickListener implements Button.OnClickListener {

        private int clientPosition = -1;
        private int clientType = -1;

        public NetworkServiceConnectButtonClickListener(int clientPosition, int clientType) {
            this.clientPosition = clientPosition;
            this.clientType = clientType;
        }

        @Override
        public void onClick(View view) {
            ChatConversationFragment chatConversationFragment = new ChatConversationFragment();
            Bundle arguments = new Bundle();
            arguments.putInt(Constants.CLIENT_POSITION, clientPosition);
            arguments.putInt(Constants.CLIENT_TYPE, clientType);
            chatConversationFragment.setArguments(arguments);
            FragmentManager fragmentManager = ((ChatActivity)context).getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_frame_layout, chatConversationFragment, Constants.FRAGMENT_TAG);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

    }

    public NetworkServiceAdapter(Context context, ArrayList<NetworkService> data) {
        this.context = context;
        this.data = data;

        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(ArrayList<NetworkService> data) {
        this.data = data;
    }

    public ArrayList<NetworkService> getData() {
        return data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        NetworkServiceViewHolder networkServiceViewHolder;

        NetworkService networkService = (NetworkService)getItem(position);

        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.network_service, parent, false);
            networkServiceViewHolder = new NetworkServiceViewHolder();
            networkServiceViewHolder.networkServiceNameTextView = (TextView)view.findViewById(R.id.networkservice_name_text_view);
            networkServiceViewHolder.networkServiceConnectButton = (Button)view.findViewById(R.id.network_service_connect_button);
            view.setTag(networkServiceViewHolder);
        } else {
            view = convertView;
        }

        networkServiceViewHolder = (NetworkServiceViewHolder)view.getTag();
        networkServiceViewHolder.networkServiceNameTextView.setText(networkService.toString());
        switch (networkService.getServiceType()) {
            case Constants.CONVERSATION_TO_SERVER:
                networkServiceViewHolder.networkServiceConnectButton.setText(
                        context.getResources().getString(R.string.connect));
                break;
            case Constants.CONVERSATION_FROM_CLIENT:
                networkServiceViewHolder.networkServiceConnectButton.setText(
                        context.getResources().getString(R.string.view));
                break;
        }
        networkServiceViewHolder.networkServiceConnectButton.setOnClickListener(
                new NetworkServiceConnectButtonClickListener(
                        position,
                        networkService.getServiceType()
                )
        );

        return view;
    }

}
