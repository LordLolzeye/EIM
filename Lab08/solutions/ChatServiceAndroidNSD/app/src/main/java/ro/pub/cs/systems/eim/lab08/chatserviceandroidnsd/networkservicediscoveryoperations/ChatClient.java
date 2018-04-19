package ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.networkservicediscoveryoperations;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.general.Constants;
import ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.general.Utilities;
import ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.model.Message;
import ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.view.ChatActivity;
import ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.view.ChatConversationFragment;

public class ChatClient {

    private Socket socket = null;

    private Context context = null;

    private SendThread sendThread = null;
    private ReceiveThread receiveThread = null;

    private BlockingQueue<String> messageQueue = new ArrayBlockingQueue<String>(Constants.MESSAGE_QUEUE_CAPACITY);

    private List<Message> conversationHistory = new ArrayList<>();

    public ChatClient(Context context, String host, int port) {
        this.context = context;
        try {
            socket = new Socket(host, port);
            Log.d(Constants.TAG, "A socket has been created on: " + socket.getInetAddress() + ":" + socket.getLocalPort());
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred while creating the socket: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        if (socket != null) {
            startThreads();
        }
    }

    public ChatClient(Context context, Socket socket) {
        this.context = context;
        this.socket = socket;
        if (socket != null) {
            startThreads();
        }
    }

    public void sendMessage(String message) {
        try {
            messageQueue.put(message);
        } catch (InterruptedException interruptedException) {
            Log.e(Constants.TAG, "An exception has occurred: " + interruptedException.getMessage());
            if (Constants.DEBUG) {
                interruptedException.printStackTrace();
            }
        }
    }

    private class SendThread extends Thread {

        @Override
        public void run() {
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (printWriter != null) {
                Log.d(Constants.TAG, "Sending messages to " + socket.getInetAddress() + ":" + socket.getLocalPort());
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        String content = messageQueue.take();
                        if (content != null) {
                            Log.d(Constants.TAG, "Sending the message: " + content);
                            printWriter.println(content);
                            printWriter.flush();
                            Message message = new Message(content, Constants.MESSAGE_TYPE_SENT);
                            conversationHistory.add(message);
                            if (context != null) {
                                ChatActivity chatActivity = (ChatActivity)context;
                                FragmentManager fragmentManager = chatActivity.getFragmentManager();
                                Fragment fragment = fragmentManager.findFragmentByTag(Constants.FRAGMENT_TAG);
                                if (fragment instanceof ChatConversationFragment && fragment.isVisible()) {
                                    ChatConversationFragment chatConversationFragment = (ChatConversationFragment)fragment;
                                    chatConversationFragment.appendMessage(message);
                                }
                            }
                        }
                    }
                } catch (InterruptedException interruptedException) {
                    Log.e(Constants.TAG, "An exception has occurred: " + interruptedException.getMessage());
                    if (Constants.DEBUG) {
                        interruptedException.printStackTrace();
                    }
                }
            }

            Log.i(Constants.TAG, "Send Thread ended");
        }

        public void stopThread() {
            interrupt();
        }

    }

    private class ReceiveThread extends Thread {

        @Override
        public void run() {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            if (bufferedReader != null) {
                Log.d(Constants.TAG, "Receiving messages from " + socket.getInetAddress() + ":" + socket.getLocalPort());
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        String content = bufferedReader.readLine();
                        if (content != null) {
                            Log.d(Constants.TAG, "Received the message: " + content);
                            Message message = new Message(content, Constants.MESSAGE_TYPE_RECEIVED);
                            conversationHistory.add(message);
                            if (context != null) {
                                ChatActivity chatActivity = (ChatActivity)context;
                                FragmentManager fragmentManager = chatActivity.getFragmentManager();
                                Fragment fragment = fragmentManager.findFragmentByTag(Constants.FRAGMENT_TAG);
                                if (fragment instanceof ChatConversationFragment && fragment.isVisible()) {
                                    ChatConversationFragment chatConversationFragment = (ChatConversationFragment)fragment;
                                    chatConversationFragment.appendMessage(message);
                                }
                            }
                        }
                    }
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }

            Log.i(Constants.TAG, "Receive Thread ended");
        }

        public void stopThread() {
            interrupt();
        }

    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setConversationHistory(List<Message> conversationHistory) {
        this.conversationHistory = conversationHistory;
    }

    public List<Message> getConversationHistory() {
        return conversationHistory;
    }

    public void startThreads() {
        sendThread = new SendThread();
        sendThread.start();

        receiveThread = new ReceiveThread();
        receiveThread.start();
    }

    public void stopThreads() {
        sendThread.stopThread();
        receiveThread.stopThread();
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }
}