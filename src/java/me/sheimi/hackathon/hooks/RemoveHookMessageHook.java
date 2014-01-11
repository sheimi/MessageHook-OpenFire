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
                final String roomDomain = params[params.length - 1];
                if (params.length < 2) {
                    MessageHookPlugin.broadCastToClient(roomDomain,
                            "Missing Formet Argument");
                    return;
                }
                MessageHookManager.getInstance().removeHookForRoom(roomDomain,
                        params[0]);
                MessageHookPlugin.broadCastToClient(roomDomain,
                        "hook has removed");
            }

        });
        thread.run();
    }

}
