package com.meizu.tool.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import javax.swing.Icon;

/**
 *<p>
 *
 *</p>
 *
 * @author liuyang
 * @date 2020/1/24 周五 17:20:00
 * @since 1.0.0
 */

public class NotificationBuilder {

    private String myGroupId;
    private Icon myIcon;
    private NotificationType myType;
    private String myTitle;
    private String mySubtitle;
    private String myContent;
    private NotificationListener myListener;

    NotificationBuilder() {
    }

    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    public NotificationBuilder myGroupId(String myGroupId) {
        this.myGroupId = myGroupId;
        return this;
    }

    public NotificationBuilder myIcon(Icon myIcon) {
        this.myIcon = myIcon;
        return this;
    }

    public NotificationBuilder myType(NotificationType myType) {
        this.myType = myType;
        return this;
    }

    public NotificationBuilder myTitle(String myTitle) {
        this.myTitle = myTitle;
        return this;
    }

    public NotificationBuilder mySubtitle(String mySubtitle) {
        this.mySubtitle = mySubtitle;
        return this;
    }

    public NotificationBuilder myContent(String myContent) {
        this.myContent = myContent;
        return this;
    }

    public NotificationBuilder myListener(NotificationListener myListener) {
        this.myListener = myListener;
        return this;
    }

    public Notification build() {
        return new Notification(this.myGroupId, this.myIcon, this.myTitle, this.mySubtitle,
            this.myContent, this.myType, this.myListener);
    }


    public String getMyGroupId() {
        return myGroupId;
    }


    public void setMyGroupId(String myGroupId) {
        this.myGroupId = myGroupId;
    }


    public Icon getMyIcon() {
        return myIcon;
    }


    public void setMyIcon(Icon myIcon) {
        this.myIcon = myIcon;
    }


    public NotificationType getMyType() {
        return myType;
    }


    public void setMyType(NotificationType myType) {
        this.myType = myType;
    }


    public String getMyTitle() {
        return myTitle;
    }


    public void setMyTitle(String myTitle) {
        this.myTitle = myTitle;
    }


    public String getMySubtitle() {
        return mySubtitle;
    }


    public void setMySubtitle(String mySubtitle) {
        this.mySubtitle = mySubtitle;
    }


    public String getMyContent() {
        return myContent;
    }


    public void setMyContent(String myContent) {
        this.myContent = myContent;
    }


    public NotificationListener getMyListener() {
        return myListener;
    }


    public void setMyListener(NotificationListener myListener) {
        this.myListener = myListener;
    }
}
