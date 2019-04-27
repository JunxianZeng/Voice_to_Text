import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * ZengJunxian
 * 2019/4/27 9:24
 */
public class RecordVoice implements Runnable {

    // 每次发送的数据大小 1280 字节
    private static final int CHUNCKED_SIZE = 1280;
    private static AudioFormat audioFormat;
    private static TargetDataLine targetDataLine;
    //设置录音停止运行
    public static boolean flag = true;
    private Object lock;
    private File audioFile;
    private ByteArrayInputStream bais;
    private ByteArrayOutputStream baos;
    //最后用来保存音频
    private ByteArrayOutputStream saveVoice;
    private AudioInputStream ais;

    RecordVoice(Object lock) throws LineUnavailableException {
        this.lock = lock;
        //音频保存地址
        String filePath = "E:\\voice\\voice_cache.wav";
        audioFile = new File(filePath);
        bais = null;
        baos = new ByteArrayOutputStream();
        saveVoice = new ByteArrayOutputStream();
        ais = null;
        audioFormat = getAudioFormat();
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
    }

    public void run() {
        System.out.println("RecordVoiceRunning.....");
        synchronized (lock) {
            while (flag) {
                try {
//                    System.out.println("Record...");
                    targetDataLine.open(audioFormat);
                    targetDataLine.start();
                    byte[] fragment = new byte[CHUNCKED_SIZE];
                    ais = new AudioInputStream(targetDataLine);
                    targetDataLine.read(fragment, 0, fragment.length);
                    baos.write(fragment);
                    saveVoice.write(fragment);
                    //取得录音输入流
                    audioFormat = getAudioFormat();
                    byte[] audioData = baos.toByteArray();
                    //将音频保存在RTASRTest
                    RTASRTest.setAudioData(audioData);
                    baos.reset();
                    lock.notifyAll();
//                    Thread.sleep(2000);
                    lock.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                if(Match.i==Match.text.length)
//                System.out.println("endofwhile");
            }
            //
            System.out.println("录音结束了");
//            System.out.println("flag = " + flag);
            if (!flag) {
                //定义最终保存的文件名
                System.out.println("开始生成语音文件");
                try {
                    byte[] data = saveVoice.toByteArray();
                    bais = new ByteArrayInputStream(data);
                    ais = new AudioInputStream(bais, audioFormat, data.length / audioFormat.getFrameSize());
                    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, audioFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stopRecognize();
                lock.notifyAll();
            }
        }
    }
    // end run

    private static void stopRecognize() {
        flag = false;
        targetDataLine.stop();
        targetDataLine.close();
    }

    private static AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        // 8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        // 8,16
        int channels = 1;
        // 1,2
        boolean signed = true;
        // true,false
        boolean bigEndian = false;
        // true,false
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }// end getAudioFormat

    public static boolean isFlag() {
        return flag;
    }

    public static void setFlag(boolean flag) {
        RecordVoice.flag = flag;
    }
}
