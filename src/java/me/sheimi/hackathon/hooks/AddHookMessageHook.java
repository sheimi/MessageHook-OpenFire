package me.sheimi.hackathon.hooks;

import me.sheimi.hackathon.MessageHook;
import me.sheimi.hackathon.MessageHookManager;
import me.sheimi.hackathon.MessageHookPlugin;

public class AddHookMessageHook extends MessageHook {

    public AddHookMessageHook() {
        super("addHook", "add a new hook", "");
    }

    @Override
    public void execute(final String[] params) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                final String roomDomain = params[params.length - 1];
                if (params.length < 4) {
                    MessageHookPlugin.broadCastToClient(roomDomain,
                            "Missing Formet Argument");
                    return;
                }
                MessageHook hook = new MessageHook(params[0], params[1],
                        params[2]);
                MessageHookManager.getInstance().addHookForRoom(roomDomain,
                        hook);
                MessageHookPlugin.broadCastToClient(roomDomain,
                        "New hook has added");
            }

        });
        thread.run();
    }

}
