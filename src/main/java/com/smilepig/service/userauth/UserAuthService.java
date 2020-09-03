package com.smilepig.service.userauth;

/**
 * Created by zhuhe on 2020/9/3
 */
public class UserAuthService {

    /**
     * 获取用户wiki授权
     *
     * @return
     */
    public String getUserAuth() {
        return "token";
    }

    /**
     * 判断用户授权是否有效
     * @return
     */
    public boolean isAuthed() {
        return false;
    }

}
