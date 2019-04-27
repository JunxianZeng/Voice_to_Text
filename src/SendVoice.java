import org.java_websocket.client.WebSocketClient;

/**
 * ZengJunxian
 * 2019/4/27 9:25
 */
public class SendVoice implements Runnable {
    private Object lock;

    // 每次发送的数据大小 1280 字节
    private static final int CHUNCKED_SIZE = 1280;
    public static boolean flag = true;
    private static byte[] audioData = new byte[CHUNCKED_SIZE];
    private MyWebSocketClient client;

    public SendVoice(Object lock, MyWebSocketClient client) {
        this.lock = lock;
        this.client = client;
    }

    public void run() {
        System.out.println("SendVoiceRunning.....");
        synchronized (lock) {
            while (flag) {
                audioData = RTASRTest.getAudioData();
                send(client, audioData);
                try {
                    lock.notifyAll();
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!flag) {
            lock.notifyAll();
                send(client, "{\"end\": true}".getBytes());
                System.out.println("\t发送结束标识完成");
            }
        }
    }

    private static void send(WebSocketClient client, byte[] bytes) {
        if (client.isClosed()) {
            throw new RuntimeException("client connect closed!");
        }

        client.send(bytes);
    }


    // 把转写结果解析为句子


    public static boolean isFlag() {
        return flag;
    }

    public static void setFlag(boolean flag) {
        SendVoice.flag = flag;
    }
}
