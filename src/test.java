import javax.sound.midi.Soundbank;
import java.util.*;

/**
 * ZengJunxian
 * 2019/4/25 11:29
 */
public class test {
    private static String[] text = {"静夜思", "李白", "床前明月光", "疑是地上霜", "举头望明月", "低头思故乡"};
    private static String[] message = {"kcwjhbc静ve夜 思dkn", "jgvhm李白", "月光hb窗前", "wljnr疑是地上霜kkj", "望明月举头", "yilyiflyli"};
    static int i = 0;
    static boolean flag = true;

    public static void main(String[] args) {
//        Timer timer = new Timer();
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                i++;
//                System.out.println(i);
//                if (i == text.length-1)
//                    timer.cancel();
//            }
//        };
//        timer.schedule(task, 5000, 5000);
        List list=new ArrayList();
        for (int j = 0; j < text.length; j++) {
            list.add(text[j]);
        }
        Iterator<String> iter = list.iterator();
        while(iter.hasNext()){
            String s =iter.next();
            System.out.println(s);
        }
        System.out.println("list.size() = " + list.size());
    }
}
