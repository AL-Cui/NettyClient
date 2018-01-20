package control

import okhttp3.*
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.slf4j.LoggerFactory
import utils.Util
import java.io.IOException
import java.security.SecureRandom
import java.util.ArrayList
import java.util.HashMap
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager

/***
 * @author Duo.Cui
 * 控制功能类
 */
class WaterPurifierControlClass {
    val logger = LoggerFactory.getLogger(javaClass.simpleName)!!
    private val MEDIA_XML_APPLICATION = MediaType.parse("application/xml;gzip;keep-alive")
    private val mOkHttpClient2 = createOKHttpClient()

    companion object {
        val Url = "https://iotwater-rd.smart-blink.com/hems/control/api/cmonitoring"
        val Url2 = "https://10.191.201.13/hems/control/api/cmonitoring"
        fun createSSLSocketFactory(): SSLSocketFactory {
            var sslSocketFactory: SSLSocketFactory? = null

            try {
                val sc = SSLContext.getInstance("TLS")
                sc.init(null, arrayOf<TrustManager>(TrustAllCerts()), SecureRandom())

                sslSocketFactory = sc.socketFactory
            } catch (e: Exception) {
            }


            return sslSocketFactory!!
        }
    }

    var commandList = Array(3, { "" })
    /***
     * 创建OkHttpClient
     * @return OkHttpClient
     */
    private fun createOKHttpClient(): OkHttpClient {
        var mBuilder = OkHttpClient.Builder()
        mBuilder.sslSocketFactory(WaterPurifierControlClass.createSSLSocketFactory())
        mBuilder.hostnameVerifier(TrustAllHostnameVerifier())
        mBuilder.cookieJar(object : CookieJar {
            private val cookieStore = HashMap<HttpUrl, List<Cookie>>()
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore.put(url, cookies)
                logger.info("捕捉到的cookie=" + cookies.toString())
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val cookies = cookieStore[url]
                logger.info("请求带过去的cookie=" + cookies.toString())
                return (cookies ?: ArrayList())
            }
        })
        return mBuilder.build()
    }

    /***
     * 控制时第一个Http请求
     * @param macId
     * @param boxId
     * @param pass
     */
    fun createRequest(macId: String, boxId: String, pass: String) {

        val requestString = setXmlInfo(macId, boxId, pass)

        logger.info("控制时第一个请求发送的xml：" + requestString)


        val request = Request.Builder()
                .url(Url2)
                .post(RequestBody.create(MEDIA_XML_APPLICATION, requestString))
                .build()

        val call = mOkHttpClient2.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                logger.info("机器的MAC地址==" + macId + "控制时的第一个请求失败的原因==" + e.toString())


            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {


                val resultString = response.body().string()
                val str = response.networkResponse().toString()

                logger.info("机器的MAC地址==" + macId + "控制时第一个请求返回结果==：" + str + resultString)

                //解析第一次返回的xml
                commandList = parserXmlId(resultString)
                //给第二次请求需要的参数
                waterPurifierCreateSecondRequest(macId, boxId, pass, commandList)
            }
        })

    }

    /***
     * 控制时第二个Http请求
     * @param macId
     * @param boxId
     * @param pass
     * @param echo
     */
    private fun waterPurifierCreateSecondRequest(macId: String, boxId: String, pass: String, echo: Array<String>) {

        val requestString = waterPurifierSetXmlInfo2(macId, boxId, pass, echo)

        logger.info("控制时第二个请求发送的xml：" + requestString)

        val request = Request.Builder()
                .url(Url2)
                .post(RequestBody.create(MEDIA_XML_APPLICATION, requestString))
                .build()
        val call = mOkHttpClient2.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                logger.info("机器的MAC地址==" + macId + "控制时的第二个请求失败的原因==" + e.toString())

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                val resultString = response.body().string()
                val str = response.networkResponse().toString()

                logger.info("机器的MAC地址==" + macId + "控制时第二个请求返回结果：" + str + resultString)

            }


        })
    }

    /***
     * 编辑第二次Http请求时的xml数据
     * @param macId
     * @param boxId
     * @param passString
     * @param echo
     * @return writeDoc.asXML()
     */
    private fun waterPurifierSetXmlInfo2(macId: String, boxId: String, passString: String, echo: Array<String>): String {
        val writeDoc = DocumentHelper.createDocument()
        val root = writeDoc.addElement(Util.CMONITORING)
        val cmd = root.addElement(Util.CMD)
        cmd.text = Util.CONTROL
        val info = root.addElement(Util.INFO)
        val monitor = info.addElement(Util.MONITOR)
        val dtver = monitor.addElement(Util.DTVER)
        dtver.text = Util.DTVER_VALUE
        val status = monitor.addElement(Util.STATUS)
        status.text = Util.STATUS_VALUE2
        val type = monitor.addElement(Util.TYPE)
        type.text = Util.TYPE_VALUE
        val id = monitor.addElement(Util.ID)
        id.text = macId
        val pass = monitor.addElement(Util.PASS)
        pass.text = passString
        val pv_addr = monitor.addElement(Util.PV_ADDR)
        pv_addr.text = Util.PV_ADDR_VALUE
        val data = root.addElement(Util.DATA)
        val control = data.addElement(Util.CONTROL)

        control.addAttribute("version", "1.0")
        control.addAttribute("boxid", boxId)

        val conf = control.addElement(Util.CONF)
        val set_time = conf.addElement(Util.SET_TIME)
        set_time.text = Util.SET_TIME_VALUE
        val center_addr = conf.addElement(Util.CENTER_ADDR)
//        center_addr.text = echo[echo.size - 1]
        val devctrl = control.addElement(Util.DEVCTRL)
        val echo1 = devctrl.addElement(Util.ECHO)
        echo1.addAttribute("id", "1")
        echo1.addAttribute("node", macId)

        if (echo[echo.size - 1] != null) {
            center_addr.text = echo[2]
            logger.info("组装的指令中的centerAddress=" + echo[2])
            if (echo[0].indexOf("f308") > 0) {
                echo1.text = "01350105ff0171028000f300"

                val orderInt = echo[0].indexOf("f308")
                val f3String = echo[0].substring(orderInt, orderInt + 20)
                val statusReply = StringBuilder(Util.NOWSTATUS)
                statusReply.replace(82, 102, f3String)
                val echo2 = devctrl.addElement(Util.ECHO)
                echo2.addAttribute("id", "2")
                echo2.addAttribute("node", macId)
                echo2.text = statusReply.toString()
            } else {
                echo1.text = "01350105ff0171018000"
                if (echo[0].indexOf("800130") > 0) {
                    val statusReply = StringBuilder(Util.NOWSTATUS)
                    val echo2 = devctrl.addElement(Util.ECHO)
                    echo2.addAttribute("id", "2")
                    echo2.addAttribute("node", macId)
                    echo2.text = statusReply.toString()

                } else if (echo[0].indexOf("800131") > 0) {
                    val statusReply = StringBuilder(Util.OFFSTATUS)
                    val echo2 = devctrl.addElement(Util.ECHO)
                    echo2.addAttribute("id", "2")
                    echo2.addAttribute("node", macId)
                    echo2.text = statusReply.toString()

                }
            }
        } else {
            val statusReply = StringBuilder(Util.NOWSTATUS)
            center_addr.text = echo[1]
            logger.info("组装的指令中的centerAddress=" + echo[1])
            echo1.text = statusReply.toString()
        }


        return writeDoc.asXML()
    }
    /***
     * 编辑第一次Http请求时的xml数据
     * @param idString
     * @param boxId
     * @param passString
     * @return writeDoc.asXML()
     */
    private fun setXmlInfo(idString: String, boxId: String, passString: String): String {
        val writeDoc = DocumentHelper.createDocument()
        val root = writeDoc.addElement(Util.CMONITORING)
        val cmd = root.addElement(Util.CMD)
        cmd.text = Util.CHECKCTRL
        val info = root.addElement(Util.INFO)
        val monitor = info.addElement(Util.MONITOR)
        val dtver = monitor.addElement(Util.DTVER)
        dtver.text = Util.DTVER_VALUE
        val status = monitor.addElement(Util.STATUS)
        status.text = Util.STATUS_VALUE2
        val type = monitor.addElement(Util.TYPE)
        type.text = Util.TYPE_VALUE
        val id = monitor.addElement(Util.ID)
        id.text = idString
        val pass = monitor.addElement(Util.PASS)
        pass.text = passString
        val pv_addr = monitor.addElement(Util.PV_ADDR)
        pv_addr.text = Util.PV_ADDR_VALUE
        val data = root.addElement(Util.DATA)
        val checkctrl = data.addElement(Util.CHECKCTRL)

        checkctrl.addAttribute("version", "1.0")
        checkctrl.addAttribute("boxid", boxId)

        val boxconf = checkctrl.addElement(Util.BOXCONF)
        boxconf.addAttribute("check_boxid_deleted", "true")

        return writeDoc.asXML()
    }

    /***
     * 解析第一次Http请求返回的xml数据
     * @param XMLString
     * @return commandList
     */
    fun parserXmlId(XMLString: String): Array<String> {


        try {
            val readDoc = DocumentHelper.parseText(XMLString)
            val rootNode = readDoc.rootElement//CMONITORING

            if (Util.CMONITORING == rootNode.name) {
                val data = rootNode.element(Util.DATA)
                val control = data.element(Util.CONTROL)
                val conf = control.element(Util.CONF)
                val centerAddress = conf.elementTextTrim(Util.CENTER_ADDR)
                val devctrl = control.element(Util.DEVCTRL)
                val echoList: List<Element>
                echoList = devctrl.elements(Util.ECHO) as List<Element>

                for (i in echoList.indices) {
                    commandList[i] = echoList[i].data.toString()
                }
                commandList[echoList.size] = centerAddress
                logger.info("centerAddress=" + commandList[echoList.size] + "echoList.size=" + echoList.size)
//                val echoOne = echoList[0].data.toString()
//                val echoTwo = echoList[1].data.toString()
//                commandList[0] = echoOne
//                commandList[1] = echoTwo
//                commandList[2] = centerAddress

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return commandList
    }
}

