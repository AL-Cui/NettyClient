package core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/***
 * @author Duo.Cui
 * MainClass
 */
public class MainClass {
    private static final Logger logger = LoggerFactory.getLogger(MainClass.class);

    public static void main(String[] args) {
//        File file = new File("/MyData/queen2/result.txt");
        File file = new File("result.txt");
//        File file = new File("/data/queen16/result.txt");
//        File file = new File("/Users/cuiduo/Downloads/result.txt");    //存放生成的机器相关属性的文件
//        File file = new File("/home/iot/MyData/machine/result.txt");
        PrintStream ps = null;
        try {
            ps = new PrintStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (int i = 1; i <= Integer.parseInt(args[0]); i++) {
            try {
                String macAddress = Util.getRandomMacAddress();
                logger.info(macAddress);
                String text = "FS-E85E-W:" + macAddress;
                ps.println(text);
                HeartBeatsClient client = new HeartBeatsClient(macAddress);
                client.start();
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
