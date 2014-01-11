<%@ page import="java.util.*,
                 org.jivesoftware.openfire.XMPPServer,
                 org.jivesoftware.openfire.user.*,
                 org.jivesoftware.util.*,
                 me.sheimi.hackathon.*,
                 org.jivesoftware.openfire.muc.*"
%>
<%@ page import="java.util.regex.Pattern"%>

<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>

<html>
<head>
    <title>Content Filter</title>
    <meta name="pageID" content="message-hook-edit-form"/>
</head>
<body>

<%
    String serviceDomain  = request.getParameter("serviceDomain");
    MultiUserChatManager multiUserChatManager = XMPPServer.getInstance()
                .getMultiUserChatManager();
    if (serviceDomain == null) {
        List<MultiUserChatService> services = multiUserChatManager.getMultiUserChatServices();
%>
<ul>
        <% for (MultiUserChatService service : services) { %>
            <li><a href="./?serviceDomain=<%= service.getServiceDomain() %>"><%= service.getName()%></a></li>       
        <% } %>
</ul>
<%
    } else if(request.getParameter("chatroom") == null){
        String subdomain = serviceDomain.replace("."+ XMPPServer.getInstance().getServerInfo().getXMPPDomain(), "");
        MultiUserChatService service = multiUserChatManager.getMultiUserChatService(subdomain);
        List<MUCRoom> chatrooms = service.getChatRooms();
%>
<ul>
        <% for (MUCRoom room : chatrooms) { %>
            <li><a href="./?serviceDomain=<%= service.getServiceDomain() %>&chatroom=<%=room.getName()%>"><%= room.getName()%></a></li>       
        <% } %>
</ul>
<%  
    } else {
        String chatroom = request.getParameter("chatroom");
        String roomDomain = chatroom + "@" + serviceDomain;

        MessageHookManager hookManager = MessageHookManager.getInstance();
        boolean success = false;
        boolean deleteItem = request.getParameter("delete") != null;
        if (deleteItem) {
            String deleteHookTrigger = request.getParameter("triggerToDelete");
            hookManager.removeHookForRoom(roomDomain, deleteHookTrigger);
        }
        if (request.getMethod().equals("POST")) {
            String hookTrigger = ParamUtils.getParameter(request, "hookTrigger");
            String hookCommand = ParamUtils.getParameter(request, "hookCommand");
            String hookDes = ParamUtils.getParameter(request, "hookDescripton");
            success = hookTrigger != null && !hookTrigger.isEmpty()
                    && hookCommand != null && !hookCommand.isEmpty()
                    && hookDes != null && !hookDes.isEmpty();
            if (success) {
                hookManager.addHookForRoom(roomDomain, new MessageHook(hookTrigger, hookDes, hookCommand));
            }
        }
        List<MessageHook> hooks = hookManager.getGeneralHooks();
        List<MessageHook> hooksForRoom = hookManager.getHooksForRoom(roomDomain);
%>

<p>
Use the form below to edit hookManager settings.<br>
</p>

<table>
    <tbody>
        <thead>
            <th>Hook Trigger</th>
            <th>Hook Command</th>
            <th>Hook Description</th>
            <th>Operations</th>
        </thead>
        <% for (MessageHook hook : hooks) { %>
        <tr>
            <td><%= hook.getHookTrigger() %></td>
            <td><%= hook.getCommand() %></td>
            <td><%= hook.getDescription() %></td>
            <td></td>
        </tr>
        <% }
        for (MessageHook hook : hooksForRoom) { %>
        <tr>
            <td><%= hook.getHookTrigger() %></td>
            <td><%= hook.getCommand() %></td>
            <td><%= hook.getDescription() %></td>
            <td><a href="./?serviceDomain=<%= serviceDomain %>&chatroom=<%=chatroom%>&delete=true&triggerToDelete=<%=hook.getHookTrigger()%>" >delete</a></td>
        </tr>
        <% } %>
    </tbody>
</table>
<hr>
<h3>Crate New Hook</h3>
<form action="./?serviceDomain=<%= serviceDomain %>&chatroom=<%=chatroom%>" method="post">
    <label>Hook Trigger</label>
    <input type="text" name="hookTrigger"/><br>
    <label>Hook Command</label>
    <input type="text" name="hookCommand"/><br>
    <label>Hook Description</label>
    <input type="text" name="hookDescripton"/><br>
    <input type="submit" value="submit"/>
</form>
<% } %>
</body>
</html>