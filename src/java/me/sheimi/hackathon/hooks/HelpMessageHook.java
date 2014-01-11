package me.sheimi.hackathon.hooks;

import java.util.List;

import me.sheimi.hackathon.MessageHook;
import me.sheimi.hackathon.MessageHookManager;
import me.sheimi.hackathon.MessageHookPlugin;

public class HelpMessageHook extends MessageHook {

    public HelpMessageHook() {
        super("help", "display hooks info", "");
    }

    @Override
    public void execute(String[] params) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                List<MessageHook> hooks = MessageHookManager.getInstance()
                        .getHooks();
                StringBuffer sb = new StringBuffer();
                sb.append('\n');
                for (MessageHook hook : hooks) {
                    sb.append(hook.getHookTrigger());
                    sb.append("  --  ");
                    sb.append(hook.getDescription());
                    sb.append("\n");
                }
                MessageHookPlugin.broadCastToClient(sb.toString());
            }

        });
        thread.run();
    }

}
