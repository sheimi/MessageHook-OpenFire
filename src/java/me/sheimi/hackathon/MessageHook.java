package me.sheimi.hackathon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

import org.jivesoftware.openfire.MessageRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatManager;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

public class MessageHook {

    private static MessageHook mHook;
    private static String prefix = ":!";
    private MultiUserChatManager mMultiUserChatManager;
    private MessageRouter mRouter;

    private MessageHook() {
        mMultiUserChatManager = XMPPServer.getInstance()
                .getMultiUserChatManager();
        mRouter = XMPPServer.getInstance().getMessageRouter();
    }

    public static synchronized MessageHook getInstance() {
        if (mHook == null) {
            mHook = new MessageHook();
        }
        return mHook;
    }

    public void processMessage(final Message message) {
        String body = message.getBody();
        final JID roomID = new JID("room1@conference.sheimi.vm");
        if (!body.startsWith(prefix))
            return;
        final String cmd = body.substring(prefix.length()).trim();
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println("Command " + cmd + " will process ... ");
                StringBuilder s = new StringBuilder();
                Process p;
                try {
                    p = Runtime.getRuntime().exec(cmd);
                    BufferedReader input = new BufferedReader(
                            new InputStreamReader(p.getInputStream()));
                    BufferedReader error = new BufferedReader(
                            new InputStreamReader(p.getErrorStream()));
                    String line = null;
                    s.append("STDOUT:\n");
                    while ((line = input.readLine()) != null) {
                        s.append(line);
                        s.append('\n');
                    }
                    s.append("STDERR:\n");
                    while ((line = error.readLine()) != null) {
                        s.append(line);
                        s.append('\n');
                    }
                    MultiUserChatService service = mMultiUserChatManager
                            .getMultiUserChatService(roomID);
                    MUCRoom room = service.getChatRoom(roomID.getNode());
                    Collection<MUCRole> c = room.getOccupants();
                    JID jid = new JID("room1@conference.sheimi.vm/Boardcast");
                    for (MUCRole role : c) {
                        JID to = role.getUserAddress();
                        Message newMessage = message.createCopy();
                        newMessage.setBody(s.toString());
                        newMessage.setTo(to);
                        newMessage.setFrom(jid);
                        mRouter.route(newMessage);
                        System.out.println(to);
                    }
                    input.close();
                    error.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
