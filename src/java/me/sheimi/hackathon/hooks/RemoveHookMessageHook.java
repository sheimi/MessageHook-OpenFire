package me.sheimi.hackathon.hooks;

import me.sheimi.hackathon.MessageHook;
import me.sheimi.hackathon.MessageHookManager;
import me.sheimi.hackathon.MessageHookPlugin;

public class RemoveHookMessageHook extends MessageHook {

    public RemoveHookMessageHook() {
        super("removeHook", "remove a hook", "");
    }

    @Override
    public void execute(final String[] params) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                if (params.length < 1) {
                    MessageHookPlugin.broadCastToClient("Missing Formet Argument");
                    return;
                }
                MessageHookManager.getInstance().removeHook(params[0]);
                MessageHookPlugin.broadCastToClient("hook has removed");
            }

        });
        thread.run();
    }

}
