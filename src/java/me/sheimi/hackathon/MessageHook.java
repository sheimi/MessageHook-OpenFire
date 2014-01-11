package me.sheimi.hackathon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.MissingFormatArgumentException;

public class MessageHook implements Comparable<MessageHook> {

    private String hookTrigger;
    private String shortDescription;
    private String description;
    private String command;

    public MessageHook(String hookTrigger, String shortDescription,
            String command) {
        this.hookTrigger = hookTrigger;
        this.shortDescription = shortDescription;
        this.description = shortDescription;
        this.command = command;
    }

    public String getHookTrigger() {
        return hookTrigger;
    }

    public void setHookTrigger(String hookTrigger) {
        this.hookTrigger = hookTrigger;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void execute(String[] params) {
        String cmd = null;
        try {
            cmd = String.format(getCommand(), (Object[]) params);
        } catch (MissingFormatArgumentException e) {
            MessageHookPlugin.broadCastToClient("Missing Formet Argument");
            return;
        }
        final String cmdFinal = cmd;
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                StringBuffer sb = new StringBuffer();
                Process p;
                try {
                    p = Runtime.getRuntime().exec(cmdFinal);
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

    public void execute() {
        execute(new String[0]);
    }

    @Override
    public int compareTo(MessageHook h) {
        // TODO Auto-generated method stub
        return getHookTrigger().compareTo(h.getHookTrigger());
    }

}
