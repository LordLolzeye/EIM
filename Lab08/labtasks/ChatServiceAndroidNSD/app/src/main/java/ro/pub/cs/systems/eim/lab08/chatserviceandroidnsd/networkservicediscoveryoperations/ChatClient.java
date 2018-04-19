package ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.networkservicediscoveryoperations;

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

                    // TODO exercise 6
                    // iterate while the thread is not yet interrupted
                    // - get the content (a line) from the messageQueue, if available, using the take() method
                    // - if the content is not null
                    //   - send the content to the PrintWriter, as a line
                    //   - create a Message instance, with the content received and Constants.MESSAGE_TYPE_SENT as message type
                    //   - add the message to the conversationHistory
                    //   - if the ChatConversationFragment is visible (query the FragmentManager for the Constants.FRAGMENT_TAG tag)
                    //   append the message to the graphic user interface

                } catch (Exception exception) {
                    Log.e(Constants.TAG, "An exception has occurred: " + exception.getMessage());
                    if (Constants.DEBUG) {
                        exception.printStackTrace();
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

                    // TODO exercise 7
                    // iterate while the thread is not yet interrupted
                    // - receive the content (a line) from the bufferedReader, if available
                    // - if the content is not null
                    //   - create a Message instance, with the content received and Constants.MESSAGE_TYPE_RECEIVED as message type
                    //   - add the message to the conversationHistory
                    //   - if the ChatConversationFragment is visible (query the FragmentManager for the Constants.FRAGMENT_TAG tag)
                    //   append the message to the graphic user interface

                } catch (Exception exception) {
                    Log.e(Constants.TAG, "An exception has occurred: " + exception.getMessage());
                    if (Constants.DEBUG) {
                        exception.printStackTrace();
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