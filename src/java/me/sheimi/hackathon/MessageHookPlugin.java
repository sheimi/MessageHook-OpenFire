package me.sheimi.hackathon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class MessageHookPlugin implements Plugin, PacketInterceptor, Runnable {

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
        while (true) {
            waitServer(fifo);
        }
    }

    public void waitServer(File fifo) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    fifo)));
            StringBuilder s = new StringBuilder();
            String line = br.readLine();
            if (line == null)
                return;
            String roomDomain = line.trim();

            line = br.readLine();
            while (line != null && !line.trim().equals("end")) {
                System.out.println(line);
                s.append(line);
                s.append('\n');
                line = br.readLine();
            }
            if (line == null)
                return;

            broadCastToClient(roomDomain, s.toString());

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void broadCastToClient(final String roomDomain,
            final String content) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                JID roomID = new JID(roomDomain);
                MessageRouter router = XMPPServer.getInstance()
                        .getMessageRouter();
                MultiUserChatService service = XMPPServer.getInstance()
                        .getMultiUserChatManager()
                        .getMultiUserChatService(roomID);
                if (service == null)
                    return;
                MUCRoom room = service.getChatRoom(roomID.getNode());
                if (room == null)
                    return;
                Collection<MUCRole> c = room.getOccupants();
                JID jid = new JID(roomID + "/Boardcast");
                for (MUCRole role : c) {
                    JID to = role.getUserAddress();
                    Message newMessage = new Message();
                    newMessage.setType(Message.Type.groupchat);
                    newMessage.setBody('\n' + content.toString().trim());
                    newMessage.setTo(to);
                    newMessage.setFrom(jid);
                    router.route(newMessage);
                }
            }
        });
        thread.start();
    }

}
