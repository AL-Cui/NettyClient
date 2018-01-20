package utils;

import core.HeartBeatsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Random;

/***
 * @author Duo.Cui
 * 工具类
 */
public class Util {
    public static final String[] strings = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    public static final int[] indexInt = {1, 3, 5, 7, 9, 11, 13, 15};
    String[] numberInt = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
    public static final Logger logger = LoggerFactory.getLogger(Util.class);
    /**
     * NULL字符串
     */
    public static final String VALUE_NULL = "null";

    /**
     * Xml:节点
     */
    public static final String NODE_TCP_MSG = "tcp_msg";

    /**
     * Xml:节点
     */
    public static final String NODE_MSG = "msg";

    /**
     * Xml:节点信息内容
     */
    public static final String MSG_VALUE_HEARTBEATRES = "heartbeatres";

    /**
     * Xml:节点信息内容
     */
    public static final String MSG_VALUE_HEARTBEAT = "heartbeat";

    /**
     * Xml:节点信息内容
     */
    public static final String MSG_VALUE_NOTIFY = "notify";

    /**
     * Xml:节点信息内容
     */
    public static final String MSG_VALUE_LINKRESET = "linkreset";

    /**
     * Xml:节点信息内容
     */
    public static final String MSG_VALUE_UPDATE = "updateversionres";

    /**
     * Xml:节点信息内容
     */
    public static final String MSG_VALUE_MACHVER = "updatemachverres";

    /**
     * Xml:节点信息内容
     */
    public static final String CMD_VALUE_RECEIVE = "receiveverionres";

    /**
     * Xml:节点信息内容
     */
    public static final String CMD_VALUE_RECEIVEMACHVER = "receivemachverres";

    /**
     * Xml:节点信息内容
     */
    public static final String MSG_VALUE_CTRLNOTIFY = "ctrlnotify";

    /**
     * Xml:节点信息内容
     */
    public static final String CMD_VALUE_UPDATEVERSION = "updateversion";

    /**
     * Xml:节点信息内容
     */
    public static final String CMD_VALUE_UPDATEMACHVER = "updatemachver";


    /**
     * Xml:节点
     */
    public static final String NODE_UPDATEFLAG = "update_flag";

    /**
     * Xml:节点信息内容
     */
    public static final String CMD_VALUE = "initialsetcmd";

    /**
     * Xml:节点信息内容
     */
    public static final String CMD_VALUE_CHECKCMD = "checkcmd";

    /**
     * Xml:节点信息内容
     */
    public static final String CMD_VALUE_BOXDELETE = "boxdelete";

    /**
     * Xml:节点
     */
    public static final String NODE_DATA = "data";

    public static final String DATA = "data";
    /**
     * Xml:节点
     */
    public static final String NODE_MAC_ADDRESS = "mac_address";

    /**
     * Xml:节点
     */
    public static final String NODE_SERVER_TIME = "server_time";

    /**
     * JKS密钥库
     */
    public static final String KEY_JKS = "JKS";

    /**
     * X.509密钥管理器
     */
    public static final String KEY_MANAGER_X509 = "SunX509";

    /**
     * SSL协议版本
     */
    public static final String SSL_VERSION = "TLSV1.2";
    public static final String SHARP_VERSION = "SHARP_AP_1_0.1.43";
    public static final String MACHINE_VERSION = "SHARPPCIANRS_AP_FPCH70_3_0";

    /** Xml:节点  */
    public static final String NODE_CMD = "cmd";

    /** Xml:节点  */
    public static final String NODE_URL = "url";

    /** Xml:节点  */
    public static final String NODE_VERSION = "version_number";

    /** Xml:节点  */
    public static final String NODE_VERSIONMACHVER = "mach_version_number";

    public static final String CMD = "cmd";

    public static final String MEMB = "memb";

    public static final String MEMBER = "member";
    public static final String DEVINFO = "devinfo";
    public static final String ECHODEV = "echodev";
    public static final String EXTENSION = "extension";
    public static final String BOX_ID = "box_id";
    public static final String PUT = "put";

    public static final String SET_TIME = "set_time";

    public static final String SET_TIME_VALUE = "1438662643073";
    public static final String PASS = "pass";

    public static final String PV_ADDR = "pv_addr";

    public static final String PV_ADDR_VALUE = "120.000.000.001";

    public static final String MEMBOPTION = "memboption";
    public static final String MONITORING = "monitoring";

    public static final String AUTH = "auth";

    public static final String INFO = "info";

    public static final String MONITOR = "monitor";

    public static final String DTVER = "dtver";

    public static final String DTVER_VALUE = "SHARP_A01 0.14.6252";

    public static final String STATUS = "status";

    public static final String STATUS_VALUE = "00";

    public static final String TYPE = "type";

    public static final String TYPE_VALUE = "wifimodule";

    public static final String ID = "id";
    public static final String ONE = "1";
    public static final String NEXT_COMM = "next_comm";

    public static final String NEXT_COMM_VALUE = "10000";

    public static final String CENTER_ADDR = "center_addr";

    public static final String CENTER_ADDR_VALUE = "shcloud-rd.sharp.cn";
    public static final String CMONITORING = "cmonitoring";

    public static final String STATUS_VALUE2 = "01";

    public static final String STATUS_VALUE3 = "02";

    public static final String CHECKCTRL = "checkctrl";

    public static final String CONTROL = "control";

    public static final String CONF = "conf";

    public static final String BOXCONF = "boxconf";

    public static final String DEVCTRL = "devctrl";

    public static final String ECHO = "echo";
    public static final String NOWSTATUS = "01350105ff01720f80013081010082040000450084020000850400000000860c080000050000000000000000880142890200008b03313034a00141c00142f0200580007f04000000000000000000000000000000000000000000000000000000f12864000014320200022f0000000000060000000000060000000b000000000000000000000000000000f228002000000000000000000000030000000003640000000000ff001401000000000000000000000000f3081742000000000100";
    public static final String OFFSTATUS = "01350105ff01720f80013181010082040000450084020000850400000000860c080000050000000000000000880142890200008b03313034a00141c00142f0200580007f04000000000000000000000000000000000000000000000000000000f12864000014320200022f0000000000060000000000060000000b000000000000000000000000000000f228002000000000000000000000030000000003640000000000ff001401000000000000000000000000f3081742000000000100";

    /***
     * 随机生成MacAddress
     * @return
     */
    public static String getRandomMacAddress() {
        String result = "";
        int index = 0;
        Random random = new Random();
        for (int i = 1; i <= 17; i++) {
            if (i == 2) {
                index = indexInt[random.nextInt(8)];
                result += strings[index];
            } else if (i % 3 == 0) {
                result += "-";
            } else {
                index = random.nextInt(16);
                result += strings[index];
            }
        }
        return result;
    }

    /***
     * 初始化SSL认证
     * @return
     */
    public static SSLContext initSSLContext() {
        SSLContext sslContext = null;
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            InputStream ksInputStream = HeartBeatsClient.class.getResourceAsStream("/purifierdevice.keystore");
            ks.load(ksInputStream, "123321".toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(ks);
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
        } catch (Exception e) {

            logger.error(e.toString());
        }
        if (sslContext !=null){
            logger.debug("sslContext初始化成功");
        }
        return sslContext;
    }
}
