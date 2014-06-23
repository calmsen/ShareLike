/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wp.web.views;

import com.wp.model.PropertyName;
import com.wp.model.composite.views.IView;
import com.wp.model.objects.Items;
import com.wp.model.objects.Users;
import com.wp.utils.WPContext;
import com.wp.web.controllers.ShareLikeController;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Данный класс представляет из себя View для псевдо объекта Location.
 * @author Ruslan Rakhmankulov
 * @e-mail rusa86@mail.ru
 */

public class ShareLikeView implements IView<Items> {
    private long object_id;
    private String node_name;
    private long user_id;
    private long share_id;
    private long shares_amount;
    private long like_id;
    private long likes_amount;
    private long sign;
    private List<Long> shares_users;
    private List<Long> likes_users;
    private int like_disabled; // запретить кликать лайки (даже если пользователь авторизован)
    private int like_none; // не показывать лайки
    private int share_disabled;// запретить кликать поделиться (даже если пользователь авторизован)
    private int share_none; // не показывать поделиться

    public long getObject_id() {
        return object_id;
    }

    public void setObject_id(long object_id) {
        this.object_id = object_id;
    }

    public String getNode_name() {
        return node_name;
    }

    public void setNode_name(String node_name) {
        this.node_name = node_name;
    }
    
    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public long getShare_id() {
        return share_id;
    }

    public void setShare_id(long share_id) {
        this.share_id = share_id;
    }

    public long getShares_amount() {
        return shares_amount;
    }

    public void setShares_amount(long shares_amount) {
        this.shares_amount = shares_amount;
    }

    public long getLike_id() {
        return like_id;
    }

    public void setLike_id(long like_id) {
        this.like_id = like_id;
    }

    public long getLikes_amount() {
        return likes_amount;
    }

    public void setLikes_amount(long likes_amount) {
        this.likes_amount = likes_amount;
    }

    public long getSign() {
        return sign;
    }

    public void setSign(long sign) {
        this.sign = sign;
    }

    public List<Long> getShares_users() {
        return shares_users;
    }

    public void setShares_users(List<Long> shares_users) {
        this.shares_users = shares_users;
    }

    public List<Long> getLikes_users() {
        return likes_users;
    }

    public void setLikes_users(List<Long> likes_users) {
        this.likes_users = likes_users;
    }

    public int getLike_disabled() {
        return like_disabled;
    }

    public void setLike_disabled(int like_disabled) {
        this.like_disabled = like_disabled;
    }

    public int getLike_none() {
        return like_none;
    }

    public void setLike_none(int like_none) {
        this.like_none = like_none;
    }

    public int getShare_disabled() {
        return share_disabled;
    }

    public void setShare_disabled(int share_disabled) {
        this.share_disabled = share_disabled;
    }

    public int getShare_none() {
        return share_none;
    }

    public void setShare_none(int share_none) {
        this.share_none = share_none;
    }
    
    public static String[] fields() {
        return new String[]{PropertyName.SHARES_USERS, PropertyName.SHARES_AMOUNT, PropertyName.LIKES_USERS, PropertyName.LIKES_AMOUNT};
    }
      
    @Override
    public void apply(Items object){
    }
}
