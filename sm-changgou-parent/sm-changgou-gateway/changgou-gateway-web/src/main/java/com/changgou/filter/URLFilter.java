package com.changgou.filter;

/**
 * @Author：Mr.ran &Date：2019/9/7 21:01
 * <p>
 * @Description：
 */

public class URLFilter {

    /***
     * 购物车订单微服务都需要用户登录，必须携带令牌，所以所有路径都过滤,订单微服务需要过滤的地址
     */
    private static final String allurl = "/user/login,/api/user/add";

    /***
     * 检查请求路径是否需要进行权限校验
     * @param url
     * @return  true:主要权限校验   false:无需权限校验
     */
    public static boolean hasAuthorize(String url){
        String[] split = allurl.split(",");
        for (String uri : split) {
            if (uri.equals(url)){
                return false;
            }
        }
        return true;

    }
}
