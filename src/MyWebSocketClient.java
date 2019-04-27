import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import util.EncryptUtil;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * ZengJunxian
 * 2019/4/27 12:43
 */
public class MyWebSocketClient extends WebSocketClient {

    private CountDownLatch handshakeSuccess;
    private CountDownLatch connectClose;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");

    MyWebSocketClient(URI serverUri, Draft protocolDraft, CountDownLatch handshakeSuccess, CountDownLatch connectClose) {
        super(serverUri, protocolDraft);
        this.handshakeSuccess = handshakeSuccess;
        this.connectClose = connectClose;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println(getCurrentTimeStr() + "\t连接建立成功！");
    }

    @Override
    public void onMessage(String msg) {
        JSONObject msgObj = JSON.parseObject(msg);
        String action = msgObj.getString("action");
        if (Objects.equals("started", action)) {
            // 握手成功
            System.out.println(getCurrentTimeStr() + "\t握手成功！sid: " + msgObj.getString("sid"));
            handshakeSuccess.countDown();
        } else if (Objects.equals("result", action)) {
            List list = getContent(msgObj.getString("data"));
            if (!list.isEmpty()) {
                //判断是否匹配成功
                if (Match.rule1(list.get(0).toString())) {
//                    System.out.println("匹配成功,下一句");
                    RTASRTest.text="";
//                    System.out.println("取消timer");
                    RTASRTest.timer.cancel();
                    RTASRTest.timerTask.cancel();
                    if(Match.i<Match.text.length) {
//                        System.out.println("重新计时");
                        RTASRTest.countTime();
                    }else {
                        RecordVoice.flag = false;
                        SendVoice.flag = false;
                        System.out.println("RecordVoice.flag = " + RecordVoice.flag);
                        System.out.println("SendVoice.flag = " + SendVoice.flag);
                        RTASRTest.timer.cancel();
                    }
                    //换到下一句
                } else {//保存整句
                    if (list.size() == 2) {
                        RTASRTest.text = RTASRTest.text + list.get(1).toString();
//                        System.out.println("list.get(1).toString() = " + list.get(1).toString());
                    }
                }
            }
        } else if (Objects.equals("error", action)) {
            // 连接发生错误
            System.out.println("Error: " + msg);
            System.exit(0);
        }
    }

    @Override
    public void onError(Exception e) {
        System.out.println(getCurrentTimeStr() + "\t连接发生错误：" + e.getMessage() + ", " + new Date());
        e.printStackTrace();
        System.exit(0);
    }

    @Override
    public void onClose(int arg0, String arg1, boolean arg2) {
        System.out.println(getCurrentTimeStr() + "\t链接关闭");
        connectClose.countDown();
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        try {
            System.out.println(getCurrentTimeStr() + "\t服务端返回：" + new String(bytes.array(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static String getCurrentTimeStr() {
        return sdf.format(new Date());
    }

    private static List<StringBuffer> getContent(String message) {
        List<StringBuffer> list = new ArrayList();
        StringBuffer resultBuilder = new StringBuffer();
        String type;
        try {
            JSONObject messageObj = JSON.parseObject(message);
            JSONObject cn = messageObj.getJSONObject("cn");
            JSONObject st = cn.getJSONObject("st");
            type = st.getString("type");
            JSONArray rtArr = st.getJSONArray("rt");
            for (int i = 0; i < rtArr.size(); i++) {
                JSONObject rtArrObj = rtArr.getJSONObject(i);
                JSONArray wsArr = rtArrObj.getJSONArray("ws");
                for (int j = 0; j < wsArr.size(); j++) {
                    JSONObject wsArrObj = wsArr.getJSONObject(j);
                    JSONArray cwArr = wsArrObj.getJSONArray("cw");
                    for (int k = 0; k < cwArr.size(); k++) {
                        JSONObject cwArrObj = cwArr.getJSONObject(k);
                        String wStr = cwArrObj.getString("w");
                        resultBuilder.append(wStr);
                    }
                }
            }
            list.add(resultBuilder);
        } catch (Exception e) {
            return null;
        }
        if (type.equals("0"))
            list.add(resultBuilder);
        return list;
    }

}