package io.datakitchen.ide.debugger;

/**
 * Represents the messages exchanged with Pydevd.
 */
public class Message {

    public static final int MSG_RUN = 101;
    public static final int MSG_LIST_THREADS = 102;
    public static final int MSG_THREAD_KILL = 104;
    public static final int MSG_THREAD_SUSPEND = 105;
    public static final int MSG_THREAD_RUN = 106;
    public static final int MSG_STEP_INTO = 107;
    public static final int MSG_STEP_OVER = 108;
    public static final int MSG_STEP_RETURN = 109;
    public static final int MSG_SET_BREAK = 111;
    public static final int MSG_REMOVE_BREAK = 112;
    public static final int MSG_GET_FRAME = 114;

    public static final int MSG_EXIT = 999;

    private final int number;
    private final Integer sequenceNumber;
    private final String payload;

    public static final Message RUN = new Message(MSG_RUN);
    public static final Message LIST_THREADS = new Message(MSG_LIST_THREADS);
    public static final Message EXIT = new Message(MSG_EXIT);


    public static Message stepReturn(String threadName) {
        return new Message(MSG_STEP_RETURN, threadName == null ? "*" : threadName);
    }

    public static Message stepOver(String threadName){
        return new Message(MSG_STEP_OVER, threadName == null ? "*" : threadName);
    }

    public static Message stepInto(String threadName){
        return new Message(MSG_STEP_INTO, threadName == null ? "*" : threadName);
    }

    public static Message threadKill(String threadName){
        return new Message(MSG_THREAD_KILL, threadName == null ? "*" : threadName);
    }

    public static Message threadRun(String threadName){
        return new Message(MSG_THREAD_RUN, threadName == null ? "*" : threadName);
    }

    public static Message setBreak(String file, int lineNumber){
        return new Message(MSG_SET_BREAK, "python-line\t"+file+"\t"+lineNumber+"\t\t\t\t");
    }

    public static Message removeBreak(String file, int lineNumber){
        return new Message(MSG_REMOVE_BREAK,  "python-line\t"+file+"\t"+lineNumber+"\t\t\t\t");
    }

    public static Message getFrame(String threadId, long frameId) {
        return new Message(MSG_GET_FRAME,threadId+"\t"+frameId+"\t");
    }

    public static Message parse(String messageStr) {
        String[] tokens = messageStr.split("\t");

        return new Message(Integer.parseInt(tokens[0]), Integer.valueOf(tokens[1]), tokens[2]);
    }

    public Message(int number, Integer sequenceNumber, String payload){
        this.number = number;
        this.sequenceNumber = sequenceNumber;
        this.payload = payload;
    }

    public Message(int number, String payload){
        this(number, null, payload);
    }

    public Message(int number){
        this(number, null, null);
    }

    public int getNumber(){
        return number;
    }

    public String getPayload(){
        return payload;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public String build(int seqNumber){
        return number+"\t"+seqNumber+ "\t"+(payload != null ? payload : "") + "\n";
    }

    public String toString(){
        return "Message:"+number+":"+payload;
    }

}
