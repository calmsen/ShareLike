define("ShareLike", ["addJqueryPlugin", "SettingsDenied", "FormParser", 'text!shareLikeCss.css', 'tmpl', 'text!shareLikeTmpl.ftl', 'fancybox', "toastmessage"], function(addJqueryPlugin, SettingsDenied, FormParser) {
    var settings = {
        node_name: "0"
        , object_id: 0
        , object_type: ""
        , shares_amount: 0
        , likes_amount: 0
        , share_id: 0
        , like_id: 0
        , sign: 0
        , share_none: 0
        , like_none: 0
        , share_disabled: 0
        , like_disabled: 0
        , base_url: window.base_url
        , user: window.wpUser
        , warningMessage: window.warningMessage
        , errorMessage: window.errorMessage
        , declension: window.declension
    }
    var doc = $(document);
    /**
     * @constant
     * @type {string}
     */
    var JQUERY_PLUGIN_NAME = "shareLike";
    
    /**
     * Конструктор ShareLike. В конструкторе определюятся все свойства объекта. 
     * Защищенные и приватные свойства называются начиная с символа подчеркивания
     * @constructor
     * @extends {Object}
     * @param {Object} options
     */
    function ShareLike(options) {
        $.extend(this, settings, options.element.data("options"), options);

        this.postfix = this.postfix || this.object_id;
        this._url = this._url || this.base_url + '/' + this.node_name + '/object/' + this.object_id;
        this.ajax_in_action = false;
        this.share_page = 1;
        this.like_page = 1;
        this.share_page_in_popup = 1;
        this.like_page_in_popup = 1;
        this.checkOpenRepostPopup = false;
        this.openShareUsersTimeout;
        this.closeShareUsersTimeout;
        this.openLikeUsersTimeout;
        this.closeLikeUsersTimeout;
        
        this._init();
    }
    /**
     * Наследуемся от класса родителя и определяем методы. Защищенные и приватные методы называются начиная с символа подчеркивания
     */
    var methods = ShareLike.prototype = new Object();
    /**
     * Создаем функциб с замыканием на наш объект
     * @param {string} name
     */
    methods._proxy = function(name) {
        var obj = this;
        return this["proxy-" + name] = this["proxy-" + name] || function(arg1) {
            obj[name](arg1);
        };
    };
    
    methods._init = function() {
        this.element.prepend(this.getShareLikeHolder());
        this.setShareLikeHolderEvents();
    };
    
    methods.setShareLikeHolderEvents = function() {

        var shareLike = this,
            additional_content = $('#rePostSettings').tmpl({
                node_name: shareLike.user.login,
                post_id: shareLike.object_id
            });

        $('#share-holder-' + this.postfix).wpSettingsDenied({
            node_name: shareLike.user.login
            , module_type: "other"
            , afterLoad: function(wpSD, content){
                content.prepend(additional_content);
                content.prepend(additional_content);
            }
            , save: function(event, wpSD, content){
                shareLike.addRepost($(event.currentTarget), wpSD.getContent(), wpSD)
            }
        });

        $('#share-holder-' + this.postfix + '')
                //.on('click', this._proxy("openRepostPopup"))
                //.on('click', this._proxy("addRemoveShare"))
                .on('mouseenter', this._proxy("openCloseShareUsersWL"));
        $('#like-holder-' + this.postfix + '')
                .on('click', this._proxy("addRemoveLike"))
                .on('mouseenter', this._proxy("openCloseLikeUsersWS"));
    };
    
    //работаем с шаблонами
    methods.getShareLikeHolder = function() {
        return $('#shareLikeHolderTmpl').addClass('share-like-wrapper').tmpl({
            postfix: this.postfix
            , share_id: this.share_id
            , shares_amount: this.shares_amount
            , like_id: this.like_id
            , likes_amount: this.likes_amount
            , user_id: this.user.id
            , sign: this.sign
            , share_none: this.share_none
            , like_none: this.like_none
            , share_disabled: this.share_disabled
            , like_disabled: this.like_disabled
        });
    }

    methods.getShareLikeUsersHolder = function() {
        return $('#shareLikeUsersHolderTmpl').tmpl({
            postfix: this.postfix
        })
    }
    
    methods.setShareUsersInsideEvents = function(data) {
        for (var key in data.users) {
            $('#share-users-lst-' + this.postfix + '').append(this.getShareUserItem({user: data.users[key]}));
        }
        $('#share-users-lnk-' + this.postfix + '')
                .on('click', this._proxy("moreShareUsers"));
        $('#share-users-open-popup-lnk-' + this.postfix + '')
                .on('click', this._proxy("openShareUsersInPopup"));
    };
    
    methods.getShareUsersInside = function(data) {
        return $('#shareUsersTmpl').tmpl({
            postfix: this.postfix
            , users_link_exists: data.users_link_exists
            , shares_amount: this.shares_amount
            , declension: this.declension
        });
    }

    methods.getShareUserItem = function(data) {
        return $('#shareUserTmpl').tmpl({
            postfix: this.postfix
            , user: data.user
        });
    };
    
    methods.setLikeUsersInsideEvents = function(data) {
        for (var key in data.users) {
            $('#like-users-lst-' + this.postfix + '').append(this.getLikeUserItem({user: data.users[key]}));
        }
        $('#like-users-lnk-' + this.postfix + '')
                .on('click', this._proxy("moreLikeUsers"));
        $('#like-users-open-popup-lnk-' + this.postfix + '')
                .on('click', this._proxy("openLikeUsersInPopup"));
    };
    
    methods.getLikeUsersInside = function(data) {
        return $('#likeUsersTmpl').tmpl({
            postfix: this.postfix
            , users_link_exists: data.users_link_exists
            , likes_amount: this.likes_amount
            , declension: this.declension
        });
    };

    methods.getLikeUserItem = function(data) {
        return $('#likeUserTmpl').tmpl({
            postfix: this.postfix
            , user: data.user
        })
    };
    // in popup
    methods.setShareLikeUsersInPopupHolderEvents = function() {
            $('#share-users-in-popup-tab-' + this.postfix + '')
                    .on('click', this._proxy("showShareUsersInPopupWL"));
            $('#like-users-in-popup-tab-' + this.postfix + '')
                    .on('click', this._proxy("showLikeUsersInPopupWS"));
    };
    
    methods.getShareLikeUsersInPopupHolder = function() {
        return $('#shareLikeUsersInPopupHolderTmpl').tmpl({
            postfix: this.postfix
            , likes_amount: this.likes_amount
            , shares_amount: this.shares_amount
            , declension: this.declension
        });
    };
    methods.setShareUsersInPopupInsideEvents = function(data) {
        for (var key in data.users) {
            $('#share-users-in-popup-lst-' + this.postfix + '').append(this.getShareUserInPopupItem({user: data.users[key]}));
        }
        $('#share-users-in-popup-lnk-' + this.postfix + '')
                .on('click', this._proxy("moreShareUsersInPopup"));
    };
    
    methods.getShareUsersInPopupInside = function(data) {
        return $('#shareUsersInPopupTmpl').tmpl({
            postfix: this.postfix
            , users_link_exists: data.users_link_exists
        });
    };

    methods.getShareUserInPopupItem = function(data) {
        return $('#shareUserInPopupTmpl').tmpl({
            postfix: this.postfix
            , user: data.user
        });
    };
    
    methods.setLikeUsersInPopupInsideEvents = function(data) {
        for (var key in data.users) {
            $('#like-users-in-popup-lst-' + this.postfix + '').append(this.getLikeUserInPopupItem({user: data.users[key]}));
        }
        $('#like-users-in-popup-lnk-' + this.postfix + '')
                .on('click', this._proxy("moreLikeUsersInPopup"));
    };
    
    methods.getLikeUsersInPopupInside = function(data) {
        return $('#likeUsersInPopupTmpl').tmpl({
            postfix: this.postfix
            , users_link_exists: data.users_link_exists
        });
    };

    methods.getLikeUserInPopupItem = function(data) {
        return $('#likeUserInPopupTmpl').tmpl({
            postfix: this.postfix
            , user: data.user
        })
    };
    
    methods.getShareUsersOnSuccess = function(data) {
        $('#share-like-users-loading-' + this.postfix + '').css({'display': 'none'});

        $('#share-like-users-holder-' + this.postfix + '')
                .append(this.getShareUsersInside({
                    users: data.users
                    , users_link_exists: data.users_link_exists
                }));
        this.setShareUsersInsideEvents(data);
        this.ajax_in_action = false;        
    };
    
    methods.getShareUsersOnError = function() {
        $('#share-like-users-loading-' + this.postfix + '').css({'display': 'none'});
        this.ajax_in_action = false;
    };
    //работаем с ajax
    methods.getShareUsers = function() {
        if (typeof this.getShareUsersDefObj == "undefined") {
            if (this.ajax_in_action) {
                return;
            }
            this.ajax_in_action = true;

            $('#share-like-users-loading-' + this.postfix + '').css({'display': 'block'});

            this.getShareUsersDefObj = $.ajax({
                url: this._url + '/shares/users',
                type: 'GET',
                dataType: 'json',
                context: this,
                success: this.getShareUsersOnSuccess,
                error: this.getShareUsersOnError
            });
        }
        return this.getShareUsersDefObj;
    };
    
    methods.getLikeUsersOnSuccess = function(data) {
        $('#share-like-users-loading-' + this.postfix + '').css({'display': 'none'});

        $('#share-like-users-holder-' + this.postfix + '')
                .append(this.getLikeUsersInside({
                    users: data.users
                    , users_link_exists: data.users_link_exists
                }));
        this.setLikeUsersInsideEvents(data);
        this.ajax_in_action = false;
    };
    
    methods.getLikeUsersOnError = function() {
        $('#share-like-users-loading-' + this.postfix + '').css({'display': 'none'});
        this.ajax_in_action = false;
    };
    
    methods.getLikeUsers = function() {
        if (typeof this.getLikeUsersDefObj == "undefined") {
            if (this.ajax_in_action) {
                return;
            }
            this.ajax_in_action = true;

            $('#share-like-users-loading-' + this.postfix + '').css({'display': 'block'});

            this.getLikeUsersDefObj = $.ajax({
                url: this._url + '/likes/users',
                type: 'GET',
                dataType: 'json',
                context: this,
                success: this.getLikeUsersOnSuccess,
                error: this.getLikeUsersOnError
            });
        }
        return this.getLikeUsersDefObj;
    };
    
    methods.moreShareUsersOnSuccess = function(data) {
        this.share_page++;
        if (data.users.length) {
            for (var key in data.users) {
                $('#share-users-lst-' + this.postfix + '').append(this.getShareUserItem({user: data.users[key]}));
            }
        }
        if (!data.users_link_exists) {
            $('#share-users-lnk-' + this.postfix + '').remove();
        }
        this.ajax_in_action = false;
    };
    
    methods.moreShareUsersOnError = function() {
        this.ajax_in_action = false;
    };
    
    methods.moreShareUsers = function() {
        if (this.ajax_in_action) {
            return;
        }
        this.ajax_in_action = true;

        $.ajax({
            url: this._url + '/shares/users/page' + (this.share_page + 1),
            type: 'GET',
            dataType: 'json',
            context: this,
            success: this.moreShareUsersOnSuccess,
            error: this.moreShareUsersOnError
        });

    };
    
    methods.moreLikeUsersOnSuccess = function(data) {
        this.like_page++;
        if (data.users.length) {
            for (var key in data.users) {
                $('#like-users-lst-' + this.postfix + '').append(this.getLikeUserItem({user: data.users[key]}));
            }
        }
        if (!data.users_link_exists) {
            $('#like-users-lnk-' + this.postfix + '').remove();
        }
        this.ajax_in_action = false;
    };
    
    methods.moreLikeUsersOnError = function() {
        this.ajax_in_action = false;
    };
    
    methods.moreLikeUsers = function() {
        if (this.ajax_in_action) {
            return;
        }
        this.ajax_in_action = true;

        $.ajax({
            url: this._url + '/likes/users/page' + (this.like_page + 1),
            type: 'GET',
            dataType: 'json',
            context: this,
            success: this.moreLikeUsersOnSuccess,
            error: this.moreLikeUsersOnError
        });

    };

    methods.getShareUsersInPopupOnSuccess = function(data) {
        $('#share-like-users-in-popup-loading-' + this.postfix + '').css({'display': 'none'});

        $('#share-like-users-in-popup-holder-' + this.postfix + '')
                .append(this.getShareUsersInPopupInside({
                    users: data.users
                    , users_link_exists: data.users_link_exists
                }));
        this.setShareUsersInPopupInsideEvents(data);
        this.ajax_in_action = false;        
    };
    
    methods.getShareUsersInPopupOnError = function() {
        $('#share-like-users-in-popup-loading-' + this.postfix + '').css({'display': 'none'});
        this.ajax_in_action = false;
    };
    
    methods.getShareUsersInPopup = function() {
        if (typeof this.getShareUsersInPopupDefObj == "undefined") {
            if (this.ajax_in_action) {
                return;
            }
            this.ajax_in_action = true;

            $('#share-like-users-in-popup-loading-' + this.postfix + '').css({'display': 'block'});

            this.getShareUsersInPopupDefObj = $.ajax({
                url: this._url + '/shares/users-in-popup',
                type: 'GET',
                dataType: 'json',
                context: this,
                success: this.getShareUsersInPopupOnSuccess,
                error: this.getShareUsersInPopupOnError
            });
        }
        return this.getShareUsersInPopupDefObj;
    };
    
    methods.getLikeUsersInPopupOnSuccess = function(data) {
        $('#share-like-users-in-popup-loading-' + this.postfix + '').css({'display': 'none'});

        $('#share-like-users-in-popup-holder-' + this.postfix + '')
                .append(this.getLikeUsersInPopupInside({
                    users: data.users
                    , users_link_exists: data.users_link_exists
                }));
        this.setLikeUsersInPopupInsideEvents(data);
        this.ajax_in_action = false;
    };
    
    methods.getLikeUsersInPopupOnError = function() {
        $('#share-like-users-in-popup-loading-' + this.postfix + '').css({'display': 'none'});
            this.ajax_in_action = false;
    };
    
    methods.getLikeUsersInPopup = function() {
        if (typeof this.getLikeUsersInPopupDefObj == "undefined") {
            if (this.ajax_in_action) {
                return;
            }
            this.ajax_in_action = true;

            $('#share-like-users-in-popup-loading-' + this.postfix + '').css({'display': 'block'});

            this.getLikeUsersInPopupDefObj = $.ajax({
                url: this._url + '/likes/users',
                type: 'GET',
                dataType: 'json',
                context: this,
                success: this.getLikeUsersInPopupOnSuccess,
                error: this.getLikeUsersInPopupOnError
            });
        }
        return this.getLikeUsersInPopupDefObj;
    };
    
    methods.moreShareUsersInPopupOnSuccess = function(data) {
        this.share_page_in_popup++;
        if (data.users.length) {
            for (var key in data.users) {
                $('#share-users-in-popup-lst-' + this.postfix + '').append(this.getShareUserInPopupItem({user: data.users[key]}));
            }
        }
        if (!data.users_link_exists) {
            $('#share-users-in-popup-lnk-' + this.postfix + '').remove();
        }
        this.ajax_in_action = false;
    };
    
    methods.moreShareUsersInPopupOnError = function() {
        this.ajax_in_action = false;
    };
    
    methods.moreShareUsersInPopup = function() {
        if (this.ajax_in_action) {
            return;
        }
        this.ajax_in_action = true;

        $.ajax({
            url: this._url + '/shares/users-in-popup/page' + (this.share_page_in_popup + 1),
            type: 'GET',
            dataType: 'json',
            context: this,
            success: this.moreShareUsersInPopupOnSuccess,
            error: this.moreShareUsersInPopupOnError
        });

    };
    
    methods.moreLikeUsersInPopupOnSuccess = function(data) {
       this.like_page_in_popup++;
        if (data.users.length) {
            for (var key in data.users) {
                $('#like-users-in-popup-lst-' + this.postfix + '').append(this.getLikeUserInPopupItem({user: data.users[key]}));
            }
        }
        if (!data.users_link_exists) {
            $('#like-users-in-popup-lnk-' + this.postfix + '').remove();
        }
        this.ajax_in_action = false; 
    };
    
    methods.moreLikeUsersInPopupOnError = function() {
        this.ajax_in_action = false;
    };
    
    methods.moreLikeUsersInPopup = function() {
        if (this.ajax_in_action) {
            return;
        }
        this.ajax_in_action = true;

        $.ajax({
            url: this._url + '/likes/users-in-popup/page' + (this.like_page_in_popup + 1),
            type: 'GET',
            dataType: 'json',
            context: this,
            success: this.moreLikeUsersInPopupOnSuccess,
            error: this.moreLikeUsersInPopupOnError
        });
    };

    methods.addShareOnBeforeSend = function() {
        this.shares_amount++;
        $('#share-link-' + this.postfix + '').addClass('shared');
        var shareAmountLink = $('#share-amount-link-' + this.postfix + '');
        shareAmountLink.html(this.shares_amount);
        //если добавился только первый пользователь
        if (this.shares_amount == 1) {
            shareAmountLink.removeClass("none");
            $('#share-amount-link-'+this.postfix+'').removeClass("disabled")
            $("#share-users-open-popup-lnk-" + this.postfix).html("Поделились " + this.shares_amount + " " + this.declension(this.shares_amount, ["человек", "человека", "человек"]));
        }
        /* getShareUserItem */
        if ($('#share-users-lst-' + this.postfix + '').length > 0) {
            var user = {
                user_id: this.user.id
                , user_login: this.user.login
                , user_full_name: (this.user.first_name ? this.user.first_name + (this.user.middle_name ? " " + this.user.middle_name : "") : this.user.login)
                , user_avatar: "/" + this.user.login + "/avatar/small_square"

            };
            $('#share-users-lst-' + this.postfix + '').prepend(this.getShareUserItem({user: user}));
        }
    };

    methods.addShareOnSuccess = function() {
        this.share_id = this.user.id;
        this.ajax_in_action = false;
    };

    methods.addShareOnError = function(data, status) {
        this.ajax_in_action = false;
        if (!this.share_id) {
            this.removeShareOnBeforeSend();
        }
    };

    methods.addShare = function() {
        if (!this.user.id) {
            this.warningMessage('Это действие доступно только для авторизованных пользователей.');
            return;
        }
        if (this.user.id == this.sign) {
            return;
        }
        if (this.ajax_in_action) {
            return;
        }
        this.ajax_in_action = true;

        this.addShareOnBeforeSend();

        var xhr = $.ajax({
            url: this._url + '/share',
            type: 'POST',
            dataType: 'json',
            data: "user_id=" + this.user.id,
            context: this,
            success: this.addShareOnSuccess,
            error: this.addShareOnError
        });
    };

    methods.removeShareOnBeforeSend = function() {
        this.shares_amount--;
        $('#share-link-' + this.postfix + '').removeClass('shared');
        var shareAmountLink = $('#share-amount-link-' + this.postfix + '');
        shareAmountLink.html(this.shares_amount != 0 ? this.shares_amount : "");
        $("#share-users-open-popup-lnk-" + this.postfix).html("Поделились " + this.shares_amount + " " + this.declension(this.shares_amount, ["человек", "человека", "человек"]));
        //если нет ниодного пользователя
        if (this.shares_amount == 0) {
            shareAmountLink.addClass("none");
            $('#share-amount-link-'+this.postfix+'').addClass("disabled")
        }
        /* removeShareUsers */
        $('#share-users-itm-' + this.user.id + '_' + this.postfix + '').remove();
        if ($('#share-users-inside-' + this.postfix + '').hasClass('active')) {
            if ($('#share-users-lst-' + this.postfix + '>li').length == 0) {
                this.closeShareUsers();
            }
        }
    };

    methods.removeShareOnSuccess = function(data) {
        this.share_id = 0;
        this.ajax_in_action = false;
    };

    methods.removeShareOnError = function(data, status) {
        this.ajax_in_action = false;
        if (this.share_id) {
            this.addShareOnBeforeSend();
        }
    };

    methods.removeShare = function() {
        if (!this.user.id) {
            this.warningMessage('Это действие доступно только для авторизованных пользователей.');
            return;
        }
        if (this.ajax_in_action) {
            return;
        }
        this.ajax_in_action = true;

        this.removeShareOnBeforeSend();

        $.ajax({
            url: this._url + '/share/' + this.share_id + "?user_id=" + this.user.id,
            type: 'DELETE',
            dataType: 'json',
            context: this,
            success: this.removeShareOnSuccess,
            error: this.removeShareOnError
        });
    }

    methods.addRemoveShare = function() {
        if (!$('#share-link-' + this.postfix).hasClass('shared')) {
            this.addShare();
        }
        else {
            this.removeShare();
        }
    }

    methods.addRepost = function(btn, holder_object, wpSD) {

        if (!this.user.id) {
            this.warningMessage('Это действие доступно только для авторизованных пользователей.');
            return;
        }
        if (this.user.id == this.sign) {
            return;
        }
        if (this.ajax_in_action) {
            return;
        }
        this.ajax_in_action = true;

        $.fancybox.close();

        if (!this.share_id) {
            this.addShareOnBeforeSend();
        }
        var need_like_for_delete = false;
        if (!this.like_id) {
            need_like_for_delete = true;
            this.addLikeOnBeforeSend();
        }

        var fP = new FormParser({
            form: holder_object,
            element: btn
        });

        var data = {};

        try {
            data = fP.parseForm();
        }
        catch (e){
            return;
        }

        data['access_denied_json'] = wpSD.getAccessDeniedJson();

        var xhr = $.ajax({
            url: this.base_url + '/' + data.node_name + '/repost/' + data.post_id,
            data: data,
            dataType: 'json',
            type: 'POST',
            context: this
        })
                .done(this.addShareOnSuccess)
                .done(this.addLikeOnSuccess)
                .fail(this.addShareOnError);

        if (need_like_for_delete) {
            xhr.fail(this.addLikeOnError);
        }

    };

    methods.addLikeOnBeforeSend = function() {
        this.likes_amount++;
        $('#like-link-' + this.postfix + '').addClass('liked');
        var likeAmountLink = $('#like-amount-link-' + this.postfix + '');
        likeAmountLink.html(this.likes_amount);
        $("#like-users-open-popup-lnk-" + this.postfix).html("Понравилось " + this.likes_amount + " " + this.declension(this.likes_amount, ["человеку", "людям", "людям"]));
        //если добавился только первый пользователь
        if (this.likes_amount == 1) {
            likeAmountLink.removeClass("none");
            $('#like-amount-link-'+this.postfix+'').removeClass("disabled")
        }
        
        /* getLikeUserItem */
        if ($('#like-users-lst-' + this.postfix + '').length > 0) {
            var user = {
                user_id: this.user.id
                , user_login: this.user.login
                , user_full_name: (this.user.first_name ? this.user.first_name + (this.user.middle_name ? " " + this.user.middle_name : "") : this.user.login)
                , user_avatar: "/" + this.user.login + "/avatar/small_square"

            };
            $('#like-users-lst-' + this.postfix + '').prepend(this.getLikeUserItem({user: user}));
        }
    }

    methods.addLikeOnSuccess = function(data) {
        this.like_id = this.user.id;
        this.ajax_in_action = false;
    }

    methods.addLikeOnError = function(data, status) {
        this.ajax_in_action = false;
        if (!this.like_id) {
            this.removeLikeOnBeforeSend();
        }
    }

    methods.addLike = function() {
        if (!this.user.id) {
            this.warningMessage('Это действие доступно только для авторизованных пользователей.');
            return;
        }
        if (this.user.id == this.sign) {
            return;
        }
        if (this.ajax_in_action) {
            return;
        }
        this.ajax_in_action = true;

        this.addLikeOnBeforeSend();

        $.ajax({
            url: this._url + '/like',
            type: 'POST',
            data: "user_id=" + this.user.id,
            dataType: 'json',
            context: this,
            success: this.addLikeOnSuccess,
            error: this.addLikeOnError
        });
    }

    methods.removeLikeOnBeforeSend = function() {
        this.likes_amount--;
        $('#like-link-' + this.postfix + '').removeClass('liked');
        var likeAmountLink = $('#like-amount-link-' + this.postfix + '');
        likeAmountLink.html(this.likes_amount != 0 ? this.likes_amount : '');
        $("#like-users-open-popup-lnk-" + this.postfix).html("Понравилось " + this.likes_amount + " " + this.declension(this.likes_amount, ["человеку", "людям", "людям"]));
        //если нет ниодного пользователя
        if (this.likes_amount == 0) {
            likeAmountLink.addClass("none");
            $('#like-amount-link-'+this.postfix+'').addClass("disabled")
        }
        /* removeLikeUsers */
        $('#like-users-itm-' + this.user.id + '_' + this.postfix + '').remove();
        if ($('#like-users-inside-' + this.postfix + '').hasClass('active')) {
            if ($('#like-users-lst-' + this.postfix + '>li').length == 0) {
                this.closeLikeUsers();
            }
        }
    }

    methods.removeLikeOnSuccess = function(data) {
        this.like_id = 0;
        this.ajax_in_action = false;
    }

    methods.removeLikeOnError = function(data, status) {
        this.ajax_in_action = false;
        if (this.like_id) {
            this.addLikeOnBeforeSend();
        }
    }

    methods.removeLike = function() {
        if (!this.user.id) {
            this.warningMessage('Это действие доступно только для авторизованных пользователей.');
            return;
        }
        if (this.ajax_in_action) {
            return;
        }
        this.ajax_in_action = true;

        this.removeLikeOnBeforeSend();

        $.ajax({
            url: this._url + '/like/' + this.like_id + "?user_id=" + this.user.id,
            type: 'DELETE',
            dataType: 'json',
            context: this,
            success: this.removeLikeOnSuccess,
            error: this.removeLikeOnError
        });
    }

    methods.addRemoveLike = function() {
        if (!$('#like-link-' + this.postfix).hasClass('liked')) {
            this.addLike();
        }
        else {
            this.removeLike();
        }
    };
    //работаем с анимациями
    methods.openShareUsers = function() {
        if ($('#share-amount-link-' + this.postfix + '').hasClass("disabled")) {
            return;
        }
        $('#share-amount-link-' + this.postfix + '').addClass('tab');

        if ($('#share-like-users-holder-' + this.postfix + '').length == 0) {
            $('#share-like-' + this.postfix + '').append(this.getShareLikeUsersHolder());
        }
        this.getShareUsers();
        $('#share-like-users-holder-' + this.postfix + '').removeClass("share-like-users-holder-none");
        this.getShareUsers().done(function() {
            //$('#share-like-users-holder-'+this.postfix+'').css({top: "-86px", right: "0px"});
            $('#share-users-inside-' + this.postfix + '').addClass('active');
        });
    };

    methods.closeShareUsers = function() {
        $('#share-like-users-holder-' + this.postfix + '').addClass("share-like-users-holder-none");
        //$('#share-like-users-holder-'+this.postfix+'').css({top: "106px", right: "0px"});
        $('#share-users-inside-' + this.postfix + '').removeClass('active');
        $('#share-amount-link-' + this.postfix + '').removeClass('tab');
    };
    
    methods.openCloseShareUsers = function() {
        var shareLike = this;
        if ($('#share-amount-link-' + this.postfix + '').hasClass("disabled")) {
            return;
        }
        if (!$('#share-users-inside-' + this.postfix + '').hasClass('active')) {
            this.openShareUsersTimeout = setTimeout(function() {
                shareLike.openShareUsers();
                $('#share-holder-' + shareLike.postfix + ', #share-users-inside-' + shareLike.postfix + '')
                        .off("mouseleave.timeout" + this.postfix).on("mouseleave.timeout" + shareLike.postfix, function() {
                            shareLike.getShareUsers().done(function() {
                                shareLike.closeShareUsersTimeout = setTimeout(function() {
                                    shareLike.closeShareUsers();
                                    doc.off("click.timeout" + shareLike.postfix);
                                }, 500);
                            });
                        })
                        .off("mouseenter.timeout" + shareLike.postfix).on("mouseenter.timeout" + shareLike.postfix, function() {
                            clearTimeout(shareLike.closeShareUsersTimeout);
                        });
                doc.off("click.timeout" + shareLike.postfix).on("click.timeout" + shareLike.postfix, function(event) {
                    if ($(event.target).closest('#share-holder-' + shareLike.postfix).length == 0 &&
                            $(event.target).closest('#share-users-inside-' + shareLike.postfix).length == 0) {
                        clearTimeout(shareLike.closeShareUsersTimeout);
                        shareLike.closeShareUsers();
                        doc.off("click.timeout" + shareLike.postfix);
                    }
                });
            }, 100);
            $('#share-holder-' + this.postfix)
                    .off("mouseleave.timeout" + this.postfix).on("mouseleave.timeout" + this.postfix, function() {
                        clearTimeout(this.openShareUsersTimeout);
                    });
        }
    };

    methods.openCloseShareUsersWL = function() {
        if ($('#like-users-inside-' + this.postfix + '').hasClass('active')) {
            clearTimeout(this.closeLikeUsersTimeout);
            this.closeLikeUsers();
            doc.off("click.timeout" + this.postfix);
        }
        this.openCloseShareUsers();
    };

    methods.openLikeUsers = function() {
        if ($('#like-amount-link-' + this.postfix + '').hasClass("disabled")) {
            return;
        }
        $('#like-amount-link-' + this.postfix + '').addClass('tab');

        if ($('#share-like-users-holder-' + this.postfix + '').length == 0) {
            $('#share-like-' + this.postfix + '').append(this.getShareLikeUsersHolder());
        }
        this.getLikeUsers();
        $('#share-like-users-holder-' + this.postfix + '').removeClass("share-like-users-holder-none");
        this.getLikeUsers().done(function() {
            //$('#share-like-users-holder-'+this.postfix+'').css({top: "-86px", right: "0px"});
            $('#like-users-inside-' + this.postfix + '').addClass('active')
        });
    };

    methods.closeLikeUsers = function() {
        $('#share-like-users-holder-' + this.postfix + '').addClass("share-like-users-holder-none");
        //$('#share-like-users-holder-'+this.postfix+'').css({top: "0px", right: "0px"});
        $('#like-users-inside-' + this.postfix + '').removeClass('active');
        $('#like-amount-link-' + this.postfix + '').removeClass('tab');
    };

    methods.openCloseLikeUsers = function() {
        var shareLike = this;
        if ($('#like-amount-link-' + this.postfix + '').hasClass("disabled")) {
            return;
        }

        if (!$('#like-users-inside-' + this.postfix + '').hasClass('active')) {
            this.openLikeUsersTimeout = setTimeout(function() {
                shareLike.openLikeUsers();
                $('#like-holder-' + shareLike.postfix + ', #like-users-inside-' + shareLike.postfix + '')
                        .off("mouseleave.timeout" + shareLike.postfix).on("mouseleave.timeout" + shareLike.postfix, function() {
                            shareLike.getLikeUsers().done(function() {
                                shareLike.closeLikeUsersTimeout = setTimeout(function() {
                                    shareLike.closeLikeUsers();
                                    doc.off("click.timeout" + shareLike.postfix);
                                }, 500);
                            });
                        })
                        .off("mouseenter.timeout" + shareLike.postfix).on("mouseenter.timeout" + shareLike.postfix, function() {
                        clearTimeout(shareLike.closeLikeUsersTimeout);
                    });
                doc.off("click.timeout" + shareLike.postfix).on("click.timeout" + shareLike.postfix, function(event) {
                    if ($(event.target).closest('#like-holder-' + shareLike.postfix).length == 0 &&
                            $(event.target).closest('#like-users-inside-' + shareLike.postfix).length == 0) {
                        clearTimeout(shareLike.closeLikeUsersTimeout);
                        shareLike.closeLikeUsers();
                        doc.off("click.timeout" + shareLike.postfix);
                    }
                });
            }, 100);
            $('#like-holder-' + shareLike.postfix)
                    .off("mouseleave.timeout" + shareLike.postfix).on("mouseleave.timeout" + shareLike.postfix, function() {
                        clearTimeout(shareLike.openLikeUsersTimeout);
                    });
        }

    };

    methods.openCloseLikeUsersWS = function() {
        if ($('#share-users-inside-' + this.postfix + '').hasClass('active')) {
            clearTimeout(this.closeShareUsersTimeout);
            this.closeShareUsers();
            doc.off("click.timeout" + this.postfix);
        }
        this.openCloseLikeUsers();
    };
    // in popup
    methods.openShareUsersInPopup = function() {
        if ($('#share-users-open-popup-lnk-' + this.postfix + '').hasClass("disabled")) {
            return;
        }

        if ($('#share-like-users-in-popup-holder-' + this.postfix + '').length == 0) {
            $('#share-like-' + this.postfix + '').append(this.getShareLikeUsersInPopupHolder());
            this.setShareLikeUsersInPopupHolderEvents();
        }
        this.getShareUsersInPopup();
        var shareLike = this;
        $.fancybox.open({
            content: $('#share-like-users-in-popup-holder-' + this.postfix + ''),
            fitToView: false,
            afterShow: function() {
                shareLike.getShareUsersInPopup().done(function() {
                    shareLike.showShareUsersInPopupWL();
                });
            },
            beforeClose: function() {
                shareLike.getShareUsersInPopup().done(function() {
                    shareLike.hideLikeUsersOrShareUsersInPopup();
                });
            }
        });
    };

    methods.openLikeUsersInPopup = function() {
        if ($('#like-users-open-popup-lnk-' + this.postfix + '').hasClass("disabled")) {
            return;
        }

        if ($('#share-like-users-in-popup-holder-' + this.postfix + '').length == 0) {
            $('#share-like-' + this.postfix + '').append(this.getShareLikeUsersInPopupHolder());
            this.setShareLikeUsersInPopupHolderEvents();
        }

        this.getLikeUsersInPopup();
        
        var shareLike = this;
        
        $.fancybox.open({
            content: $('#share-like-users-in-popup-holder-' + this.postfix + ''),
            fitToView: false,
            afterShow: function() {
                shareLike.getLikeUsersInPopup().done(function() {
                    shareLike.showLikeUsersInPopupWS();
                });
            },
            beforeClose: function() {
                shareLike.getLikeUsersInPopup().done(function() {
                    shareLike.hideLikeUsersOrShareUsersInPopup();
                });
            }
        });
    };

    methods.showShareUsersInPopup = function() {
        if ($('#share-users-in-popup-tab-' + this.postfix + '').hasClass("active")) {
            return;
        }

        this.getShareUsersInPopup();

        this.getShareUsersInPopup().done(function() {
            $('#share-users-in-popup-tab-' + this.postfix + '').addClass('active');
            $('#share-users-in-popup-inside-' + this.postfix + '').addClass('active');
        });
    };

    methods.hideShareUsersInPopup = function() {
        if (!$('#share-users-in-popup-tab-' + this.postfix + '').hasClass("active")) {
            return;
        }
        this.getShareUsersInPopup().done(function() {
            $('#share-users-in-popup-tab-' + this.postfix + '').removeClass('active');
            $('#share-users-in-popup-inside-' + this.postfix + '').removeClass('active');
        });
    };

    methods.showShareUsersInPopupWL = function() {
        if ($('#like-users-in-popup-tab-' + this.postfix + '').hasClass("active")) {
            this.hideLikeUsersInPopup();
        }
        this.showShareUsersInPopup();
    };

    methods.showLikeUsersInPopup = function() {
        if ($('#like-users-in-popup-tab-' + this.postfix + '').hasClass("active")) {
            return;
        }

        this.getLikeUsersInPopup();

        this.getLikeUsersInPopup().done(function() {
            $('#like-users-in-popup-tab-' + this.postfix + '').addClass('active');
            $('#like-users-in-popup-inside-' + this.postfix + '').addClass('active');
        });
    };

    methods.hideLikeUsersInPopup = function() {
        if (!$('#like-users-in-popup-tab-' + this.postfix + '').hasClass("active")) {
            return;
        }
        this.getLikeUsersInPopup().done(function() {
            $('#like-users-in-popup-tab-' + this.postfix + '').removeClass('active');
            $('#like-users-in-popup-inside-' + this.postfix + '').removeClass('active');
        });
    };

    methods.showLikeUsersInPopupWS = function() {
        if ($('#share-users-in-popup-tab-' + this.postfix + '').hasClass("active")) {
            this.hideShareUsersInPopup();
        }
        this.showLikeUsersInPopup();
    };
    
    methods.hideLikeUsersOrShareUsersInPopup = function() {
        if ($('#like-users-in-popup-tab-' + this.postfix + '').hasClass("active")) {
            this.hideLikeUsersInPopup();
        }
        if ($('#share-users-in-popup-tab-' + this.postfix + '').hasClass("active")) {
            this.hideShareUsersInPopup();
        }
    };
    /**
     * Зарегистрируем наш объект в плагинах jQuery
     */
    addJqueryPlugin(ShareLike, JQUERY_PLUGIN_NAME);
    
    return ShareLike;
})