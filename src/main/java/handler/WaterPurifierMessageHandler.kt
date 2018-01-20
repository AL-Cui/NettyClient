package handler

import control.WaterPurifierBinding
import control.WaterPurifierControlClass
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.slf4j.LoggerFactory
import utils.Util

/***
 * client 消息处理handler
 */
class WaterPurifierMessageHandler(private val macAddress: String): SimpleChannelInboundHandler<String>() {
    val logger = LoggerFactory.getLogger(javaClass.simpleName)!!
    lateinit var arrayResult: Array<String>
    var boxId = ""
    var passString = ""


    override fun channelRead0(channelHandlerContext: ChannelHandlerContext, string: String) {
//        logger.info("WaterPurifierMessageHandler拦截到的消息=" + string)
        readXML(string,channelHandlerContext)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

    /***
     * 解析收到的数据
     * @param contents
     * @return
     */
    private fun readXML(contents: String, channelHandlerContext: ChannelHandlerContext?) {
        try {
            var readDoc: Document = DocumentHelper.parseText(contents)
            val rootNode: Element = readDoc.rootElement
            if (Util.NODE_TCP_MSG.equals(rootNode.name)) {
                if (Util.MSG_VALUE_HEARTBEATRES.equals(rootNode.elementTextTrim(Util.NODE_MSG))) {   //收到心跳包回复，什么也不做

                } else if (Util.MSG_VALUE_CTRLNOTIFY.equals(rootNode.elementTextTrim(Util.NODE_MSG))) {
                    //收到服务器发过来的指令
//                    session!!.write(createHeartBeatxml(macAddress))    //收到控制指令后立即回复心跳包
                    if (logger.isDebugEnabled) {
                        logger.debug("收到的服务器指令 = ${rootNode.elementTextTrim(Util.NODE_CMD)}")
                    }
                    when (rootNode.elementTextTrim(Util.NODE_CMD)) {
                        Util.CMD_VALUE -> {
                            if (logger.isDebugEnabled) {
                                logger.debug("收到绑定指令")
                            }
                            //绑定指令，取得boxID，pass
                            arrayResult = WaterPurifierBinding().boxIDCreateFirstRequest(macAddress)
                            boxId = arrayResult[0]
                            passString = arrayResult[1]

                        }
                        Util.CMD_VALUE_BOXDELETE -> {
                            if (logger.isDebugEnabled) {
                                logger.debug("收到解绑指令")
                            }
                            boxId = null.toString()
                            if (logger.isDebugEnabled) {
                                logger.debug("boxId:" + boxId)
                            }
                        }
                        Util.CMD_VALUE_CHECKCMD -> {
                            if (logger.isDebugEnabled) {
                                logger.debug("收到控制指令")
                            }
                            //睡眠200ms
                            Thread.sleep(50)
                            //去HMS取控制指令，并上报结果，再把状态给回去
                            WaterPurifierControlClass().createRequest(macAddress, arrayResult[0], arrayResult[1])
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}