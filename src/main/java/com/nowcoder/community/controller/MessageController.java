package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.mapper.UserMapper;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @author yl
 * @date 2025-04-24 13:53
 */
@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    // 获取私信
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        System.out.println("user = " + user);

        // 设置分页查询
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.getConversationCount(user.getId()));

        // 所有私信
        List<Message> messageList = messageService.getConversations(user.getId(), page.getOffset(), page.getLimit());
        System.out.println("messageList = " + messageList);
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (messageList != null) {
            for (Message message : messageList) {
                Map<String, Object> map = new HashMap<>();
                // 私信内容
                map.put("conversation", message);
                // 对方信息
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                User target = userMapper.getById(targetId);
                System.out.println("target = " + target);
                map.put("target", target);

                // 未读数量
                int unreadCount = messageService.getLetterUnreadCount(user.getId(), message.getConversationId());
                map.put("unreadCount", unreadCount);

                // 私信条数
                int letterCount = messageService.getLetterCount(message.getConversationId());
                map.put("letterCount", letterCount);

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        // 所有未读私信
        int letterUnreadCount = messageService.getLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        // 所有未读系统通知
        int noticeUnreadCount = messageService.getNoticeUnreadCount(user.getId(), 0, null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";
    }

    // 发私信
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        // 发送方私信
        User user = hostHolder.getUser();

        // 接收方信息
        User target = userMapper.getByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "接收方用户不存在");
        }

        // 创建私信
        Message message = new Message();
        message.setFromId(user.getId());
        message.setToId(target.getId());
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setStatus(0);
        if (target.getId() < user.getId()) {
            message.setConversationId(target.getId() + "_" + user.getId());
        } else {
            message.setConversationId(user.getId() + "_" + target.getId());
        }
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page) {
        page.setPath("/letter/detail" + conversationId);
        page.setRows(messageService.getLetterCount(conversationId));
        System.out.println("rows" + page.getRows());

        // 该对话中的所有消息
        List<Message> messageList = messageService.getLetterDetail(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();

        if (messageList != null) {
            for (Message message : messageList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);

                User fromUser = userMapper.getById(message.getFromId());
                map.put("fromUser", fromUser);
                letters.add(map);
            }
        }
        // 私信
        model.addAttribute("letters", letters);
        // 发送者
        model.addAttribute("target", getLetterTarget(conversationId));

        // 设置已读
        List<Integer> ids = getLetterIds(messageList);
        System.out.println("ids = " + ids);
        if (!ids.isEmpty()) {
            messageService.readMessages(ids);
        }

        return "/site/letter-detail";
    }

    // 消息列表
    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();

        // 评论类通知  最新的通知
        Message message = messageService.getLastedNotice(user.getId(), TOPIC_COMMENT);
        if (message != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", message);
            String content = HtmlUtils.htmlEscape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            map.put("user", userService.findUserById((Integer) data.get("userId")));
            // 通知的信息
            map.put("entityType", data.get("entityType"));
            map.put("entityId", data.get("entityId"));
            map.put("postId", data.get("postId"));

            // 通知的数量
            int noticeCount = messageService.getNoticeCount(user.getId(), TOPIC_COMMENT);
            map.put("count", noticeCount);

            int unread = messageService.getNoticeUnreadCount(user.getId(), 0, TOPIC_COMMENT);
            map.put("unread", unread);

            model.addAttribute("commentNotice", map);
        }

        // 点赞类通知  最新的通知
        message = messageService.getLastedNotice(user.getId(), TOPIC_LIKE);
        if (message != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", message);



            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            map.put("user", userService.findUserById((Integer) data.get("userId")));
            // 通知的信息
            map.put("entityType", data.get("entityType"));
            map.put("entityId", data.get("entityId"));
            map.put("postId", data.get("postId"));

            // 通知的数量
            int noticeCount = messageService.getNoticeCount(user.getId(), TOPIC_LIKE);
            map.put("count", noticeCount);
            // 未读的数量
            int unread = messageService.getNoticeUnreadCount(user.getId(), 0, TOPIC_LIKE);
            map.put("unread", unread);

            model.addAttribute("likeNotice", map);
        }

        // 关注类通知  最新的通知
        message = messageService.getLastedNotice(user.getId(), TOPIC_FOLLOW);
        if (message != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", message);
            String content = HtmlUtils.htmlEscape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            map.put("user", userService.findUserById((Integer) data.get("userId")));
            // 通知的信息
            map.put("entityType", data.get("entityType"));
            map.put("entityId", data.get("entityId"));
            map.put("postId", data.get("postId"));

            // 通知的数量
            int noticeCount = messageService.getNoticeCount(user.getId(), TOPIC_FOLLOW);
            map.put("count", noticeCount);
            // 未读的数量
            int unread = messageService.getNoticeUnreadCount(user.getId(), 0, TOPIC_FOLLOW);
            map.put("unread", unread);

            model.addAttribute("followNotice", map);
        }

        int noticeUnreadCount = messageService.getNoticeUnreadCount(user.getId(), 0, null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        int letterUnreadCount = messageService.getLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/notice";
    }

    // 通知信息的详情
    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model){
        User user = hostHolder.getUser();

        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.getNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.getNoticeDetail(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 内容
                System.out.println("notice.content = " + notice.getContent());
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data =  JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));

                map.put("fromUser", userService.findUserById(notice.getToId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);

        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessages(ids);
        }

        return "/site/notice-detail";
    }

    private List<Integer> getLetterIds(List<Message> messageList) {
        List<Integer> ids = new ArrayList<>();

        if (messageList != null) {
            for (Message message : messageList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if (id0 == hostHolder.getUser().getId()) {
            return userMapper.getById(id1);
        } else {
            return userMapper.getById(id0);
        }
    }


}
