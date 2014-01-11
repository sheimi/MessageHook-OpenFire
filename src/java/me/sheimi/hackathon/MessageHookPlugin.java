package me.sheimi.hackathon;

import java.io.File;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class MessageHookPlugin implements Plugin, PacketInterceptor {

    private InterceptorManager mInterceptorManager;

    public MessageHookPlugin() {
        mInterceptorManager = InterceptorManager.getInstance();
    }

    @Override
    public void interceptPacket(Packet packet, Session session,
            boolean incoming, boolean processed) throws PacketRejectedException {
        if (!(packet instanceof Message))
            return;
        if (!incoming || processed)
            return;
        final Message message = (Message) packet;
        if (message.getType() != Message.Type.groupchat)
            return;
        System.out.println(message.toString());
        if (message.getFrom().getNode().equals("room1"))
            return;
        String body = message.getBody();
        if (body == null)
            return;
        MessageHook.getInstance().processMessage(message);

    }

    @Override
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        mInterceptorManager.addInterceptor(this);
    }

    @Override
    public void destroyPlugin() {
        mInterceptorManager.removeInterceptor(this);
    }

}
