package ro.pub.cs.systems.eim.lab08.chatservicejmdns.general;

public interface Constants {

    final public static boolean DEBUG = true;

    final public static String TAG = "[Chat Service]";

    final public static String SERVICE_NAME = "Chat";
    final public static String SERVICE_TYPE = "_chatservice._tcp.local.";
    final public static String SERVICE_DESCRIPTION = "\u001bEIM Chat Service with JmDNS";

    final public static String FRAGMENT_TAG = "ContainerFrameLayout";

    final public static int MESSAGE_QUEUE_CAPACITY = 50;

    final public static int CONVERSATION_TO_SERVER = 1;
    final public static int CONVERSATION_FROM_CLIENT = 2;

    final public static int MESSAGE_TYPE_SENT = 1;
    final public static int MESSAGE_TYPE_RECEIVED = 2;

    final public static int ALPHABET_LENGTH = 26;
    final public static char FIRST_LETTER = 'a';
    final public static int IDENTIFIER_LENGTH = 5;

    final public static String CLIENT_POSITION = "clientPosition";
    final public static String CLIENT_TYPE = "clientType";

}
