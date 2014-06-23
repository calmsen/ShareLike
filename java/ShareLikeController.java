/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wp.web.controllers;

import com.wp.model.PropertyName;
import com.wp.model.objects.Items;
import com.wp.model.objects.Nodes;
import com.wp.servicies.interfaces.IBlogService;
import com.wp.utils.mybatis.plugins.TLU;
import com.wp.servicies.interfaces.IShareLikeService;
import com.wp.utils.Cast;
import com.wp.utils.Is;
import com.wp.web.exceptions.BadRequestException;
import com.wp.web.exceptions.ForbiddenException;
import com.wp.web.exceptions.UnauthorizedException;
import com.wp.web.exceptions.InternalServerErrorException;
import com.wp.web.exceptions.NotFoundException;
import com.wp.web.utils.R;
import com.wp.web.views.ArticleView;
import com.wp.web.views.ShareLikeView;
import java.util.HashMap;
import java.util.Map;
import org.jboss.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;

/*
 * Данный класс представляет из себя контроллер<br>
 * которые содержит restapi методы для организации афиш
 * @author Ruslan Rakhmankulov
 * @e-mail rusa86@mail.ru
 */
@Controller
public class ShareLikeController extends AController {
    Logger log = Logger.getLogger(ShareLikeController.class);//логирование
    @Autowired
    IShareLikeService _shareLikeService;
    @Autowired
    IBlogService _blogService;
    
    /**
     * добавляем "Поделиться"
     * @param alias индентификатор узла
     * @param itemId индентификатор объекта
     * @return data - 
     */
    
    @RequestMapping(value = "/{alias}/object/{itemId}/share", method = RequestMethod.POST )
    public @ResponseBody Map<String, Object> addShare(HttpServletRequest request, @PathVariable String alias, @PathVariable String itemId) {
        Map<String, Object> data = new HashMap<String, Object>();
        Nodes node = getNodeByAlias(alias);
        if (node == null) {
            throw new NotFoundException("Страница не найдена.");
        }
        long itemIdLong;
        try {
            itemIdLong = Long.parseLong(itemId);
        } catch (NumberFormatException ex) {
            throw new NotFoundException("Страница не найдена.");
        }
        long userId = 0l;
        if (!Is.Empty(request.getParameter("user_id"))) {
            try {
                userId = Long.parseLong(request.getParameter("user_id"));
            } catch (NumberFormatException ex) {
                throw new BadRequestException("Не правильно передан параметр user_id.");
            }
        }
        if (userId > 0 && user.getId() > 0 && userId != user.getId()) {
            throw new ForbiddenException("Вы авторизовались под другим пользователем. Перезагрузите страницу.");
        }
        if (Is.Empty(user)) {
            throw new UnauthorizedException("Вы не авторизованы.");
        }
        if (_gs.isOwnerNode(user, node)) {
            throw new BadRequestException("Пользователь не может поделиться своим контентом.");
        }
        //определим node пользователя        
        Nodes userNode = _gs.getNodeByName(user.getName());
        if(userNode == null) {
            throw new NullPointerException("Пользователь не имееет узлов");
        }        
        if (userNode.getId() == node.getId()) {
            throw new BadRequestException("Пользователь не может поделиться своим объектом.");
        }
        _shareLikeService.addShare(user, new Items(itemIdLong));
        data.put("share_id", user.getId());
        return data;
      
    }
    
    /**
     * удаляем "Поделиться"
     * @param alias имя или индентификатор узла
     * @param itemId индентификатор объекта
     * @param shareId
     * @return data -
     */
    @RequestMapping(value = "/{alias}/object/{itemId}/share/{shareId}", method = RequestMethod.DELETE )
    public @ResponseBody Map<String, Object> removeShare(HttpServletRequest request, @PathVariable String alias, @PathVariable String itemId, @PathVariable String shareId) {
        Map<String, Object> data = new HashMap<String, Object>();
        Nodes node = getNodeByAlias(alias);
        if (node == null) {
            throw new NotFoundException("Страница не найдена.");
        }
        long itemIdLong;
        try {
            itemIdLong = Long.parseLong(itemId);
        } catch (NumberFormatException ex) {
            throw new NotFoundException("Страница не найдена.");
        }
        long shareIdLong;
        try {
            shareIdLong = Long.parseLong(shareId);
        } catch (NumberFormatException ex) {
            throw new NotFoundException("Страница не найдена.");
        }
        
        long userId = 0l;
        if (!Is.Empty(request.getParameter("user_id"))) {
            try {
                userId = Long.parseLong(request.getParameter("user_id"));
            } catch (NumberFormatException ex) {
                throw new BadRequestException("Неправильно передан параметр user_id.");
            }
        }
        if (userId > 0 && user.getId() > 0 && userId != user.getId()) {
            throw new ForbiddenException("Вы авторизовались под другим пользователем. Перезагрузите страницу.");
        }
        _shareLikeService.removeShare(user, new Items(itemIdLong));
        
        return data;
      
    }
    /**
     * выбираем пользователей которые ставили "Поделиться"
     * @param request
     * @param alias имя или индентификатор узла
     * @param itemId индентификатор объекта
     * @return data - 
     */
    public Map<String, Object> getShareUsers(HttpServletRequest request,  @PathVariable String alias, @PathVariable String itemId, @PathVariable String page, int sizePage) {
        Map<String, Object> data = new HashMap<String, Object>();
        Nodes node = getNodeByAlias(alias);
        if (node == null) {
            throw new NotFoundException("Страница не найдена.");
        }
        long itemIdLong;
        try {
            itemIdLong = Long.parseLong(itemId);
        } catch (NumberFormatException ex) {
            throw new NotFoundException("Страница не найдена.");
        }
        int pageLong;
        try {
            pageLong = Integer.parseInt(page);
        } catch (NumberFormatException ex) {
            throw new NotFoundException("Страница не найдена.");
        }
        
        try {
            data = _shareLikeService.getShareUsers(itemIdLong, pageLong, sizePage);
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().equals("user")) {
                throw new UnauthorizedException("Вы не авторизованы.");
            } else if (ex.getMessage().equals("nodeId") || ex.getMessage().equals("objectId")) {
                throw new NotFoundException("Страница не найдена.");
            }
            
        } catch (NullPointerException ex) {
            throw new InternalServerErrorException("Произошла ошибка на сервере.");
        }
        
        return data;
      
    }
    
    @RequestMapping(value = "/{alias}/object/{itemId}/shares/users/page{page}", method = RequestMethod.GET )
    public @ResponseBody Map<String, Object> getShareUsers(HttpServletRequest request,  @PathVariable String alias, @PathVariable String itemId, @PathVariable String page) {
        return getShareUsers(request, alias, itemId, "1", 5);
    }    
    @RequestMapping(value = "/{alias}/object/{itemId}/shares/users", method = RequestMethod.GET )
    public @ResponseBody Map<String, Object> getShareUsers(HttpServletRequest request,  @PathVariable String alias, @PathVariable String itemId) {
        return getShareUsers(request, alias, itemId, "1", 5);
    }
    @RequestMapping(value = "/{alias}/object/{itemId}/shares/users-in-popup/page{page}", method = RequestMethod.GET )
    public @ResponseBody Map<String, Object> getShareUsersInPopup(HttpServletRequest request,  @PathVariable String alias, @PathVariable String itemId, @PathVariable String page) {
        return getShareUsers(request, alias, itemId, page, 30);
    }
    @RequestMapping(value = "/{alias}/object/{itemId}/shares/users-in-popup", method = RequestMethod.GET )
    public @ResponseBody Map<String, Object> getShareUsersInPopup(HttpServletRequest request,  @PathVariable String alias, @PathVariable String itemId) {
        return getShareUsers(request, alias, itemId, "1", 30);
    }
    
    /**
     * добавляем "Мне нравится"
     * @param alias имя или индентификатор узла
     * @param itemId индентификатор объекта
     * @return data -
     */
    @RequestMapping(value = "/{alias}/object/{itemId}/like", method = RequestMethod.POST )
    public @ResponseBody Map<String, Object> addLike(HttpServletRequest request, @PathVariable String alias, @PathVariable String itemId) {
        Map<String, Object> data = new HashMap<String, Object>();
        Nodes node = getNodeByAlias(alias);
        if (node == null) {
            throw new NotFoundException("Страница не найдена.");
        }
        long itemIdLong;
        try {
            itemIdLong = Long.parseLong(itemId);
        } catch (NumberFormatException ex) {
            throw new NotFoundException("Страница не найдена.");
        }
        long userId = 0l;
        if (!Is.Empty(request.getParameter("user_id"))) {
            try {
                userId = Long.parseLong(request.getParameter("user_id"));
            } catch (NumberFormatException ex) {
                throw new BadRequestException("Неправильно передан параметр user_id.");
            }
        }
        if (userId > 0 && user.getId() > 0 && userId != user.getId()) {
            throw new ForbiddenException("Вы авторизовались под другим пользователем. Перезагрузите страницу.");
        }
        _shareLikeService.addLike(user, new Items(itemIdLong));
        data.put("like_id", user.getId());
        return data;
      
    }
    
    /**
     * удаляем "Мне нравится"
     * @param alias имя или индентификатор узла
     * @param itemId индентификатор объекта
     * @param likeId
     * @return data -
     */
    @RequestMapping(value = "/{alias}/object/{itemId}/like/{likeId}", method = RequestMethod.DELETE )
    public @ResponseBody Map<String, Object> removeLike(HttpServletRequest request, @PathVariable String alias, @PathVariable String itemId, @PathVariable String likeId) {
        Map<String, Object> data = new HashMap<String, Object>();
        Nodes node = getNodeByAlias(alias);
        if (node == null) {
            throw new NotFoundException("Страница не найдена.");
        }
        long itemIdLong;
        try {
            itemIdLong = Long.parseLong(itemId);
        } catch (NumberFormatException ex) {
            throw new NotFoundException("Страница не найдена.");
        }
        long likeIdLong;
        try {
            likeIdLong = Long.parseLong(likeId);
        } catch (NumberFormatException ex) {
            throw new NotFoundException("Страница не найдена.");
        }
        long userId = 0l;
        if (!Is.Empty(request.getParameter("user_id"))) {
            try {
                userId = Long.parseLong(request.getParameter("user_id"));
            } catch (NumberFormatException ex) {
                throw new BadRequestException("Не правильно передан параметр user_id.");
            }
        }
        if (userId > 0 && user.getId() > 0 && userId != user.getId()) {
            throw new ForbiddenException("Вы авторизовались под другим пользователем. Перезагрузите страницу.");
        }
        _shareLikeService.removeLike(user, new Items(itemIdLong));
        
        return data;
      
    }
    /**
     * выбираем пользователей которые ставили "Мне нравится"
     * @param request
     * @param alias имя или индентификатор узла
     * @param itemId индентификатор объекта
     * @param page текущая страница
     * @param sizePage количество записей на странице
     * @return data - 
     */
    public Map<String, Object> getLikeUsers(HttpServletRequest request, String alias, @PathVariable String itemId, String page, int sizePage) {
        Map<String, Object> data = new HashMap<String, Object>();
        Nodes node = getNodeByAlias(alias);
        if (node == null) {
            throw new NotFoundException("Страница не найдена.");
        }
        long itemIdLong;
        try {
            itemIdLong = Long.parseLong(itemId);
        } catch (NumberFormatException ex) {
            throw new NotFoundException("Страница не найдена.");
        }
        int pageLong;
        try {
            pageLong = Integer.parseInt(page);
        } catch (NumberFormatException ex) {
            throw new NotFoundException("Страница не найдена.");
        }
        
        try {
            data = _shareLikeService.getLikeUsers(itemIdLong, pageLong, sizePage);
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().equals("user")) {
                throw new UnauthorizedException("Вы не авторизованы.");
            } else if (ex.getMessage().equals("nodeId") || ex.getMessage().equals("objectId")) {
                throw new NotFoundException("Страница не найдена.");
            }
            
        } catch (NullPointerException ex) {
            throw new InternalServerErrorException("Произошла ошибка на сервере.");
        }
        
        return data;
      
    }
    
    @RequestMapping(value = "/{alias}/object/{itemId}/likes/users/page{page}", method = RequestMethod.GET )
    public @ResponseBody Map<String, Object> getLikeUsers(HttpServletRequest request,  @PathVariable String alias, @PathVariable String itemId, @PathVariable String page) {
        return getLikeUsers(request, alias, itemId, "1", 5);
    }    
    @RequestMapping(value = "/{alias}/object/{itemId}/likes/users", method = RequestMethod.GET )
    public @ResponseBody Map<String, Object> getLikeUsers(HttpServletRequest request,  @PathVariable String alias, @PathVariable String itemId) {
        return getLikeUsers(request, alias, itemId, "1", 5);
    }
    @RequestMapping(value = "/{alias}/object/{itemId}/likes/users-in-popup/page{page}", method = RequestMethod.GET )
    public @ResponseBody Map<String, Object> getLikeUsersInPopup(HttpServletRequest request,  @PathVariable String alias, @PathVariable String itemId, @PathVariable String page) {
        return getLikeUsers(request, alias, itemId, page, 30);
    }
    @RequestMapping(value = "/{alias}/object/{itemId}/likes/users-in-popup", method = RequestMethod.GET )
    public @ResponseBody Map<String, Object> getLikeUsersInPopup(HttpServletRequest request,  @PathVariable String alias, @PathVariable String itemId) {
        return getLikeUsers(request, alias, itemId, "1", 30);
    }
    /**
     * http://localhost:8084/wp/84195/object/84272/shareLikeView
     * @param model
     * @param request
     * @param alias
     * @param itemId
     * @return 
     */
    @RequestMapping(value = "/{alias}/object/{itemId}/shareLikeView", method = RequestMethod.GET )
    public String getShareLikeView(ModelMap model, HttpServletRequest request, @PathVariable String alias, @PathVariable String itemId) {
        Nodes node = getNodeByAlias(alias);
        long itemIdLong;
        try {
            itemIdLong = Long.parseLong(itemId);
        } catch (NumberFormatException ex) {
            throw new NotFoundException("Страница не найдена.");
        }
        ArticleView article = _blogService.getArticle(itemIdLong);
        _shareLikeService.setShareLike(article, user);
        
        model.put("slv", article.getSlv());
        
        return R.SHARE_LIKE_VIEW;
    }
    @RequestMapping(value = "/playerView", method = RequestMethod.GET )
    public String getPlayerView(ModelMap model, HttpServletRequest request) {
        return "common/playerView";
    }
}
