package me.sheimi.hackathon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.xmpp.packet.Message;

public class MessageHook {

    private static MessageHook mHook;
    private static String prefix = ":!";

    private MessageHook() {
    }

    public static synchronized MessageHook getInstance() {
        if (mHook == null) {
            mHook = new MessageHook();
        }
        return mHook;
    }

    public void processMessage(final Message message) {
        String body = message.getBody();
        if (!body.startsWith(prefix))
            return;
        final String cmd = body.substring(prefix.length()).trim();
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println("Command " + cmd + " will process ... ");
                StringBuffer sb = new StringBuffer();
                Process p;
                try {
                    p = Runtime.getRuntime().exec(cmd);
                    BufferedReader input = new BufferedReader(
                            new InputStreamReader(p.getInputStream()));
                    BufferedReader error = new BufferedReader(
                            new InputStreamReader(p.getErrorStream()));
                    String line = null;
                    sb.append("STDOUT:\n");
                    while ((line = input.readLine()) != null) {
                        sb.append(line);
                        sb.append('\n');
                    }
                    sb.append("STDERR:\n");
                    while ((line = error.readLine()) != null) {
                        sb.append(line);
                        sb.append('\n');
                    }
                    input.close();
                    error.close();
                    MessageHookPlugin.broadCastToClient(sb.toString());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
