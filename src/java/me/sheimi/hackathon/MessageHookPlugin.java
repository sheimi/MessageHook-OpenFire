package me.sheimi.hackathon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collection;

import org.jivesoftware.openfire.MessageRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatManager;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class MessageHookPlugin implements Plugin, PacketInterceptor, Runnable {

    private InterceptorManager mInterceptorManager;
    private MultiUserChatManager mMultiUserChatManager;
    private MessageRouter mRouter;

    public MessageHookPlugin() {
        mInterceptorManager = InterceptorManager.getInstance();
        mMultiUserChatManager = XMPPServer.getInstance()
                .getMultiUserChatManager();
        mRouter = XMPPServer.getInstance().getMessageRouter();
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
        MessageHookManager.getInstance().processMessage(message);

    }

    @Override
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        mInterceptorManager.addInterceptor(this);
        createFIFOServer();
    }

    @Override
    public void destroyPlugin() {
        stopFIFOServer();
        mInterceptorManager.removeInterceptor(this);
    }

    public static final String TMP_DIR = "/tmp/message-hook/";
    public static final String FIFO_SERVER = TMP_DIR + "chatroom-broadcast";
    public static final int THREAD_HOLD = 500;

    private void createFIFOServer() {
        File tmpDir = new File(TMP_DIR);
        if (!tmpDir.exists())
            tmpDir.mkdirs();
        try {
            Runtime.getRuntime().exec("mkfifo " + FIFO_SERVER);
            Thread thread = new Thread(this);
            thread.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void stopFIFOServer() {
        File fifo = new File(FIFO_SERVER);
        fifo.delete();
    }

    @Override
    public void run() {
        File fifo = new File(FIFO_SERVER);
        JID roomID = new JID("room1@conference.sheimi.vm");
        while (true) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(fifo)));
                StringBuilder s = new StringBuilder();
                String line = null;
                while ((line = br.readLine()) != null) {
                    s.append(line);
                    s.append('\n');
                }
                br.close();
                
                Thread.sleep(THREAD_HOLD);

                MultiUserChatService service = mMultiUserChatManager
                        .getMultiUserChatService(roomID);
                MUCRoom room = service.getChatRoom(roomID.getNode());
                Collection<MUCRole> c = room.getOccupants();
                JID jid = new JID("room1@conference.sheimi.vm/Boardcast");
                for (MUCRole role : c) {
                    JID to = role.getUserAddress();
                    Message newMessage = new Message();
                    newMessage.setType(Message.Type.groupchat);
                    newMessage.setBody('\n' + s.toString().trim());
                    newMessage.setTo(to);
                    newMessage.setFrom(jid);
                    mRouter.route(newMessage);
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void broadCastToClient(String content) {
        File fifo = new File(MessageHookPlugin.FIFO_SERVER);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(fifo)));
            bw.write(content);
            bw.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
