package ro.pub.cs.systems.eim.lab08.chatserviceandroidnsd.model;

public class Message {

    private String content;
    private int type;

    public Message() {
        content = new String();
        type = -1;
    }

    public Message(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

}
