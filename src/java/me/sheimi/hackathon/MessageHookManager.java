package me.sheimi.hackathon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.sheimi.hackathon.hooks.AddHookMessageHook;
import me.sheimi.hackathon.hooks.HelpMessageHook;
import me.sheimi.hackathon.hooks.RemoveHookMessageHook;

import org.xmpp.packet.Message;

public class MessageHookManager {

    private static MessageHookManager mHook;
    private static String prefix = ":!";
    private static Map<String, MessageHook> mHooks = new HashMap<String, MessageHook>();

    private MessageHookManager() {
        addHook(new HelpMessageHook());
        addHook(new RemoveHookMessageHook());
        addHook(new AddHookMessageHook());
        addHook(new MessageHook("execute", "execute an command", "%s"));
        addHook(new MessageHook("argument test", "for argument test", "%s%s%s"));
    }

    public static synchronized MessageHookManager getInstance() {
        if (mHook == null) {
            mHook = new MessageHookManager();
        }
        return mHook;
    }

    public List<MessageHook> getHooks() {
        List<MessageHook> hooks = new ArrayList<MessageHook>(mHooks.values());
        Collections.sort(hooks);
        return hooks;
    }

    public void addHook(MessageHook hook) {
        mHooks.put(hook.getHookTrigger(), hook);
    }
    
    public void removeHook(String hookTrigger) {
        mHooks.remove(hookTrigger);
    }

    public void processMessage(final Message message) {
        String body = message.getBody();
        if (!body.startsWith(prefix))
            return;
        String cmd = body.substring(prefix.length()).trim();
        String[] cmds = cmd.split(":");
        MessageHook hook = mHooks.get(cmds[0]);
        if (hook == null) {
            MessageHookPlugin.broadCastToClient("No such command, you can use help for details");
            return;
        }
        if (cmds.length > 1) {
            String[] params = new String[cmds.length - 1];
            for (int i = 0; i < cmds.length - 1; i++) {
                params[i] = cmds[i + 1].trim();
            }
            hook.execute(params);
        } else {
            hook.execute();
        }
    }
}
