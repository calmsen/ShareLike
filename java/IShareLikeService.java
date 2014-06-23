/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wp.servicies.interfaces;

import com.wp.model.objects.Items;
import com.wp.model.objects.Users;
import com.wp.web.views.ShareLikeView;
import java.util.*;

/**
 * Данный интерфей содержит методы для работы с виджетом "Поделиться и лайки"
 * @author Ruslan Rakhmankulov
 * @e-mail rusa86@mail.ru
 */
public interface IShareLikeService {
    
    /**
     * @param user пользователь, который делиться
     * @param item объект которым деляться
     * добавляем "Поделиться"
     */
     void addShare(Users user, Items item) throws IllegalArgumentException, NullPointerException;
    
     /**
     * удаляем "Поделиться"
     * @param user
     * @param item
     */
     void removeShare(Users user, Items item) throws IllegalArgumentException, NullPointerException;
     
     /**
     * берем пользователей который ставили "Поделиться"
     * @param objectId
     * @param page
     * @return модель данных
     */
     Map<String, Object> getShareUsers(long objectId, int page);
     /**
     * берем пользователей который ставили "Поделиться"
     * @param objectId
     * @param page
     * @param sizePage
     * @return модель данных
     */
     Map<String, Object> getShareUsers(long objectId, int page, int sizePage);
    
    /**
     * @param user
     * @param item
     * добавляем "Мне нравится"
     */
     void addLike(Users user, Items item) throws IllegalArgumentException, NullPointerException;
    
     /**
     * удаляем "Мне нравится"
     * @param user
     * @param item
     */
     void removeLike(Users user, Items item) throws IllegalArgumentException, NullPointerException;
     
     /**
     * берем пользователей который ставили "Мне нравится"
     * @param objectId
     * @param page
     * @return модель данных
     */
     Map<String, Object> getLikeUsers(long objectId, int page);
     /**
     * берем пользователей который ставили "Мне нравится"
     * @param objectId
     * @param page
     * @param sizePage
     * @return модель данных
     */
     Map<String, Object> getLikeUsers(long objectId, int page, int sizePage);
     /**
      * берем "лайки и поделиться" для конкретногой вьюшки
      * @param item вьюшка, например ArticleView
      * @return 
      */
     ShareLikeView getShareLike(IShareLikesView objectView);
     /**
      * возмем "лайки и поделиться" и установим для конкретной вьюшки
      * @param objectView вьюшка, например ArticleView
      * @param user 
      */
     void setShareLike(IShareLikesView objectView, Users user);
     /**
      * возмем "лайки и поделиться" и установим для конкретных вьюшек
      * @param objectsView вьюшки, например List<ArticleView>
      * @param user 
      */
     <T extends IShareLikesView> void setShareLike(List<T> objectsView, Users user);
     /**
      * возмем "лайки и поделиться" и установим для конкретных вьюшек
      * @param objectsView вьюшки, например List<ArticleView>
      * @param user 
      */
     <T extends IShareLikesView> void setShareLike(List<T> objectsView, Users user, int share_none, int like_none, int share_disabled, int like_disabled);
}
