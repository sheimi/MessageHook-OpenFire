package me.sheimi.hackathon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.MissingFormatArgumentException;

public class MessageHook implements Comparable<MessageHook> {

    private String hookTrigger;
    private String shortDescription;
    private String description;
    private String command;
    private boolean includeDomain;

    public MessageHook(String hookTrigger, String shortDescription,
            String command) {
        this(hookTrigger, shortDescription, command, true);
    }

    public MessageHook(String hookTrigger, String shortDescription,
            String command, boolean includeParameter) {
        this.hookTrigger = hookTrigger;
        this.shortDescription = shortDescription;
        this.description = shortDescription;
        this.command = command;
        this.includeDomain = includeParameter;
    }

    public String getHookTrigger() {
        return hookTrigger;
    }

    public boolean isIncludeDomain() {
        return includeDomain;
    }

    public void setIncludeDomain(boolean includeDomain) {
        this.includeDomain = includeDomain;
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
        final String roomDomain = params[params.length - 1];
        String cmd = null;
        try {
            String[] newParams = Arrays.copyOfRange(params, 0,
                    params.length - 1);
            cmd = String.format(getCommand(), (Object[]) newParams);
            if (includeDomain) {
                cmd = cmd + " " + roomDomain;
            }
        } catch (MissingFormatArgumentException e) {
            MessageHookPlugin.broadCastToClient(roomDomain,
                    "Missing Formet Argument");
            return;
        }
        final String cmdFinal = cmd;
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println("Command [" + cmdFinal
                        + "] will be invode ...");
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
                    MessageHookPlugin.broadCastToClient(roomDomain,
                            sb.toString());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public int compareTo(MessageHook h) {
        // TODO Auto-generated method stub
        return getHookTrigger().compareTo(h.getHookTrigger());
    }

}
