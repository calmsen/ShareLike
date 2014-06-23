/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wp.servicies.impl;

import com.wp.annotation.OffTLU;
import com.wp.model.PropertyName;
import com.wp.model.composite.PropertySimple;
import com.wp.model.enums.EventWallEnum;
import com.wp.model.enums.TypesEnum;
import com.wp.model.ia.Events;
import com.wp.model.objects.Items;
import com.wp.model.objects.Nodes;
import com.wp.model.objects.Users;
import com.wp.servicies.interfaces.IBlogService;
import com.wp.servicies.interfaces.ICircleService;
import com.wp.servicies.interfaces.ISectionsAndFiltersService;
import com.wp.servicies.interfaces.IShareLikeService;
import com.wp.servicies.interfaces.IShareLikesView;
import com.wp.servicies.interfaces.IWallService;
import com.wp.utils.Is;
import com.wp.utils.mybatis.plugins.Conditions.OPERANDS;
import com.wp.utils.mybatis.plugins.TLU;
import com.wp.web.exceptions.BadRequestException;
import com.wp.web.exceptions.UnauthorizedException;
import com.wp.web.views.ArticleView;
import com.wp.web.views.ImageView;
import com.wp.web.views.ShareLikeView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Данный класс представляет из себя сервис который реализует методы интерфеса  IPosterService
 * @author Paul Shishakov
 * @e-mail paulandweb@gmail.com
 */
@Service
@TransactionConfiguration(transactionManager = "transactionPostgresManager")
@Transactional(rollbackFor = Exception.class)
public class ShareLikeServiceImpl extends AService<IShareLikeService> implements IShareLikeService {
    
    @Autowired
    IWallService _wallService;
    
    @Autowired
    IBlogService _blogService;
    
    @Autowired
    ICircleService _circleService;
    
    Logger _log = Logger.getLogger(ShareLikeServiceImpl.class);
    @Autowired
    ISectionsAndFiltersService _sectionsService;
    
    @Override
    @OffTLU
    public void addShare(Users user, Items item) throws IllegalArgumentException, NullPointerException{
        if (user == null && user.getId() == 0) {
            throw new UnauthorizedException("user");
        }
        Nodes node = null;
        if (item.getNode() == null) {
            item = _io.ls(item.getId());
        }
        node = item.getNode();
        //определим node пользователя        
        Nodes userNode = _gs.getNodeByName(user.getName());
        
        TLU.extend(Items.class).off().names(PropertyName.SHARES_USERS, PropertyName.SHARES_AMOUNT);
        List<Items> objects = _io.lsdeep(item, false, TypesEnum.SHARELIKE.getType(), null);
        Items object = null;
        if (Is.Empty(objects)) {
            object = _io.mk("sharelike", node, item, TypesEnum.SHARELIKE.getType());
        } else {
            object = objects.get(0);
        }
        //проверим поставил ли пользователь "Поделиться"
        List<Long> sharesUsers = null;
        if (object.toMap().containsKey(PropertyName.SHARES_USERS)) {
            sharesUsers = object.getValues(PropertyName.SHARES_USERS, Long.class);
            for(int i = 0; i < sharesUsers.size(); i++) {
                if (sharesUsers.get(i) == user.getId()) {
                    return;
                }
            }
        } else {
            sharesUsers = new ArrayList<Long>();
            List<PropertySimple> props = new ArrayList<PropertySimple>();
            props.add(new PropertySimple(PropertyName.SHARES_USERS, sharesUsers));
            _cs.savePropertiesList(object, props);
        }
        //если пользователь не ставил "Поделиться"
        long sharesAmount = 0l;
        if (object.toMap().containsKey(PropertyName.SHARES_AMOUNT)) {
            sharesAmount = object.getValue(PropertyName.SHARES_AMOUNT, Long.class);
        }
        List<PropertySimple> props = new ArrayList<PropertySimple>();
        props.add(PropertySimple.add(PropertyName.SHARES_USERS, user.getId()));
        props.add(new PropertySimple(PropertyName.SHARES_AMOUNT, ++sharesAmount));
        _cs.savePropertiesList(object, props);
        
        //создадим копию узла 
        Items clone = _io.copy(object, userNode, null, false, true);
        //удалим свойства лайков и поделиться
        props = _cs.allPropertiesSimple(clone);
        if (props.size() > 0) {
            for(int i = 0; i < props.size(); i++) {
                String name = props.get(i).getName();
                if (name.equals(PropertyName.SHARES_USERS) || name.equals(PropertyName.LIKES_USERS) || name.equals(PropertyName.SHARES_AMOUNT) || name.equals(PropertyName.LIKES_AMOUNT)) {
                    //удалим свойство
                    _ut.utilize(userNode, clone, new Events(props.get(i).getId()));
                }
            }
        }
    }

    @Override
    @OffTLU
    public void removeShare(Users user, Items item) throws IllegalArgumentException, NullPointerException{
        if (user == null && user.getId() == 0) {
            throw new UnauthorizedException("user");
        }
        Nodes node = null;
        if (item.getNode() == null) {
            item = _io.ls(item.getId());
        }
        node = item.getNode();
        //определим node пользователя        
        Nodes userNode = _gs.getNodeByName(user.getName());
        TLU.extend(Items.class).off().names(PropertyName.SHARES_USERS, PropertyName.SHARES_AMOUNT);
        List<Items> objects = _io.lsdeep(item, false, TypesEnum.SHARELIKE.getType(), null);
        Items object = null;
        if (Is.Empty(objects)) {
            object = _io.mk("sharelike", node, item, TypesEnum.SHARELIKE.getType());
        } else {
            object = objects.get(0);
        }
        //проверим поставил ли пользователь "Поделиться"
        boolean shareUserExists = false;
        int shareUserIndexForDelete = 0;
        List<Long> sharesUsers = null;
        if (object.toMap().containsKey(PropertyName.SHARES_USERS)) {    
            sharesUsers = object.getValues(PropertyName.SHARES_USERS, Long.class);
            for(int i = 0; i < sharesUsers.size(); i++) {
                if (sharesUsers.get(i) == user.getId()) {
                    shareUserIndexForDelete = i;
                    shareUserExists = true;
                }
            }
        }
        
        //если пользователь ставил "Поделиться"
        long sharesAmount = 1l;
        if (object.toMap().containsKey(PropertyName.SHARES_AMOUNT)) {
            sharesAmount = object.getValue(PropertyName.SHARES_AMOUNT, Long.class);
        }
        if (!shareUserExists) {
            return;
        }
        List<PropertySimple> props = new ArrayList<PropertySimple>();
        props.add(PropertySimple.removeAt(PropertyName.SHARES_USERS, shareUserIndexForDelete));
        props.add(new PropertySimple(PropertyName.SHARES_AMOUNT, --sharesAmount));
        _cs.savePropertiesList(object, props);
        //удалим клонирующий объект
        TLU.filter().where().variable("copy_id").operand(OPERANDS.EQ).value(object.getId());
        List<Items> clones = _io.ls(userNode, object.getType());
        TLU.remove();
        
        _ut.utilizeItemsAndAssociationByParentItem(userNode, clones.get(0));
    }
    
    static class UserData {
        private long user_id;
        private String user_login;
        private String user_full_name;
        private String user_avatar;

        public long getUser_id() {
            return user_id;
        }

        public void setUser_id(long user_id) {
            this.user_id = user_id;
        }

        public String getUser_login() {
            return user_login;
        }

        public void setUser_login(String user_login) {
            this.user_login = user_login;
        }

        public String getUser_full_name() {
            return user_full_name;
        }

        public void setUser_full_name(String user_full_name) {
            this.user_full_name = user_full_name;
        }

        public String getUser_avatar() {
            return user_avatar;
        }

        public void setUser_avatar(String user_avatar) {
            this.user_avatar = user_avatar;
        }            
    }
    
    private UserData getUsersItem(Users user) {
        UserData usersItem = new UserData();
        usersItem.setUser_id(user.getId());
        usersItem.setUser_login(user.getName());
        if (user.getFirst_name() != null && user.getLast_name() != null) {
            usersItem.setUser_full_name(user.getFirst_name()+" "+user.getLast_name());
        } else {
            usersItem.setUser_full_name(user.getName());
        }
        usersItem.setUser_avatar("/"+user.getName()+"/avatar/small_square");
        return usersItem;
    }
    
    @Override
    @OffTLU
    public Map<String, Object> getShareUsers(long objectId, int page) {
        return getShareUsers(objectId, page, 5);
    }
    
    @Override
    @OffTLU
    public Map<String, Object> getShareUsers(long objectId, int page, int sizePage) {
        if (page == 0) {
            page = 1;
        }
        Map<String, Object> data = new HashMap<String, Object>();
        
        TLU.extend(Items.class).off().names(PropertyName.SHARES_USERS);        
        List<Items> objects = _io.lsdeep(new Items(objectId), false, TypesEnum.SHARELIKE.getType(), null);
        
        if (!Is.Empty(objects)) {
            Items object = objects.get(0);
            List<UserData> sharesUsersList = new ArrayList<UserData>();
            List<Long> sharesUsers = object.getValues(PropertyName.SHARES_USERS, Long.class);
            if (sharesUsers != null && sharesUsers.size() > 0) {    
                for(int i = 0; i < sharesUsers.size(); i++) {
                    if (i >= (page - 1) * sizePage && i < page * sizePage) {
                        if (sharesUsers.get(i) == 0) {
                            continue;
                        }
                        Users userObj = _us.getUserById(sharesUsers.get(i), false);
                        sharesUsersList.add(getUsersItem(userObj));
                    }

                }
            } else {
                sharesUsers = new ArrayList();
            }
            boolean usersLinkExists = true;
            //проверим есть ли следующая страница
            if (Math.ceil((double)sharesUsers.size()/sizePage) == page) {
                usersLinkExists = false;
            }
            data.put("users_link_exists", usersLinkExists);
            data.put("users", sharesUsersList);
        } else {
            data.put("users_link_exists", false);
            data.put("users", new ArrayList<UserData>());
        }
            
        return data;
    }
    
    @Override
    @OffTLU
    public void addLike(Users user, Items item) throws IllegalArgumentException, NullPointerException{
        if (user == null && user.getId() == 0) {
            throw new UnauthorizedException("user");
        }
        Nodes node = null;
        if (item.getNode() == null) {
            item = _io.ls(item.getId());
        }
        node = item.getNode();
        
        //проверим поставил ли пользователь "Мне нравится"
        TLU.extend(Items.class).off().names(PropertyName.LIKES_USERS, PropertyName.LIKES_AMOUNT);
        List<Items> objects = _io.lsdeep(item, false, TypesEnum.SHARELIKE.getType(), null);
        Items object = null;
        if (Is.Empty(objects)) {
            object = _io.mk("sharelike", node, item, TypesEnum.SHARELIKE.getType());
        } else {
            object = objects.get(0);
        }
        List<Long> likesUsers = null;
        if (object.toMap().containsKey(PropertyName.LIKES_USERS)) {    
            likesUsers = object.getValues(PropertyName.LIKES_USERS, Long.class);
            for(int i = 0; i < likesUsers.size(); i++) {
                if (likesUsers.get(i) == user.getId()) {
                    return;
                }
            }
        } else {
            likesUsers = new ArrayList<Long>();
            List<PropertySimple> props = new ArrayList<PropertySimple>();
            props.add(new PropertySimple(PropertyName.LIKES_USERS, likesUsers));
            _cs.savePropertiesList(object, props);
        }
        //если пользователь не ставил "Мне нравится"
        long likesAmount = 0l;
        if (object.toMap().containsKey(PropertyName.LIKES_AMOUNT)) {
            likesAmount = object.getValue(PropertyName.LIKES_AMOUNT, Long.class);
        }
        List<PropertySimple> props = new ArrayList<PropertySimple>();
        props.add(PropertySimple.add(PropertyName.LIKES_USERS, user.getId()));
        props.add(new PropertySimple(PropertyName.LIKES_AMOUNT, ++likesAmount));
        _cs.savePropertiesList(object, props);
    }

    @Override
    @OffTLU
    public void removeLike(Users user, Items item) throws IllegalArgumentException, NullPointerException{
        if (user == null && user.getId() == 0) {
            throw new IllegalArgumentException("user");
        }
        Nodes node = null;
        if (item.getNode() == null) {
            item = _io.ls(item.getId());
        }
        node = item.getNode();
        //проверим поставил ли пользователь "Мне нравится"
        TLU.extend(Items.class).off().names(PropertyName.LIKES_USERS, PropertyName.LIKES_AMOUNT);
        List<Items> objects = _io.lsdeep(item, false, TypesEnum.SHARELIKE.getType(), null);
        Items object = null;
        if (Is.Empty(objects)) {
            object = _io.mk("sharelike", node, item, TypesEnum.SHARELIKE.getType());
        } else {
            object = objects.get(0);
        }
        boolean likeUserExists = false;
        int likeUserIndexForDelete = 0;
        List<Long> likesUsers = null;
        if (object.toMap().containsKey(PropertyName.LIKES_USERS)) {    
            likesUsers = object.getValues(PropertyName.LIKES_USERS, Long.class);
            for(int i = 0; i < likesUsers.size(); i++) {
                if (likesUsers.get(i) == user.getId()) {
                    likeUserIndexForDelete = i;
                    likeUserExists = true;
                }
            }
        }
        
        //если пользователь ставил "Поделиться"
        long likesAmount = 1l;
        if (object.toMap().containsKey(PropertyName.LIKES_AMOUNT)) {
            likesAmount = object.getValue(PropertyName.LIKES_AMOUNT, Long.class);
        }
        if (!likeUserExists) {
            return;
        }
        List<PropertySimple> props = new ArrayList<PropertySimple>();
        props.add(PropertySimple.removeAt(PropertyName.LIKES_USERS, likeUserIndexForDelete));
        props.add(new PropertySimple(PropertyName.LIKES_AMOUNT, --likesAmount));
        _cs.savePropertiesList(object, props);
    }

    @Override
    @OffTLU
    public Map<String, Object> getLikeUsers(long objectId, int page) {
        return getLikeUsers(objectId, page, 5);
    }
    
    @Override
    @OffTLU
    public Map<String, Object> getLikeUsers(long objectId, int page, int sizePage) {
        if (page == 0) {
            page = 1;
        }
        Map<String, Object> data = new HashMap<String, Object>();
        //возмем список объектов 
        TLU.extend(Items.class).off().names(PropertyName.LIKES_USERS);    
        List<Items> objects = _io.lsdeep(new Items(objectId), false, TypesEnum.SHARELIKE.getType(), null);
        
        if (!Is.Empty(objects)) {
            Items object = objects.get(0);
            List<UserData> likesUsersList = new ArrayList<UserData>();
            List<Long> likesUsers = object.getValues(PropertyName.LIKES_USERS, Long.class);
            if (likesUsers != null && likesUsers.size() > 0) {    
                for(int i = 0; i < likesUsers.size(); i++) {
                    if (i >= (page - 1) * sizePage && i < page * sizePage) {
                        if (likesUsers.get(i) == 0) {
                            continue;
                        }
                        Users userObj = _us.getUserById(likesUsers.get(i), false);
                        likesUsersList.add(getUsersItem(userObj));
                    }
                }
            } else {
                likesUsers = new ArrayList();
            }
            boolean usersLinkExists = true;
            //проверим есть ли следующая страница
            if (Math.ceil((double)likesUsers.size()/sizePage) == page) {
                usersLinkExists = false;
            }
            data.put("users_link_exists", usersLinkExists);
            data.put("users", likesUsersList);
        } else {
            data.put("users_link_exists", false);
            data.put("users", new ArrayList<UserData>());
        }
            
        return data;
    }    
    @Override
    @OffTLU
    public ShareLikeView getShareLike(IShareLikesView objectView) {
        
        ShareLikeView slv = new ShareLikeView();
        // Выберем объект SHARELIKE
        TLU.extend(Items.class).off().names(ShareLikeView.fields());
        List<Items> objects = _io.lsdeep(new Items(objectView.getId()), false, TypesEnum.SHARELIKE.getType(), null);
        if (Is.Empty(objects)) {
            slv.setObject_id(objectView.getId());
            slv.setNode_name(objectView.getNode_name());
            slv.setSign(objectView.getSign());
            slv.setShares_users(new ArrayList<Long>());
            slv.setLikes_users(new ArrayList<Long>());
            return slv;
        }
        Items object = objects.get(0);
        slv.setObject_id(object.getParent_id());
        slv.setNode_name(object.getNode().getName());
        slv.setSign(object.getNode().getOwner().getId());
        slv.setShares_amount(object.getValue(PropertyName.SHARES_AMOUNT, Long.class, 0l));
        slv.setLikes_amount(object.getValue(PropertyName.LIKES_AMOUNT, Long.class, 0l));
        slv.setShares_users(new ArrayList<Long>());
        slv.setShares_users(object.getValues(PropertyName.SHARES_USERS, Long.class, new ArrayList<Long>()));
        slv.setLikes_users(new ArrayList<Long>());
        slv.setLikes_users(object.getValues(PropertyName.LIKES_USERS, Long.class, new ArrayList<Long>()));
        return slv;
    }
    
    @Override
    public void setShareLike(IShareLikesView objectView, Users user) {        
        setShareLike(objectView, user, 0, 0, 0, 0);
    }
    
    
    public void setShareLike(IShareLikesView objectView, Users user, int share_none, int like_none, int share_disabled, int like_disabled) {        
        ShareLikeView slv = This().getShareLike(objectView);
        slv.setShare_none(share_none);
        slv.setShare_disabled(share_disabled);
        // скроим лайки если никто еще данный пост не лайкал :)
        if (like_none > 0 && slv.getLikes_amount() == 0) {
            slv.setLike_none(like_none);
        }
        slv.setLike_disabled(like_disabled);
        
        if (!Is.Empty(user)) {
            slv.setUser_id(user.getId());
            for(long us: slv.getShares_users()) {
                if (us == user.getId()) {
                    slv.setShare_id(us);
                }
            }
            for(long us: slv.getLikes_users()) {
                if (us == user.getId()) {
                    slv.setLike_id(us);
                }
            }
        }
        objectView.setSlv(slv);
    }
    @Override
    public <T extends IShareLikesView> void setShareLike(List<T> objectsView, Users user) {
        if (!Is.Empty(objectsView)) {
            for (int i = 0, l = objectsView.size(); i < l; i++) {
                setShareLike(objectsView.get(i), user);
            }
        }  
    }
    @Override
    public <T extends IShareLikesView> void setShareLike(List<T> objectsView, Users user, int share_none, int like_none, int share_disabled, int like_disabled) {
        if (!Is.Empty(objectsView)) {
            for (int i = 0, l = objectsView.size(); i < l; i++) {
                setShareLike(objectsView.get(i), user, share_none, like_none, share_disabled, like_disabled);
            }
        }  
    }
}
