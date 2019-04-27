import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * ZengJunxian
 * 2019/4/25 11:06
 */
class Match {
    static String[] text = {"静夜思", "床前明月光", "疑是地上霜", "举头望明月", "低头思故乡"};
    static int i = 0;

    //15秒内完全匹配
    static boolean rule1(String message) {
        String rule1 = null;
        if(i<text.length)
        {
             rule1 = ".*" + text[i] + ".*";
        }
        if (i<text.length&&Pattern.matches(rule1, message)) {
            System.out.println(text[i]);//success
            //System.out.println("i="+i);
            i++;
            if(i==text.length){
                RecordVoice.flag=false;
                SendVoice.flag=false;
                RTASRTest.timer.cancel();
                RTASRTest.timerTask.cancel();
            }
            return true;
        }
        return false;
    }

    /**
     * @param message 输入一个消息字符串
     * @return List 当前判定的诗句与输入的诗句的交集字符集合
     */
    //15秒结束后查找有多少的字正确
    public static List rule2(String message) {
        char[] arr1 = text[i].toCharArray();
        char[] arr2 = message.toCharArray();
        List<Character> list1 = new ArrayList<>();
        List<Character> list2 = new ArrayList<>();
        for (char c : arr1) {
            list1.add(c);
        }
        for (char c : arr2) {
            list2.add(c);
        }
        list2.retainAll(list1);
        return list2;
    }
}