package heartbeat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Util;

/***
 * @author Duo.Cui
 * 心跳机制相关类
 */
@Sharable
public class ConnectorIdleStateTrigger extends ChannelInboundHandlerAdapter {
    private String macAddress;

    public ConnectorIdleStateTrigger(String mac) {

        this.macAddress = mac;
    }

    private static final Logger logger = LoggerFactory.getLogger(ConnectorIdleStateTrigger.class);
    private String string;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                    string = createHeartBeatXml(macAddress);
                    ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(string+"\r\n",
                            CharsetUtil.UTF_8));

                    ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate());
                ReferenceCountUtil.release(HEARTBEAT_SEQUENCE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /***
     * 创建心跳xml
     * @param macAddress
     * @return result
     */
    public String createHeartBeatXml(String macAddress) {
        String result;
        Document writeDoc = DocumentHelper.createDocument();
        Element root = writeDoc.addElement(Util.NODE_TCP_MSG);
        Element msg = root.addElement(Util.NODE_MSG);
        msg.setText(Util.MSG_VALUE_HEARTBEAT);
        Element data = root.addElement(Util.NODE_DATA);
        Element mac_address = data.addElement(Util.NODE_MAC_ADDRESS);
        mac_address.setText(macAddress);
        Element version_number = data.addElement(Util.NODE_VERSION);
        version_number.setText(Util.SHARP_VERSION);
        Element mach_version_number = data.addElement(Util.NODE_VERSIONMACHVER);
        mach_version_number.setText(Util.MACHINE_VERSION);
        result = writeDoc.asXML();

        return result;
    }
}
