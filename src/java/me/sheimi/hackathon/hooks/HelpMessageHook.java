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
    public void execute(final String[] params) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                String roomDomain = params[params.length - 1];
                List<MessageHook> hooks = MessageHookManager.getInstance()
                        .getGeneralHooks();
                List<MessageHook> hooksForRoom = MessageHookManager
                        .getInstance().getHooksForRoom(roomDomain);
                StringBuffer sb = new StringBuffer();
                sb.append("\nGeneral Hooks: \n");
                for (MessageHook hook : hooks) {
                    sb.append(hook.getHookTrigger());
                    sb.append("  --  ");
                    sb.append(hook.getDescription());
                    sb.append("\n");
                }
                sb.append("\nHooks for this chatroom:");
                for (MessageHook hook : hooksForRoom) {
                    sb.append(hook.getHookTrigger());
                    sb.append("  --  ");
                    sb.append(hook.getDescription());
                    sb.append("\n");
                }
                MessageHookPlugin.broadCastToClient(roomDomain, sb.toString());
            }

        });
        thread.run();
    }

}
