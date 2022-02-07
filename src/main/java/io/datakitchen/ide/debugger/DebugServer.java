package io.datakitchen.ide.debugger;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

public class DebugServer  {

    private static class MessageBody {
        private final Message message;
        private final Consumer<Message> callback;

        public MessageBody(Message message, Consumer<Message> callback){
            this.message = message;
            this.callback = callback;
        }

        public MessageBody(Message message){
            this(message, null);
        }
    }

    private boolean active = false;
    private int seqNumber = 1;
    private final Thread receiverThread;
    private Thread outputThread;
    private ServerSocket serverSocket;
    private Socket socket;
    private final ArrayBlockingQueue<MessageBody> outputQueue = new ArrayBlockingQueue<>(10);
    private final ArrayBlockingQueue<String> inputQueue = new ArrayBlockingQueue<>(10);
    private final DebugServerCallback callback;
    private final Map<Integer, Consumer<Message>> consumers = new HashMap<>();

    public DebugServer(DebugServerCallback callback) {
        this.callback = callback;
        receiverThread = new Thread(this::run);
        receiverThread.start();
    }

    public void close(){
        active = false;
        receiverThread.interrupt();
        try {
            outputQueue.put(new MessageBody(Message.EXIT));
            if (outputThread != null) {
                outputThread.join();
            }
        } catch (InterruptedException ignored){}
        if (socket != null) {
            try { socket.close(); }catch (IOException ignored){}
        }
        try { serverSocket.close(); }catch (IOException ignored){}
    }

    private void run(){
        active = true;

        try {

            this.serverSocket = ServerSocketFactory.getDefault().createServerSocket(59977);
            this.socket = serverSocket.accept();
            callback.processConnected();

            outputThread = new Thread(this::sendMessages);
            outputThread.start();
            outputQueue.put(new MessageBody(Message.RUN));

            InputStream input = this.socket.getInputStream();
            byte[] messageBytes = new byte[16384];
            while (this.active) {
                while (input.available() == 0) {
                    Thread.sleep(500);
                }

                int received = 0;
                if ((received = input.read(messageBytes, 0, messageBytes.length)) > 0) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(messageBytes, 0, received)));
                    String messageStr;
                    while ((messageStr = reader.readLine()) != null) {
                        System.out.println("Message:<<" + messageStr + ">>");

                        Message message = Message.parse(messageStr);

                        Integer answerNumber = message.getSequenceNumber();

                        if (answerNumber != null && consumers.containsKey(answerNumber)){
                            // it's an answer
                            consumers.remove(answerNumber).accept(message);
                        } else {
                            callback.messageReceived(message);
                        }
                    }
                }
            }
        }catch(InterruptedException ignored){
        }catch(SocketException ex) {
            ex.printStackTrace();
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public String getMessage(){
        try {
            return inputQueue.take();
        }catch(InterruptedException ex){
            return null;
        }
    }

    public void sendMessage(Message message){
        try {
            outputQueue.put(new MessageBody(message));
        }catch (InterruptedException ignored){}
    }

    public void sendMessage(Message message, Consumer<Message> callback) {
        try {
            outputQueue.put(new MessageBody(message, callback));
        }catch (InterruptedException ignored){}
    }
    private synchronized int nextSeqNumber(){
        /*
         sequence numbers from this side are odd.
         sequence numbers coming from python are even.
        */
        int number = this.seqNumber;
        seqNumber+=2;
        return number;
    }

    private void doSendMessage(Message message, int seqNumber, OutputStream output) throws IOException{
        String messageString = message.build(seqNumber);

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(byteBuffer);
        writer.write(messageString);
        writer.flush();

        output.write(byteBuffer.toByteArray());
    }

    private void sendMessages(){
        try {
            OutputStream output = socket.getOutputStream();
            while (this.active) {
                MessageBody message = outputQueue.take();

                if (message.message == Message.EXIT){
                    break;
                }

                int seqNumber = this.nextSeqNumber();

                if (message.callback != null){
                    this.consumers.put(seqNumber, message.callback);
                }

                doSendMessage(message.message, seqNumber, output);
            }
        }catch (IOException ex){
            ex.printStackTrace();
        } catch (InterruptedException ex){
            ex.printStackTrace();
        }
    }

}
