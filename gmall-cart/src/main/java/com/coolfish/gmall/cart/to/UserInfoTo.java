package com.coolfish.gmall.cart.to;

import lombok.Data;


/**
 * @author 28251
 */
@Data
public class UserInfoTo {

    private Long userId;

    private String userKey;

    /**
     * 是否临时用户
     */
    private Boolean tempUser = false;

}
