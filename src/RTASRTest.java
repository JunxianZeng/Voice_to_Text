import org.java_websocket.WebSocket;
import util.EncryptUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

/**
 * 实时转写调用demo
 * 此demo只是一个简单的调用示例，不适合用到实际生产环境中
 *
 * @author white
 */
public class RTASRTest {
    // appid
    private static final String APPID = "5cc01b61";
    // appid对应的secret_key
    private static final String SECRET_KEY = "63d352fd54f5c89b2851811bc5dfed5b";
    // 请求地址
    private static final String HOST = "rtasr.xfyun.cn/v1/ws";
    private static final String BASE_URL = "ws://" + HOST;
    private static final String ORIGIN = "http://" + HOST;
    // 每次发送的数据大小 1280 字节
    private static final int CHUNCKED_SIZE = 1280;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
    private static byte[] audioData = new byte[CHUNCKED_SIZE];

    static String text = new String();
    private static Object lock;
    static MyWebSocketClient client;
    static Timer timer;
    static TimerTask timerTask;

    public static void main(String[] args) throws Exception {
        lock = new Object();
        buildConnect();
//        Timer timer = new Timer();
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println("System.currentTimeMillis() = " + System.currentTimeMillis());
//                System.out.println("下一句");
//                System.out.println("match.i = " + match.i);
//                if (text.length() != 0 && match.i < match.text.length) {
//                    match.rule2(text).toString();
//                    text = "";
//                }
//                match.i++;
//                if (match.i == match.text.length) {
//                    flag = false;
//                    match.i = 0;
//                    System.out.println("时间到，结束");//
//                    timer.cancel();
//                }
//            }
//        };
//
//        timer.schedule(task, 5000, 5000);
        Thread thread1 = new Thread(new RecordVoice(lock));
        Thread thread2 = new Thread(new SendVoice(lock, client));
        thread1.start();
        thread2.start();
        countTime();
    }

    public static void countTime() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
//                System.out.println("执行TimeTask" + timerTask.toString());
                if (Match.i < Match.text.length) {
//                    System.out.println("Match.i = " + Match.i);
//                    System.out.println("时间到,部分匹配：");
                    System.out.println("text = " + text);
                    System.out.println("匹配结果：" + Match.rule2(text));
                    Match.i++;
                    text = "";
                    timer.cancel();
                    timerTask.cancel();
                    if (Match.i < Match.text.length)
                        countTime();
                    else {
                        RecordVoice.flag = false;
                        SendVoice.flag = false;
                        timer.cancel();
                    }
                }
            }
        };
        System.out.println("计时15秒");
        timer.schedule(timerTask, 15000);
    }

    public static void buildConnect() {
        URI url = null;
        try {
            url = new URI(BASE_URL + getHandShakeParams(APPID, SECRET_KEY));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        DraftWithOrigin draft = new DraftWithOrigin(ORIGIN);
        CountDownLatch handshakeSuccess = new CountDownLatch(1);
        CountDownLatch connectClose = new CountDownLatch(1);
        client = new MyWebSocketClient(url, draft, handshakeSuccess, connectClose);
        client.connect();
        while (!client.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
            System.out.println(getCurrentTimeStr() + "\t连接中");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 等待握手成功
        try {
            handshakeSuccess.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static String getHandShakeParams(String appId, String secretKey) {
        String ts = System.currentTimeMillis() / 1000 + "";
        String signa = "";
        try {
            signa = EncryptUtil.HmacSHA1Encrypt(EncryptUtil.MD5(appId + ts), secretKey);
            return "?appid=" + appId + "&ts=" + ts + "&signa=" + URLEncoder.encode(signa, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private static String getCurrentTimeStr() {
        return sdf.format(new Date());
    }

    public static Object getLock() {
        return lock;
    }

    public static void setLock(Object lock) {
        RTASRTest.lock = lock;
    }

    public static byte[] getAudioData() {
        return audioData;
    }

    public static void setAudioData(byte[] audioData) {
        RTASRTest.audioData = audioData;
    }

    public static String getText() {
        return text;
    }

    public static void setText(String text) {
        RTASRTest.text = text;
    }
}