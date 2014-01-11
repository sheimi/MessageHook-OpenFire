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
    private static Map<String, MessageHook> mGeneralHooks = new HashMap<String, MessageHook>();
    private static Map<String, Map<String, MessageHook>> mHooksForRoom = new HashMap<String, Map<String, MessageHook>>();

    private MessageHookManager() {
        addGeneralHook(new HelpMessageHook());
        addGeneralHook(new RemoveHookMessageHook());
        addGeneralHook(new AddHookMessageHook());
        addGeneralHook(new MessageHook("execute", "execute an command", "%s", false));
        addGeneralHook(new MessageHook("argument test", "for argument test",
                "%s%s%s"));
    }

    public static synchronized MessageHookManager getInstance() {
        if (mHook == null) {
            mHook = new MessageHookManager();
        }
        return mHook;
    }

    public List<MessageHook> getGeneralHooks() {
        List<MessageHook> hooks = new ArrayList<MessageHook>(
                mGeneralHooks.values());
        Collections.sort(hooks);
        return hooks;
    }

    private Map<String, MessageHook> getHooksMapForRoom(String roomDomain) {
        Map<String, MessageHook> roomHooks = mHooksForRoom.get(roomDomain);
        if (roomHooks == null) {
            roomHooks = new HashMap<String, MessageHook>();
            mHooksForRoom.put(roomDomain, roomHooks);
        }
        return roomHooks;
    }

    public List<MessageHook> getHooksForRoom(String roomDomain) {
        Map<String, MessageHook> roomHooks = getHooksMapForRoom(roomDomain);
        List<MessageHook> hooks = new ArrayList<MessageHook>(roomHooks.values());
        Collections.sort(hooks);
        return hooks;
    }

    public void addHookForRoom(String roomDomain, MessageHook hook) {
        Map<String, MessageHook> roomHooks = getHooksMapForRoom(roomDomain);
        roomHooks.put(hook.getHookTrigger(), hook);
    }

    public void addGeneralHook(MessageHook hook) {
        mGeneralHooks.put(hook.getHookTrigger(), hook);
    }

    public void removeHookForRoom(String roomDomain, String hookTrigger) {
        Map<String, MessageHook> roomHooks = getHooksMapForRoom(roomDomain);
        roomHooks.remove(hookTrigger);
    }

    public MessageHook getHook(String roomDomain, String hookTrigger) {
        Map<String, MessageHook> roomHooks = getHooksMapForRoom(roomDomain);
        MessageHook hook = roomHooks.get(hookTrigger);
        if (hook == null) {
            hook = mGeneralHooks.get(hookTrigger);
        }
        return hook;
    }

    public void processMessage(final Message message) {
        String body = message.getBody();
        if (!body.startsWith(prefix))
            return;
        String cmd = body.substring(prefix.length()).trim();
        String[] cmds = cmd.split(":");
        String roomDomain = message.getTo().getNode() + "@"
                + message.getTo().getDomain();
        MessageHook hook = getHook(roomDomain, cmds[0]);
        if (hook == null) {
            MessageHookPlugin.broadCastToClient(roomDomain,
                    "No such command, you can use help for details");
            return;
        }
        if (cmds.length > 1) {
            String[] params = new String[cmds.length];
            for (int i = 0; i < cmds.length - 1; i++) {
                params[i] = cmds[i + 1].trim();
            }
            params[params.length - 1] = roomDomain;
            hook.execute(params);
        } else {
            hook.execute(new String[] { roomDomain });
        }
    }
}
