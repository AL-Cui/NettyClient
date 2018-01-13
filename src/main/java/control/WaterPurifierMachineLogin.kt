package control

import okhttp3.*
import org.dom4j.DocumentHelper
import org.slf4j.LoggerFactory
import utils.Util
import java.io.IOException
import java.security.SecureRandom
import java.util.ArrayList
import java.util.HashMap
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager

class WaterPurifierMachineLogin {
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

    private fun createOKHttpClient(): OkHttpClient {
        var mBuilder = OkHttpClient.Builder()
        mBuilder.sslSocketFactory(WaterPurifierMachineLogin.createSSLSocketFactory())
        mBuilder.hostnameVerifier(TrustAllHostnameVerifier())
        mBuilder.cookieJar(object : CookieJar {
            private val cookieStore = HashMap<HttpUrl, List<Cookie>>()
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore.put(url, cookies)

            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val cookies = cookieStore[url]
                return cookies ?: ArrayList()
            }
        })
        return mBuilder.build()
    }

//    private var mOkHttpClient = OkHttpClient.Builder()
//            .cookieJar(object : CookieJar {
//                private val cookieStore = HashMap<HttpUrl, List<Cookie>>()
//
//                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
//                    cookieStore.put(url, cookies)
//                }
//
//                override fun loadForRequest(url: HttpUrl): List<Cookie> {
//                    val cookies = cookieStore[url]
//                    return cookies ?: ArrayList()
//                }
//            })
//            .build()

    fun createRequest(macId: String, boxId: String, pass: String) {

        val requestString = setXmlInfo(macId, boxId, pass)
        if (logger.isDebugEnabled) {
            logger.debug("机器登录发送的xml：" + requestString)
        }
        val request = Request.Builder()
                .url(Url2)
                .post(RequestBody.create(MEDIA_XML_APPLICATION, requestString))
                .build()
        val call = mOkHttpClient2.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (logger.isDebugEnabled) {
                    logger.debug("机器的MAC地址==" + macId + "机器登录时的请求失败的原因==" + e.toString())
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                val resultString = response.body().string()
                val str = response.networkResponse().toString()

                if (logger.isDebugEnabled) {
                    logger.debug("机器的MAC地址==" + macId + "机器登录返回的结果代码：" + str + resultString)
                }
            }

        })

    }


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
        status.text = Util.STATUS_VALUE3
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

        checkctrl.setAttributeValue("version", "1.0")
        checkctrl.setAttributeValue("boxid", boxId)
        val devinfo = checkctrl.addElement(Util.DEVINFO)
        val echodev = devinfo.addElement(Util.ECHODEV)
        echodev.setAttributeValue("node", idString)
        echodev.setAttributeValue("register", "true")
        echodev.text = "013501"

        val devctrl = checkctrl.addElement(Util.DEVCTRL)
        val echo = devctrl.addElement(Util.ECHO)
        echo.setAttributeValue("node", idString)

        echo.text = "01350105ff01721480013081010082040000450084020000850400000000860c080000050000000000000000880142890200008a030000058b033130348c0c4650434837300000000000009d0a098081868889a0c0f2f39e05048081a0f39f111495818180010101000101010101020202a00141c00142f0200580007f04000000000000000000000000000000000000000000000000000000f12864000014320200022f0000000000060000000000060000000b000000000000000000000000000000f22800600000000000000000000000000000000003640000000000ff0017010000000000000000000000f3080000000000000000"
        val boxconf = checkctrl.addElement(Util.BOXCONF)
        boxconf.setAttributeValue("check_boxid_deleted", "true")


        return writeDoc.asXML()
    }
}