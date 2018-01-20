package control

import okhttp3.*
import org.dom4j.DocumentHelper
import org.slf4j.LoggerFactory
import utils.Util
import java.io.IOException
import java.security.SecureRandom
import java.util.ArrayList
import java.util.HashMap
import java.util.Arrays
import java.security.KeyStore
import javax.net.ssl.*

/***
 * @author Duo.Cui
 * 机器绑定类
 */
class WaterPurifierBinding {
    val logger = LoggerFactory.getLogger(javaClass.simpleName)!!
    private val MEDIA_XML_APPLICATIOON = MediaType.parse("application/xml;charset=utf-8")
    private val mOkHttpClient2 = createOKHttpClient()

    companion object {
        val Url = "https://iotwater-rd.smart-blink.com/hems/upload/api/monitoring"
        val Url2 = "https://10.191.201.13/hems/upload/api/monitoring"
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

    var resultList: Array<String> = Array(2, { "" })
    /***
     * 创建OkHttpClient
     * @return OkHttpClient
     */
    private fun createOKHttpClient(): OkHttpClient {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers = trustManagerFactory.trustManagers
        if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
            throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers))
        }
        val trustManager = trustManagers[0] as X509TrustManager

        var mBuilder = OkHttpClient.Builder()
        mBuilder.sslSocketFactory(createSSLSocketFactory(), trustManager)
        mBuilder.hostnameVerifier(TrustAllHostnameVerifier())
        mBuilder.cookieJar(object : CookieJar {
            private val cookieStore = HashMap<HttpUrl, List<Cookie>>()
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                logger.info("绑定时捕捉到的cookie=" + cookies.toString())
//                if (cookies.size == 2) {
//                    cookieStore.put(url, cookies)
//                }
                cookieStore.put(url, cookies)
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val cookies = cookieStore[url]
                logger.info("绑定时发送的cookie=" + cookies.toString())
                return cookies ?: ArrayList()
            }
        })
        return mBuilder.build()
    }

    /***
     * 绑定过程中的第一次Http请求,创建boxId
     * @param macId
     * @return resultList
     */
    fun boxIDCreateFirstRequest(macId: String): Array<String> {

        val postString = setXmlInfo1(macId)

        logger.info("绑定时第一个请求发送的参数==" + postString)

        val request = Request.Builder()
//                .url("https://shcloud-rd.sharp.cn/hems/upload/api/monitoring")
                .url(Url2)
                .post(RequestBody.create(MEDIA_XML_APPLICATIOON, postString))
                .build()
        val call = mOkHttpClient2.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.info("机器的MAC地址==" + macId + "绑定时的第一个请求失败的原因==" + e.toString())

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val resultString = response.body().string()
                val boxId = parserXmlId(resultString)
                val str = response.networkResponse().toString()
                resultList[0] = boxId
                resultList[1] = boxIdCreateSecondRequest(boxId, macId)

                logger.info("机器的MAC地址==" + macId + "绑定时的第一个请求结果==" + str + resultString)


            }

        })
        return resultList
    }

    /***
     * 绑定过程中的第二次Http请求，创建passString、centerAddress
     * @param boxId
     * @param macId
     * @return passString
     */
    fun boxIdCreateSecondRequest(boxId: String, macId: String): String {
        var passString = ""
        val postString = setXmlInfo2(macId, boxId)

        logger.info("绑定时第二个请求发送的参数==" + postString)

        val request = Request.Builder()
                .url(Url2)
                .post(RequestBody.create(MEDIA_XML_APPLICATIOON, postString))
                .build()
        val call = mOkHttpClient2.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                logger.info("机器的MAC地址==" + macId + "绑定时的第二个请求失败的原因==" + e.toString())

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val str = response.networkResponse().toString()
                val resultString = response.body().string()

                logger.info("机器的MAC地址==" + macId + "绑定时第二个请求返回结果==" + str + resultString)

                val list = parserXmlpass(resultString)
                resultList[1] = list[0]
                passCreateThirdRequest(list[0], macId, boxId, list[1])
            }

        })
        return passString
    }
    /***
     * 绑定过程中的第三次Http请求
     * @param macId
     * @param pass
     * @param boxId
     * @param centerAddress
     */
    private fun passCreateThirdRequest(pass: String, macId: String, boxId: String, centerAddress: String) {
        val postString = setXmlInfo3(macId, pass, centerAddress)

        logger.info("绑定时第三个请求发送的参数==" + postString)

        val request = Request.Builder()
                .url(Url2)
                .post(RequestBody.create(MEDIA_XML_APPLICATIOON, postString))
                .build()
        val call = mOkHttpClient2.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                logger.info("机器的MAC地址==" + macId + "绑定时的第三个请求失败的原因==" + e.toString())

            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val str = response.networkResponse().toString()
                val resultString = response.body().string()

                logger.info("机器的MAC地址==" + macId + "绑定时第三个请求返回结果==" + str + resultString)

                WaterPurifierMachineLogin().createRequest(macId, boxId, pass)
            }
        })


    }

    /***
     * 根据第一次Http请求返回的xml解析出boxId
     * @param XMLString
     * @return boxId
     */
    fun parserXmlId(XMLString: String): String {

        var boxId = ""

        try {
            val readDoc = DocumentHelper.parseText(XMLString)
            val rootNode = readDoc.rootElement//MONITORING

            if (Util.MONITORING == rootNode.name) {
                val data = rootNode.element(Util.DATA)
                val extension = data.element(Util.EXTENSION)

                boxId = extension.elementTextTrim(Util.BOX_ID)

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return boxId
    }

    /**
     * 根据第二次请求返回的xml解析出pass、centerAddress
     * @param XMLString
     * @return secondResultList
     */
    fun parserXmlpass(XMLString: String): Array<String> {

        var secondResultList: Array<String> = Array(2, { "" })
        var pass: String

        try {
            val readDoc = DocumentHelper.parseText(XMLString)
            val rootNode = readDoc.rootElement//MONITORING

            if (Util.MONITORING.equals(rootNode.name)) {
                val data = rootNode.element(Util.DATA)
                val put = data.element(Util.PUT)

                pass = put.elementTextTrim(Util.PASS)
                var centerAddress = put.elementTextTrim(Util.CENTER_ADDR)
                secondResultList[0] = pass
                secondResultList[1] = centerAddress

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return secondResultList
    }

    /***
     * 编辑第一次Http请求所需的xml
     * @param idString
     * @return writeDoc.asXML()
     */
    private fun setXmlInfo1(idString: String): String {
        val writeDoc = DocumentHelper.createDocument()
        val root = writeDoc.addElement(Util.MONITORING)
        val cmd = root.addElement(Util.CMD)
        cmd.text = Util.AUTH
        val info = root.addElement(Util.INFO)
        val monitor = info.addElement(Util.MONITOR)
        val dtver = monitor.addElement(Util.DTVER)
        dtver.text = Util.DTVER_VALUE
        val status = monitor.addElement(Util.STATUS)
        status.text = Util.STATUS_VALUE
        val type = monitor.addElement(Util.TYPE)
        type.text = Util.TYPE_VALUE
        val id = monitor.addElement(Util.ID)
        id.text = idString
        val pass = monitor.addElement(Util.PASS)
        pass.text = "        "
        val pv_addr = monitor.addElement(Util.PV_ADDR)
        pv_addr.text = Util.PV_ADDR_VALUE
        val data = root.addElement(Util.DATA)
        val memboption = data.addElement(Util.MEMBOPTION)

        memboption.addAttribute("force_create_id", "true")
        memboption.addAttribute("app_secret", "orCPmn4UHx5bptZm1mmEUQ3XC1q%2BI%2B3gRdCVX4YfTEY%3D")
        return writeDoc.asXML()
    }

    /***
     * 编辑第二次Http请求所需的xml
     * @param idString
     * @param boxId
     * @return writeDoc.asXML()
     */
    private fun setXmlInfo2(idString: String, boxId: String): String {
        val writeDoc = DocumentHelper.createDocument()
        val root = writeDoc.addElement(Util.MONITORING)
        val cmd = root.addElement(Util.CMD)
        cmd.text = Util.MEMB
        val info = root.addElement(Util.INFO)
        val monitor = info.addElement(Util.MONITOR)
        val dtver = monitor.addElement(Util.DTVER)
        dtver.text = Util.DTVER_VALUE
        val status = monitor.addElement(Util.STATUS)
        status.text = Util.STATUS_VALUE
        val type = monitor.addElement(Util.TYPE)
        type.text = Util.TYPE_VALUE
        val id = monitor.addElement(Util.ID)
        id.text = idString
        val pass = monitor.addElement(Util.PASS)
        pass.text = "        "
        val pv_addr = monitor.addElement(Util.PV_ADDR)
        pv_addr.text = Util.PV_ADDR_VALUE
        val data = root.addElement(Util.DATA)
        val member = data.addElement(Util.MEMBER)
        member.text = Util.ONE
        val extension = data.addElement(Util.EXTENSION)
        val box_id = extension.addElement(Util.BOX_ID)

        box_id.text = boxId
        return writeDoc.asXML()
    }


    /***
     * 编辑第三次Http请求所需的xml
     * @param idString
     * @param passString
     * @param centerAddress
     * @return writeDoc.asXML()
     */
    private fun setXmlInfo3(idString: String, passString: String, centerAddress: String): String {
        val writeDoc = DocumentHelper.createDocument()
        val root = writeDoc.addElement(Util.MONITORING)
        val cmd = root.addElement(Util.CMD)
        cmd.text = Util.PUT
        val info = root.addElement(Util.INFO)
        val monitor = info.addElement(Util.MONITOR)
        val dtver = monitor.addElement(Util.DTVER)
        dtver.text = Util.DTVER_VALUE
        val status = monitor.addElement(Util.STATUS)
        status.text = Util.STATUS_VALUE
        val type = monitor.addElement(Util.TYPE)
        type.text = Util.TYPE_VALUE
        val id = monitor.addElement(Util.ID)
        id.text = idString
        val pass = monitor.addElement(Util.PASS)
        pass.text = passString
        val pv_addr = monitor.addElement(Util.PV_ADDR)
        pv_addr.text = Util.PV_ADDR_VALUE
        val data = root.addElement(Util.DATA)
        val put = data.addElement(Util.PUT)
        val set_time = put.addElement(Util.SET_TIME)
        set_time.text = Util.SET_TIME_VALUE
        val next_common = put.addElement(Util.NEXT_COMM)
        next_common.text = Util.NEXT_COMM_VALUE
        val center_addr = put.addElement(Util.CENTER_ADDR)
        center_addr.text = centerAddress

        return writeDoc.asXML()
    }

}